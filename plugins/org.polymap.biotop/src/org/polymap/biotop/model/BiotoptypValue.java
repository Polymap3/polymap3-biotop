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
public interface BiotoptypValue
        extends ValueComposite, Composite {
    
//  [INFO] MdbImportOperation -     column: TK25 - INT
//  [INFO] MdbImportOperation -     column: Objektnummer - TEXT
//  [INFO] MdbImportOperation -     column: Nr_Biotoptyp - BYTE
//  [INFO] MdbImportOperation -     column: Biotop_Unternummer - TEXT
//  [INFO] MdbImportOperation -     column: Biotoptyp_Flächenprozent - BYTE
//  [INFO] MdbImportOperation -     column: Biotoptyp_Länge - INT
//  [INFO] MdbImportOperation -     column: Biotoptyp_Breite - FLOAT
//  [INFO] MdbImportOperation -     column: Pflegerückstand - BYTE

    @Optional
    @ImportColumn("Nr_Biotoptyp")
    Property<String>            biotoptypArtNr();

    @Optional
    @ImportColumn("Biotop_Unternummer")
    Property<String>            unternummer();

    @Optional
    @ImportColumn("Biotoptyp_Flächenprozent")
    Property<Double>            flaechenprozent();

    @Optional
    @ImportColumn("Biotoptyp_Länge")
    Property<Double>            laenge();

    @Optional
    @ImportColumn("Biotoptyp_Breite")
    Property<Double>            breite();

    @Optional
    @ImportColumn("Pflegerückstand")
    Property<Integer>           pflegerueckstand();

}
