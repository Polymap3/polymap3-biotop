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
    BiotoptypArtComposite.Mixin.class,
    PropertyChangeSupport.Mixin.class,
    ModelChangeSupport.Mixin.class,
    QiEntity.Mixin.class
//    JsonState.Mixin.class
} )
public interface BiotoptypArtComposite
    extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite {

//    [INFO] MdbImportOperation -     column: Nr_Biotoptyp - BYTE
//    [INFO] MdbImportOperation -     column: Biotoptyp_Code - TEXT
//    [INFO] MdbImportOperation -     column: Biotoptyp - TEXT
//    [INFO] MdbImportOperation -     column: Untergruppen_Code - TEXT
//    [INFO] MdbImportOperation -     column: Biotopgruppe_Code - TEXT
//    [INFO] MdbImportOperation -     column: Schutz_§26 - BYTE
//    [INFO] MdbImportOperation -     column: Nr_§26 - LONG
//    [INFO] MdbImportOperation -     column: FFH_Relevanz - LONG

    /** */
    @Optional
    @ImportColumn("Nr_Biotoptyp")
    Property<String>            nummer();

    /** Import von: Biotoptyp */
    @Optional
    @ImportColumn("Biotoptyp")
    Property<String>            name();

    @Optional
    @ImportColumn("Biotoptyp_Code")
    Property<String>            code();

    @Optional
    @ImportColumn("Untergruppen_Code")
    Property<String>            untergruppeCode();

    @Optional
    @ImportColumn("Biotopgruppe_Code")
    Property<String>            biotopgruppeCode();

    @Optional
    @ImportColumn("Schutz_§26")
    Property<Integer>           schutz26();

    @Optional
    @ImportColumn("Nr_§26")
    Property<Integer>           nummer26();

    @Optional
    @ImportColumn("FFH_Relevanz")
    Property<Integer>           ffh_Relevanz();


    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements BiotoptypArtComposite {

        private static Log log = LogFactory.getLog( Mixin.class );

    }

}
