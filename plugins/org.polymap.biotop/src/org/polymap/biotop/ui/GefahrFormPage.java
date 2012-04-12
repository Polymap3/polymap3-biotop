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
import org.polymap.biotop.model.GefahrArtComposite;
import org.polymap.biotop.model.GefahrComposite;
import org.polymap.biotop.model.GefahrValue;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class GefahrFormPage
        extends ValueArtFormPage<GefahrValue,GefahrArtComposite,GefahrComposite> {

    protected GefahrFormPage( Feature feature, FeatureStore featureStore ) {
        super( feature, featureStore );
    }

    public String getTitle() {
        return "Gefährdungen";
    }

    public Class<GefahrArtComposite> getArtType() {
        return GefahrArtComposite.class;
    }

    public Iterable<GefahrComposite> getElements() {
        return GefahrComposite.forEntity( biotop );
    }

    public GefahrComposite newElement( GefahrArtComposite art ) {
        return GefahrComposite.newInstance( art );
    }

    public void updateElements( Collection<GefahrComposite> coll ) {
        GefahrComposite.updateEntity( biotop, coll );
    }

    public EntityType<GefahrComposite> addViewerColumns( FeatureTableViewer viewer ) {
        // entity types
        final BiotopRepository repo = BiotopRepository.instance();
        final EntityType<GefahrComposite> type = repo.entityType( GefahrComposite.class );

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
