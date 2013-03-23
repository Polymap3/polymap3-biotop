/* 
 * polymap.org
 * Copyright 2012-2013, Polymap GmbH. All rights reserved.
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
package org.polymap.biotop;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.rwt.lifecycle.IEntryPoint;

import org.eclipse.jface.dialogs.ErrorDialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.CorePlugin;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BiotopWorkbench
        extends PolymapWorkbench
        implements IEntryPoint {

    private static final Log log = LogFactory.getLog( BiotopWorkbench.class );

    
    /**
     * Handle the given error by opening an error dialog and logging the given
     * message to the CorePlugin log.
     * 
     * @param src
     * @param msg The error message. If null, then a standard message is used.
     * @param e The reason of the error, must not be null.
     */
    public static void handleError( String pluginId, Object src, final String msg, Throwable e) {
        log.error( msg, e );

        final Status status = new Status( IStatus.ERROR, pluginId, e.getLocalizedMessage(), e );
        CorePlugin.getDefault().getLog().log( status );

        final Display display = Polymap.getSessionDisplay();
        if (display == null) {
            log.error( "No display -> no error message." );
            return;
        }
        
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    Shell shell = PolymapWorkbench.getShellToParentOn();
                    ErrorDialog dialog = new ErrorDialog(
                            shell,
                            "Achtung",
                            msg != null ? msg : "Fehler beim Ausführen der Operation.",
                            status,
                            IStatus.OK | IStatus.INFO | IStatus.WARNING | IStatus.ERROR );
//                dialog.setBlockOnOpen( true );
                    dialog.open();
                }
                catch (Throwable ie) {
                    log.warn( ie );
                }
            }
        };
        if (Display.getCurrent() != null) {
            runnable.run();
        }
        else {
            display.asyncExec( runnable );
        }
    }

    
    // instance *******************************************
    
    public int createUI() {
        return createUI( new BiotopWorkbenchAdvisor() );
    }
    
    
//    protected void createExceptionUI( Throwable e ){
//        try {
//            Display display = PlatformUI.createDisplay();
//            
//            final Shell mainShell = new Shell( display, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX );
//            mainShell.setLayout( new FillLayout() );
//            mainShell.setText( "Exception." );
//            
//            Label msg = new Label( mainShell, SWT.DEFAULT );
//            msg.setText( "Exception: " + e.toString() );
//    
//            mainShell.addShellListener( new ShellAdapter() {
//                public void shellClosed( ShellEvent ev ){
//                    mainShell.dispose();
//                }
//            });
//            
//            // center
//            Rectangle parentSize = display.getBounds();
//            Rectangle mySize = mainShell.getBounds();
//            mainShell.setLocation( (parentSize.width - mySize.width)/2+parentSize.x,
//                    (parentSize.height - mySize.height)/2+parentSize.y );
//            mainShell.open();
//            while (!mainShell.isDisposed()) {
//                if (!display.readAndDispatch()) {
//                    display.sleep();
//                }
//            }
//            PolymapWorkbench.restart(); 
//        }
//        catch (Exception e1) {
//            e.printStackTrace();
//        }
//    }
    
}
