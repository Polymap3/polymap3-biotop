/*
 * polymap.org
 * Copyright 2011, Falko Br�utigam, and other contributors as
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
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.event.ModelChangeSupport;
import org.polymap.core.qi4j.event.PropertyChangeSupport;

import org.polymap.biotop.model.importer.ImportColumn;
import org.polymap.biotop.model.importer.ImportTable;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
@Concerns( {
    PropertyChangeSupport.Concern.class
} )
@Mixins( {
    StoerungsArtComposite.Mixin.class,
    PropertyChangeSupport.Mixin.class,
    ModelChangeSupport.Mixin.class,
    QiEntity.Mixin.class
//    JsonState.Mixin.class
} )
@ImportTable("Referenz_Beeintr�chtigung")
public interface StoerungsArtComposite
    extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite {

//    INFO] MdbImportOperation - Table: Referenz_Beeintr�chtigung
//    [INFO] MdbImportOperation -     column: Nr_Beeintr�chtigung - BYTE
//    [INFO] MdbImportOperation -     column: Beeintr�chtigung - TEXT
    
    @Optional
    @ImportColumn("Nr_Beeintr�chtigung")
    Property<String>            nummer();

    @Optional
    @ImportColumn("Beeintr�chtigung")
    Property<String>            name();


    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements StoerungsArtComposite {

    }

}
