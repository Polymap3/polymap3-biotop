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
package org.polymap.biotop.model.importer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.operation.DefaultFeatureOperation;
import org.polymap.core.data.operation.IFeatureOperation;
import org.polymap.core.qi4j.QiModule.EntityCreator;

import org.polymap.biotop.model.BiotopComposite;
import org.polymap.biotop.model.BiotopRepository;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ShapeImportOperation
        extends DefaultFeatureOperation
        implements IFeatureOperation {

    private static Log log = LogFactory.getLog( ShapeImportOperation.class );

    public Status execute( IProgressMonitor monitor )
    throws Exception {
        monitor.beginTask( "Shapefile importieren", context.features().size() );
        
        BiotopRepository repo = BiotopRepository.instance();
        
        Map<String,BiotopComposite> created = new HashMap();
        FeatureIterator<SimpleFeature> it = context.features().features();
        try {
            GeometryFactory factory = new GeometryFactory();

            while (it.hasNext()) {
                SimpleFeature feature = it.next();
                final Object objnr = feature.getAttribute( "OBJNR" );
                final Object tk25 = feature.getAttribute( "TK25" );
                
                BiotopComposite entity = null;
                
                if (objnr == null || tk25 == null) {
                    entity = newEntity( feature );
                    // with objnr or tk25 null it is not referencable anyway
                    created.put( entity.id(), entity );
                    log.info( "    unmapped entity created: " + objnr + ", " + tk25 );
                }
                else {
                    String key = objnr.toString() + tk25.toString();
                    entity = created.get( key );
                    if (entity == null) {
                        entity = newEntity( feature );
                        created.put( key, entity );
                        //log.info( "    entity created for key: " + key );
                        
                        Geometry featureGeom = (Geometry)feature.getDefaultGeometry();
                        MultiPolygon newGeom = null;
                        if (featureGeom instanceof MultiPolygon) {
                            newGeom = (MultiPolygon)featureGeom;
                        }
                        else if (featureGeom instanceof Polygon) {
                            newGeom = factory.createMultiPolygon( new Polygon[] { (Polygon)featureGeom } );
                        }
                        else {
                            throw new RuntimeException( "Unhandled feature geometry: " + featureGeom );
                        }
                        entity.geom().set( newGeom );
                        //log.info( "    geometry created: " + newGeom );
                    }
                    else {
                        MultiPolygon geom = entity.geom().get();
                        
                        List<Geometry> polygons = asList( geom );
                        
                        Geometry featureGeom = (Geometry)feature.getDefaultGeometry();
                        if (featureGeom instanceof Polygon) {
                            polygons.add( featureGeom );
                        }
                        else if (featureGeom instanceof MultiPolygon) {
                            polygons.addAll( asList( featureGeom ) );
                        }
                        else {
                            throw new RuntimeException( "Unhandled feature geometry: " + featureGeom );
                        }
                        
                        MultiPolygon newGeom = factory.createMultiPolygon( 
                                polygons.toArray( new Polygon[polygons.size()] ) );
                        entity.geom().set( newGeom );
                        //log.info( "    geometry updated to: " + newGeom );
                    }
                }
                if (monitor.isCanceled()) {
                    return Status.Cancel;
                }
                monitor.worked( 1 );
            }
        }
        finally {
            it.close();
        }
        return Status.OK;
    }

    
    protected List<Geometry> asList( Geometry geom ) {
        List<Geometry> result = new ArrayList();
        for (int i=0; i < geom.getNumGeometries(); i++) {
            result.add( geom.getGeometryN( i ) );
        }
        return result;
    }
    
    
    protected BiotopComposite newEntity( final SimpleFeature feature )
    throws Exception {
        return BiotopRepository.instance().newEntity( 
                BiotopComposite.class, null, new EntityCreator<BiotopComposite>() {
                    public void create( BiotopComposite instance ) throws Exception {
                        Object objnr = feature.getAttribute( "OBJNR" );
                        instance.objnr_sbk().set( objnr != null ? objnr.toString() : null );

                        Object tk25 = feature.getAttribute( "TK25" );
                        instance.tk25().set( tk25 != null ? tk25.toString() : null );
                        
                        Object value = feature.getAttribute( "BT_CODE" );
                        instance.bt_code().set( value != null ? value.toString() : null );

                        value = feature.getAttribute( "WERT" );
                        instance.wert().set( value != null ? value.toString() : null );
                    }
                });
    }

}