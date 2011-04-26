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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

import com.vividsolutions.jts.geom.Geometry;

import org.polymap.core.model.Entity;
import org.polymap.core.qi4j.EntityMixin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@Mixins( {
    BiotopComposite.Mixin.class, 
    EntityMixin.class
//    JsonState.Mixin.class
} )
public interface BiotopComposite
    extends EntityComposite, Entity {

    @Optional
    Property<Geometry>          geom();
    
    Property<String>            schluessel();

    Property<Integer>           biotoptyp();
    
    /** */
    @Optional
    Property<Integer>           erhaltungszustand();

    /**
     * Status_ID
     */
    Property<String>            statusid();

    @Optional
    Property<Date>              erfasst();
    
    @Optional
    Property<Date>              bearbeitet();
    
    @Optional
    Property<String>            bearbeiter();
    

    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements BiotopComposite {
        
        private static Log log = LogFactory.getLog( Mixin.class );
    
    }
    
}
