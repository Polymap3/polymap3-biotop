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
 * A Value/Art composite that combines {@link StoerungValue} and
 * {@link StoerungArtComposite}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class StoerungComposite
        extends ValueArtComposite<StoerungValue,StoerungsArtComposite> { 

    // factory ********************************************
    
    protected static class ArtFinder 
            implements ValueArtFinder<StoerungValue,StoerungsArtComposite> {

        public StoerungsArtComposite find( StoerungValue value ) {
            assert value != null;
            StoerungsArtComposite template = QueryExpressions.templateFor( StoerungsArtComposite.class );
            BooleanExpression expr = QueryExpressions.eq( template.nummer(), value.artNr().get() );
            Query<StoerungsArtComposite> matches = repo().findEntities( StoerungsArtComposite.class, expr, 0 , 1 );
            return matches.find();
        }
    }

    public static Collection<StoerungComposite> forEntity( BiotopComposite biotop ) {
        List<StoerungComposite> result = new ArrayList( 256 );
        for (StoerungValue value : biotop.stoerungen().get()) {
            result.add( new StoerungComposite( value, new ArtFinder() ) );
        }
        return Collections.unmodifiableCollection( result );
    }

    public static StoerungComposite newInstance( final StoerungsArtComposite art ) {
        assert art != null;
        ValueBuilder<StoerungValue> builder = repo().newValueBuilder( StoerungValue.class );
        builder.prototype().artNr().set( art.nummer().get() );
        StoerungValue newValue = builder.newInstance();
        return new StoerungComposite( newValue, new ArtFinder() );
    }

    public static void updateEntity( BiotopComposite biotop, Collection<StoerungComposite> coll ) {
        biotop.stoerungen().set( Collections2.transform( coll, new Function<StoerungComposite,StoerungValue>() {
            public StoerungValue apply( StoerungComposite input ) {
                return input.value();
            }
        }));
    }

    // instance *******************************************
    
    private StoerungComposite( StoerungValue value,
            ValueArtFinder<StoerungValue, StoerungsArtComposite> artFinder ) {
        super( value, artFinder );
    }

    public String id() {
        return art().id();
    }

    public Property<String> nummer() {
        return art().nummer();
    }

    public Property<String> name() {
        return art().name();
    }

}
