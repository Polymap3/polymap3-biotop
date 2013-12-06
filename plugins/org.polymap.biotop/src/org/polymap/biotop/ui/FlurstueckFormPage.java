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

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.getOnlyElement;

import java.util.HashMap;
import java.util.Map;

import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.Property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.forms.widgets.Section;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.model.EntityType;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.project.Layers;
import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.data.entityfeature.CompositesFeatureContentProvider;
import org.polymap.rhei.data.entityfeature.PlainValuePropertyAdapter;
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.data.entityfeature.PropertyDescriptorAdapter;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.form.DefaultFormEditorPage;
import org.polymap.rhei.form.FormEditorDialog;
import org.polymap.rhei.form.IFormEditorPage2;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.form.IFormEditorToolkit;

import org.polymap.biotop.BiotopPlugin;
import org.polymap.biotop.model.BiotopComposite;
import org.polymap.biotop.model.BiotopRepository;
import org.polymap.biotop.model.FlurstueckComposite;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FlurstueckFormPage
        extends DefaultFormEditorPage
        implements IFormEditorPage2 {

    private static Log log = LogFactory.getLog( FlurstueckFormPage.class );
    
    private BiotopRepository                repo;

    private BiotopComposite                 biotop;

    private IFormEditorToolkit              tk;

    private FeatureTableViewer              viewer;

    private Map<String,FlurstueckComposite> model = new HashMap();

    private boolean                         dirty;


    protected FlurstueckFormPage( Feature feature, FeatureStore fs ) {
        super( "Flurstücke", "Flurstücke", feature, fs );
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

        pageSite.setFormTitle( "Biotop: " + biotop.objnr().get() );
        pageSite.getPageBody().setLayout( FormLayoutFactory.defaults()
                .spacing( BiotopFormPageProvider.SECTION_SPACING*2 )
                .margins( BiotopFormPageProvider.SECTION_SPACING ).create() );

        Composite section1 = createGemarkungSection( pageSite.getPageBody() );
        section1.setLayoutData( FormDataFactory.filled().bottom( -1 ).create() );

        Composite section2 = createTableSection( pageSite.getPageBody() );
        section2.setLayoutData( FormDataFactory.filled().top( section1 ).create() );

        pageSite.getPageBody().layout( true );
    }


    @Override
    public void doLoad( IProgressMonitor monitor ) throws Exception {
        if (viewer != null) {
            model = new HashMap();
            for (FlurstueckComposite elm : biotop.flurstuecke()) {
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
            // remove all (no ManyAssociation#clear())
            for (FlurstueckComposite entity : biotop.flurstuecke()) {
                biotop.flurstuecke().remove( entity );
            }
            // add model entities
            for (FlurstueckComposite elm : model.values() ) {
                biotop.flurstuecke().add( elm );
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

    
    protected Composite createGemarkungSection( final Composite parent ) {
        final Section section = tk.createSection( parent, Section.TITLE_BAR );
        section.setLayout( new FormLayout() );
        section.setText( "Gemarkung/Gemeinde" );

        final Composite client = tk.createComposite( section );
        client.setLayoutData( new SimpleFormData().fill().create() );
        FillLayout layout = new FillLayout();
        layout.marginHeight = layout.marginWidth = layout.spacing = 3;
        client.setLayout( layout /*RowLayoutFactory.swtDefaults().pack( false ).justify( true ).create()*/ );
        section.setClient( client );

        // Gemeinden
        final StringBuilder buf = new StringBuilder( 256 );
        try {
            IMap map = ((PipelineFeatureSource)fs).getLayer().getMap();
            ILayer layer = getOnlyElement( filter( map.getLayers(), Layers.hasLabel( "Gemeinden" ) ), null );
            
            if (layer == null) {
                buf.append( "[Keine Ebene 'Gemeinden' gefunden.]" );
            }
            else {
                fs = PipelineFeatureSource.forLayer( layer, false );
                FeatureCollection gemeinden = fs.getFeatures( DataPlugin.ff.intersects(
                        DataPlugin.ff.property( fs.getSchema().getGeometryDescriptor().getLocalName() ),
                        DataPlugin.ff.literal( biotop.geom().get() ) ) );

                gemeinden.accepts( new FeatureVisitor() {
                    public void visit( Feature gemeinde ) {
                        buf.append( buf.length() > 0 ? ", " : "" );
                        Property nameProp = gemeinde.getProperty( "ORTSNAME" );
                        buf.append( nameProp != null ? nameProp.getValue().toString() : "-" );
                    }
                }, null );
                log.info( "Kommunen: " + buf.toString() );
            }
        }
        catch (Exception e) {
            log.warn( "", e );
            buf.append( "[Fehler: " + e.getLocalizedMessage() + "]" );
        }
        newFormField( "Gemeinde(n)" ).setParent( client )
                .setProperty( new PlainValuePropertyAdapter( "Gemeinde", buf.toString() ) )
                .setEnabled( false ).setField( new StringFormField() ).create();

        // Gemarkung
        final StringBuilder buf2 = new StringBuilder( 256 );
        try {
            IMap map = ((PipelineFeatureSource)fs).getLayer().getMap();
            ILayer layer = getOnlyElement( filter( map.getLayers(), Layers.hasLabel( "Gemarkungen" ) ), null );
            
            if (layer == null) {
                buf2.append( "[Keine Ebene 'Gemarkungen' gefunden.]" );
            }
            else {
                fs = PipelineFeatureSource.forLayer( layer, false );
                FeatureCollection gemarkungen = fs.getFeatures( DataPlugin.ff.intersects(
                        DataPlugin.ff.property( fs.getSchema().getGeometryDescriptor().getLocalName() ),
                        DataPlugin.ff.literal( biotop.geom().get() ) ) );

                gemarkungen.accepts( new FeatureVisitor() {
                    public void visit( Feature gemeinde ) {
                        buf2.append( buf2.length() > 0 ? ", " : "" );
                        Property nameProp = gemeinde.getProperty( "GMK" );
                        buf2.append( nameProp != null ? nameProp.getValue().toString() : "-" );
                    }
                }, null );
            }
        }
        catch (Exception e) {
            log.warn( "", e );
            buf2.append( "[Konnte nicht ermittelt werden.]" );
        }
        newFormField( "Gemarkung(en)" ).setParent( client )
                .setProperty( new PlainValuePropertyAdapter( "Gemarkung", buf2.toString() ) )
                .setField( new StringFormField() ).setEnabled( false ).create().setEnabled( false );
        return section;
    }

    
    protected Composite createTableSection( final Composite parent ) {
        final Section section = tk.createSection( parent, /*SWT.BORDER |*/ Section.TITLE_BAR );
        section.setLayout( new FormLayout() );
        section.setText( "Flurstücke" );

        final Composite client = tk.createComposite( section );
        client.setLayoutData( new SimpleFormData().fill().create() );
        client.setLayout( new FormLayout() );
        section.setClient( client );

        viewer = new FeatureTableViewer( client, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );
        viewer.getTable().setLayoutData( new SimpleFormData().fill().right( 100, -40 ).height( 200 ).create() );

        // columns
        final EntityType<FlurstueckComposite> type = repo.entityType( FlurstueckComposite.class );
        PropertyDescriptorAdapter prop = null;
        prop = new PropertyDescriptorAdapter( type.getProperty( "zaehler" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Zähler" ).setWeight( 50, 100 ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "nenner" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Nenner" ).setWeight( 50, 100 ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "gemarkung" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Gemarkung" ).setWeight( 50, 150 ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "gemeinde" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Gemeinde" ).setWeight( 50, 150 ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "lage" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Lage" ).setWeight( 100, 200 ) );

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
                final FlurstueckComposite flurstueck = repo.newEntity( FlurstueckComposite.class, null );
                FormEditorDialog dialog = new FormEditorDialog( new DefaultFormEditorPage( "flurstück", "flurstück", null, null ) {
                    @Override
                    public void createFormContent( IFormEditorPageSite site ) {
                        site.setEditorTitle( "Flurstück" );
                        site.setFormTitle( "Neues Flurstück mit Zähler/Nenner anlegen" );
                        site.getPageBody().setLayout( ColumnLayoutFactory.defaults().spacing( 3 ).margins( 3 ).create() );
                        site.newFormField( site.getPageBody(), 
                                new PropertyAdapter( flurstueck.zaehler() ), 
                                new StringFormField(), new NotNullValidator(), "Zähler" );
                        site.newFormField( site.getPageBody(), 
                                new PropertyAdapter( flurstueck.nenner() ),
                                new StringFormField(), new NotNullValidator(), "Nenner" );
                        site.newFormField( site.getPageBody(), 
                                new PropertyAdapter( flurstueck.gemarkung() ),
                                new StringFormField(), null, "Gemarkung" );
                        site.newFormField( site.getPageBody(), 
                                new PropertyAdapter( flurstueck.gemeinde() ),
                                new StringFormField(), null, "Gemeinde" );
                        site.newFormField( site.getPageBody(), 
                                new PropertyAdapter( flurstueck.lage() ),
                                new StringFormField(), null, "Lage" );
                    }
                });
                
                dialog.setBlockOnOpen( true );
                if (dialog.open() == Window.OK) {
                    model.put( flurstueck.id(), flurstueck );
                    
                    pageSite.fireEvent( this, "flurstueck", IFormFieldListener.VALUE_CHANGE, null );
                    viewer.refresh( true );
                    dirty = true;
                }
                else {
                    repo.removeEntity( flurstueck );
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
                
                pageSite.fireEvent( this, "flurstueck", IFormFieldListener.VALUE_CHANGE, null );
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
