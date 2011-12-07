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
import java.util.List;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.operation.DefaultFeatureOperation;
import org.polymap.core.data.operation.IFeatureOperation;
import org.polymap.core.qi4j.QiModule.EntityCreator;

import org.polymap.biotop.model.BiotopComposite;
import org.polymap.biotop.model.BiotopRepository;

/**
 * 
 * <p/>
 * There is code to unify biotops with same OBJNR and TK25 into a
 * {@link MultiPolygon}. Unfortunatelly they wanted one entity per Geometry.
 * So this code is commented out.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ShapeImportOperation
        extends DefaultFeatureOperation
        implements IFeatureOperation {

    private static Log log = LogFactory.getLog( ShapeImportOperation.class );

    private static GeometryFactory  factory = new GeometryFactory();
    
    private static final double     bufferDistance = 5;
    
    
    public Status execute( IProgressMonitor monitor )
    throws Exception {
        monitor.beginTask( "Shapefile importieren", context.features().size() );
        
        BiotopRepository repo = BiotopRepository.instance();
        
        //Map<String,BiotopComposite> created = new HashMap();
        
        FeatureIterator<SimpleFeature> it = context.features().features();
        try {
            while (it.hasNext()) {
                SimpleFeature feature = it.next();
                Geometry featureGeom = (Geometry)feature.getDefaultGeometry();
                List<Geometry> featurePolygons = asPolygons( featureGeom );
                
                if (featurePolygons.size() > 1) {
                    log.warn( "Diese Version von Biotop unterstützt keine Multi-Geometrien!" );
                }

//                final Object objnr = feature.getAttribute( "OBJNR" );
//                final Object tk25 = feature.getAttribute( "TK25" );
//                final Object unr = feature.getAttribute( "UNR" );
//                
                BiotopComposite entity = null;
//                
//                if (objnr == null || tk25 == null || unr == null) {
//                    entity = newEntity( feature );
//                    // with objnr or tk25 null it is not referencable anyway
//                    created.put( entity.id(), entity );
//                    log.info( "    unmapped entity created: " + objnr + ", " + tk25 );
//                }
//                else {
//                    String key = objnr.toString() + tk25.toString();
                    
                    // search in local map
                    entity = null;  //created.get( key );
                    
//                    // search database
//                    if (entity == null) {
//                        BiotopComposite template = QueryExpressions.templateFor( BiotopComposite.class );
//                        BooleanExpression expr = QueryExpressions.and(
//                                QueryExpressions.eq( template.objnr_sbk(), objnr.toString() ),
//                                QueryExpressions.eq( template.tk25(), tk25.toString() ) );
//                        entity = repo.findEntities( BiotopComposite.class, expr, 0, 1 ).find();
//                        if (entity != null) {
//                            created.put( key, entity );
//                            log.info( "found entity in db: " + entity );
//                        }
//                    }

                    // create entity
                    if (entity == null) {
                        entity = newEntity( feature );
                        //created.put( key, entity );
                        
                        entity.geom().set( asMultiPolygon( featurePolygons ) );
                    }
//                    // update geometry
//                    else {
//                        MultiPolygon geom = entity.geom().get();
//                        List<Geometry> polygons = asPolygons( geom );
//                        polygons.addAll( featurePolygons );
//                        
//                        entity.geom().set( asMultiPolygon( polygons ) );
//                    }
//                }
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

    
    protected MultiPolygon asMultiPolygon( List<Geometry> polygons ) {
        Polygon[] array = new Polygon[polygons.size()];
        int count = 0;
        for (Geometry geom : polygons) {
            array[count++] = (Polygon)geom;
        }
        return factory.createMultiPolygon( array );
    }
    
    
    /**
     * Convert the given Geometry in a List of Polygons. Multi geometries are
     * converted to a list. Points and Lines are buffered.
     */
    protected List<Geometry> asPolygons( Geometry featureGeom ) {
        List<Geometry> polygons = new ArrayList();
        // Polygon
        if (featureGeom instanceof Polygon) {
            polygons.add( featureGeom );
        }
        else if (featureGeom instanceof MultiPolygon) {
            polygons.addAll( asList( featureGeom ) );
        }
        // Line
        else if (featureGeom instanceof LineString) {
            Geometry polygon = ((LineString)featureGeom).buffer( bufferDistance, 3 );
            polygons.add( polygon );
        }
        else if (featureGeom instanceof MultiLineString) {
            Geometry polygon = ((MultiLineString)featureGeom).buffer( bufferDistance, 3 );
            polygons.addAll( asList( polygon ) );
        }
        // Point
        else if (featureGeom instanceof Point) {
            Geometry polygon = ((Point)featureGeom).buffer( bufferDistance*2, 3 );
            polygons.add( polygon );
        }
        else if (featureGeom instanceof MultiPoint) {
            Geometry polygon = ((MultiPoint)featureGeom).buffer( bufferDistance*2, 3 );
            polygons.addAll( asList( polygon ) );
        }
        else {
            throw new RuntimeException( "Unhandled feature geometry: " + featureGeom );
        }
        return polygons;
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
        return BiotopRepository.instance().newBiotop( new EntityCreator<BiotopComposite>() {
            public void create( BiotopComposite instance ) throws Exception {
                Object objnr = feature.getAttribute( "OBJNR" );
                instance.objnr_sbk().set( objnr != null ? objnr.toString() : null );

                Object tk25 = feature.getAttribute( "TK25" );
                instance.tk25().set( tk25 != null ? tk25.toString() : null );

                Object unr = feature.getAttribute( "UNR" );
                instance.unr().set( unr != null ? unr.toString() : null );

                Object value = feature.getAttribute( "BT_CODE" );
                instance.bt_code().set( value != null ? value.toString() : null );

                value = feature.getAttribute( "WERT" );
                instance.wert().set( value != null ? value.toString() : null );
            }
        });
    }

}