/*
 * polymap.org
 * Copyright (C) 2011-2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.biotop.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import java.security.Principal;

import net.refractions.udig.catalog.IGeoResource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import static org.qi4j.api.query.QueryExpressions.matches;
import static org.qi4j.api.query.QueryExpressions.eq;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.query.grammar.Predicate;

import org.eclipse.swt.widgets.Composite;

import org.polymap.core.model.Entity;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.data.entityfeature.AbstractEntityFilter;
import org.polymap.rhei.data.entityfeature.EntityProvider;
import org.polymap.rhei.data.entityfeature.catalog.EntityGeoResourceImpl;
import org.polymap.rhei.field.CheckboxFormField;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.filter.IFilter;
import org.polymap.rhei.filter.IFilterEditorSite;
import org.polymap.rhei.filter.IFilterProvider;
import org.polymap.biotop.model.BiotopComposite;
import org.polymap.biotop.model.BiotopEntityProvider;
import org.polymap.biotop.model.BiotopRepository;
import org.polymap.biotop.model.BiotoptypArtComposite2;
import org.polymap.biotop.model.constant.Erhaltungszustand;
import org.polymap.biotop.model.constant.Pflegezustand;
import org.polymap.biotop.model.constant.Schutzstatus;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BiotopFilterProvider
        implements IFilterProvider {

    private static Log log = LogFactory.getLog( BiotopFilterProvider.class );

    private ILayer                  layer;


    public List<IFilter> addFilters( ILayer _layer ) throws Exception {
        this.layer = _layer;
        log.debug( "addFilters(): layer= " + layer );

        final BiotopRepository repo = BiotopRepository.instance();
        IGeoResource geores = layer.getGeoResource();

        if (geores instanceof EntityGeoResourceImpl
                && geores.resolve( EntityProvider.class, null ) instanceof BiotopEntityProvider) {

            List<IFilter> result = new ArrayList();

//            result.add( new TransientFilter(
//                    "__allFilter__", layer,
//                    "Alle", null, Filter.INCLUDE, Integer.MAX_VALUE ) );

            result.add( new StandardFilter( layer ) );

            result.add( new MeineFilter() );

//            result.add( new AbstractEntityFilter( "__archiv__", layer, "Archiv", null, 10000, BiotopComposite.class ) {
//                protected Query<? extends Entity> createQuery( IFilterEditorSite  site ) {
//                    BiotopComposite template = QueryExpressions.templateFor( BiotopComposite.class );
//                    EqualsPredicate predicate = QueryExpressions.eq( template.status(), Status.archiviert.id );
//                    return BiotopRepository.instance().findEntities( BiotopComposite.class, predicate, 0, getMaxResults() );
//                }
//            });
//
//            result.add( new AbstractEntityFilter( "__pilze__", layer, "mit Pilzen", null, 10000, BiotopComposite.class ) {
//                protected Query<? extends Entity> createQuery( IFilterEditorSite  site ) {
//                    BiotopComposite template = QueryExpressions.templateFor( BiotopComposite.class );
//
//                    ValueBuilder<PilzValue> builder = repo.newValueBuilder( PilzValue.class );
//                    builder.prototype().artNr().set( "*" );
//
//                    ContainsPredicate<PilzValue> predicate = QueryExpressions.contains(
//                            template.pilze(), builder.newInstance() );
//                    return repo.findEntities( BiotopComposite.class, predicate, 0, getMaxResults() );
//                }
//            });
//
//            result.add( new AbstractEntityFilter( "__tiere__", layer, "mit Tieren", null, 10000, BiotopComposite.class ) {
//                protected Query<? extends Entity> createQuery( IFilterEditorSite  site ) {
//                    BiotopComposite template = QueryExpressions.templateFor( BiotopComposite.class );
//
//                    ValueBuilder<TierValue> builder = repo.newValueBuilder( TierValue.class );
//                    TierValue prototype = builder.prototype();
//                    prototype.tierArtNr().set( "*" );
//
//                    ContainsPredicate<TierValue> predicate = QueryExpressions.contains(
//                            template.tiere(), builder.newInstance() );
//                    log.debug( "Predicate:" + predicate );
//                    return repo.findEntities( BiotopComposite.class, predicate, 0, getMaxResults() );
//                }
//            });
//
//            result.add( new AbstractEntityFilter( "__pflanzen__", layer, "mit Pflanzen", null, 10000, BiotopComposite.class ) {
//                protected Query<? extends Entity> createQuery( IFilterEditorSite  site ) {
//                    BiotopComposite template = QueryExpressions.templateFor( BiotopComposite.class );
//
//                    ValueBuilder<PflanzeValue> builder = repo.newValueBuilder( PflanzeValue.class );
//                    PflanzeValue prototype = builder.prototype();
//                    prototype.pflanzenArtNr().set( "*" );
//
//                    ContainsPredicate<PflanzeValue> predicate = QueryExpressions.contains(
//                            template.pflanzen(), builder.newInstance() );
//                    log.debug( "Predicate:" + predicate );
//                    return repo.findEntities( BiotopComposite.class, predicate, 0, getMaxResults() );
//                }
//            });

//            result.add( new AbstractEntityFilter( "__2010__", layer, "Jahr: 2010", null, null, 10000, BiotopComposite.class ) {
//                protected BooleanExpression createQuery( IFilterEditorSite  site ) {
//                    AntragComposite template = QueryExpressions.templateFor( AntragComposite.class );
//                    return QueryExpressions.eq( template.jahr(), 2010 );
//                }
//            });

//            result.add( new StandardFilter() );

            return result;
        }
        return null;
    }


    /*
     * 
     */
    class StandardFilter
            extends AbstractEntityFilter {

        private FilterFieldBuilder bekanntmachung;
        private FilterFieldBuilder biotoptyp;
        private FilterFieldBuilder biotoptypCode;

        
        public StandardFilter( ILayer layer ) {
            super( "__biotop--", layer, "Naturschutz...", null, 15000, BiotopComposite.class );
        }
        
        public boolean hasControl() {
            return true;
        }

        @Override
        public Composite createControl( Composite parent, IFilterEditorSite site ) {
            setSite(site );
            Composite result = site.createStandardLayout( parent );
            
            BiotopComposite template = QueryExpressions.templateFor( BiotopComposite.class );

//            final PicklistFormField statusField = new PicklistFormField( Status.all );
//            site.addStandardLayout( site.newFormField( result, "status", String.class,
//                    statusField, null, "Status" ) );
//            Polymap.getSessionDisplay().asyncExec( new Runnable() {
//                public void run() {
//                    statusField.setValue( Status.aktuell.id );
//                }
//            });
            
            site.addStandardLayout( site.newFormField( result, "objnr", String.class,
                    new StringFormField(), null, "Biotopnummer" ) );
            
            site.addStandardLayout( site.newFormField( result, "tk25", String.class,
                    new StringFormField(), null, "TK25-Nr." ) );
            
            site.addStandardLayout( site.newFormField( result, "objnr_sbk", String.class,
                    new StringFormField(), null, "Objekt-Nr. (SBK)" ) );
            
            Map<String,BiotoptypArtComposite2> typen = BiotopRepository.instance().btNamen();
            biotoptyp = newFormField( String.class ).setParent( result )
                    .setLabel( "Biotoptyp" )
                    .setField( new PicklistFormField( typen.keySet() ) );
            biotoptyp.create();
            
            biotoptypCode = newFormField( String.class ).setParent( result ).setLabel( "Biotoptyp-Code" );
            biotoptypCode.create();
            
            site.addStandardLayout( site.newFormField( result, "schutzstatus", String.class,
                    new PicklistFormField( Schutzstatus.all ), null, "Schutzstatus" ) );
            
            site.addStandardLayout( site.newFormField( result, "geprueft", String.class,
                    new CheckboxFormField(), null, "Geprüft" ) );
            
            site.addStandardLayout( site.newFormField( result, "waldbiotop", String.class,
                    new CheckboxFormField(), null, "Waldbiotop" ) );
            
//            bekanntmachung = newFormField( Date.class ).setParent( result )
//                    .setLabel( "Bekanntmachung" )
//                    .setField( new DateTimeFormField() );
//            bekanntmachung.create();
            
            site.addStandardLayout( site.newFormField( result, "erfasst", String.class,
                    new StringFormField(), null, "Erfasst im Jahr" ) );
            
            site.addStandardLayout( site.newFormField( result, "bearbeitet", String.class,
                    new StringFormField(), null, "Bearbeitet im Jahr" ) );
            
            site.addStandardLayout( site.newFormField( result, "erhaltung", String.class,
                    new PicklistFormField( Erhaltungszustand.all ), null, "Erhaltungszustand" ) );
            
            site.addStandardLayout( site.newFormField( result, "pflege", String.class,
                    new PicklistFormField( Pflegezustand.all ), null, "Pflegezustand" ) );
            
            final CheckboxFormField pflegeField = new CheckboxFormField();
            site.addStandardLayout( site.newFormField( result, "pflegebedarf", String.class,
                    pflegeField, null, "Pflegebedarf" ) );
            
            StringFormField naturraumField = new StringFormField();
            //naturraumField.setEnabled( false );
            site.addStandardLayout( site.newFormField( result, "naturraum", String.class,
                    naturraumField, null, "Naturraum (Nr.)" ) );
            
            return result;
        }

        protected Query<? extends Entity> createQuery( IFilterEditorSite site ) {
            BiotopComposite template = QueryExpressions.templateFor( BiotopComposite.class );

            BooleanExpression expr = andEquals( null, template.status(), (Integer)site.getFieldValue( "status" ) );
            expr = andMatches( expr, template.objnr(), (String)site.getFieldValue( "objnr" ) );
            expr = andMatches( expr, template.tk25(), (String)site.getFieldValue( "tk25" ) );
            expr = andMatches( expr, template.objnr_sbk(), (String)site.getFieldValue( "objnr_sbk" ) );
            expr = andEquals( expr, template.schutzstatus(), (Integer)site.getFieldValue( "schutzstatus" ) );
            expr = andEquals( expr, template.geprueft(), (Boolean)site.getFieldValue( "geprueft" ) );
            expr = andEquals( expr, template.waldbiotop(), (Boolean)site.getFieldValue( "waldbiotop" ) );
            expr = andEquals( expr, template.erhaltungszustand(), (Integer)site.getFieldValue( "erhaltung" ) );
            expr = andEquals( expr, template.pflegeZustand(), (Integer)site.getFieldValue( "pflege" ) );
            expr = andEquals( expr, template.pflegeBedarf(), (Boolean)site.getFieldValue( "pflegebedarf" ) );
            expr = andEquals( expr, template.naturraumNr(), (String)site.getFieldValue( "naturraum" ) );

//            if (bekanntmachung.getValue() != null) {
//                Date[] dates = yearFromTo( bekanntmachung.getValue() );
//                expr = and( expr, QueryExpressions.and(
//                        QueryExpressions.gt( template.erfassung().get().wann(), dates[0] ),
//                        QueryExpressions.lt( template.erfassung().get().wann(), dates[1] ) ) );
//                
//            }
            
            if (biotoptyp.isSet() && biotoptypCode.isSet()) {
                throw new RuntimeException( "Biotoptyp und Code dürfen nicht zusammen verwendet werden." );
            }
            if (biotoptyp.isSet()) {
                BiotoptypArtComposite2 entity = BiotopRepository.instance().btNamen().get( biotoptyp.getValue() );
                expr = and( expr, eq( template.biotoptyp2ArtNr(), entity.nummer().get() ) );
            }
            if (biotoptypCode.isSet()) {
                String value = biotoptypCode.getValue();
                for (BiotoptypArtComposite2 biotoptyp : BiotopRepository.instance().btNamen().values()) {
                    if (value.equals( biotoptyp.code().get() )) {
                        expr = and( expr, eq( template.biotoptyp2ArtNr(), biotoptyp.nummer().get() ) );
                    }
                }
            }
            
            String value = site.getFieldValue( "erfasst" );
            if (value != null) {
                Date[] dates = yearFromTo( value );
                expr = and( expr, QueryExpressions.and(
                        QueryExpressions.gt( template.erfassung().get().wann(), dates[0] ),
                        QueryExpressions.lt( template.erfassung().get().wann(), dates[1] ) ) );
            }

            value = site.getFieldValue( "bearbeitet" );
            if (value != null) {
                Date[] dates = yearFromTo( value );
                expr = and( expr, QueryExpressions.and(
                        QueryExpressions.gt( template.bearbeitung().get().wann(), dates[0] ),
                        QueryExpressions.lt( template.bearbeitung().get().wann(), dates[1] ) ) );
            }
            return BiotopRepository.instance().findEntities( BiotopComposite.class, expr, 0, getMaxResults() );
        }
        
        protected BooleanExpression andMatches( BooleanExpression expr, Property<String> prop, String value ) {
            return value != null ? and( expr, matches( prop, value ) ) : expr;
        }

        protected <T> BooleanExpression andEquals( BooleanExpression expr, Property<T> prop, T value ) {
            return value != null ? and( expr, eq( prop, value ) ) : expr;
        }
        
        protected Date[] yearFromTo( String year ) {
            Date[] result = new Date[2];
            Calendar cal = Calendar.getInstance();
            cal.set( Calendar.YEAR, Integer.parseInt( year ) );
            result[0] = cal.getTime();
            cal.add( Calendar.YEAR, 1 );
            result[1] = cal.getTime();
            return result;
        }
    }
    
    
    /**
     *
     */
    class MeineFilter
            extends AbstractEntityFilter {

        public MeineFilter() {
            super( "__my__", layer, "Von mir bearbeitet", null, 1000, BiotopComposite.class );
        }

        protected Query<? extends Entity> createQuery( IFilterEditorSite site ) {
            BiotopComposite template = QueryExpressions.templateFor( BiotopComposite.class );

//            BooleanExpression statusQuery = QueryExpressions.notEq( template.status(), Status.archiviert.id );

            Principal user = Polymap.instance().getUser();
            Predicate principalQuery = QueryExpressions.eq(
                    template.bearbeitung().get().wer(), user.getName() );

            BooleanExpression expr = principalQuery;  //QueryExpressions.and( principalQuery, statusQuery );
            return BiotopRepository.instance().findEntities( BiotopComposite.class, expr , 0, getMaxResults() );

// checking all principals
//            Set<Principal> principals = Polymap.instance().getPrincipals();
//            for (Principal principal : principals) {
//                MatchesPredicate principalQuery = QueryExpressions.matches( template.bearbeiter(), "*" + principal.getName() + "*" );
//                principalsQuery = principalsQuery != null
//                        ? QueryExpressions.or( principalsQuery, principalQuery )
//                        : principalQuery;
//            }
//
//            return QueryExpressions.and( openQuery, principalsQuery );
        }

    }

}
