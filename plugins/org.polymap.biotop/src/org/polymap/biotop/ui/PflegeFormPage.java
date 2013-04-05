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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.forms.widgets.Section;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.model.EntityType;
import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.data.entityfeature.CompositesFeatureContentProvider;
import org.polymap.rhei.data.entityfeature.CompositesFeatureContentProvider.FeatureTableElement;
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.data.entityfeature.PropertyDescriptorAdapter;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.DefaultFormEditorPage;
import org.polymap.rhei.form.DefaultFormPageLayouter;
import org.polymap.rhei.form.IFormEditorPage2;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.form.IFormEditorToolkit;

import org.polymap.biotop.BiotopPlugin;
import org.polymap.biotop.model.BiotopComposite;
import org.polymap.biotop.model.BiotopRepository;
import org.polymap.biotop.model.PflegeArtComposite;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br‰utigam</a>
 */
public class PflegeFormPage
        extends DefaultFormEditorPage
        implements IFormEditorPage2 {

    private BiotopRepository                repo;

    private BiotopComposite                 biotop;

    private IFormEditorToolkit              tk;

    private DefaultFormPageLayouter         layouter;

    private FeatureTableViewer              viewer;

    private Map<String, PflegeArtComposite> model = new HashMap();

    private boolean                         dirty;


    protected PflegeFormPage( Feature feature, FeatureStore fs ) {
        super( "Pflege", "Pflege", feature, fs );
        this.repo = BiotopRepository.instance();
        this.biotop = repo.findEntity( BiotopComposite.class, feature.getIdentifier().getID() );
    }

    
    @Override
    public void dispose() {
        model = null;
    }


    @Override
    public void createFormContent( IFormEditorPageSite _site ) {
        super.createFormContent( _site );
        tk = pageSite.getToolkit();
        layouter = new DefaultFormPageLayouter();

        pageSite.setFormTitle( "Biotop: " + biotop.objnr().get() );
        pageSite.getPageBody().setLayout( new FormLayout() );

        Section section1 = createFieldsSection( pageSite.getPageBody() );
        section1.setLayoutData( new SimpleFormData( SECTION_SPACING ).fill().top( 0, 0 ).bottom( 40 ).create() );
        
        Section section2 = createTableSection( pageSite.getPageBody() );
        section2.setLayoutData( new SimpleFormData( SECTION_SPACING ).fill().top( section1 ).bottom( 100 ).create() );
        
        pageSite.getPageBody().layout( true );
    }


    @Override
    public void doLoad( IProgressMonitor monitor ) throws Exception {
        if (viewer != null) {
            model = new HashMap();
            for (PflegeArtComposite elm : biotop.pflege()) {
                //elm.addPropertyChangeListener( this );
                model.put( elm.id(), elm );
            }
            viewer.setInput( model.values() );
            viewer.refresh();
        }
        dirty = false;
    }

    
    @Override
    public void doSubmit( IProgressMonitor monitor ) throws Exception {
        if (model != null) { 
            //Iterables.removeIf( biotop.pflege(), Predicates.alwaysTrue() );
            for (PflegeArtComposite entity : biotop.pflege()) {
                biotop.pflege().remove( entity );
            }
            for (PflegeArtComposite elm : model.values() ) {
                biotop.pflege().add( elm );
            }
        }
        dirty = false;
    }

    
    @Override
    public boolean isDirty() {
        return dirty;
    }

    
    @Override
    public boolean isValid() {
        return true;
    }

    
    protected Section createFieldsSection( final Composite parent ) {
        Section section = newSection( "Pfege/Entwicklung", false, null );
        ((Composite)section.getClient()).setLayout( new FormLayout() );

//        layouter.setFieldLayoutData( pageSite.newFormField( (Composite)section.getClient(), 
//                new PropertyAdapter( biotop.pflegeBedarf() ),
//                new CheckboxFormField(), null, "Pflegebedarf" ) );

        Composite field = layouter.setFieldLayoutData( pageSite.newFormField( (Composite)section.getClient(), 
                new PropertyAdapter( biotop.pflegeEntwicklung() ),
                new TextFormField(), null, "Pflege/Entwicklung" ) );
        ((FormData)field.getLayoutData()).height = 100;
        
        layouter.newLayout();
        return section;
    }

    
    protected Section createTableSection( final Composite parent ) {
        final Section section = tk.createSection( parent, /*SWT.BORDER |*/ Section.TITLE_BAR );
        section.setLayout( new FormLayout() );
        section.setText( "Pflegemaﬂnahmen" );

        final Composite client = tk.createComposite( section/*, SWT.BORDER*/ );
        client.setLayoutData( new SimpleFormData().fill().create() );
        client.setLayout( new FormLayout() );
        section.setClient( client );

        viewer = new FeatureTableViewer( client, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );
        viewer.getTable().setLayoutData( new SimpleFormData().fill().right( 100, -40 ).create() );

        // columns
        final EntityType<PflegeArtComposite> type = repo.entityType( PflegeArtComposite.class );
        PropertyDescriptorAdapter prop = null;
        prop = new PropertyDescriptorAdapter( type.getProperty( "name" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Name" ).setWeight( 50, 200 ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "beschreibung" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Beschreibung" ).setWeight( 100, 300 ) );

        // model/content
        viewer.setContent( new CompositesFeatureContentProvider( null, type ) );
        try {
            doLoad( new NullProgressMonitor() );
        } 
        catch (Exception e) {
            throw new RuntimeException( e );
        }
        client.layout( true );
        parent.layout( true );

        // add action
        Button addBtn = new Button( client, SWT.PUSH );
        addBtn.setImage( BiotopPlugin.getDefault().imageForName( "icons/add.gif" ) );
        addBtn.setLayoutData( new SimpleFormData()
                .left( viewer.getTable(), SECTION_SPACING )
                .top( 0 ).right( 100 ).height( 30 ).create() );
        
        addBtn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {
                ListDialog list = new ListDialog( PolymapWorkbench.getShellToParentOn() );
                list.setTitle( "Pflegemaﬂnahmen hinzuf¸gen" );
                list.setMessage( "W‰hlen Sie eine Maﬂnahme" );
                list.setContentProvider( new CompositesFeatureContentProvider( null, type ) );
                list.setLabelProvider( new LabelProvider() {
                    public String getText( Object o ) {
                        FeatureTableElement elm = (FeatureTableElement)o;
                        return elm.getValue( "name" ) + " -- " + elm.getValue( "beschreibung" ) + "";
                    }
                });
                list.setInput( repo.findEntities( PflegeArtComposite.class, null, 0, 1000 ) );
                
                list.setBlockOnOpen( true );
                if (list.open() == Window.OK) {
                    FeatureTableElement elm = (FeatureTableElement)list.getResult()[0];
                    PflegeArtComposite entity = (PflegeArtComposite)elm.getComposite();
                    model.put( entity.id(), entity );
                    
                    pageSite.fireEvent( this, "pflege", IFormFieldListener.VALUE_CHANGE, null );
                    viewer.refresh( true );
                    dirty = true;
                }
            }
        });

        // remove
        final Button removeBtn = new Button( client, SWT.PUSH );
        removeBtn.setImage( BiotopPlugin.getDefault().imageForName( "icons/delete_edit.gif" ) );
        removeBtn.setLayoutData( new SimpleFormData()
                .left( viewer.getTable(), SECTION_SPACING )
                .top( addBtn, 5 ).right( 100 ).height( 30 ).create() );

        removeBtn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                model.remove( viewer.getSelectedElements()[0].fid() );
                
                pageSite.fireEvent( this, "pflege", IFormFieldListener.VALUE_CHANGE, null );
                viewer.refresh( true );
                dirty = true;
            }
        });
        removeBtn.setEnabled( false );
        viewer.addSelectionChangedListener( new ISelectionChangedListener() {
            public void selectionChanged( SelectionChangedEvent ev ) {
                removeBtn.setEnabled( !viewer.getSelection().isEmpty() );
            }
        });
        return section;
    }

}
