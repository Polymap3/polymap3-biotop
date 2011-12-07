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

import org.polymap.biotop.BiotopPlugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class RemoveArtValueAction
        extends Action
        implements ISelectionChangedListener {

    private static Log log = LogFactory.getLog( RemoveArtValueAction.class );

    
    public RemoveArtValueAction() {
        super( "L�schen" );
        setToolTipText( "Eintrag l�schen" );
        setImageDescriptor( BiotopPlugin.imageDescriptorFromPlugin(
                BiotopPlugin.PLUGIN_ID, "icons/delete.gif" ) );
        setEnabled( false );
    }


    public void selectionChanged( SelectionChangedEvent ev ) {
        IStructuredSelection sel = (IStructuredSelection)ev.getSelection();
        log.info( "selection: " + sel.size() );
        setEnabled( !sel.isEmpty() );
    }

}
