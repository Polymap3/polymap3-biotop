/*
 * polymap.org
 * Copyright 2011, Falko Br�utigam, and other contributors as
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

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.ManyAssociation;
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

import org.polymap.biotop.model.constant.Erhaltungszustand;
import org.polymap.biotop.model.constant.GefahrLevel;
import org.polymap.biotop.model.constant.Schutzstatus;
import org.polymap.biotop.model.constant.Status;
import org.polymap.biotop.model.importer.ImportColumn;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
@Concerns( {
    PropertyChangeSupport.Concern.class
} )
@Mixins( {
    BiotopComposite.Mixin.class,
    PropertyChangeSupport.Mixin.class,
    ModelChangeSupport.Mixin.class,
    QiEntity.Mixin.class,
    JsonState.Mixin.class
} )
public interface BiotopComposite
    extends QiEntity, JsonState, PropertyChangeSupport, ModelChangeSupport, EntityComposite {
    
//    [INFO] MdbImportOperation -     column: TK25 - INT
//    [INFO] MdbImportOperation -     column: Objektnummer - TEXT
//    [INFO] MdbImportOperation -     column: Biotopname - TEXT
//    [INFO] MdbImportOperation -     column: GSCHLNEU - LONG
//    [INFO] MdbImportOperation -     column: GSCHLNEU2 - LONG
//    [INFO] MdbImportOperation -     column: GSCHLNEU3 - LONG
//    [INFO] MdbImportOperation -     column: Biotopfl�che - FLOAT
//    [INFO] MdbImportOperation -     column: L�nge_Biotop - INT
//    [INFO] MdbImportOperation -     column: Breite_Biotop - INT
//    [INFO] MdbImportOperation -     column: Anzahl_Teilfl�chen - BYTE
//    [INFO] MdbImportOperation -     column: Lage - TEXT
//    [INFO] MdbImportOperation -     column: Forstliche_Karte - TEXT
//    [INFO] MdbImportOperation -     column: Lage_TK25 - TEXT
//    [INFO] MdbImportOperation -     column: Rechtswert - LONG
//    [INFO] MdbImportOperation -     column: Hochwert - LONG
//    [INFO] MdbImportOperation -     column: H�he_min - INT
//    [INFO] MdbImportOperation -     column: H�he_max - INT
//    [INFO] MdbImportOperation -     column: Nr_Kartierer - INT
//    [INFO] MdbImportOperation -     column: Erfassung - SHORT_DATE_TIME
//    [INFO] MdbImportOperation -     column: Eingabe - SHORT_DATE_TIME
//    [INFO] MdbImportOperation -     column: Biotopbeschreibung - MEMO
//    [INFO] MdbImportOperation -     column: Angrenzende_Bereiche - MEMO
//    [INFO] MdbImportOperation -     column: Bemerkungen - MEMO
//    [INFO] MdbImportOperation -     column: Quellen_f�r_Artangaben - MEMO
//    [INFO] MdbImportOperation -     column: Vegetationseinheiten - MEMO
//    [INFO] MdbImportOperation -     column: Pflege_Entwicklung - MEMO
//    [INFO] MdbImportOperation -     column: Nr_Potentielle_Gef�hrdung - BYTE
//    [INFO] MdbImportOperation -     column: Ausbildung - BYTE
//    [INFO] MdbImportOperation -     column: Nr_Naturraum - BYTE
//    [INFO] MdbImportOperation -     column: Nr_Naturraum_Flu�auen - BYTE
//    [INFO] MdbImportOperation -     column: Nr_FA - TEXT
//    [INFO] MdbImportOperation -     column: Nr_Revier - TEXT
//    [INFO] MdbImportOperation -     column: Nr_im_Revier - TEXT
//    [INFO] MdbImportOperation -     column: Nr_Eigentumsart - BYTE
//    [INFO] MdbImportOperation -     column: Abteilung - TEXT
//    [INFO] MdbImportOperation -     column: Teilfl�che - TEXT
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

    /** Anzahl der Teil-Geometrien. Wird aus der Geometry errechnet. */
    @Computed
    Property<Integer>           numGeom();

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
    Property<String>            unr();

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
    Property<Integer>           pflegeZustand();

    @Optional
    @ImportColumn("Pflege_Entwicklung")
    Property<String>            pflegeEntwicklung();

    @Optional
    Property<Boolean>           pflegeBedarf();

    /** Fr�her in {@link BiotoptypValue}. */
    @Optional
    Property<Integer>           pflegeRueckstand();

    /**
     * Pflegema�nahmen
     */
    @Optional
    @UseDefaults
    ManyAssociation<PflegeArtComposite> pflege();

    @Optional
    @UseDefaults
    ManyAssociation<FlurstueckComposite> flurstuecke();

