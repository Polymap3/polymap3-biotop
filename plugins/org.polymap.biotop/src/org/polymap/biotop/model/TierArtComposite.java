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

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@Concerns( {
    PropertyChangeSupport.Concern.class
} )
@Mixins( {
    TierArtComposite.Mixin.class,
    PropertyChangeSupport.Mixin.class,
    ModelChangeSupport.Mixin.class,
    QiEntity.Mixin.class
//    JsonState.Mixin.class
} )
public interface TierArtComposite
    extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite {

//    [INFO] MdbImportOperation - Table: Referenz_Tiere
//    [INFO] MdbImportOperation -     column: Nr_Tier - INT
//    [INFO] MdbImportOperation -     column: Nr_Artengruppe_Tiere - BYTE
//    [INFO] MdbImportOperation -     column: ART_Name - TEXT
//    [INFO] MdbImportOperation -     column: Gattung - TEXT
//    [INFO] MdbImportOperation -     column: Species - TEXT
//    [INFO] MdbImportOperation -     column: SYS_Code - TEXT
//    [INFO] MdbImportOperation -     column: Nummer_RLSachsen - BYTE
//    [INFO] MdbImportOperation -     column: Schutzstatus - TEXT
    
    @Optional
    @ImportColumn("Nr_Tier")
    Property<String>            nummer();

    @Optional
    @ImportColumn("Nr_Artengruppe_Tiere")
    Property<String>            artengruppeNr();

    @Optional
    @ImportColumn("Gattung")
    Property<String>            gattung();

    @Optional
    @ImportColumn("Species")
    Property<String>            species();

    @Optional
    @ImportColumn("ART_Name")
    Property<String>            name();

    @Optional
    @ImportColumn("Nummer_RLSachsen")
    Property<String>            rlSachsenNr();

    @Optional
    @ImportColumn("Schutzstatus")
    Property<String>            schutzstatus();

    @Optional
    @ImportColumn("SYS_Code")
    Property<String>            sysCode();


    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements TierArtComposite {

        private static Log log = LogFactory.getLog( Mixin.class );

    }

}
