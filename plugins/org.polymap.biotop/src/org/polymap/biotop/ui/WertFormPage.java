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
import org.polymap.biotop.model.WertArtComposite;
import org.polymap.biotop.model.WertComposite;
import org.polymap.biotop.model.WertValue;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class WertFormPage
        extends ValueArtFormPage<WertValue,WertArtComposite,WertComposite> {

    protected WertFormPage( Feature feature, FeatureStore featureStore ) {
        super( feature, featureStore );
    }

    public String getTitle() {
        return "Wertbestimmend";
    }

    public Class<WertArtComposite> getArtType() {
        return WertArtComposite.class;
    }

    public Iterable<WertComposite> getElements() {
        return WertComposite.forEntity( biotop );
    }

    public WertComposite newElement( WertArtComposite art ) {
        return WertComposite.newInstance( art );
    }

    public void updateElements( Collection<WertComposite> coll ) {
        WertComposite.updateEntity( biotop, coll );
    }

    public EntityType<WertComposite> addViewerColumns( FeatureTableViewer viewer ) {
        // entity types
        final BiotopRepository repo = BiotopRepository.instance();
        final EntityType<WertComposite> type = repo.entityType( WertComposite.class );

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
