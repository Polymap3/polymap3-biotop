/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.biotop.ui;

import java.util.Collection;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;
import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.model.EntityType;
import org.polymap.rhei.data.entityfeature.PropertyDescriptorAdapter;
import org.polymap.biotop.model.BiotopRepository;
import org.polymap.biotop.model.StoerungsArtComposite;
import org.polymap.biotop.model.StoerungComposite;
import org.polymap.biotop.model.StoerungValue;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class StoerungFormPage
        extends ValueArtFormPage<StoerungValue,StoerungsArtComposite,StoerungComposite> {

    protected StoerungFormPage( Feature feature, FeatureStore featureStore ) {
        super( feature, featureStore );
    }

    public String getTitle() {
        return "Beeinträchtigungen";
    }

    public Class<StoerungsArtComposite> getArtType() {
        return StoerungsArtComposite.class;
    }

    public Iterable<StoerungComposite> getElements() {
        return StoerungComposite.forEntity( biotop );
    }

    public StoerungComposite newElement( StoerungsArtComposite art ) {
        return StoerungComposite.newInstance( art );
    }

    public void updateElements( Collection<StoerungComposite> coll ) {
        StoerungComposite.updateEntity( biotop, coll );
    }

    public EntityType<StoerungComposite> addViewerColumns( FeatureTableViewer viewer ) {
        // entity types
        final BiotopRepository repo = BiotopRepository.instance();
        final EntityType<StoerungComposite> type = repo.entityType( StoerungComposite.class );

        // columns
        PropertyDescriptorAdapter prop = null;
        prop = new PropertyDescriptorAdapter( type.getProperty( "name" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop )
                 .setHeader( "Name" ));
//        prop = new PropertyDescriptorAdapter( type.getProperty( "menge" ) );
//        viewer.addColumn( new DefaultFeatureTableColumn( prop )
//                 .setHeader( "Menge" ));
//        prop = new PropertyDescriptorAdapter( type.getProperty( "mengenstatusNr" ) );
//        viewer.addColumn( new DefaultFeatureTableColumn( prop )
//                 .setHeader( "MengenstatusNr" ));

        return type;
    }

}