//    @Optional
//    @ImportColumn("Teilfl�che")
//    Property<String>            teilflaeche();

    @Optional
    Property<String>            bt_code();

    /** Ist der {@link Schutzstatus}. Kommt aus Shapefile und wird nach {@link #schutzstatus()} �berf�hrt. */
    @Optional
    Property<String>            wert();

    /** @see Schutzstatus */
    @Optional
    @UseDefaults
    Property<Integer>           schutzstatus();

    @Optional
    Property<String>            biotopkuerzel();

    /** @see Erhaltungszustand */
    @Optional
    @UseDefaults
    Property<Integer>           erhaltungszustand();

    /** @see Status */
    @Optional
    @UseDefaults
    Property<Integer>           status();

    @Optional
    Property<AktivitaetValue>   erfassung();

    @Optional
    Property<AktivitaetValue>   bearbeitung();

    /** Wenn {@link #status()} <code>nicht_aktiv</code>, dann Wann, Wer, Warum gel�scht. */
    @Optional
    Property<AktivitaetValue>   loeschung();

    /** Letzte Bekanntmachung. */
    @Optional
    Property<AktivitaetValue>   bekanntmachung();

    /** Letzte Pr�fung der Daten. */
    @Optional
    @UseDefaults
    Property<Boolean>           geprueft();

    /**
     * <b>Alter</b> SBK-Biotoptyp.
     * 
     * @see BiotoptypArtComposite#nummer() 
     */
    @Optional
    Property<String>            biotoptypArtNr();

    /**
     * Verkn�pfung zu aktuellem {@link BiotoptypArtComposite2}.
     *  
     * @see BiotoptypArtComposite2#nummer() 
     */
    @Optional
    Property<String>            biotoptyp2ArtNr();

