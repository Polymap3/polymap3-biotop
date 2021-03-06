/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id: $
 */
package org.polymap.biotop;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import org.polymap.core.workbench.PolymapActionBarAdvisor;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public class BiotopWorkbenchWindowAdvisor
        extends WorkbenchWindowAdvisor {

    public BiotopWorkbenchWindowAdvisor( final IWorkbenchWindowConfigurer configurer ) {
        super( configurer );
    }


    public ActionBarAdvisor createActionBarAdvisor( final IActionBarConfigurer configurer ) {
        return new PolymapActionBarAdvisor( configurer );
    }


    public void preWindowOpen() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setInitialSize( new Point( 8000, 6000 ) );
        configurer.setShowCoolBar( true );
        configurer.setShowPerspectiveBar( true );
        configurer.setTitle( "POLYMAP3|Biotop" );
        configurer.setShellStyle( SWT.TITLE /*| SWT.MAX | SWT.RESIZE*/ );
        configurer.setShowProgressIndicator( true );
        configurer.setShowMenuBar( false );
    }


    public void postWindowOpen() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        Shell shell = window.getShell();
        shell.setMaximized( true );
    }

}
