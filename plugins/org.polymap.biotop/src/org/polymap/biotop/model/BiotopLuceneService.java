/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
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
package org.polymap.biotop.model;

import java.util.Arrays;

import java.io.File;
import net.refractions.udig.catalog.ITransientResolve;

import org.geotools.feature.NameImpl;
import org.geotools.feature.type.AttributeDescriptorImpl;
import org.geotools.feature.type.AttributeTypeImpl;
import org.geotools.feature.type.FeatureTypeImpl;
import org.geotools.feature.type.GeometryDescriptorImpl;
import org.geotools.feature.type.GeometryTypeImpl;
import org.geotools.referencing.CRS;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.MultiPolygon;

import org.polymap.core.data.feature.lucenestore.LuceneDataStore;
import org.polymap.core.data.feature.lucenestore.LuceneServiceImpl;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordStore;

/**
 * The catalog service based on a {@link LuceneServiceImpl} for the biotop related
 * features.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BiotopLuceneService
        extends LuceneServiceImpl
        implements ITransientResolve {

    private static LuceneDataStore      dataStore;
    
    
    public static LuceneDataStore dataStore() {
        if (dataStore == null) {
            try {
                LuceneRecordStore store = new LuceneRecordStore( 
                        new File( "/home/falko/servers/workspace-biotop/data/org.polymap.biotop/" ), false );

                FeatureType biotopSchema = biotopSchema();
                dataStore = new LuceneDataStore( store, biotopSchema );
            }
            catch (RuntimeException e) {
                throw e;
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }
        return dataStore;
    }

    
    public static FeatureType biotopSchema() 
    throws NoSuchAuthorityCodeException, FactoryException {
        // crs
        CoordinateReferenceSystem crs = CRS.decode( "EPSG:31468" );
        // geometry
        GeometryDescriptor geom = new GeometryDescriptorImpl(
                new GeometryTypeImpl( name( "geom"), MultiPolygon.class, crs, false, false, null, null, null ),
                name( "geom" ), 1, 1, true, null );
        // attributes
        PropertyDescriptor[] attrs = new PropertyDescriptor[] {
                geom,
                new AttributeDescriptorImpl(
                        new AttributeTypeImpl( name( "objnr"), String.class, false, false, null, null, null ),
                        name( "objnr"), 1, 1, true, "" ),
                new AttributeDescriptorImpl(
                        new AttributeTypeImpl( name( "tk25"), String.class, false, false, null, null, null ),
                        name( "tk25"), 1, 1, true, "" )
        };
        // biotopSchema
        return new FeatureTypeImpl( 
                name( "Biotop" ), Arrays.asList( attrs ), geom, false, null, null, null );
    }

    
    protected static Name name( String localPart ) {
        return new NameImpl( BiotopRepository.NAMESPACE, localPart );
    }
    
    
    // instance *******************************************
    
    public BiotopLuceneService() {
        super( "Biotop2", BiotopRepository.NAMESPACE, dataStore() );
    }

    
    public <T> boolean canResolve( Class<T> adaptee ) {
        if (adaptee.equals( ITransientResolve.class )) {
            return true;
        }
        return super.canResolve( adaptee );
    }

}