//    /** Nur w�hrend Import: prozent f�r den aktuellen Biotoptyp. */
//    @Optional
//    Property<Double>            biotoptypArtProzent();

    /**
     * Mehrere Geometrien pro Biotop werden beim Import aufgel�st, so auch
     * die damit verbundenen Biotoptypen. Es gibt nur noch einen Biotoptyp
     * pro Biotop in {@link #biotoptypArtNr()}.
     * <p/>
     * Siehe auch: <a href="http://polymap.org/biotop/ticket/32">Ticket #32</a> 
     */
    @Optional
    @UseDefaults
    @Deprecated
    Property<Collection<BiotoptypValue>> biotoptypen();
    
    
    /**
     * Neue, einheitliche Artdaten ohne Mengenangaben. Verweis auf
     * {@link ArtdatenComposite#nummer()}.
     */
    @Optional
    @UseDefaults
    Property<Collection<String>> arten();

    /**
     * SBK-Planzenarten
     * @see PflanzeComposite
     */
    @Optional
    @UseDefaults
    Property<Collection<PflanzeValue>> pflanzen();

    /** SBK: Moose/Flechten/Pilze */
    @Optional
    @UseDefaults
    Property<Collection<PilzValue>> pilze();

    /**
     * SBK Tierarten
     * @see TierComposite
     */
    @Optional
    @UseDefaults
    Property<Collection<TierValue>> tiere();

    /**
     * @see GefahrComposite
     */
    @Optional
    @UseDefaults
    Property<Collection<GefahrValue>> gefahr();

    /** 
     * Potentielle Gef�hrdung 
     * @see GefahrLevel
     */
    //@Optional
    @UseDefaults
    Property<Integer> gefahrLevel();

    /**
     * @see StoerungComposite
     */
    @Optional
    @UseDefaults
    Property<Collection<StoerungValue>> stoerungen();

    /**
     * @see GefahrComposite
     */
    @Optional
    @UseDefaults
    Property<Collection<WertValue>> werterhaltend();

    @Optional
    @ImportColumn("Nr_Naturraum")
    Property<String>            naturraumNr();

    @Optional
    @ImportColumn("Nr_Naturraum_Flu�auen")
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
     * Neue, bislang nicht in den SBS-Daten enthaltene Waldbiotope erfasst die UNB in
     * der Landkreis-Biotopebene der Fachschale. Um diese Biotope schnell und
     * unkompliziert als Waldbiotope identifizieren zu k�nnen, ist ein gesondertes
     * Attribut *Waldbiotop* (Checkbox in der GUI) n�tig. Dem SBS wird hernach eine
     * speziell konfigurierte WFS-URL (eingeschr�nkt auf die erfassten oder
     * ge�nderten Waldbiotope innerhalb eines definierten Zeitraumes) zur Verf�gung
     * gestellt.
     * <p/>
     * Siehe: #44: Waldbiotopkartierung fehlt (http://polymap.org/biotop/ticket/44)
     */
    @UseDefaults
    Property<Boolean>           waldbiotop();
    
    
    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements BiotopComposite {

        private static Log log = LogFactory.getLog( Mixin.class );

        private BiotopRepository    repo = BiotopRepository.instance();
        
        private PropertyInfo        flaecheInfo = new GenericPropertyInfo( BiotopComposite.class, "flaeche" );
        private PropertyInfo        umfangInfo = new GenericPropertyInfo( BiotopComposite.class, "umfang" );
        private PropertyInfo        numGeomInfo = new GenericPropertyInfo( BiotopComposite.class, "numGeom" );
//        private PropertyInfo        bearbeitetInfo = new GenericPropertyInfo( BiotopComposite.class, "bearbeitet" );
//        private PropertyInfo        bearbeiterInfo = new GenericPropertyInfo( BiotopComposite.class, "bearbeiter" );


        public Property<Double> flaeche() {
            return new ComputedPropertyInstance( flaecheInfo ) {
                public Object get() {
                    Geometry geom = geom().get();
                    return geom != null
                        ? new BigDecimal( geom.getArea() ).setScale( 2, RoundingMode.HALF_UP ).doubleValue()
                        : -1;
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

        public Property<Integer> numGeom() {
            return new ComputedPropertyInstance( numGeomInfo ) {
                public Object get() {
                    Geometry geom = geom().get();
                    return geom != null ? geom.getNumGeometries() : -1;
                }
            };
        }

//        public void beforeCompletion()
//        throws UnitOfWorkCompletionException {
//            EntityState entityState = EntityInstance.getEntityInstance( composite ).entityState();
//            //return qi4j.getEntityState( composite ).lastModified();
//            
//            // hope that Qi4J lets run just one UoW completion at once; otherwise we have
//            // race consitions between check and set of lastModified property between the
//            // threads
//            
//            switch (entityState.status()) {
//                case NEW:
//                case UPDATED:
//            Principal user = Polymap.instance().getUser();
//            
//            ValueBuilder<AktivitaetValue> builder = BiotopRepository.instance().newValueBuilder( AktivitaetValue.class );
//            AktivitaetValue prototype = builder.prototype();
//            prototype.wann().set( new Date() );
//            prototype.wer().set( user.getName() );
//            prototype.bemerkung().set( "" );
//            bearbeitung().set( builder.newInstance() );
//        }

        
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
