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
import org.opengis.feature.type.PropertyDescriptor;

import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.model.EntityType;
import org.polymap.rhei.data.entityfeature.PropertyDescriptorAdapter;
import org.polymap.biotop.model.BiotopRepository;
import org.polymap.biotop.model.PflanzeComposite;
import org.polymap.biotop.model.PflanzeValue;
import org.polymap.biotop.model.PflanzenArtComposite;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PflanzenFormPage
        extends ValueArtFormPage<PflanzeValue,PflanzenArtComposite,PflanzeComposite> {

    protected PflanzenFormPage( Feature feature, FeatureStore featureStore ) {
        super( feature, featureStore );
    }

    public String getId() {
        return getClass().getName();
    }

    public String getTitle() {
        return "Pflanzen";
    }

    public Class<PflanzenArtComposite> getArtType() {
        return PflanzenArtComposite.class;
    }

    public Iterable<PflanzeComposite> getElements() {
        return PflanzeComposite.forEntity( biotop );
    }

    public PflanzeComposite newElement( PflanzenArtComposite art ) {
        return PflanzeComposite.newInstance( art );
    }

    public void updateElements( Collection<PflanzeComposite> coll ) {
        PflanzeComposite.updateEntity( biotop, coll );
    }


    public EntityType<PflanzeComposite> addViewerColumns( FeatureTableViewer viewer ) {
        // entity types
        final BiotopRepository repo = BiotopRepository.instance();
        final EntityType<PflanzeComposite> type = repo.entityType( PflanzeComposite.class );

        PropertyDescriptor prop = null;
        prop = new PropertyDescriptorAdapter( type.getProperty( "name" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop )
                 .setHeader( "Name" ));
        prop = new PropertyDescriptorAdapter( type.getProperty( "taxname" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop )
                 .setHeader( "Wissenschaftl." ));
        prop = new PropertyDescriptorAdapter( type.getProperty( "schutzstatus" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop )
                 .setHeader( "Schutzstatus" ));
        prop = new PropertyDescriptorAdapter( type.getProperty( "menge" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop )
                 .setHeader( "Menge" ) 
                 .setEditing( true ) );
//        prop = new PropertyDescriptorAdapter( type.getProperty( "mengenstatusNr" ) );
//        viewer.addColumn( new DefaultFeatureTableColumn( prop )
//                 .setHeader( "MengenstatusNr" ) );
        return type;
    }


    
//    protected Section createPflanzenSection( Composite parent ) {
//        Section section = tk.createSection( parent, Section.TITLE_BAR /*| Section.TREE_NODE*/ );
//        section.setText( "Pflanzen" );
//
//        Composite client = tk.createComposite( section );
//        client.setLayout( new FormLayout() );
//        section.setClient( client );
//
//        viewer = new FeatureTableViewer( client, SWT.NONE );
//        viewer.getTable().setLayoutData( new SimpleFormData().fill().right( 100, -40 ).create() );
//
//        // entity types
//        final BiotopRepository repo = BiotopRepository.instance();
//        final EntityType<PflanzeValue> valueType = repo.entityType( PflanzeValue.class );
//        final EntityType<PflanzenArtComposite> compType = repo.entityType( PflanzenArtComposite.class );
//
//        // columns
//        PropertyDescriptor prop = new PropertyDescriptorAdapter( valueType.getProperty( "pflanzenArtNr" ) );
//        viewer.addColumn( new DefaultFeatureTableColumn( prop )
//                 .setHeader( "Nummer" ) );
//        prop = new PropertyDescriptorAdapter( compType.getProperty( "name" ) );
//        viewer.addColumn( new DefaultFeatureTableColumn( prop )
//                 .setHeader( "Name" ));
//        prop = new PropertyDescriptorAdapter( compType.getProperty( "taxname" ) );
//        viewer.addColumn( new DefaultFeatureTableColumn( prop )
//                 .setHeader( "Taxname" ));
//        prop = new PropertyDescriptorAdapter( compType.getProperty( "schutzstatus" ) );
//        viewer.addColumn( new DefaultFeatureTableColumn( prop )
//                 .setHeader( "Schutzstatus" ));
//        prop = new PropertyDescriptorAdapter( valueType.getProperty( "menge" ) );
//        viewer.addColumn( new DefaultFeatureTableColumn( prop )
//                 .setHeader( "Menge" ) );
////                 .setEditing( true ) );
//        prop = new PropertyDescriptorAdapter( valueType.getProperty( "mengenstatusNr" ) );
//        viewer.addColumn( new DefaultFeatureTableColumn( prop )
//                 .setHeader( "MengenstatusNr" ) );
//
//        // content
//        viewer.setContent( new LinkedCompositesContentProvider<PflanzeValue,PflanzenArtComposite>(
//                biotop.pflanzen().get(), valueType, compType ) {
//            
//            protected PflanzenArtComposite linkedElement( PflanzeValue elm ) {
//                PflanzenArtComposite template = QueryExpressions.templateFor( PflanzenArtComposite.class );
//                BooleanExpression expr = QueryExpressions.eq( template.nummer(), elm.pflanzenArtNr().get() );
//                Query<PflanzenArtComposite> matches = repo.findEntities( PflanzenArtComposite.class, expr, 0 , 1 );
//                return matches.find();
//            }
//        });
//        viewer.setInput( biotop.pflanzen().get() );
//
//        // actions
//        final RemoveValueArtAction removeAction = new RemoveValueArtAction() {
//            public void run() {
//                Collection<PflanzeValue> pflanzen = biotop.pflanzen().get();
//                for (IFeatureTableElement sel : viewer.getSelectedElements()) {
//                    FeatureTableElement elm = (FeatureTableElement)sel;
//                    //viewer.remove( sel );
//                    pflanzen.remove( elm.getComposite() );
//                }
//                biotop.pflanzen().set( pflanzen );
//                viewer.refresh();
//            }
//        };
//        viewer.addSelectionChangedListener( removeAction );
//        
//        Button removeBtn = new ActionButton( client, removeAction );
//        removeBtn.setLayoutData( new SimpleFormData()
//                .left( viewer.getTable(), SECTION_SPACING )
//                .top( 0 ).right( 100 ).height( 30 ).create() );
//        
//        return section;
//    }

}
