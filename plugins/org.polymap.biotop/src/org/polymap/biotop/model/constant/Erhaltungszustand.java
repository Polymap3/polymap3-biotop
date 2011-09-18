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
 * Provides 'Erhaltungszustand' constants.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
public class Erhaltungszustand
        extends ConstantWithSynonyms<String> {

    /** Provides access to the elements of this type. */
    public static final Type<Erhaltungszustand,String> all = new Type<Erhaltungszustand,String>();
    
    public static final Erhaltungszustand guenstig = new Erhaltungszustand( 0, "günstig", "günstig" );

    public static final Erhaltungszustand unzureichend = new Erhaltungszustand( 1, "ungünstig/unzureichend", "unzureichend" );

    public static final Erhaltungszustand schlecht = new Erhaltungszustand( 2, "ungünstig/schlecht", "schlecht" );

    
    // instance *******************************************
    
    private String          description;
    
    
    private Erhaltungszustand( int id, String label, String description, String... synonyms ) {
        super( id, label, synonyms );
        this.description = description;
        all.add( this );
    }

    protected String normalizeValue( String value ) {
        return value.trim().toLowerCase();
    }
    
}
