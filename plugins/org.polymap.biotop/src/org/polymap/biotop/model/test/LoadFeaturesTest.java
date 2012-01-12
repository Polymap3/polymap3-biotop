/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
 */
package org.polymap.biotop.model.test;

import java.io.File;

import org.geotools.data.Query;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.type.FeatureType;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicInteger;

import org.polymap.core.data.feature.lucenestore.LuceneDataStore;
import org.polymap.core.data.feature.lucenestore.LuceneFeatureStore;
import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordStore;

import org.polymap.biotop.model.BiotopLuceneService;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LoadFeaturesTest {

    public static void main( String[] args ) 
    throws Exception {
        LuceneRecordStore store = new LuceneRecordStore( 
                new File( "/home/falko/servers/workspace-biotop/data/org.polymap.biotop/" ), false );

        FeatureType biotopSchema = BiotopLuceneService.biotopSchema();
        LuceneDataStore ds = BiotopLuceneService.dataStore();
        LuceneFeatureStore fs = ds.getFeatureSource( biotopSchema.getName() );

        Timer timer = new Timer();
        System.out.println( "Count: " + fs.getCount( new Query() ) + " (" + timer.elapsedTime() + "ms)" );

        timer.start();
        System.out.println( "Bounds: " + fs.getBounds() + " (" + timer.elapsedTime() + "ms)" );

        Query query = new Query();
        loadFeatures( fs, query );
        loadFeatures( fs, query );
        
        store.close();
    }

    
    /**
     *
     * @param store
     * @throws Exception
     */
    private static void loadFeatures( LuceneFeatureStore store, Query query )
    throws Exception {
        Timer timer = new Timer();
        
        final AtomicInteger states = new AtomicInteger( 0 );
        final AtomicInteger props = new AtomicInteger( 0 );
        int strings = 0;
        
        store.getFeatures( query ).accepts( new FeatureVisitor() {
            public void visit( Feature feature ) {
                states.incrementAndGet();
                
                props.addAndGet( feature.getProperties().size() );
                
//                System.out.println( feature.getIdentifier() );
//                System.out.println( "    " + feature.getProperty( "tk25" ) );
//              System.out.println( "    " + feature.getProperty( "objnr" ) );
//                System.out.println( "    " + feature.getProperty( "geom" ) );
            }}, null );
        
        System.out.println( "Load time: " + timer.elapsedTime() + "ms; states: " + states + "; properties: " + props );
    }
}
