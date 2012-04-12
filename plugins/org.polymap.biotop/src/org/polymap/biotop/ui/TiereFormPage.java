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
import org.eclipse.jface.action.Action;

import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.model.EntityType;
import org.polymap.rhei.data.entityfeature.PropertyDescriptorAdapter;
import org.polymap.biotop.model.BiotopRepository;
import org.polymap.biotop.model.TierArtComposite;
import org.polymap.biotop.model.TierComposite;
import org.polymap.biotop.model.TierValue;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class TiereFormPage
        extends ValueArtFormPage<TierValue,TierArtComposite,TierComposite> {


    protected TiereFormPage( Feature feature, FeatureStore featureStore ) {
        super( feature, featureStore );
    }

    public String getTitle() {
        return "Tiere";
    }

    public Class<TierArtComposite> getArtType() {
        return TierArtComposite.class;
    }

    public Iterable<TierComposite> getElements() {
        return biotop.getTiere2();
    }

    public TierComposite newElement( TierArtComposite art ) {
        return biotop.newTier2( art );
    }

    public void updateElements( Collection<TierComposite> coll ) {
        biotop.setTiere2( coll );
    }

    public EntityType<TierComposite> addViewerColumns( FeatureTableViewer viewer ) {
        // entity types
        final BiotopRepository repo = BiotopRepository.instance();
        final EntityType<TierComposite> type = repo.entityType( TierComposite.class );

        PropertyDescriptorAdapter prop = new PropertyDescriptorAdapter( type.getProperty( "name" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop )
                 .setHeader( "Name" ));
        prop = new PropertyDescriptorAdapter( type.getProperty( "gattung" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop )
                 .setHeader( "Gattung" ));
        prop = new PropertyDescriptorAdapter( type.getProperty( "schutzstatus" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop )
                 .setHeader( "Schutzstatus" ));
        prop = new PropertyDescriptorAdapter( type.getProperty( "menge" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop )
                 .setHeader( "Menge" )
                 .setEditing( true ));
        prop = new PropertyDescriptorAdapter( type.getProperty( "mengenstatusNr" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop )
                 .setHeader( "MengenstatusNr" ));
        return type;
    }

    public Action[] getEditorActions() {
        return null;
    }

}
