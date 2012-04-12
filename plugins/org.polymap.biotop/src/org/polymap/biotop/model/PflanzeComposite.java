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
 * A Value/Art composite that combines {@link PflanzeValue} and
 * {@link PflanzenArtComposite}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PflanzeComposite
        extends ValueArtComposite<PflanzeValue,PflanzenArtComposite> { 

    // factory ********************************************
    
    protected static class PflanzenArtFinder 
            implements ValueArtFinder<PflanzeValue,PflanzenArtComposite> {

        public PflanzenArtComposite find( PflanzeValue value ) {
            assert value != null;
            PflanzenArtComposite template = QueryExpressions.templateFor( PflanzenArtComposite.class );
            BooleanExpression expr = QueryExpressions.eq( template.nummer(), value.pflanzenArtNr().get() );
            Query<PflanzenArtComposite> matches = repo().findEntities( PflanzenArtComposite.class, expr, 0 , 1 );
            return matches.find();
        }
    }

    public static Collection<PflanzeComposite> forEntity( BiotopComposite biotop ) {
        List<PflanzeComposite> result = new ArrayList( 256 );
        for (PflanzeValue value : biotop.pflanzen().get()) {
            result.add( new PflanzeComposite( value, new PflanzenArtFinder() ) );
        }
        return Collections.unmodifiableCollection( result );
    }

    public static PflanzeComposite newInstance( final PflanzenArtComposite art ) {
        assert art != null;
        ValueBuilder<PflanzeValue> builder = repo().newValueBuilder( PflanzeValue.class );
        builder.prototype().pflanzenArtNr().set( art.nummer().get() );
        PflanzeValue newValue = builder.newInstance();
        return new PflanzeComposite( newValue, new PflanzenArtFinder() );
    }

    public static void updateEntity( BiotopComposite biotop, Collection<PflanzeComposite> coll ) {
        biotop.pflanzen().set( Collections2.transform( coll, new Function<PflanzeComposite,PflanzeValue>() {
            public PflanzeValue apply( PflanzeComposite input ) {
                return input.value();
            }
        }));
    }

    // instance *******************************************
    
    private PflanzeComposite( PflanzeValue value,
            ValueArtFinder<PflanzeValue, PflanzenArtComposite> artFinder ) {
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

    public Property<String> schutzstatus() {
        return art().schutzstatus();    
    }

    public Property<Integer> menge() {
        return new ValueProperty( PflanzeValue.class, value().menge() );
    }

    public Property<Double> mengenstatusNr() {
        return new ValueProperty( PflanzeValue.class, value().mengenstatusNr() );
    }

}
