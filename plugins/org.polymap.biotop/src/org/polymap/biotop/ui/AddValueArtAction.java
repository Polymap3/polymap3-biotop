/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.PlatformUI;

import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.data.ui.featuretable.IFeatureTableElement;
import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;
import org.polymap.core.model.EntityType.Property;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.data.entityfeature.CompositesFeatureContentProvider;
import org.polymap.rhei.data.entityfeature.PropertyDescriptorAdapter;

import org.polymap.biotop.BiotopPlugin;
import org.polymap.biotop.model.BiotopRepository;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class AddValueArtAction<A extends Entity>
        extends Action
        implements ISelectionChangedListener {

    private static Log log = LogFactory.getLog( AddValueArtAction.class );

    private Class<A>            artType;
    
    private Iterable<A>         content;
    
    
    public AddValueArtAction( Class<A> artType, Iterable<A> content ) {
        super( "Hinzufügen" );
        this.artType = artType;
        this.content = content;
        
        setToolTipText( "Eintrag hinzufügen" );
        setImageDescriptor( BiotopPlugin.imageDescriptorFromPlugin(
                BiotopPlugin.PLUGIN_ID, "icons/add.gif" ) );
        setEnabled( true );
    }


    public void selectionChanged( SelectionChangedEvent ev ) {
    }

    
    protected abstract void execute( A element ) throws Exception;
    
    
    public void run() {
        try {
            ArtTableDialog dialog = new ArtTableDialog();
            dialog.setBlockOnOpen( true );

            if (dialog.open() == Window.OK) {
                assert dialog.sel.length == 1 : "Selected: " + dialog.sel.length;
                final IFeatureTableElement sel = dialog.sel[0];
                execute( Iterables.find( content, new Predicate<A>() {
                    public boolean apply( A input ) {
                        return input.id().equals( sel.fid() );
                    }}));
            }
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( BiotopPlugin.PLUGIN_ID, this, "Fehler beim Öffnen Suchtabelle.", e );
        }
    }

    
    /**
     * 
     */
    class ArtTableDialog
            extends TitleAreaDialog {

        private FeatureTableViewer      viewer;

        private IFeatureTableElement[]  sel;

        
        public ArtTableDialog() {
            super( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() );
            setShellStyle( getShellStyle() | SWT.RESIZE );
        }

        protected Image getImage() {
            return getShell().getDisplay().getSystemImage( SWT.ICON_QUESTION );
        }

        protected Point getInitialSize() {
            return new Point( 800, 600 );
            //return super.getInitialSize();
        }

        protected Control createDialogArea( Composite parent ) {
            Composite area = (Composite)super.createDialogArea( parent );

            setTitle( "Auswählen" );
            setMessage( "Wählen Sie die \"Art\" des neuen Eintrages." );

            viewer = new FeatureTableViewer( area, SWT.V_SCROLL | SWT.H_SCROLL );
            viewer.getTable().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

            // entity types
            final BiotopRepository repo = BiotopRepository.instance();
            final EntityType<A> type = repo.entityType( artType );

            // columns
            for (Property prop : type.getProperties()) {
                PropertyDescriptorAdapter adapter = new PropertyDescriptorAdapter( prop );
                viewer.addColumn( new DefaultFeatureTableColumn( adapter )
                         .setHeader( StringUtils.capitalize( prop.getName() ) ));
                
            }

            // model/content
            viewer.setContent( new CompositesFeatureContentProvider( content, type ) );
            viewer.setInput( content );

            // selection
            viewer.addSelectionChangedListener( new ISelectionChangedListener() {
                public void selectionChanged( SelectionChangedEvent ev ) {
                    sel = viewer.getSelectedElements();
                    getButton( IDialogConstants.OK_ID ).setEnabled( sel.length > 0 );
                }
            });
            
            area.pack();
            return area;
        }

        
        protected void createButtonsForButtonBar( Composite parent ) {
            super.createButtonsForButtonBar( parent );
            //createButton( parent, RESET_BUTTON, "Zurücksetzen", false );

            getButton( IDialogConstants.OK_ID ).setEnabled( false );
        }

    }
    
}
