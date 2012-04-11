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

import java.util.HashMap;
import java.util.Map;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;
import org.opengis.feature.type.PropertyDescriptor;

import org.qi4j.api.query.Query;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.forms.widgets.Section;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.data.ui.featuretable.IFeatureTableElement;
import org.polymap.core.model.EntityType;
import org.polymap.core.project.ui.util.SimpleFormData;

import org.polymap.rhei.data.entityfeature.CompositesFeatureContentProvider;
import org.polymap.rhei.data.entityfeature.PropertyDescriptorAdapter;
import org.polymap.rhei.form.DefaultFormPageLayouter;
import org.polymap.rhei.form.IFormEditorPage2;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.form.IFormEditorToolkit;

import org.polymap.biotop.model.BiotopComposite;
import org.polymap.biotop.model.BiotopRepository;
import org.polymap.biotop.model.PflanzeComposite;
import org.polymap.biotop.model.PflanzenArtComposite;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PflanzenFormPage
        implements IFormEditorPage2 {

    static final int                SECTION_SPACING = BiotopFormPageProvider.SECTION_SPACING;

    private Feature                 feature;

    private FeatureStore            fs;

    private BiotopComposite         biotop;

    IFormEditorPageSite             site;

    private IFormEditorToolkit      tk;

    private FeatureTableViewer      viewer;

    private Map<String,PflanzeComposite> model;

    private DefaultFormPageLayouter layouter;
    
    private boolean                 dirty;


    protected PflanzenFormPage( Feature feature, FeatureStore featureStore ) {
        this.feature = feature;
        this.fs = featureStore;
        this.biotop = BiotopRepository.instance().findEntity(
                BiotopComposite.class, feature.getIdentifier().getID() );
    }

    public void dispose() {
    }

    public String getId() {
        return getClass().getName();
    }

    public String getTitle() {
        return "Pflanzen";
    }

    public void createFormContent( IFormEditorPageSite _site ) {
        site = _site;
        tk = site.getToolkit();
        layouter = new DefaultFormPageLayouter();

        site.setFormTitle( "Biotop: " + biotop.objnr().get() );
        FormLayout layout = new FormLayout();
        site.getPageBody().setLayout( layout );

        Section pflanzenSection = createPflanzen2Section( site.getPageBody() );
        pflanzenSection.setLayoutData( new SimpleFormData( SECTION_SPACING )
                .left( 0 ).right( 100 ).top( 0, 0 ).bottom( 100 ).create() );
    }


    public void doLoad( IProgressMonitor monitor ) throws Exception {
        if (viewer != null) { viewer.refresh(); }
    }

    public void doSubmit( IProgressMonitor monitor ) throws Exception {
        if (model != null) { biotop.setPflanzen2( model.values() ); }
        dirty = false;
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean isValid() {
        return true;
    }

    
    protected Section createPflanzen2Section( Composite parent ) {
        Section section = tk.createSection( parent, Section.TITLE_BAR );
        section.setText( "Pflanzen" );

        Composite client = tk.createComposite( section );
        client.setLayout( new FormLayout() );
        section.setClient( client );

        viewer = new FeatureTableViewer( client, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );
        viewer.getTable().setLayoutData( new SimpleFormData().fill().bottom( 100 ).right( 100, -40 ).create() );

        // entity types
        final BiotopRepository repo = BiotopRepository.instance();
        final EntityType<PflanzeComposite> type = repo.entityType( PflanzeComposite.class );

        // columns
        PropertyDescriptor prop = null;
        prop = new PropertyDescriptorAdapter( type.getProperty( "name" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop )
                 .setHeader( "Name" ));
        prop = new PropertyDescriptorAdapter( type.getProperty( "taxname" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop )
                 .setHeader( "Taxname" ));
        prop = new PropertyDescriptorAdapter( type.getProperty( "schutzstatus" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop )
                 .setHeader( "Schutzstatus" ));
        prop = new PropertyDescriptorAdapter( type.getProperty( "menge" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop )
                 .setHeader( "Menge" ) );
//                 .setEditing( true ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "mengenstatusNr" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop )
                 .setHeader( "MengenstatusNr" ) );

        // model/content
        model = new HashMap();
        for (PflanzeComposite elm : biotop.getPflanzen2()) {
            model.put( elm.id(), elm );
        }
        viewer.setContent( new CompositesFeatureContentProvider( model.values(), type ) );
        viewer.setInput( biotop.getPflanzen2() );

        // add action
        Query<PflanzenArtComposite> arten = repo.findEntities( PflanzenArtComposite.class, null, 0, 10000 );
        AddValueArtAction addAction = new AddValueArtAction<PflanzenArtComposite>( PflanzenArtComposite.class, arten ) {
            protected void execute( PflanzenArtComposite sel ) throws Exception {
                assert sel != null;
                model.put( sel.id(), biotop.newPflanze2( sel ) );
                dirty = true;
                viewer.getTable().pack();
                site.reloadEditor();
            }
        };
        ActionButton addBtn = new ActionButton( client, addAction );
        addBtn.setLayoutData( new SimpleFormData()
                .left( viewer.getTable(), SECTION_SPACING )
                .top( 0 ).right( 100 ).height( 30 ).create() );

        // remove action
        final RemoveValueArtAction removeAction = new RemoveValueArtAction() {
            public void execute() throws Exception {
                for (IFeatureTableElement sel : viewer.getSelectedElements()) {
                    PflanzeComposite elm = (PflanzeComposite)((CompositesFeatureContentProvider.FeatureTableElement)sel).getComposite();
                    if (model.remove( elm.id() ) == null) {
                        throw new IllegalStateException( "Konnte nicht gelöscht werden: " + elm );
                    }
                    dirty = true;
                }
                // refresh my viewer and update dirty/valid flags of the editor
                site.reloadEditor();
            }
        };
        viewer.addSelectionChangedListener( removeAction );
        
        ActionButton removeBtn = new ActionButton( client, removeAction );
        removeBtn.setLayoutData( new SimpleFormData()
                .left( viewer.getTable(), SECTION_SPACING )
                .top( addBtn, 5 ).right( 100 ).height( 30 ).create() );

        return section;
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

    public Action[] getEditorActions() {
        return null;
    }

}
