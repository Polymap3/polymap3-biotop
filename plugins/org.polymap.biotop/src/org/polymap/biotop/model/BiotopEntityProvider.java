/*
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as indicated
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
 */
package org.polymap.biotop.model;

import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.visitor.DuplicatingFilterVisitor;
import org.geotools.referencing.CRS;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.PropertyName;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Joiner;
import com.vividsolutions.jts.geom.MultiPolygon;

import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModule.EntityCreator;
import org.polymap.rhei.data.entityfeature.DefaultEntityProvider;
import org.polymap.rhei.data.entityfeature.EntityProvider2;
import org.polymap.biotop.model.constant.Status;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BiotopEntityProvider
        extends DefaultEntityProvider<BiotopComposite>
        implements EntityProvider2<BiotopComposite> {

    private static Log log = LogFactory.getLog( BiotopEntityProvider.class );

    /** 
     * The properties (name/type) of the feature type provided. 
     */
    private enum PROP {
        Biotopnummer( String.class ), 
        SBK( String.class, "SBK/TK25/UNr." ), 
        Name( String.class ), 
        Beschreibung( String.class ), 
        Biotoptyp( String.class ), 
        Geprueft( Boolean.class, "Geprüft" ), 
        Wert( String.class ), 
        Archiv( Integer.class );
        
        private Class       type;
        
        private String      name = name();
        
        PROP( Class type ) {
            this.type = type;
        }
        PROP( Class type, String name ) {
            this.type = type;
            this.name = name;
        }
        public Class type() {
            return type;
        }
        public <T> T cast( Object value ) {
            return (T)type.cast( value );
        }
        public String toString() {
            return name;
        }
    }

    
    public BiotopEntityProvider( QiModule repo, FidsQueryProvider queryProvider ) {
        super( repo, BiotopComposite.class, new NameImpl( BiotopRepository.NAMESPACE, "Biotop" ), queryProvider );
    }


    public BiotopComposite newEntity( final EntityCreator<BiotopComposite> creator )
    throws Exception {
        return ((BiotopRepository)repo).newBiotop( creator );
    }


    public FeatureType buildFeatureType() {
        EntityType entityType = getEntityType();

        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName( getEntityName() );

        CoordinateReferenceSystem crs = getCoordinateReferenceSystem( getDefaultGeometry() );
        builder.add( getDefaultGeometry(), MultiPolygon.class, crs );
        builder.setDefaultGeometry( getDefaultGeometry() );
        
        for (PROP prop : PROP.values()) {
            builder.add( prop.toString(), prop.type() );            
        }
        return builder.buildFeatureType();
    }


    public Query transformQuery( Query query ) {
        Filter filter = query.getFilter();
//        if (filter == null) {
//            log.warn( "Filter is NULL!" );
//            filter = Filter.INCLUDE;
//        }
        Filter dublicate = filter == null ? null : (Filter)filter.accept( new DuplicatingFilterVisitor() {
            
            public Object visit( PropertyName input, Object data ) {
                if (input.getPropertyName().equals( PROP.Wert.toString() )) {
                    return getFactory( data ).property( "wert" );
                }
                else if (input.getPropertyName().equals( PROP.Biotopnummer.toString() )) {
                    return getFactory( data ).property( "objnr" );
                }
                else if (input.getPropertyName().equals( PROP.Beschreibung.toString() )) {
                    return getFactory( data ).property( "beschreibung" );
                }
                else if (input.getPropertyName().equals( PROP.Name.toString() )) {
                    return getFactory( data ).property( "name" );
                }
                else if (input.getPropertyName().equals( PROP.SBK.toString() )) {
                    throw new RuntimeException( "Das Feld ist errechnet und kann nicht durchsucht werden: " + PROP.SBK.toString() );
                }
                else if (input.getPropertyName().equals( PROP.Biotoptyp.toString() )) {
                    throw new RuntimeException( "Das Feld ist errechnet und kann nicht durchsucht werden: " + PROP.Biotoptyp.toString() );
                }
                else if (input.getPropertyName().equals( PROP.Geprueft.toString() )) {
                    return getFactory( data ).property( "geprueft" );
                }
                else if (input.getPropertyName().equals( PROP.Archiv.toString() )) {
                    return getFactory( data ).property( "status" );
                }
                return input;
            }
        }, null );
        DefaultQuery result = new DefaultQuery( query );
        result.setFilter( dublicate );
        return result;
    }


    public Feature buildFeature( Entity entity, FeatureType schema ) {
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder( (SimpleFeatureType)schema );
        BiotopComposite biotop = (BiotopComposite)entity;
        try {
            fb.set( getDefaultGeometry(), biotop.geom().get() );
            fb.set( PROP.Biotopnummer.toString(), biotop.objnr().get() );
            fb.set( PROP.SBK.toString(), Joiner.on( "/" ).useForNull( "-" )
                    .join( biotop.objnr_sbk().get(), biotop.tk25().get(), biotop.unr().get() ) );
            fb.set( PROP.Name.toString(), biotop.name().get() );
            fb.set( PROP.Beschreibung.toString(), biotop.beschreibung().get() );
            fb.set( PROP.Biotoptyp.toString(), biotop.biotoptypArtNr().get() );
            fb.set( PROP.Wert.toString(), biotop.wert().get() );
            fb.set( PROP.Geprueft.toString(), biotop.geprueft().get() /*.booleanValue() ? "ja" : "nein"*/ );
            fb.set( PROP.Archiv.toString(), biotop.status().get() /*== Status.nicht_aktuell.id ? "ja" : "nein"*/ );
            
            String nummer = biotop.biotoptypArtNr().get();
            BiotoptypArtComposite biotoptyp = ((BiotopRepository)repo).btForNummer( nummer );
            fb.set( PROP.Biotoptyp.toString(), biotoptyp != null ? biotoptyp.name().get() : null );
        }
        catch (Exception e) {
            log.warn( "", e );
        }
        return fb.buildFeature( biotop.id() );
    }


    public void modifyFeature( Entity entity, String propName, Object value )
    throws Exception {
        BiotopComposite biotop = (BiotopComposite)entity;
        if (propName.equals( getDefaultGeometry() )) {
            biotop.geom().set( (MultiPolygon)value );
        }
        else if (propName.equals( PROP.Name.toString() )) {
            biotop.name().set( (String)value );
        }
        else if (propName.equals( PROP.Beschreibung.toString() )) {
            biotop.beschreibung().set( (String)value );
        }
        else if (propName.equals( PROP.Biotoptyp.toString() )) {
            biotop.biotoptypArtNr().set( (String)value );
        }
        else if (propName.equals( PROP.Geprueft.toString() )) {
            biotop.geprueft().set( value.equals( "ja" ) );
        }
        else if (propName.equals( PROP.Archiv.toString() )) {
            biotop.status().set( value.equals( "ja" ) ? Status.nicht_aktuell.id : Status.aktuell.id );
        }
        else {
            throw new RuntimeException( "Unhandled property: " + propName );
        }
    }


    public CoordinateReferenceSystem getCoordinateReferenceSystem( String propName ) {
        try {
            return CRS.decode( "EPSG:31468" );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    public String getDefaultGeometry() {
        return "geom";
    }


//    public ReferencedEnvelope getBounds() {
//        org.qi4j.api.query.Query<BiotopComposite> result = repo.findEntities( type.getType(), new GetBoundsQuery( getDefaultGeometry() ), 0, 10 );
//        Iterator<BiotopComposite> it = result.iterator();
//        
//        try {
//            Geometry geom = (Geometry)type.getProperty( getDefaultGeometry() ).getValue( it.next() );
//            double minX = geom.getEnvelopeInternal().getMinX();
//
//            geom = (Geometry)type.getProperty( getDefaultGeometry() ).getValue( it.next() );
//            double maxX = geom.getEnvelopeInternal().getMaxX();
//
//            geom = (Geometry)type.getProperty( getDefaultGeometry() ).getValue( it.next() );
//            double minY = geom.getEnvelopeInternal().getMinY();
//
//            geom = (Geometry)type.getProperty( getDefaultGeometry() ).getValue( it.next() );
//            double maxY = geom.getEnvelopeInternal().getMaxY();
//
//            return new ReferencedEnvelope( minX, maxX, minY, maxY, getCoordinateReferenceSystem( null ) );
//        }
//        catch (Exception e) {
//            throw new RuntimeException( e );
//        }
//    }
    
}
