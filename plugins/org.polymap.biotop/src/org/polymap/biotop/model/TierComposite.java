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
package org.polymap.biotop.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.value.ValueBuilder;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

/**
 * A Value/Art composite that combines {@link TierValue} and
 * {@link TierArtComposite}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class TierComposite
        extends ValueArtComposite<TierValue,TierArtComposite> { 

    // factory ********************************************
    
    protected static class TierArtFinder 
            implements ValueArtFinder<TierValue,TierArtComposite> {

        public TierArtComposite find( TierValue value ) {
            assert value != null;
            TierArtComposite template = QueryExpressions.templateFor( TierArtComposite.class );
            BooleanExpression expr = QueryExpressions.eq( template.nummer(), value.tierArtNr().get() );
            Query<TierArtComposite> matches = repo().findEntities( TierArtComposite.class, expr, 0 , 1 );
            return matches.find();
        }
    }

    public static Collection<TierComposite> forEntity( BiotopComposite biotop ) {
        List<TierComposite> result = new ArrayList( 256 );
        for (TierValue value : biotop.tiere().get()) {
            result.add( new TierComposite( value, new TierArtFinder() ) );
        }
        return Collections.unmodifiableCollection( result );
    }

    public static TierComposite newInstance( TierArtComposite art ) {
        assert art != null;
        ValueBuilder<TierValue> builder = repo().newValueBuilder( TierValue.class );
        builder.prototype().tierArtNr().set( art.nummer().get() );
        TierValue newValue = builder.newInstance();
        return new TierComposite( newValue, new TierArtFinder() );
    }

    public static void updateEntity( BiotopComposite biotop, Collection<TierComposite> coll ) {
        biotop.tiere().set( Collections2.transform( coll, new Function<TierComposite,TierValue>() {
            public TierValue apply( TierComposite input ) {
                return input.value();
            }
        }));
    }

    // instance *******************************************

    private TierComposite( TierValue value, ValueArtFinder<TierValue, TierArtComposite> artFinder ) {
        super( value, artFinder );
    }

    public String id() {
        return art().id();
    }

    public Property<String> nummer() {
        return art().nummer();
    }

    public Property<String> artengruppeNr() {
        return art().artengruppeNr();
    }

    public Property<String> gattung() {
        return art().gattung();
    }

    public Property<String> species() {
        return art().species();
    }

    public Property<String> sysCode() {
        return art().sysCode();
    }

    public Property<String> name() {
        return art().name();
    }

    public Property<String> schutzstatus() {
        return art().schutzstatus();    
    }

    public Property<Integer> menge() {
        return new ValueProperty( TierValue.class, value().menge() );
    }

    public Property<Double> mengenstatusNr() {
        return new ValueProperty( TierValue.class, value().mengenstatusNr() );
    }

}
