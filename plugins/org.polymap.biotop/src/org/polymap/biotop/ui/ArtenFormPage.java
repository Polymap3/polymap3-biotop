/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.biotop.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;
import org.opengis.feature.type.PropertyDescriptor;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.query.grammar.EqualsPredicate;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Section;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.data.ui.featuretable.IFeatureTableElement;
import org.polymap.core.model.EntityType;
import org.polymap.core.model.EntityType.Property;
import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.data.entityfeature.CompositesFeatureContentProvider;
import org.polymap.rhei.data.entityfeature.PropertyDescriptorAdapter;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.form.DefaultFormEditorPage;
import org.polymap.rhei.form.IFormEditorPage2;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.form.IFormEditorToolkit;

import org.polymap.biotop.BiotopPlugin;
import org.polymap.biotop.model.ArtdatenComposite;
import org.polymap.biotop.model.BiotopComposite;
import org.polymap.biotop.model.BiotopRepository;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ArtenFormPage
        extends DefaultFormEditorPage
        implements IFormEditorPage2 {

    private static Log log = LogFactory.getLog( ArtenFormPage.class );
    
    private BiotopRepository        repo;
    
    private BiotopComposite         biotop;

    private IFormEditorToolkit      tk;

    private boolean                 dirty;

    private FeatureTableViewer      viewer;

    private Map<String,ArtdatenComposite> model = new HashMap();

    
    public ArtenFormPage( Feature feature, FeatureStore fs ) {
        super( "Artdaten", "Artdaten", feature, fs );
        this.repo = BiotopRepository.instance();
        this.biotop = repo.findEntity( BiotopComposite.class, feature.getIdentifier().getID() );        
    }


    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public boolean isValid() {
        return true;
    }


    @Override
    public void doLoad( IProgressMonitor monitor ) throws Exception {
        BooleanExpression expr = null;
        ArtdatenComposite template = QueryExpressions.templateFor( ArtdatenComposite.class );
        for (String nummer : biotop.arten().get()) {
            EqualsPredicate<String> eq = QueryExpressions.eq( template.nummer(), nummer );
            expr = expr != null ? QueryExpressions.or( expr, eq ) : eq;
        }
        model.clear();
        if (expr != null) {
            for (ArtdatenComposite art : repo.findEntities( ArtdatenComposite.class, expr, 0, 1000 )) {
                model.put( art.nummer().get(), art );
            }
        }
        viewer.setInput( model.values() );
        viewer.refresh();
        dirty = false;
    }


    @Override
    public void doSubmit( IProgressMonitor monitor ) throws Exception {
        biotop.arten().set( model.keySet() );
        dirty = false;
    }


    @Override
    public void dispose() {
    }


    @Override
    public void createFormContent( IFormEditorPageSite _site ) {
        super.createFormContent( _site );
        tk = pageSite.getToolkit();
        pageSite.setFormTitle( "Biotop: " + biotop.objnr().get() );
        pageSite.getPageBody().setLayout( new FormLayout() );

        Section section = newSection( "Arten", false, null );
        ((Composite)section.getClient()).setLayout( new FormLayout() );
        section.setLayoutData( new SimpleFormData( SECTION_SPACING ).fill().top( 0, 0 ).bottom( 0, 500 ).create() );

        viewer = new FeatureTableViewer( (Composite)section.getClient(), SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );
        viewer.getTable().setLayoutData( new SimpleFormData().fill().right( 100, -40 ).create() );

        // columns
        final EntityType<ArtdatenComposite> type = repo.entityType( ArtdatenComposite.class );
        PropertyDescriptor prop = null;
        prop = new PropertyDescriptorAdapter( type.getProperty( "bezeichnung" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Name" ));
        prop = new PropertyDescriptorAdapter( type.getProperty( "nomenklatur" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "wiss." ));
        prop = new PropertyDescriptorAdapter( type.getProperty( "kategorie" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Kategorie" ));
        prop = new PropertyDescriptorAdapter( type.getProperty( "gruppe" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Gruppe" ));
        prop = new PropertyDescriptorAdapter( type.getProperty( "nummer" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Nr." ).setWeight( 1, 60 ));
        prop = new PropertyDescriptorAdapter( type.getProperty( "rls" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Rote Liste" ).setWeight( 1, 60 ));
        prop = new PropertyDescriptorAdapter( type.getProperty( "natura2000" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Natura2000" ).setWeight( 1, 60 ));
        prop = new PropertyDescriptorAdapter( type.getProperty( "BNatSchG" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "BNatSchG" ).setWeight( 1, 60 ));

        // model/content
        viewer.setContent( new CompositesFeatureContentProvider( model.values(), type ) );
        viewer.setInput( model.values() );
        try {
            doLoad( new NullProgressMonitor() );
        } 
        catch (Exception e) {
            throw new RuntimeException( e );
        }

        Button addBtn = new Button( (Composite)section.getClient(), SWT.PUSH );
        addBtn.setToolTipText( "Eintrag hinzufügen" );
        addBtn.setImage( BiotopPlugin.getDefault().imageForName( "icons/add.gif" ) );
        addBtn.setLayoutData( new SimpleFormData().left( viewer.getTable(), SECTION_SPACING ).top( 0 ).right( 100 ).height( 30 ).create() );
        addBtn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                try {
                    ArtTableDialog dialog = new ArtTableDialog();
                    dialog.setBlockOnOpen( true );

                    if (dialog.open() == Window.OK) {
                        assert dialog.sel.length == 1 : "Selected: " + dialog.sel.length;
                        
                        for (IFeatureTableElement elm : dialog.sel) {
                            ArtdatenComposite art = repo.findEntity( ArtdatenComposite.class, elm.fid() );
                            model.put( art.nummer().get(), art );
                        }
                        viewer.refresh( true );
                        viewer.getTable().getParent().layout( true );
                        //((Composite)section.getClient()).layout( true );

                        dirty = true;
                        pageSite.fireEvent( this, "ValueArtFormPage", IFormFieldListener.VALUE_CHANGE, null );
                    }
                }
                catch (Exception e) {
                    PolymapWorkbench.handleError( BiotopPlugin.PLUGIN_ID, this, "Fehler beim Öffnen Suchtabelle.", e );
                }
            }
        });

        final Button removeBtn = new Button( (Composite)section.getClient(), SWT.PUSH );
        removeBtn.setToolTipText( "Eintrag entfernen" );
        removeBtn.setImage( BiotopPlugin.getDefault().imageForName( "icons/delete_edit.gif" ) );
        removeBtn.setLayoutData( new SimpleFormData().left( viewer.getTable(), SECTION_SPACING ).top( addBtn, 5 ).right( 100 ).height( 30 ).create() );
        removeBtn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                for (IFeatureTableElement elm : viewer.getSelectedElements()) {
                    ArtdatenComposite art = repo.findEntity( ArtdatenComposite.class, elm.fid() );
                    model.remove( art.nummer().get() );
                }
                viewer.refresh( true );
                viewer.getTable().getParent().layout( true );
                dirty = true;
                pageSite.fireEvent( this, "ValueArtFormPage", IFormFieldListener.VALUE_CHANGE, null );
            }
        });
        removeBtn.setEnabled( false );
        viewer.addSelectionChangedListener( new ISelectionChangedListener() {
            public void selectionChanged( SelectionChangedEvent ev ) {
                removeBtn.setEnabled( viewer.getSelectedElements().length > 0 );
            }
        });
        
        viewer.getTable().pack();
        pageSite.getPageBody().layout( true );
        ((Composite)section.getClient()).layout( true );
    }
    
    
    /**
     * 
     */
    static class ArtTableDialog
            extends TitleAreaDialog {

        private Text                    searchTxt;
        
        private Label                   resultLabel;

        private FeatureTableViewer      viewer;

        private IFeatureTableElement[]  sel;

        final BiotopRepository          repo = BiotopRepository.instance();
        
        final EntityType<ArtdatenComposite> type = repo.entityType( ArtdatenComposite.class );
        
        
        public ArtTableDialog() {
            super( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() );
            setShellStyle( getShellStyle() | SWT.RESIZE );
        }

        protected Image getImage() {
            return getShell().getDisplay().getSystemImage( SWT.ICON_QUESTION );
        }

        protected Point getInitialSize() {
            return new Point( 1000, 700 );
            //return super.getInitialSize();
        }

        protected Control createDialogArea( Composite parent ) {
            Composite result = (Composite)super.createDialogArea( parent );
            
            Composite area = new Composite( result, SWT.NONE );
            area.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

            FormLayout layout = new FormLayout();
            layout.marginHeight = layout.marginWidth = layout.spacing = 10;
            area.setLayout( layout );

            setTitle( "Auswählen" );
            setMessage( "Wählen Sie die \"Art\" des neuen Eintrages." );

            // search text
            Label l = new Label( area, SWT.NONE );
            l.setText( "Suchen nach:" );
            l.setLayoutData( SimpleFormData.filled().top( 0, 4 ).bottom( -1 ).right( -1 ).create() );
            
            resultLabel = new Label( area, SWT.NONE );
            resultLabel.setText( "Ergebnisse: 0" );
            resultLabel.setLayoutData( SimpleFormData.filled().top( 0, 4 ).bottom( -1 ).left( 50 ).create() );
            
            searchTxt = new Text( area, SWT.BORDER );
            searchTxt.setFocus();
            searchTxt.setToolTipText( "Geben Sie min. 3 Anfangsbuchstaben einer Bezeichnung, Nummer oder Kategorie ein.\nBitte beachten Sie Groß- und Kleinschreibung!" );
            searchTxt.setLayoutData( SimpleFormData.filled().left( l ).right( resultLabel ).bottom( -1 ).create() );
            searchTxt.addModifyListener( new ModifyListener() {
                public void modifyText( ModifyEvent ev ) {
                    updateSearch( searchTxt.getText() );
                }
            });

            // table
            viewer = new FeatureTableViewer( area, SWT.BORDER | SWT.MULTI );
            viewer.getTable().setLayoutData( SimpleFormData.filled().top( searchTxt ).create() );

            // columns
            for (Property prop : type.getProperties()) {
                PropertyDescriptorAdapter adapter = new PropertyDescriptorAdapter( prop );
                viewer.addColumn( new DefaultFeatureTableColumn( adapter )
                         .setHeader( StringUtils.capitalize( prop.getName() ) ));
                
            }

            // model/content
            //Query<ArtdatenComposite> content = repo.findEntities( ArtdatenComposite.class, null, 0, 100 );
            viewer.setContent( new CompositesFeatureContentProvider( Collections.EMPTY_LIST, type ) );
            viewer.setInput( Collections.EMPTY_LIST );

            // selection
            viewer.addSelectionChangedListener( new ISelectionChangedListener() {
                public void selectionChanged( SelectionChangedEvent ev ) {
                    sel = viewer.getSelectedElements();
                    getButton( IDialogConstants.OK_ID ).setEnabled( sel.length > 0 );
                }
            });
            
            viewer.getTable().pack( true );
            return result;
        }

        
        protected void updateSearch( String text ) {
            if (text.length() > 2) {
                ArtdatenComposite template = QueryExpressions.templateFor( ArtdatenComposite.class );
                BooleanExpression expr = QueryExpressions.or( 
                        QueryExpressions.matches( template.bezeichnung(), "*"+text+"*" ),
                        QueryExpressions.matches( template.kategorie(), "*"+text+"*" ),
                        QueryExpressions.matches( template.nomenklatur(), "*"+text+"*" ),
                        QueryExpressions.matches( template.gruppe(), "*"+text+"*" )
                        );
                Query<ArtdatenComposite> content = repo.findEntities( ArtdatenComposite.class, expr, 0, 1000 );
                viewer.setContent( new CompositesFeatureContentProvider( content, type ) );
                viewer.setInput( content );

                long count = content.count();
                resultLabel.setText( "Ergebnisse: " + count + (count >= 1000 ? "  (oder mehr)" : "") );
            }
        }

        
        protected void createButtonsForButtonBar( Composite parent ) {
            super.createButtonsForButtonBar( parent );
            //createButton( parent, RESET_BUTTON, "Zurücksetzen", false );

            getButton( IDialogConstants.OK_ID ).setEnabled( false );
        }

    }

}
