/*
 * polymap.org
 * Copyright 2013, Falko Br‰utigam. All rights reserved.
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
package org.polymap.biotop.model.constant;

import org.polymap.rhei.model.ConstantWithSynonyms;

/**
 * Provides 'Potentielle Gef‰hrdung' constants.
 *
 * @author <a href="http://www.polymap.de">Falko Br‰utigam</a>
 */
public class GefahrLevel
        extends ConstantWithSynonyms<String> {

    /** Provides access to the elements of this type. */
    public static final Type<GefahrLevel,String> all = new Type<GefahrLevel,String>();

    public static final GefahrLevel unknown = new GefahrLevel( 0, "keine Angaben", "" );
    public static final GefahrLevel schwach = new GefahrLevel( 1, "schwach gef‰hrdet", "" );
    public static final GefahrLevel m‰ﬂig = new GefahrLevel( 2, "m‰ﬂig gef‰hrdet", "" );
    public static final GefahrLevel stark = new GefahrLevel( 3, "stark gef‰hrdet", "" );


    // instance *******************************************

    private String          description;


    private GefahrLevel( int id, String label, String description, String... synonyms ) {
        super( id, label, synonyms );
        this.description = description;
        all.add( this );
    }

    protected String normalizeValue( String value ) {
        return value.trim().toLowerCase();
    }

}
