/*
 * polymap.org
 * Copyright 2010, Falko Bräutigam, and other contributors as indicated
 * by the @authors tag.
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
 *
 * $Id: $
 */
package org.polymap.biotop.model.constant;

import org.polymap.rhei.model.ConstantWithSynonyms;

/**
 * Provides 'Schutzstatus' constants.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Schutzstatus
        extends ConstantWithSynonyms<String> {

    /** Provides access to the elements of this type. */
    public static final Type<Schutzstatus,String> all = new Type<Schutzstatus,String>();

    public static final Schutzstatus para_26_30 = new Schutzstatus( 0, "§26 / §30", "" );

    public static final Schutzstatus wertvoll = new Schutzstatus( 1, "wertvoll", "" );


    // instance *******************************************

    private String          description;


    private Schutzstatus( int id, String label, String description, String... synonyms ) {
        super( id, label, synonyms );
        this.description = description;
        all.add( this );
    }

    protected String normalizeValue( String value ) {
        return value.trim().toLowerCase();
    }

}
