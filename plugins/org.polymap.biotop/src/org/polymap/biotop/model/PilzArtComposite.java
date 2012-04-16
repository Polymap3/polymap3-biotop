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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@Concerns( {
    PropertyChangeSupport.Concern.class
} )
@Mixins( {
    PilzArtComposite.Mixin.class,
    PropertyChangeSupport.Mixin.class,
    ModelChangeSupport.Mixin.class,
    QiEntity.Mixin.class
//    JsonState.Mixin.class
} )
@ImportTable("Referenz_Mo_Fle_Pil")
public interface PilzArtComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite {

//    [INFO] MdbImportOperation - Table: Referenz_Mo_Fle_Pil
//    [INFO] MdbImportOperation -     column: Nr_Art - INT
//    [INFO] MdbImportOperation -     column: Nr_Artengruppe_Pflanzen - BYTE
//    [INFO] MdbImportOperation -     column: TAXNR - TEXT
//    [INFO] MdbImportOperation -     column: TAXNAME - TEXT
//    [INFO] MdbImportOperation -     column: DEUTSCHNAME - TEXT
//    [INFO] MdbImportOperation -     column: Nummer_RLSachsen - BYTE
    
    @Optional
    @ImportColumn("Nr_Art")
    Property<String>            nummer();

    @Optional
    @ImportColumn("Nr_Artengruppe_Pflanzen")
    Property<String>            artengruppeNr();

    @Optional
    @ImportColumn("TAXNR")
    Property<Integer>           taxnr();

    @Optional
    @ImportColumn("TAXNAME")
    Property<String>            taxname();

    @Optional
    @ImportColumn("DEUTSCHNAME")
    Property<String>            name();


    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements PilzArtComposite {

        private static Log log = LogFactory.getLog( Mixin.class );

    }

}
