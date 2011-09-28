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

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface TierValue
        extends ValueComposite, Composite {
    
//    [INFO] MdbImportOperation - Table: Tiere
//    [INFO] MdbImportOperation -     column: TK25 - INT
//    [INFO] MdbImportOperation -     column: Objektnummer - TEXT
//    [INFO] MdbImportOperation -     column: Nr_Tier - INT
//    [INFO] MdbImportOperation -     column: Menge_Tierart - INT
//    [INFO] MdbImportOperation -     column: Nr_Mengenstatus - BYTE
    
    @Optional
    @ImportColumn("Nr_Tier")
    Property<String>            tierArtNr();

    @Optional
    @ImportColumn("Menge_Tierart")
    Property<Integer>           menge();

    @Optional
    @ImportColumn("Nr_Mengenstatus")
    Property<Double>            mengenstatusNr();

}
