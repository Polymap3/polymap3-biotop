/* 
 * polymap.org
 * Copyright 2010, Falko Br‰utigam, and other contributors as indicated
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
 *
 * 
 * @author <a href="http://www.polymap.de">Falko Br‰utigam</a>
 */
public class Pflegezustand
        extends ConstantWithSynonyms<String> {

    /** Provides access to the elements of this type. */
    public static final Type<Pflegezustand,String> all = new Type<Pflegezustand,String>();
    
    public static final Pflegezustand gut = new Pflegezustand( 0, "gut", "" );

    public static final Pflegezustand maessig = new Pflegezustand( 1, "m‰ﬂig", "" );

    public static final Pflegezustand schlecht = new Pflegezustand( 2, "schlecht", "" );

    
    // instance *******************************************
    
    private String          description;
    
    
    private Pflegezustand( int id, String label, String description, String... synonyms ) {
        super( id, label, synonyms );
        this.description = description;
        all.add( this );
    }

    protected String normalizeValue( String value ) {
        return value.trim().toLowerCase();
    }
    
}
