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


import java.lang.reflect.Type;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder.StateVisitor;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;

/**
 * Models a m:n association where <code>V</code> (Value) represents the 'join table'
 * with the modifyable values and <code>A</code> (Art) represents the unmodifiable
 * Art/Type properties of the valueProvider.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface ValueArtComposite<V extends ValueComposite,A extends EntityComposite>
    extends TransientComposite {

    public void init( V value, ValueArtFinder<V,A> artFinder );
    
    public V value();
    
    public A art();
    

//    /**
//     * 
//     */
//    public static interface ValueProvider<V extends ValueComposite> {
//        
//        public V get();
//        
//        public void set( V value );
//        
//    }
    
    
    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin<V extends ValueComposite,A extends EntityComposite>
            implements ValueArtComposite<V,A> {

        private V                       value;

        private ValueArtFinder<V,A>     artFinder;
        
        /** Caching the valueProvider returned by the {@link #artFinder}. */
        private A                       art;
        
        
        public void init( V _value, ValueArtFinder<V,A> _artFinder ) {
            assert _value != null && _artFinder != null;
            this.value = _value;
            this.artFinder = _artFinder;
        }
        
        public boolean equals( Object obj ) {
            if (obj instanceof ValueArtComposite) {
                ValueArtComposite rhs = (ValueArtComposite)obj;
                return art() == rhs.art()
                        || art().equals( rhs.art() );
            }
            return false;
        }
        
        public V value() {
            return value;  //valueProvider.get();
        }

        public A art() {
            if (art == null) {
                art = artFinder.find( value() );
            }
            return art;
        }

        
        /**
         * 
         *
         * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
         */
        public class ValueProperty<T>
                implements Property<T> {

            private Property<T>         prop;
            

            public ValueProperty( Property<T> prop ) {
                this.prop = prop;
            }


            // Property ***************************************
            
            public T get() {
                return prop.get();
            }

            public void set( final T newValue )
                    throws IllegalArgumentException, IllegalStateException {
                ValueBuilder<V> builder = (ValueBuilder<V>)BiotopRepository.instance().newValueBuilder( prop.get().getClass() );
                final V prototype = builder.prototype();
                value().state().visitProperties( new StateVisitor() {
                    public void visitProperty( QualifiedName visitedName, Object visitedValue ) {
                        Property<Object> visitedProp = prototype.state().getProperty( visitedName );
                        visitedProp.set( visitedName.equals( prop.qualifiedName() )
                                ? newValue
                                : visitedValue );
                    }
                });
                value = builder.newInstance();
            }

            public boolean isComputed() {
                return prop.isComputed();
            }

            public boolean isImmutable() {
                return prop.isImmutable();
            }

            public <I> I metaInfo( Class<I> infoType ) {
                return prop.metaInfo( infoType );
            }

            public QualifiedName qualifiedName() {
                return prop.qualifiedName();
            }

            public Type type() {
                return prop.type();
            }
        }

    }
    
}
