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
import java.util.List;

import java.security.Principal;

import net.refractions.udig.catalog.IGeoResource;

import org.opengis.filter.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.query.grammar.ContainsPredicate;
import org.qi4j.api.query.grammar.EqualsPredicate;
import org.qi4j.api.query.grammar.MatchesPredicate;
import org.qi4j.api.value.ValueBuilder;

import org.polymap.core.model.Entity;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.data.entityfeature.AbstractEntityFilter;
import org.polymap.rhei.data.entityfeature.EntityProvider;
import org.polymap.rhei.data.entityfeature.catalog.EntityGeoResourceImpl;
import org.polymap.rhei.filter.IFilter;
import org.polymap.rhei.filter.IFilterEditorSite;
import org.polymap.rhei.filter.IFilterProvider;
import org.polymap.rhei.filter.TransientFilter;

import org.polymap.biotop.model.BiotopComposite;
import org.polymap.biotop.model.BiotopEntityProvider;
import org.polymap.biotop.model.BiotopRepository;
import org.polymap.biotop.model.BiotoptypValue;
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

            result.add( new TransientFilter(
                    "__allFilter__", layer,
                    "Alle", null, Filter.INCLUDE, Integer.MAX_VALUE ) );

            result.add( new MeineFilter() );

            result.add( new AbstractEntityFilter( "__gelöscht__", layer, "Gelöscht", null, 1000, BiotopComposite.class ) {
                protected Query<? extends Entity> createQuery( IFilterEditorSite  site ) {
                    BiotopComposite template = QueryExpressions.templateFor( BiotopComposite.class );
                    EqualsPredicate predicate = QueryExpressions.eq( template.status(), Status.nicht_aktuell.id );
                    return BiotopRepository.instance().findEntities( BiotopComposite.class, predicate, 0, getMaxResults() );
                }
            });

            result.add( new AbstractEntityFilter( "__biotoptypen__", layer, "mit Biotoptypen", null, 1000, BiotopComposite.class ) {
                protected Query<? extends Entity> createQuery( IFilterEditorSite  site ) {
                    BiotopRepository repo = BiotopRepository.instance();
                    BiotopComposite template = QueryExpressions.templateFor( BiotopComposite.class );

                    ValueBuilder<BiotoptypValue> builder = repo.newValueBuilder( BiotoptypValue.class );
                    BiotoptypValue prototype = builder.prototype();
                    prototype.unternummer().set( "*" );

                    ContainsPredicate<BiotoptypValue> predicate = QueryExpressions.contains(
                            template.biotoptypen(), builder.newInstance() );
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
