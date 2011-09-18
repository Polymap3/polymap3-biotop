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

import java.util.Collection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Computed;
import org.qi4j.api.property.ComputedPropertyInstance;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyInfo;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
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
    BiotopComposite.Mixin.class,
    PropertyChangeSupport.Mixin.class,
    ModelChangeSupport.Mixin.class,
    QiEntity.Mixin.class
//    JsonState.Mixin.class
} )
public interface BiotopComposite
    extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite {
    
//    [INFO] MdbImportOperation -     column: TK25 - INT
//    [INFO] MdbImportOperation -     column: Objektnummer - TEXT
//    [INFO] MdbImportOperation -     column: Biotopname - TEXT
//    [INFO] MdbImportOperation -     column: GSCHLNEU - LONG
//    [INFO] MdbImportOperation -     column: GSCHLNEU2 - LONG
//    [INFO] MdbImportOperation -     column: GSCHLNEU3 - LONG
//    [INFO] MdbImportOperation -     column: Biotopfläche - FLOAT
//    [INFO] MdbImportOperation -     column: Länge_Biotop - INT
//    [INFO] MdbImportOperation -     column: Breite_Biotop - INT
//    [INFO] MdbImportOperation -     column: Anzahl_Teilflächen - BYTE
//    [INFO] MdbImportOperation -     column: Lage - TEXT
//    [INFO] MdbImportOperation -     column: Forstliche_Karte - TEXT
//    [INFO] MdbImportOperation -     column: Lage_TK25 - TEXT
//    [INFO] MdbImportOperation -     column: Rechtswert - LONG
//    [INFO] MdbImportOperation -     column: Hochwert - LONG
//    [INFO] MdbImportOperation -     column: Höhe_min - INT
//    [INFO] MdbImportOperation -     column: Höhe_max - INT
//    [INFO] MdbImportOperation -     column: Nr_Kartierer - INT
//    [INFO] MdbImportOperation -     column: Erfassung - SHORT_DATE_TIME
//    [INFO] MdbImportOperation -     column: Eingabe - SHORT_DATE_TIME
//    [INFO] MdbImportOperation -     column: Biotopbeschreibung - MEMO
//    [INFO] MdbImportOperation -     column: Angrenzende_Bereiche - MEMO
//    [INFO] MdbImportOperation -     column: Bemerkungen - MEMO
//    [INFO] MdbImportOperation -     column: Quellen_für_Artangaben - MEMO
//    [INFO] MdbImportOperation -     column: Vegetationseinheiten - MEMO
//    [INFO] MdbImportOperation -     column: Pflege_Entwicklung - MEMO
//    [INFO] MdbImportOperation -     column: Nr_Potentielle_Gefährdung - BYTE
//    [INFO] MdbImportOperation -     column: Ausbildung - BYTE
//    [INFO] MdbImportOperation -     column: Nr_Naturraum - BYTE
//    [INFO] MdbImportOperation -     column: Nr_Naturraum_Flußauen - BYTE
//    [INFO] MdbImportOperation -     column: Nr_FA - TEXT
//    [INFO] MdbImportOperation -     column: Nr_Revier - TEXT
//    [INFO] MdbImportOperation -     column: Nr_im_Revier - TEXT
//    [INFO] MdbImportOperation -     column: Nr_Eigentumsart - BYTE
//    [INFO] MdbImportOperation -     column: Abteilung - TEXT
//    [INFO] MdbImportOperation -     column: Teilfläche - TEXT
//    [INFO] MdbImportOperation -     column: Index - BYTE
//    [INFO] MdbImportOperation -     column: WG_Nr - BYTE
//    [INFO] MdbImportOperation -     column: WB_Nr - INT
//    [INFO] MdbImportOperation -     column: Nr_Klimastufe - BYTE
//    [INFO] MdbImportOperation -     column: Nr_Standort - BYTE
//    [INFO] MdbImportOperation -     column: Nr_LBT - BYTE
//    [INFO] MdbImportOperation -     column: Totholzstufe_liegend - BYTE
//    [INFO] MdbImportOperation -     column: Totholzstufe_stehend - BYTE

    @Optional
    Property<MultiPolygon>      geom();

    /** Wird aus der Geometry errechnet. */
    @Computed
    Property<Double>            flaeche();

    /** Wird aus der Geometry errechnet. */
    @Computed
    Property<Double>            umfang();

    /** Interne Objektnummer - laufende Nummer. */
    @Optional
    Property<String>            objnr();

    /** Importierte Objektnummer des SBK (objnr). */
    @Optional
    @ImportColumn("Objektnummer")
    Property<String>            objnr_sbk();

    /** Alte Objektnummer Landkreise. */
    @Optional
    Property<String>            objnr_landkreise();

    @Optional
    @ImportColumn("TK25")
    Property<String>            tk25();

    @Optional
    @ImportColumn("Lage_TK25")
    Property<String>            lage_tk25();

    @Optional
    @ImportColumn("Lage")
    Property<String>            lage();

    @Optional
    @ImportColumn("Biotopname")
    Property<String>            name();

    @Optional
    @ImportColumn("Biotopbeschreibung")
    Property<String>            beschreibung();

    @Optional
    @ImportColumn("Bemerkungen")
    Property<String>            bemerkungen();

    @Optional
    @ImportColumn("Angrenzende_Bereiche")
    Property<String>            angrenzendeBereiche();

    @Optional
    @ImportColumn("Abteilung")
    Property<String>            abteilung();

    @Optional
    @ImportColumn("Ausbildung")
    Property<String>            ausbildung();

    @Optional
    @ImportColumn("Pflege_Entwicklung")
    Property<String>            pflegeEntwicklung();

    @Optional
    @ImportColumn("Teilfläche")
    Property<String>            teilflaeche();

    @Optional
    Property<String>            bt_code();

    @Optional
    Property<String>            wert();

    @Optional
    Property<Integer>           biotoptyp();

    @Optional
    Property<String>            biotopkuerzel();

    /** @see Erhaltungszustand */
    @Optional
    Property<Integer>           erhaltungszustand();

    /** @see Schutzstatus */
    @Optional
    Property<Integer>           schutzstatus();

    /** @see Status */
    @Optional
    Property<Integer>           status();

    @Optional
    Property<AktivitaetValue>   erfassung();

    @Optional
    Property<AktivitaetValue>   bearbeitung();

    /** Wenn {@link #status()} <code>nicht_aktiv</code>, dann Wann, Wer, Warum gelöscht. */
    @Optional
    Property<AktivitaetValue>   löschung();

    /** Letzte Bekanntmachung. */
    @Optional
    Property<AktivitaetValue>   bekanntmachung();

    /** Letzte Prüfung der Daten. */
    @Optional
    Property<Boolean>           geprueft();

    @Optional
    @UseDefaults
    Property<Collection<BiotoptypValue>> biotoptypen();

//    @Computed
//    Property<Date>              bearbeitet();
//
//    @Computed
//    Property<String>            bearbeiter();

    @Optional
    @ImportColumn("Nr_Naturraum")
    Property<String>            naturraumNr();

    @Optional
    @ImportColumn("Nr_Naturraum_Flußauen")
    Property<String>            naturraumFlussauenNr();

    @Optional
    @ImportColumn("Nr_FA")
    Property<String>            faNr();

    @Optional
    @ImportColumn("Nr_Revier")
    Property<String>            revierNr();

    @Optional
    @ImportColumn("Nr_im_Revier")
    Property<String>            imRevierNr();

    @Optional
    @ImportColumn("Nr_Eigentumsart")
    Property<String>            eigentumsartNr();

    
    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements BiotopComposite {

        private static Log log = LogFactory.getLog( Mixin.class );

        private PropertyInfo        flaecheInfo = new GenericPropertyInfo( BiotopComposite.class, "flaeche" );
        private PropertyInfo        umfangInfo = new GenericPropertyInfo( BiotopComposite.class, "umfang" );
//        private PropertyInfo        bearbeitetInfo = new GenericPropertyInfo( BiotopComposite.class, "bearbeitet" );
//        private PropertyInfo        bearbeiterInfo = new GenericPropertyInfo( BiotopComposite.class, "bearbeiter" );


        public Property<Double> flaeche() {
            return new ComputedPropertyInstance( flaecheInfo ) {
                public Object get() {
                    Geometry geom = geom().get();
                    return geom != null ? geom.getArea() : -1;
                }
            };
        }

        public Property<Double> umfang() {
            return new ComputedPropertyInstance( umfangInfo ) {
                public Object get() {
                    Geometry geom = geom().get();
                    return geom != null ? geom.getLength() : -1;
                }
            };
        }

//        public Property<Date> bearbeitet() {
//            return new ComputedPropertyInstance( groesseInfo ) {
//                public Object get() {
//                    Long lastModified = _lastModified().get();
//                    return new Date( lastModified != null ? lastModified : 0 );
//                }
//            };
//        }
//
//        public Property<String> bearbeiter() {
//            return new ComputedPropertyInstance( groesseInfo ) {
//                public Object get() {
//                    return _lastModifiedBy().get();
//                }
//            };
//        }

    }

}
