/* 
 * polymap.org
 * Copyright 2010, Falko Bräutigam, and other contributors as indicated
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * $Id: $
 */
package org.polymap.biotop.model.idgen;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.json.JSONObject;
import org.json.JSONTokener;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;

import org.polymap.core.runtime.Polymap;

import org.polymap.biotop.model.BiotopComposite;

/**
 * This service generates 'Antragsnummern'.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
@Mixins(
        BiotopnummerGeneratorService.Mixin.class
)
public interface BiotopnummerGeneratorService
        extends ServiceComposite {

    /**
     * Generate the next {@link BiotopComposite#objnr()}.
     */
    public String generate();
    
    
    public abstract class Mixin
            implements BiotopnummerGeneratorService {

        private static final Log log = LogFactory.getLog( BiotopnummerGeneratorService.class );

        private int         count;
        
        private String      prefix;
        
        private File        file;

        
        public Mixin() {
            InputStreamReader in = null; 
            try {
                file = new File( Polymap.getWorkspacePath().toFile(), "BiotopnummerGenerator.json" );
                if (file.exists()) {
                    in = new InputStreamReader( 
                            new BufferedInputStream( new FileInputStream( file ) ), "UTF-8" );
                    JSONObject json = new JSONObject( new JSONTokener( in ) );

                    count = json.getInt( "count" );
                    prefix = json.getString( "prefix" );
                }
                else {
                    count = 1;
                    prefix = "14522-";  //Mittelsachsen
                }
            }
            catch (Exception e) {
                throw new RuntimeException( "Fehler beim Initialisieren.", e );
            }
            finally {
                IOUtils.closeQuietly( in );
            }
        }


        public synchronized String generate() {
            String result = prefix + count++;
            log.debug( "generated ID: " + result );
            
            storeCount();
            return result;
        }
        

        protected void storeCount() {
            OutputStreamWriter out = null;
            try {
                JSONObject json = new JSONObject();
                json.put( "count", count );
                json.put( "prefix", prefix );
                
                out = new OutputStreamWriter( 
                        new BufferedOutputStream( new FileOutputStream( file, false ) ), "UTF-8" );
                out.write( json.toString( 4 ) );
            }
            catch (Exception e) {
                throw new RuntimeException( "Fehler beim Anlegen einer neuen Biotopnummer.", e );
            }
            finally {
                IOUtils.closeQuietly( out );
            }
        }
    }

}
