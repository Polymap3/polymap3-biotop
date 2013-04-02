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
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Joiner;
import com.vividsolutions.jts.geom.MultiPolygon;

import org.polymap.core.data.util.Geometries;
import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;
import org.polymap.core.model.EntityType.Property;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModule.EntityCreator;
import org.polymap.rhei.data.entityfeature.DefaultEntityProvider;
import org.polymap.rhei.data.entityfeature.EntityProvider2;

import org.polymap.biotop.model.constant.Schutzstatus;
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
        Biotopnummer( String.class, "objnr", true ), 
        SBK( String.class, null, false, "SBK/TK25/UNr." ), 
        Name( String.class, "name", true ), 
        Beschreibung( String.class, "beschreibung", true ), 
        Biotoptyp( String.class, null, false ), 
        Geprueft( Boolean.class, "geprueft", true, "Geprüft" ), 
        Schutzstatus( String.class, "schutzstatus", false ), 
        Status( String.class, "status", true );

        public static PROP forName( String name ) {
            for (PROP prop : PROP.values()) {
                if (prop.name .equals( name )) {
                    return prop;
                }
            }
            return null;
        }
        
        /** The Feature property type. */
        Class       type;
        /** The Feature property name. */
        String      name = name();
        // The Entity property name. */
        String      mappedName = name();
        /* */
        boolean     searchable = true;
        
        PROP( Class type, String mapped, boolean searchable ) {
            this.type = type;
            this.mappedName = mapped;
            this.searchable = searchable;
        }
        PROP( Class type, String mapped, boolean searchable, String name ) {
            this( type, mapped, searchable );
            this.name = name;
        }
        public <T> T cast( Object value ) {
            return (T)type.cast( value );
        }
        public String toString() {
            return name;
        }
    }

    
    public BiotopEntityProvider( QiModule repo ) {
        super( repo, BiotopComposite.class, new NameImpl( BiotopRepository.NAMESPACE, "Biotop" ) );
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
            builder.add( prop.name, prop.type );
        }
        return builder.buildFeatureType();
    }


    @Override
    public Query transformQuery( Query query ) {
        Filter filter = query.getFilter();
        Filter dublicate = filter == null ? null : (Filter)filter.accept( new DuplicatingFilterVisitor() {
            // property name
            @Override
            public Object visit( PropertyName input, Object data ) {
                PROP prop = PROP.forName( input.getPropertyName() );
                if (prop != null) {
                    if (!prop.searchable) {
//                        MessageDialog.openInformation( PolymapWorkbench.getShellToParentOn(),
//                                "Achtung", "Das Feld '" + prop.name + "' kann im Standardfilter nicht durchsucht werden.\nBenutzen Sie stattdessen den Filter 'Naturschutz'." );
//                        return Filter.EXCLUDE;
                        throw new RuntimeException( "Das Feld '" + prop.name + "' kann im Standardfilter nicht durchsucht werden. Benutzen Sie stattdessen den Filter 'Naturschutz'." );
                    }
                    else {
                        return getFactory( data ).property( prop.mappedName );
                    }
                }
                else {
                    log.info( "No such prop: " + input.getPropertyName() );
                    return input;
                }
            }
            // literal: status -> id
            @Override
            public Object visit( Literal literal, Object data ) {
                Object value = literal.getValue();
                if (value instanceof String) {
                    Status status = Status.all.forLabelOrSynonym( (String)value );
                    if (status != null) {
                        return getFactory( data ).literal( status.id );
                    }
                }
                return literal;
            }
            // StandardFilterProvider does isLike for all Strings
            @Override
            public Object visit( PropertyIsLike isLike, Object data ) {
                PropertyName propName = (PropertyName)visit( (PropertyName)isLike.getExpression(), data );
                if (propName.getPropertyName().equals( PROP.Status.mappedName )) {
                    FilterFactory2 ff2 = getFactory( data );
                    return ff2.equals( propName, 
                            ff2.literal( Status.all.forLabelOrSynonym( isLike.getLiteral() ).id ) );
                }
                else {
                    return super.visit( isLike, data );
                }
            }
        }, null );
        // XXX change requested properties
        DefaultQuery result = new DefaultQuery( query );
        result.setFilter( dublicate );
        return result;
    }


    @Override
    public Feature buildFeature( Entity entity, FeatureType schema ) {
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder( (SimpleFeatureType)schema );
        BiotopComposite biotop = (BiotopComposite)entity;
        try {
            fb.set( getDefaultGeometry(), biotop.geom().get() );
            
            EntityType<BiotopComposite> entityType = getEntityType();
            for (PROP prop : PROP.values()) {
                if (prop.mappedName != null) {
                    Property entityProp = entityType.getProperty( prop.mappedName );
                    if (entityProp != null) {
                        Object value = entityProp.getValue( biotop );
                        fb.set( prop.name, entityProp.getValue( biotop ) );
                    }
                }
            }
            
            fb.set( PROP.SBK.name, Joiner.on( "/" ).useForNull( "-" )
                    .join( biotop.objnr_sbk().get(), biotop.tk25().get(), biotop.unr().get() ) );
            
            String nummer = biotop.biotoptypArtNr().get();
            BiotoptypArtComposite biotoptyp = ((BiotopRepository)repo).btForNummer( nummer );
            fb.set( PROP.Biotoptyp.name, biotoptyp != null ? biotoptyp.name().get() : null );
            
            Schutzstatus schutzstatus = Schutzstatus.all.forId( biotop.schutzstatus().get() );
            fb.set( PROP.Schutzstatus.name, schutzstatus.label );

            Status status = Status.all.forId( biotop.status().get() );
            fb.set( PROP.Status.name, status.label );
        }
        catch (Exception e) {
            log.warn( "", e );
        }
        return fb.buildFeature( biotop.id() );
    }


    @Override
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
//        else if (propName.equals( PROP.Biotoptyp.toString() )) {
//            biotop.biotoptypArtNr().set( (String)value );
//        }
        else if (propName.equals( PROP.Geprueft.toString() )) {
            biotop.geprueft().set( "ja".equals( value ) );
        }
        else if (propName.equals( PROP.Status.toString() )) {
            biotop.status().set( Status.all.forLabelOrSynonym( (String)value ).id );
        }
//        else {
//            throw new RuntimeException( "Unhandled property: " + propName );
//        }
    }


    public CoordinateReferenceSystem getCoordinateReferenceSystem( String propName ) {
        try {
            return Geometries.crs( "EPSG:31468" );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    public String getDefaultGeometry() {
        return "geom";
    }

}
