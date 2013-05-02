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
import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.data.ui.featuretable.IFeatureTableElement;
import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;
import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.data.entityfeature.CompositesFeatureContentProvider;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.form.DefaultFormPageLayouter;
import org.polymap.rhei.form.IFormEditorPage2;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.form.IFormEditorToolkit;

import org.polymap.biotop.model.BiotopComposite;
import org.polymap.biotop.model.BiotopRepository;
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

    protected IFormEditorToolkit    tk;

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

    public byte getPriority() {
        return 0;
    }

    public void createFormContent( IFormEditorPageSite _site ) {
        site = _site;
        tk = site.getToolkit();
        layouter = new DefaultFormPageLayouter();

        site.setFormTitle( "Biotop: " + biotop.objnr().get() );
        site.getPageBody().setLayout( new FormLayout() );

        createSection( site.getPageBody() );
    }


    public void doLoad( IProgressMonitor monitor ) throws Exception {
        if (viewer != null) {
            model = new HashMap();
            for (C elm : getElements()) {
                elm.addPropertyChangeListener( this );
                model.put( elm.id(), elm );
            }
            viewer.setInput( model.values() );
            viewer.refresh(); 
        }
        dirty = false;
    }

    public void doSubmit( IProgressMonitor monitor ) throws Exception {
        if (model != null) { 
            updateElements( model.values() );
        }
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
            // update dirty/valid flags of the editor
            site.fireEvent( this, "ValueArtFormPage", IFormFieldListener.VALUE_CHANGE, null );
            viewer.refresh( true );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }
    
    protected Section createSection( final Composite parent ) {
        final Section section = tk.createSection( parent, /*SWT.BORDER |*/ Section.TITLE_BAR );
        section.setLayoutData( new SimpleFormData( 5 ).fill().top( 0, 0 ).bottom( 100, -10 ).create() );
        section.setLayout( new FormLayout() );
        section.setText( getTitle() );

        final Composite client = tk.createComposite( section/*, SWT.BORDER*/ );
        client.setLayoutData( new SimpleFormData().fill().bottom( 100 ).create() );
        client.setLayout( new FormLayout() );
        section.setClient( client );

        viewer = new FeatureTableViewer( client, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );
        viewer.getTable().setLayoutData( new SimpleFormData().fill().right( 100, -40 ).create() );

        // entity types
        final BiotopRepository repo = BiotopRepository.instance();

        // columns
        EntityType type = addViewerColumns( viewer );

        // model/content
        viewer.setContent( new CompositesFeatureContentProvider( null, type ) );
        try {
            doLoad( new NullProgressMonitor() );
        } catch (Exception e) {
            throw new RuntimeException( e );
        }
        client.layout( true );
        parent.layout( true );

        // add action
        Query<A> arten = repo.findEntities( getArtType(), null, 0, 10000 );
        AddValueArtAction addAction = new AddValueArtAction<A>( getArtType(), arten ) {
            protected void execute( A sel ) throws Exception {
                assert sel != null;
                model.put( sel.id(), newElement( sel ) );
                dirty = true;
                Polymap.getSessionDisplay().asyncExec( new Runnable() {
                    public void run() {
                        // update dirty/valid flags of the editor
                        site.fireEvent( this, "ValueArtFormPage", IFormFieldListener.VALUE_CHANGE, null );
                        viewer.refresh( true );
                        viewer.getTable().layout( true );
                    }
                });
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
                    C elm = (C)((CompositesFeatureContentProvider.FeatureTableElement)sel).getComposite();
                    if (model.remove( elm.id() ) == null) {
                        throw new IllegalStateException( "Konnte nicht gelöscht werden: " + elm );
                    }
                    dirty = true;
                }
                Polymap.getSessionDisplay().asyncExec( new Runnable() {
                    public void run() {
                        // update dirty/valid flags of the editor
                        site.fireEvent( this, "ValueArtFormPage", IFormFieldListener.VALUE_CHANGE, null );
                        viewer.refresh();
                    }
                });
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
