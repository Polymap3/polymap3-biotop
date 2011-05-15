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
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

import org.polymap.core.model.Composite;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface BiotoptypValue
        extends ValueComposite, Composite {

    @Optional
    Property<String>            objnr();

    @Optional
    Property<String>            objnr_sbk();

    @Optional
    Property<Integer>           tk25();

    @Optional
    Property<Integer>           nummer();

    @Optional
    Property<String>            unternummer();

    @Optional
    Property<Double>            flaechenprozent();

    @Optional
    Property<Double>            laenge();

    @Optional
    Property<Double>            breite();

    @Optional
    Property<Integer>           pflegerueckstand();

}
