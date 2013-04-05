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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.biotop.BiotopPlugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class RemoveValueArtAction
        extends Action
        implements ISelectionChangedListener {

    private static Log log = LogFactory.getLog( RemoveValueArtAction.class );

    
    public RemoveValueArtAction() {
        super( "Löschen" );
        setToolTipText( "Eintrag löschen" );
        setImageDescriptor( BiotopPlugin.imageDescriptorFromPlugin(
                BiotopPlugin.PLUGIN_ID, "icons/delete_edit.gif" ) );
        setEnabled( false );
    }


    public void selectionChanged( SelectionChangedEvent ev ) {
        IStructuredSelection sel = (IStructuredSelection)ev.getSelection();
        log.debug( "selection: " + sel.size() );
        setEnabled( !sel.isEmpty() );
    }


    protected abstract void execute() throws Exception;
    
    public void run() {
        try {
            execute();
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( BiotopPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
            //throw new RuntimeException( e );
        }
    }

}
