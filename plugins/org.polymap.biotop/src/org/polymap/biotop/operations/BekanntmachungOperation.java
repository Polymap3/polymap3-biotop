/* 
 * polymap.org
 * Copyright (C) 2012-2014, Falko Bräutigam. All rights reserved.
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

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import java.security.Principal;

import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.value.ValueBuilder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;

import org.eclipse.ui.PlatformUI;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.operation.DefaultFeatureOperation;
import org.polymap.core.data.operation.FeatureOperationExtension;
import org.polymap.core.data.operation.IFeatureOperation;
import org.polymap.core.data.operation.IFeatureOperationContext;
import org.polymap.core.data.util.ProgressListenerAdaptor;
import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.ui.FormLayoutFactory;

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

    private String          bemerkung;
    
    private Date            datum;
    

    @Override
    public boolean init( IFeatureOperationContext ctx ) {
        super.init( ctx );
        try {
            return context.featureSource().getSchema().getName().getLocalPart().equals( "Biotop" );
        }
        catch (Exception e) {
            log.warn( "", e );
            return false;
        }
    }


    @Override
    public Status execute( IProgressMonitor monitor ) throws Exception {
        monitor.beginTask( context.adapt( FeatureOperationExtension.class ).getLabel(), context.features().size() );
        
        final Date now = new Date();
        final Principal user = Polymap.instance().getUser();
        assert user != null;
        
        // input: Bemerkung
//        final StringBuilder dialogResult = new StringBuilder();
        context.adapt( Display.class ).syncExec( new Runnable() {
            public void run() {
                final InputDialog dialog = new InputDialog();
                dialog.setBlockOnOpen( true );
                if (dialog.open() == Dialog.OK ) {
//                    dialogResult.append( dialog.getValue() );
                }
            }
            
        });
        
        // process features
        if (datum != null) {
            final BiotopRepository repo = BiotopRepository.instance();
            context.features().accepts( new FeatureVisitor() {            
                public void visit( Feature feature ) {
                    BiotopComposite biotop = repo.findEntity( BiotopComposite.class, feature.getIdentifier().getID() );

                    ValueBuilder<AktivitaetValue> builder = repo.newValueBuilder( AktivitaetValue.class );
                    AktivitaetValue prototype = builder.prototype();
                    prototype.wann().set( datum );
                    prototype.wer().set( user.getName() );
                    prototype.bemerkung().set( bemerkung );
                    biotop.bekanntmachung().set( builder.newInstance() );
                }
            }, new ProgressListenerAdaptor( monitor ) );
        }
        
        monitor.done();
        return Status.OK;
    }

    
    /**
     * 
     */
    class InputDialog
            extends TitleAreaDialog {

        private Text                    bemerkungTxt;
        
        private DateTime                datumDt;
        
        
        public InputDialog() {
            super( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() );
            setShellStyle( getShellStyle() | SWT.RESIZE );
        }

//        protected Image getImage() {
//            return getShell().getDisplay().getSystemImage( SWT.ICON_QUESTION );
//        }

        @Override
        protected Point getInitialSize() {
            return new Point( 400, 200 );
        }

        @Override
        protected void okPressed() {
            Calendar cal = Calendar.getInstance( Locale.GERMANY );
            cal.set( datumDt.getYear(), datumDt.getMonth(), datumDt.getDay(),
                    datumDt.getHours(), datumDt.getMinutes(), datumDt.getSeconds() );
            datum = cal.getTime();
            log.info( "Datum: " + datum );
            
            bemerkung = bemerkungTxt.getText();
            super.okPressed();
        }

        @Override
        protected Control createDialogArea( Composite parent ) {
            Composite result = (Composite)super.createDialogArea( parent );
            
            Composite area = new Composite( result, SWT.NONE );
            area.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
            area.setLayout( FormLayoutFactory.defaults().margins( 10 ).spacing( 10 ).create() );

            setTitle( "Bekanntmachung" );
            setMessage( "Geben Sie Datum und Bemerkung zu dieser Bekanntmachung an" );

            // bemerkung
            Label l = new Label( area, SWT.NONE );
            l.setText( "Datum" );
            l.setLayoutData( SimpleFormData.filled().top( 0, 4 ).bottom( -1 ).right( -1 ).width( 85 ).create() );
            
            datumDt = new DateTime( area, SWT.MEDIUM | SWT.DROP_DOWN | SWT.BORDER );
            datumDt.setFocus();
            datumDt.setLayoutData( SimpleFormData.filled().left( l ).bottom( -1 ).create() );

            // bemerkung
            l = new Label( area, SWT.NONE );
            l.setText( "Bemerkung" );
            l.setLayoutData( SimpleFormData.filled().top( datumDt, 4 ).bottom( -1 ).right( -1 ).width( 85 ).create() );
            
            bemerkungTxt = new Text( area, SWT.BORDER );
            bemerkungTxt.setLayoutData( SimpleFormData.filled().top( datumDt).left( l ).bottom( -1 ).create() );
            return result;
        }
    }
    
}
