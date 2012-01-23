/*
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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
import org.qi4j.api.query.grammar.ContainsPredicate;
import org.qi4j.api.query.grammar.EqualsPredicate;
import org.qi4j.api.query.grammar.MatchesPredicate;
import org.qi4j.api.value.ValueBuilder;

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
import org.polymap.biotop.model.BiotoptypArtComposite;
import org.polymap.biotop.model.PflanzeValue;
import org.polymap.biotop.model.TierValue;
import org.polymap.biotop.model.constant.Erhaltungszustand;
import org.polymap.biotop.model.constant.Pflegezustand;
import org.polymap.biotop.model.constant.Schutzstatus;
import org.polymap.biotop.model.constant.Status;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BiotopFilterProvider
        implements IFilterProvider {

    private static Log log = LogFactory.getLog( BiotopFilterProvider.class );

    private ILayer                  layer;


    public List<IFilter> addFilters( ILayer _layer )
    throws Exception {
        this.layer = _layer;
        log.debug( "addFilters(): layer= " + layer );

        IGeoResource geores = layer.getGeoResource();

        if (geores instanceof EntityGeoResourceImpl
                && geores.resolve( EntityProvider.class, null ) instanceof BiotopEntityProvider) {

            List<IFilter> result = new ArrayList();

//            result.add( new TransientFilter(
//                    "__allFilter__", layer,
//                    "Alle", null, Filter.INCLUDE, Integer.MAX_VALUE ) );

            result.add( new StandardFilter( layer ) );

            result.add( new MeineFilter() );

            result.add( new AbstractEntityFilter( "__archiv__", layer, "Archiv", null, 10000, BiotopComposite.class ) {
                protected Query<? extends Entity> createQuery( IFilterEditorSite  site ) {
                    BiotopComposite template = QueryExpressions.templateFor( BiotopComposite.class );
                    EqualsPredicate predicate = QueryExpressions.eq( template.status(), Status.nicht_aktuell.id );
                    return BiotopRepository.instance().findEntities( BiotopComposite.class, predicate, 0, getMaxResults() );
                }
            });

            result.add( new AbstractEntityFilter( "__tiere__", layer, "mit Tieren", null, 10000, BiotopComposite.class ) {
                protected Query<? extends Entity> createQuery( IFilterEditorSite  site ) {
                    BiotopRepository repo = BiotopRepository.instance();
                    BiotopComposite template = QueryExpressions.templateFor( BiotopComposite.class );

                    ValueBuilder<TierValue> builder = repo.newValueBuilder( TierValue.class );
                    TierValue prototype = builder.prototype();
                    prototype.tierArtNr().set( "*" );

                    ContainsPredicate<TierValue> predicate = QueryExpressions.contains(
                            template.tiere(), builder.newInstance() );
                    log.debug( "Predicate:" + predicate );
                    return repo.findEntities( BiotopComposite.class, predicate, 0, getMaxResults() );
                }
            });

            result.add( new AbstractEntityFilter( "__pflanzen__", layer, "mit Pflanzen", null, 10000, BiotopComposite.class ) {
                protected Query<? extends Entity> createQuery( IFilterEditorSite  site ) {
                    BiotopRepository repo = BiotopRepository.instance();
                    BiotopComposite template = QueryExpressions.templateFor( BiotopComposite.class );

                    ValueBuilder<PflanzeValue> builder = repo.newValueBuilder( PflanzeValue.class );
                    PflanzeValue prototype = builder.prototype();
                    prototype.pflanzenArtNr().set( "*" );

                    ContainsPredicate<PflanzeValue> predicate = QueryExpressions.contains(
                            template.pflanzen(), builder.newInstance() );
                    log.debug( "Predicate:" + predicate );
                    return repo.findEntities( BiotopComposite.class, predicate, 0, getMaxResults() );
                }
            });

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

        public StandardFilter( ILayer layer ) {
            super( "__biotop--", layer, "Naturschutz", null, 15000, BiotopComposite.class );
        }
        
        public boolean hasControl() {
            return true;
        }

        public Composite createControl( Composite parent, IFilterEditorSite site ) {
            Composite result = site.createStandardLayout( parent );
            
            BiotopComposite template = QueryExpressions.templateFor( BiotopComposite.class );

            final PicklistFormField statusField = new PicklistFormField( Status.all );
            site.addStandardLayout( site.newFormField( result, "status", String.class,
                    statusField, null, "Status" ) );
            
            site.addStandardLayout( site.newFormField( result, "objnr", String.class,
                    new StringFormField(), null, "Biotopnummer" ) );
            
            site.addStandardLayout( site.newFormField( result, "tk25", String.class,
                    new StringFormField(), null, "TK25-Nr." ) );
            
            site.addStandardLayout( site.newFormField( result, "objnr_sbk", String.class,
                    new StringFormField(), null, "Objekt-Nr. (SBK)" ) );
            
            Map<String,BiotoptypArtComposite> typen = BiotopRepository.instance().biotoptypen();
            site.addStandardLayout( site.newFormField( result, "biotoptypArtNr", String.class,
                    new PicklistFormField( typen.keySet() ), null, "Biotoptyp" ) );
            
            site.addStandardLayout( site.newFormField( result, "kuerzel", String.class,
                    new StringFormField(), null, "Biotopkürzel" ) );
            
            site.addStandardLayout( site.newFormField( result, "schutzstatus", String.class,
                    new PicklistFormField( Schutzstatus.all ), null, "Schutzstatus" ) );
            
            site.addStandardLayout( site.newFormField( result, "geprueft", String.class,
                    new CheckboxFormField(), null, "Geprüft" ) );
            
            site.addStandardLayout( site.newFormField( result, "erfasst", String.class,
                    new StringFormField(), null, "Erfasst vor (Jahr)" ) );
            
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
            
            Polymap.getSessionDisplay().asyncExec( new Runnable() {
                public void run() {
                    statusField.setValue( Status.aktuell.id );
                    pflegeField.setEnabled( false );
                }
            });
            return result;
        }

        protected Query<? extends Entity> createQuery( IFilterEditorSite site ) {
            BiotopComposite template = QueryExpressions.templateFor( BiotopComposite.class );

            BooleanExpression expr = andEquals( null, template.status(), (Integer)site.getFieldValue( "status" ) );
            expr = andMatches( expr, template.objnr(), (String)site.getFieldValue( "objnr" ) );
            expr = andMatches( expr, template.tk25(), (String)site.getFieldValue( "tk25" ) );
            expr = andMatches( expr, template.objnr_sbk(), (String)site.getFieldValue( "objnr_sbk" ) );
            expr = andMatches( expr, template.biotopkuerzel(), (String)site.getFieldValue( "kuerzel" ) );
            expr = andEquals( expr, template.schutzstatus(), (Integer)site.getFieldValue( "schutzstatus" ) );
            expr = andEquals( expr, template.geprueft(), (Boolean)site.getFieldValue( "geprueft" ) );
            expr = andEquals( expr, template.erhaltungszustand(), (Integer)site.getFieldValue( "erhaltung" ) );
            expr = andEquals( expr, template.pflegezustand(), (Integer)site.getFieldValue( "pflege" ) );
            expr = andEquals( expr, template.naturraumNr(), (String)site.getFieldValue( "naturraum" ) );

            String value = site.getFieldValue( "biotoptypArtNr" );
            if (value != null) {
                BiotoptypArtComposite entity = BiotopRepository.instance().biotoptypen().get( value );
                expr = and( expr, eq( template.biotoptypArtNr(), entity.nummer().get() ) );
            }
            
            value = site.getFieldValue( "erfasst" );
            if (value != null) {
                expr = and( expr, QueryExpressions.le( template.erfassung().get().wann(), new Date() ) );
            }
            return BiotopRepository.instance().findEntities( BiotopComposite.class, expr, 0, getMaxResults() );
        }
        
        protected BooleanExpression andMatches( BooleanExpression expr, Property<String> prop, String value ) {
            return value != null ? and( expr, matches( prop, value ) ) : expr;
        }

        protected <T> BooleanExpression andEquals( BooleanExpression expr, Property<T> prop, T value ) {
            return value != null ? and( expr, eq( prop, value ) ) : expr;
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

            BooleanExpression statusQuery = QueryExpressions.notEq( template.status(), Status.nicht_aktuell.id );

            Principal user = Polymap.instance().getUser();
            MatchesPredicate principalQuery = QueryExpressions.matches(
                    template._lastModifiedBy(), "*" + user.getName() + "*" );

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
