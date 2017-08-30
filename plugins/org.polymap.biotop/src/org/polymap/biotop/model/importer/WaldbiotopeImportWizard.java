/* 
 * polymap.org
 * Copyright (C) 2017, Falko Bräutigam. All rights reserved.
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
package org.polymap.biotop.model.importer;

import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;

import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.operation.OperationSupport;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.biotop.BiotopPlugin;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WaldbiotopeImportWizard
        extends Wizard
        implements IImportWizard {

    private static final Log log = LogFactory.getLog( WaldbiotopeImportWizard.class );
    
    private WaldbiotopeImportPage   page;

    /** The temporary dir to {@link #unpackZip(IProgressMonitor)} into. */
    private File                    zipDir;

    public void init( IWorkbench workbench, IStructuredSelection selection ) {
        addPage( page = new WaldbiotopeImportPage() );
    }


    public boolean performFinish() {
        try {
            WaldbiotopeImportOperation op = new WaldbiotopeImportOperation( zipDir );
            OperationSupport.instance().execute( op, true, true );
            return true;
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( BiotopPlugin.PLUGIN_ID, this, "Daten konnten nicht korrekt importiert werden.", e );
            return false;
        }
    }

    
    protected File[] unpackZip( InputStream in ) throws ZipException, IOException {
        ZipInputStream zip = new ZipInputStream( in );
        try {
            zipDir = Files.createTempDirectory( WaldbiotopeImportOperation.class.getSimpleName() ).toFile();
            FileUtils.forceDeleteOnExit( zipDir );
            
            ZipEntry entry = null;
            while ((entry = zip.getNextEntry()) != null) {
                log.info( "ZIP entry: " + entry );
                
                File f = new File( zipDir, entry.getName() );
                Files.copy( zip, f.toPath() );
            }
            return zipDir.listFiles();
        }
        catch (Throwable e) {
            log.warn( "", e );
            throw new RuntimeException( e );
        }
        finally {
            IOUtils.closeQuietly( zip );
        }
    }

}
