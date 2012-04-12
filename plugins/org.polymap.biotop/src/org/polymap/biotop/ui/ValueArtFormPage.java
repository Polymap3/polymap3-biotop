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
import java.util.HashMap;
import java.util.Map;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;
import org.qi4j.api.query.Query;
import org.qi4j.api.value.ValueComposite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.forms.widgets.Section;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.data.ui.featuretable.IFeatureTableElement;
import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;
import org.polymap.core.project.ui.util.SimpleFormData;

import org.polymap.rhei.data.entityfeature.CompositesFeatureContentProvider;
import org.polymap.rhei.form.DefaultFormPageLayouter;
import org.polymap.rhei.form.IFormEditorPage2;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.form.IFormEditorToolkit;

import org.polymap.biotop.model.BiotopComposite;
import org.polymap.biotop.model.BiotopRepository;
import org.polymap.biotop.model.PflanzeComposite;
import org.polymap.biotop.model.ValueArtComposite;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class ValueArtFormPage<V extends ValueComposite, A extends Entity, C extends ValueArtComposite<V,A>>
        implements IFormEditorPage2, PropertyChangeListener {

    static final int                SECTION_SPACING = BiotopFormPageProvider.SECTION_SPACING;

    private Feature                 feature;

    private FeatureStore            fs;

    protected BiotopComposite       biotop;

    IFormEditorPageSite             site;

    private IFormEditorToolkit      tk;

    private FeatureTableViewer      viewer;

    private Map<String,C>           model;

    private DefaultFormPageLayouter layouter;
    
    private boolean                 dirty;

    // sub-class interface ********************************
    
    public abstract String getTitle();
    
    public abstract Class<A> getArtType();
    
    public abstract EntityType<C> addViewerColumns( FeatureTableViewer viewer2 );
    
    public abstract Iterable<C> getElements();

    public abstract void updateElements( Collection<C> coll );
    
    public abstract C newElement( A art );

    // impl ***********************************************
    
    protected ValueArtFormPage( Feature feature, FeatureStore featureStore ) {
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

    public void createFormContent( IFormEditorPageSite _site ) {
        site = _site;
        tk = site.getToolkit();
        layouter = new DefaultFormPageLayouter();

        site.setFormTitle( "Biotop: " + biotop.objnr().get() );
        FormLayout layout = new FormLayout();
        site.getPageBody().setLayout( layout );

        Section section = createSection( site.getPageBody() );
        section.setLayoutData( new SimpleFormData( SECTION_SPACING )
                .left( 0 ).right( 100 ).top( 0, 0 ).bottom( 100 ).create() );
    }


    public void doLoad( IProgressMonitor monitor ) throws Exception {
        if (viewer != null) { viewer.refresh(); }
    }

    public void doSubmit( IProgressMonitor monitor ) throws Exception {
        if (model != null) { updateElements( model.values() ); }
        dirty = false;
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean isValid() {
        return true;
    }
    
    /**
     * Handles Value property changes. 
     */
    public void propertyChange( PropertyChangeEvent evt ) {
        try {
            dirty = true;
            site.reloadEditor();
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }
    
    protected Section createSection( Composite parent ) {
        Section section = tk.createSection( parent, Section.TITLE_BAR );
        section.setText( getTitle() );

        Composite client = tk.createComposite( section );
        client.setLayout( new FormLayout() );
        section.setClient( client );

        viewer = new FeatureTableViewer( client, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );
        viewer.getTable().setLayoutData( new SimpleFormData().fill().bottom( 100 ).right( 100, -40 ).create() );

        // entity types
        final BiotopRepository repo = BiotopRepository.instance();

        // columns
        EntityType type = addViewerColumns( viewer );

        // model/content
        model = new HashMap();
        for (C elm : getElements()) {
            elm.addPropertyChangeListener( this );
            model.put( elm.id(), elm );
        }
        viewer.setContent( new CompositesFeatureContentProvider( model.values(), type ) );
        viewer.setInput( model );

        // add action
        Query<A> arten = repo.findEntities( getArtType(), null, 0, 10000 );
        AddValueArtAction addAction = new AddValueArtAction<A>( getArtType(), arten ) {
            protected void execute( A sel ) throws Exception {
                assert sel != null;
                model.put( sel.id(), newElement( sel ) );
                dirty = true;
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

    public Action[] getEditorActions() {
        return null;
    }

}
