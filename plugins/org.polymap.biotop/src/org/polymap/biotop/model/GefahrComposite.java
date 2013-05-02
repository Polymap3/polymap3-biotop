/*
 * polymap.org
 * Copyright 2011-2013, Falko Bräutigam. All rights reserved.
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
 * A Value/Art composite that combines {@link GefahrValue} and
 * {@link GefahrArtComposite}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class GefahrComposite
        extends ValueArtComposite<GefahrValue,GefahrArtComposite> { 

    // factory ********************************************
    
    protected static class ArtFinder 
            implements ValueArtFinder<GefahrValue,GefahrArtComposite> {

        public GefahrArtComposite find( GefahrValue value ) {
            assert value != null;
            GefahrArtComposite template = QueryExpressions.templateFor( GefahrArtComposite.class );
            BooleanExpression expr = QueryExpressions.eq( template.nummer(), value.artNr().get() );
            Query<GefahrArtComposite> matches = repo().findEntities( GefahrArtComposite.class, expr, 0 , 1 );
            return matches.find();
        }
    }

    public static Collection<GefahrComposite> forEntity( BiotopComposite biotop ) {
        List<GefahrComposite> result = new ArrayList( 256 );
        for (GefahrValue value : biotop.gefahr().get()) {
            result.add( new GefahrComposite( value, new ArtFinder() ) );
        }
        return Collections.unmodifiableCollection( result );
    }

    public static GefahrComposite newInstance( final GefahrArtComposite art ) {
        assert art != null;
        ValueBuilder<GefahrValue> builder = repo().newValueBuilder( GefahrValue.class );
        builder.prototype().artNr().set( art.nummer().get() );
        GefahrValue newValue = builder.newInstance();
        return new GefahrComposite( newValue, new ArtFinder() );
    }

    public static void updateEntity( BiotopComposite biotop, Collection<GefahrComposite> coll ) {
        biotop.gefahr().set( Collections2.transform( coll, new Function<GefahrComposite,GefahrValue>() {
            public GefahrValue apply( GefahrComposite input ) {
                return input.value();
            }
        }));
    }

    // instance *******************************************
    
    private GefahrComposite( GefahrValue value,
            ValueArtFinder<GefahrValue, GefahrArtComposite> artFinder ) {
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
