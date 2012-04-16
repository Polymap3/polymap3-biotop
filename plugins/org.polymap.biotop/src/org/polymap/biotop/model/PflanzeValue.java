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

import org.polymap.biotop.model.importer.ImportColumn;
import org.polymap.biotop.model.importer.ImportTable;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@ImportTable("Pflanzen")
public interface PflanzeValue
        extends ValueComposite, Composite {
    
//    [INFO] MdbImportOperation - Table: Pflanzen
//    [INFO] MdbImportOperation -     column: TK25 - INT
//    [INFO] MdbImportOperation -     column: Objektnummer - TEXT
//    [INFO] MdbImportOperation -     column: Nr_Pflanze - INT
//    [INFO] MdbImportOperation -     column: Menge_Pflanzenart - INT
//    [INFO] MdbImportOperation -     column: Nr_Mengenstatus - BYTE

    @Optional
    @ImportColumn("Nr_Pflanze")
    Property<String>            pflanzenArtNr();

    @Optional
    @ImportColumn("Menge_Pflanzenart")
    Property<Integer>           menge();

    @Optional
    @ImportColumn("Nr_Mengenstatus")
    Property<Double>            mengenstatusNr();

}
