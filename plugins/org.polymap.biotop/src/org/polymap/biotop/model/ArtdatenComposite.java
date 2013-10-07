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
    PropertyChangeSupport.Mixin.class,
    ModelChangeSupport.Mixin.class,
    QiEntity.Mixin.class,
    JsonState.Mixin.class
} )
@ImportTable("Referenz_Pflanzen")
public interface ArtdatenComposite
    extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite {
    
    //Artbezeichnung Artbezeichnung wiss. Artengruppe BNatSchG Natura 2000 Rote Liste Sachsen
    
    @Optional
    @ImportColumn("ID_Art")
    Property<String>            nummer();

    @Optional
    @ImportColumn("Artbezeichnung")
    Property<String>            bezeichnung();

    @Optional
    @ImportColumn("Artbezeichnung (wiss)")
    Property<String>            nomenklatur();

    @Optional
    @ImportColumn("Artengruppe")
    Property<String>            gruppe();

    @Optional
    @ImportColumn("BNatSchG")
    Property<String>            BNatSchG();

    @Optional
    @ImportColumn("Natura 2000")
    Property<String>            natura2000();

    /** Rote Liste Sachsen */
    @Optional
    @ImportColumn("Rote Liste Sachsen")
    Property<String>            rls();

    /** Abbildung der alten Unterteilung in: Pflanzen, Fische, Tiere, etc. */
    @Optional
    @ImportColumn("Kategorie")
    Property<String>            kategorie();

}
