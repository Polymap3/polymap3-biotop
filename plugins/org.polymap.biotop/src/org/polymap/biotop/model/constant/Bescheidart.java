/* 
 * polymap.org
 * Copyright 2010, Falko Br�utigam, and other contributors as indicated
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
 * Provides 'Kostenbescheidtyp' constants.
 * 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 * @version ($Revision$)
 */
public class Bescheidart
        extends ConstantWithSynonyms<String> {

    /** Provides access to the elements of this type. */
    public static final Type<Bescheidart,String> all = new Type<Bescheidart,String>();
    
    public static final Bescheidart VKV = new Bescheidart( 10, "Vorl�ufiger Kostenbescheid", 
            "Gem�� � 15 Abs. 1 S�chsVwKG wird mit der Bearbeitung des Antrages erst nach Zahlung des Kostenvorschusses begonnen. Nach Vornahme der Amtshandlung ergeht unter Beachtung des bezahlten Kostenvorschusses eine endg�ltige Kostenentscheidung. Wird der Kostenvorschuss in der gesetzten Frist nicht bezahlt, wird der Antrag gem�� � 15 Abs. 1 S�chsVwKG als zur�ckgenommen behandelt. Die daf�r zu erhebende Geb�hr wird gem�� � 10 Abs. 2 S�chsVwKG festgesetzt.",
            "vkv" );

    public static final Bescheidart VRK = new Bescheidart( 20, "Kostenbescheid", 
            "Detaillierte Informationen zur Geb�hrenfestsetzung entnehmen Sie bitte der Anlage.",
            "vrk" );

    public static final Bescheidart VRR = new Bescheidart( 30, "Rechnung", 
            "Detaillierte Informationen �ber die Rechnungspositionen entnehmen Sie bitte der Anlage.",
            "vrr" );

    
    // instance *******************************************
    
    private String          description;
    
    
    private Bescheidart( int id, String label, String description, String... synonyms ) {
        super( id, label, synonyms );
        this.description = description;
        all.add( this );
    }

    protected String normalizeValue( String value ) {
        return value.trim().toLowerCase();
    }
    
}
