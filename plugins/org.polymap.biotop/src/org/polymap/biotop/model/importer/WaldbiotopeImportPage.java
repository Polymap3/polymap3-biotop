/*
 * polymap.org
 * Copyright 2017, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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
package org.polymap.biotop.model.importer;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import org.eclipse.rwt.widgets.Upload;
import org.eclipse.rwt.widgets.UploadEvent;
import org.eclipse.rwt.widgets.UploadItem;
import org.eclipse.rwt.widgets.UploadListener;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WaldbiotopeImportPage
        extends WizardPage
        implements IWizardPage, UploadListener {

    private static final Log log = LogFactory.getLog( WaldbiotopeImportPage.class );

    public static final String          ID = "WaldbiotopeImportPage";

    private Upload                      upload;

    private List                        tablesList;


    protected WaldbiotopeImportPage() {
        super( ID );
        setTitle( "Import-Datei auswählen." );
        setDescription( "Wählen Sie eine *.zip Datei für den Import aus. Diese muss Shapefile und Csv enthalten.");
    }


    public void createControl( Composite parent ) {
        Composite fileSelectionArea = new Composite( parent, SWT.NONE );
        FormLayout layout = new FormLayout();
        layout.spacing = 5;
        fileSelectionArea.setLayout( layout );

        upload = new Upload( fileSelectionArea, SWT.BORDER, /*Upload.SHOW_PROGRESS |*/ Upload.SHOW_UPLOAD_BUTTON );
        upload.setBrowseButtonText( "Datei" );
        upload.setUploadButtonText( "Laden" );
        upload.setLastFileUploaded( "/home/falko/Data/biotop/Waldbiotope/WBK_2017_20170816.zip" );
        upload.addUploadListener( this );
        FormData data = new FormData();
        data.left = new FormAttachment( 0 );
        data.right = new FormAttachment( 100 );
        upload.setLayoutData( data );

        tablesList = new List( fileSelectionArea, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL );
        data = new FormData();
        data.top = new FormAttachment( upload );
        data.bottom = new FormAttachment( 100 );
        data.left = new FormAttachment( 0 );
        data.right = new FormAttachment( 100 );
        tablesList.setLayoutData( data );

        setControl( fileSelectionArea );
        setPageComplete( false );
    }


    @Override
    public WaldbiotopeImportWizard getWizard() {
        return (WaldbiotopeImportWizard)super.getWizard();
    }


    // UploadListener *************************************

    public void uploadInProgress( UploadEvent ev ) {
    }

    public void uploadFinished( UploadEvent ev ) {
        UploadItem item = upload.getUploadItem();
        try {
            log.info( "Uploaded: " + item.getFileName() + ", path=" + item.getFilePath() );

            File[] files = getWizard().unpackZip( item.getFileInputStream() );
            String[] names = new String[ files.length ];
            for (int i=0; i<files.length; i++) {
                names[i] = files[i].getName();
            }
            tablesList.setItems( names );
            setPageComplete( true );
        }
        catch (IOException e) {
            PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, WaldbiotopeImportPage.this, "Fehler beim Upload der Daten.", e );
        }
    }

}
