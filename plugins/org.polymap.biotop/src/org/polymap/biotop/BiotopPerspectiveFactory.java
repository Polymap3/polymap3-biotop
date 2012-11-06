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
package org.polymap.biotop;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;

import org.polymap.core.project.ui.layer.LayerNavigator;
import org.polymap.core.project.ui.project.ProjectView;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BiotopPerspectiveFactory
        implements IPerspectiveFactory {

    public void createInitialLayout( IPageLayout layout ) {
        String editorArea = layout.getEditorArea();
        layout.setEditorAreaVisible( true );

        IFolderLayout topLeft = layout.createFolder( "topLeft", IPageLayout.LEFT, 0.23f, editorArea );
        IFolderLayout bottomLeft = layout.createFolder( "bottomLeft", IPageLayout.BOTTOM, 0.25f, "topLeft" );
//        IFolderLayout topRight = layout.createFolder(
//                "topRight", IPageLayout.RIGHT, 0.72f, editorArea );
        IPlaceholderFolderLayout bottom = layout.createPlaceholderFolder( "bottom", IPageLayout.BOTTOM, 0.70f, editorArea );
        IFolderLayout topRight = layout.createFolder( "topRight", IPageLayout.RIGHT, 0.70f, editorArea );

        topLeft.addView( ProjectView.ID );
  
        topRight.addView( "org.polymap.core.mapeditor.ToolingView" );

        bottomLeft.addView( LayerNavigator.ID );
        bottomLeft.addPlaceholder( "org.polymap.rhei.FilterView:*" );

        bottom.addPlaceholder( "org.polymap.*:*" );
        bottom.addPlaceholder( "org.polymap.*" );
        bottom.addPlaceholder( "org.eclipse.*" );

        bottom.addPlaceholder( "net.refractions.udig.catalog.ui.CatalogView" );
        bottom.addPlaceholder( "org.polymap.geocoder.*" );

        bottom.addPlaceholder( "org.polymap.core.data.ui.featureTable.view:*" );
        bottom.addPlaceholder( "org.polymap.*" );
        bottom.addPlaceholder( "org.eclipse.*" );

        // add shortcuts to show view menu
        layout.addShowViewShortcut( "net.refractions.udig.catalog.ui.CatalogView" );
        layout.addShowViewShortcut( "org.polymap.core.mapeditor.ToolingView" );
    }
    
}
