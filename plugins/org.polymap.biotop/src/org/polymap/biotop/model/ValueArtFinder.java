/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
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

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.value.ValueComposite;

import org.polymap.core.model.Entity;

/**
 * Provides the logic to find the corresponding Art {@link EntityComposite}
 * for a given value.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface ValueArtFinder<V extends ValueComposite,A extends Entity> {

    public A find( V value );
    
}
