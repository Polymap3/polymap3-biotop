/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.biotop.operations;

import java.util.Date;
import java.security.Principal;

import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.value.ValueBuilder;

import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.operation.DefaultFeatureOperation;
import org.polymap.core.data.operation.FeatureOperationExtension;
import org.polymap.core.data.operation.IFeatureOperation;
import org.polymap.core.data.operation.IFeatureOperationContext;
import org.polymap.core.data.util.ProgressListenerAdaptor;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.biotop.model.AktivitaetValue;
import org.polymap.biotop.model.BiotopComposite;
import org.polymap.biotop.model.BiotopRepository;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BekanntmachungOperation
        extends DefaultFeatureOperation
        implements IFeatureOperation {

    private static Log log = LogFactory.getLog( BekanntmachungOperation.class );


    public boolean init( IFeatureOperationContext ctx ) {
        super.init( ctx );
        try {
            return context.featureSource().getSchema().getName().getLocalPart().toLowerCase().contains( "biotop" );
        }
        catch (Exception e) {
            log.warn( "", e );
            return false;
        }
    }


    public Status execute( IProgressMonitor monitor )
    throws Exception {
        monitor.beginTask( context.adapt( FeatureOperationExtension.class ).getLabel(), context.features().size() );
        
        final Date now = new Date();
        final Principal user = Polymap.instance().getUser();
        assert user != null;
        
        // input: Bemerkung
        final StringBuilder dialogResult = new StringBuilder();
        context.adapt( Display.class ).syncExec( new Runnable() {
            public void run() {
                final InputDialog dialog = new InputDialog( PolymapWorkbench.getShellToParentOn(), "Bemerkung", "Geben Sie einen Bemerkung zu dieser Bekanntmachung an", "", null );
                dialog.setBlockOnOpen( true );
                if (dialog.open() == Dialog.OK ) {
                    dialogResult.append( dialog.getValue() );
                }
            }
            
        });
        
        // process features
        if (dialogResult.length() > 0) {
            final BiotopRepository repo = BiotopRepository.instance();
            context.features().accepts( new FeatureVisitor() {            
                public void visit( Feature feature ) {
                    BiotopComposite biotop = repo.findEntity( BiotopComposite.class, feature.getIdentifier().getID() );

                    ValueBuilder<AktivitaetValue> builder = repo.newValueBuilder( AktivitaetValue.class );
                    AktivitaetValue prototype = builder.prototype();
                    prototype.wann().set( now );
                    prototype.wer().set( user.getName() );
                    prototype.bemerkung().set( dialogResult.toString() );
                    biotop.bekanntmachung().set( builder.newInstance() );

                }
            }, new ProgressListenerAdaptor( monitor ) );
        }
        
        monitor.done();
        return Status.OK;
    }

}
