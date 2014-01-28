/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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

import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.operation.DefaultFeatureOperation;
import org.polymap.core.data.operation.FeatureOperationExtension;
import org.polymap.core.data.operation.IFeatureOperation;
import org.polymap.core.data.operation.IFeatureOperationContext;
import org.polymap.core.data.util.ProgressListenerAdaptor;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.security.SecurityUtils;

import org.polymap.biotop.model.BiotopComposite;
import org.polymap.biotop.model.BiotopRepository;
import org.polymap.biotop.model.constant.Schutzstatus;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SchutzstatusOperation
        extends DefaultFeatureOperation
        implements IFeatureOperation {

    private static Log log = LogFactory.getLog( SchutzstatusOperation.class );


    public boolean init( IFeatureOperationContext ctx ) {
        super.init( ctx );
        try {
            return SecurityUtils.isAdmin( Polymap.instance().getUser() )
                    && context.featureSource().getSchema().getName().getLocalPart().equals( "Biotop" );
        }
        catch (Exception e) {
            log.warn( "", e );
            return false;
        }
    }


    public Status execute( IProgressMonitor monitor ) throws Exception {
        monitor.beginTask( context.adapt( FeatureOperationExtension.class ).getLabel(), context.features().size() );
        
        final BiotopRepository repo = BiotopRepository.instance();
        context.features().accepts( new FeatureVisitor() {            
            public void visit( Feature feature ) {
                BiotopComposite biotop = repo.findEntity( BiotopComposite.class, feature.getIdentifier().getID() );

                String wert = biotop.wert().get();
                Schutzstatus schutzstatus = Schutzstatus.all.forLabelOrSynonym( wert );
                if (schutzstatus != null) {
                    log.info( "Schutzstatus: " + wert + " -> " + schutzstatus.label );
                    biotop.schutzstatus().set( schutzstatus.id );
                }
                else {
                    log.warn( "Keine Schutzstatus für: '" + wert + "'" );
                    biotop.schutzstatus().set( Schutzstatus.para_26_30.id );
                }
            }
        }, new ProgressListenerAdaptor( monitor ) );
        
        monitor.done();
        return Status.OK;
    }

}
