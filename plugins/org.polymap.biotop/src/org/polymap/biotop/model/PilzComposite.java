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
 * A Value/Art composite that combines {@link PilzValue} and
 * {@link PilzArtComposite}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PilzComposite
        extends ValueArtComposite<PilzValue,PilzArtComposite> { 

    // factory ********************************************
    
    protected static class PflanzenArtFinder 
            implements ValueArtFinder<PilzValue,PilzArtComposite> {

        public PilzArtComposite find( PilzValue value ) {
            assert value != null;
            PilzArtComposite template = QueryExpressions.templateFor( PilzArtComposite.class );
            BooleanExpression expr = QueryExpressions.eq( template.nummer(), value.artNr().get() );
            Query<PilzArtComposite> matches = repo().findEntities( PilzArtComposite.class, expr, 0 , 1 );
            return matches.find();
        }
    }

    public static Collection<PilzComposite> forEntity( BiotopComposite biotop ) {
        List<PilzComposite> result = new ArrayList( 256 );
        for (PilzValue value : biotop.pilze().get()) {
            result.add( new PilzComposite( value, new PflanzenArtFinder() ) );
        }
        return Collections.unmodifiableCollection( result );
    }

    public static PilzComposite newInstance( final PilzArtComposite art ) {
        assert art != null;
        ValueBuilder<PilzValue> builder = repo().newValueBuilder( PilzValue.class );
        builder.prototype().artNr().set( art.nummer().get() );
        PilzValue newValue = builder.newInstance();
        return new PilzComposite( newValue, new PflanzenArtFinder() );
    }

    public static void updateEntity( BiotopComposite biotop, Collection<PilzComposite> coll ) {
        biotop.pilze().set( Collections2.transform( coll, new Function<PilzComposite,PilzValue>() {
            public PilzValue apply( PilzComposite input ) {
                return input.value();
            }
        }));
    }

    // instance *******************************************
    
    private PilzComposite( PilzValue value,
            ValueArtFinder<PilzValue, PilzArtComposite> artFinder ) {
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

    public Property<Integer> taxnr() {
        return art().taxnr();
    }

    public Property<String> taxname() {
        return art().taxname();
    }

    public Property<String> name() {
        return art().name();
    }

    public Property<Integer> menge() {
        return new ValueProperty( PilzValue.class, value().menge() );
    }

    public Property<Double> mengenstatusNr() {
        return new ValueProperty( PilzValue.class, value().mengenstatusNr() );
    }

}
