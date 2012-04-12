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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Type;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder.StateVisitor;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;

import org.polymap.core.model.Composite;
import org.polymap.core.model.Entity;
import org.polymap.core.runtime.ListenerList;

/**
 * Models a m:n association where <code>V</code> (Value) represents the 'join table'
 * with the modifyable values and <code>A</code> (Art) represents the unmodifiable
 * Art/Type properties of the valueProvider.
 * <p/>
 * Sub-classes can pass-through properties of the Art. Properties of the Value should
 * be wrapped in a {@link ValueProperty}. Any property modifications are cached inside.
 * The {@link #value()} can be used to update the corresponding Entity when all changes
 * are done.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class ValueArtComposite<V extends ValueComposite,A extends Entity>
        implements Composite {

    // factory / helper ***********************************
    
    protected static BiotopRepository repo() {
        return BiotopRepository.instance();
    }
    
    // instance *******************************************
    
    private V                       value;

    private ValueArtFinder<V,A>     artFinder;

    /** Caching the value returned by the {@link #artFinder}. */
    private A                       art;

    private ListenerList<PropertyChangeListener> listeners = new ListenerList();


    public ValueArtComposite( V _value, ValueArtFinder<V,A> _artFinder ) {
        assert _value != null && _artFinder != null;
        this.value = _value;
        this.artFinder = _artFinder;
    }

    public abstract String id();
    
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
            if (art == null) {
                throw new IllegalStateException( "Keine Art gefunden für: " + value() );
            }
        }
        return art;
    }

    public void addPropertyChangeListener( PropertyChangeListener l ) {
        listeners.add( l );
    }

    public void removePropertyChangeListener( PropertyChangeListener l ) {
        assert listeners != null;
        listeners.remove( l );
    }

    protected void fireEvent( String propName, Object newValue, Object oldValue ) {
        PropertyChangeEvent ev = new PropertyChangeEvent( this, propName, oldValue, newValue );
        for (PropertyChangeListener l : listeners) {
            l.propertyChange( ev );
        }
    }

    /**
     * Wraps a {@link Property} of an {@link ValueComposite}. Settings the
     * property results in creating a new value, copying all properties but
     * the new one. The caller does not have to deal wit it.
     *
     * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
     */
    public class ValueProperty<T>
            implements Property<T> {

        private Property<T>                     prop;

        private Class<? extends ValueComposite> valueType;


        public ValueProperty( Class<? extends ValueComposite> valueType, Property<T> prop ) {
            assert prop != null && valueType != null;
            this.prop = prop;
            this.valueType = valueType;
        }


        // Property ***************************************

        public T get() {
            return prop.get();
        }

        public void set( final T newValue )
        throws IllegalArgumentException, IllegalStateException {
            ValueBuilder<V> builder = (ValueBuilder<V>)BiotopRepository.instance().newValueBuilder( valueType );
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
            fireEvent( prop.qualifiedName().name(), newValue, null );
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
