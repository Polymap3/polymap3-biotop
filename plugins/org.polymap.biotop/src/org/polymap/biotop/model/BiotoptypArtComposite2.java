/*
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
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
@ImportTable("Referenz_Biotoptypen")
public interface BiotoptypArtComposite2
    extends QiEntity, JsonState, PropertyChangeSupport, ModelChangeSupport, EntityComposite {

// CSV:
//    Bezeichnung Biotoptyp 2012
//    Code 2002
//    Code 2012
//    Nr_Btyp_2012
//    NR_VWV_ALT_2002 
//    VwV 2012
//    Schutz_P26_2002 
//    Schutz §26/§30_2012

    /** */
    @Optional
    @ImportColumn("ID_Biotoptyp")
    Property<String>            nummer();

    @Optional
    @ImportColumn("Bezeichnung Biotoptyp 2012")
    Property<String>            bezeichnung();

    @Optional
    @ImportColumn("Bezeichnung Biotoptyp 2002")
    Property<String>            bezeichnung_2002();

    @Optional
    @ImportColumn("Code 2012")
    Property<String>            code();

    @Optional
    @ImportColumn("Code 2002")
    Property<String>            code_2002();

    @Optional
    @ImportColumn("Schutz §26/§30_2012")
    Property<String>            schutz26();

    @Optional
    @ImportColumn("Schutz_P26_2002")
    Property<String>            schutz26_2002();

    @Optional
    @ImportColumn("Nr_Btyp_2012")
    Property<String>            nummer_2012();

    @Optional
    @ImportColumn("Nr_Btyp_2002")
    Property<String>            nummer_2002();

    @Optional
    @ImportColumn("NR_VWV_ALT_2002")
    Property<String>            vwv_2002();

    @Optional
    @ImportColumn("VwV 2012")
    Property<String>            vwv();


//    /**
//     * Methods and transient fields.
//     */
//    public static abstract class Mixin
//            implements BiotoptypArtComposite {
//
//        private static Log log = LogFactory.getLog( Mixin.class );
//
//    }

}
