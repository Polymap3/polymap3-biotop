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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

/**
 * Load the {@link BiotopPerspectiveFactory} by default.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public class BiotopWorkbenchAdvisor
        extends WorkbenchAdvisor {

    private static final Log log = LogFactory.getLog( BiotopWorkbenchAdvisor.class );

    public void initialize( IWorkbenchConfigurer configurer ) {
        getWorkbenchConfigurer().setSaveAndRestore( false );
        super.initialize( configurer );
    }


    public String getInitialWindowPerspectiveId() {
        return "org.polymap.biotop.BiotopPerspective";
    }


    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
            final IWorkbenchWindowConfigurer windowConfigurer ) {
        return new BiotopWorkbenchWindowAdvisor( windowConfigurer );
    }

}
