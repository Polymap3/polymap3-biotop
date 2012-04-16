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
 * A Value/Art composite that combines {@link WertValue} and
 * {@link WertArtComposite}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WertComposite
        extends ValueArtComposite<WertValue,WertArtComposite> { 

    // factory ********************************************
    
    protected static class ArtFinder 
            implements ValueArtFinder<WertValue,WertArtComposite> {

        public WertArtComposite find( WertValue value ) {
            assert value != null;
            WertArtComposite template = QueryExpressions.templateFor( WertArtComposite.class );
            BooleanExpression expr = QueryExpressions.eq( template.nummer(), value.artNr().get() );
            Query<WertArtComposite> matches = repo().findEntities( WertArtComposite.class, expr, 0 , 1 );
            return matches.find();
        }
    }

    public static Collection<WertComposite> forEntity( BiotopComposite biotop ) {
        List<WertComposite> result = new ArrayList( 256 );
        for (WertValue value : biotop.werterhaltend().get()) {
            result.add( new WertComposite( value, new ArtFinder() ) );
        }
        return Collections.unmodifiableCollection( result );
    }

    public static WertComposite newInstance( final WertArtComposite art ) {
        assert art != null;
        ValueBuilder<WertValue> builder = repo().newValueBuilder( WertValue.class );
        builder.prototype().artNr().set( art.nummer().get() );
        WertValue newValue = builder.newInstance();
        return new WertComposite( newValue, new ArtFinder() );
    }

    public static void updateEntity( BiotopComposite biotop, Collection<WertComposite> coll ) {
        biotop.werterhaltend().set( Collections2.transform( coll, new Function<WertComposite,WertValue>() {
            public WertValue apply( WertComposite input ) {
                return input.value();
            }
        }));
    }

    // instance *******************************************
    
    private WertComposite( WertValue value,
            ValueArtFinder<WertValue, WertArtComposite> artFinder ) {
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
