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

import com.vividsolutions.jts.geom.MultiPolygon;
import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.event.ModelChangeSupport;
import org.polymap.core.qi4j.event.PropertyChangeSupport;

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

    @Optional
    Property<MultiPolygon>      geom();

    /** Länge/Fläche. Wird aus der Geometry errechnet. */
    @Computed
    Property<Double>            groesse();

    /** Interne Objektenummer - laufende Nummer. */
    @Optional
    Property<String>            objnr();

    /** Importierte Objektnummer des SBK (objnr). */
    @Optional
    Property<String>            objnr_sbk();

    /** Alte Objektnummer Landkreise. */
    @Optional
    Property<String>            objnr_landkreise();

    /** Importiert aus SBK. */
    @Optional
    Property<Integer>           tk25();

    @Optional
    Property<String>            oid();

    @Optional
    Property<String>            name();

    @Optional
    Property<String>            beschreibung();

    @Optional
    Property<String>            bemerkungen();

    @Optional
    Property<String>            unr();

    @Optional
    Property<String>            bid();

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

    @Optional
    @UseDefaults
    Property<Collection<BiotoptypValue>>    biotoptypen();

//    @Computed
//    Property<Date>              bearbeitet();
//
//    @Computed
//    Property<String>            bearbeiter();


    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements BiotopComposite {

        private static Log log = LogFactory.getLog( Mixin.class );

        private PropertyInfo        groesseInfo = new GenericPropertyInfo( BiotopComposite.class, "groesse" );
//        private PropertyInfo        bearbeitetInfo = new GenericPropertyInfo( BiotopComposite.class, "bearbeitet" );
//        private PropertyInfo        bearbeiterInfo = new GenericPropertyInfo( BiotopComposite.class, "bearbeiter" );


        public Property<Double> groesse() {
            return new ComputedPropertyInstance( groesseInfo ) {
                public Object get() {
                    MultiPolygon geom = geom().get();
                    return geom != null ? geom.getArea() : -1;
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
