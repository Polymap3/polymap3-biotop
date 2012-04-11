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

import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

import org.polymap.core.model.Composite;

/**
 * A Value-Art composite that combines {@link PflanzeValue} and
 * {@link PflanzenArtComposite}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@Concerns({
    //PropertyChangeSupport.Concern.class
})
@Mixins({
    PflanzeComposite.Mixin.class,
    //PropertyChangeSupport.Mixin.class
})
public interface PflanzeComposite
    extends ValueArtComposite<PflanzeValue,PflanzenArtComposite>, 
            /*PropertyChangeSupport,*/ Composite {

    public String id();

    public boolean equals( Object obj );

    @Optional
    Property<String>            nummer();

    @Optional
    Property<String>            artengruppeNr();

    @Optional
    Property<Integer>           taxnr();

    @Optional
    Property<String>            taxname();

    @Optional
    Property<String>            name();

    @Optional
    Property<String>            schutzstatus();

    /**
     * @return {@link PflanzeValue#menge()} 
     */
    @Optional
    Property<Integer>           menge();

    @Optional
    Property<Double>            mengenstatusNr();

    
    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            extends ValueArtComposite.Mixin<PflanzeValue,PflanzenArtComposite>
            implements PflanzeComposite {

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
            return new ValueProperty( value().menge() );
        }

        public Property<Double> mengenstatusNr() {
            return new ValueProperty( value().mengenstatusNr() );
        }

    }
    
}
