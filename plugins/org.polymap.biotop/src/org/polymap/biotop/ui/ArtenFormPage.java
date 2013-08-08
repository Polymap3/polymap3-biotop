/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.biotop.ui;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.forms.widgets.Section;

import org.polymap.core.project.ui.util.SimpleFormData;

import org.polymap.rhei.form.DefaultFormEditorPage;
import org.polymap.rhei.form.IFormEditorPage;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.form.IFormEditorToolkit;

import org.polymap.biotop.model.BiotopComposite;
import org.polymap.biotop.model.BiotopRepository;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ArtenFormPage
        extends DefaultFormEditorPage
        implements IFormEditorPage {

    private static Log log = LogFactory.getLog( ArtenFormPage.class );
    
    private BiotopRepository        repo;
    
    private BiotopComposite         biotop;

    private IFormEditorToolkit tk;


    public ArtenFormPage( Feature feature, FeatureStore fs ) {
        super( "Artdaten", "Artdaten", feature, fs );
        this.repo = BiotopRepository.instance();
        this.biotop = repo.findEntity( BiotopComposite.class, feature.getIdentifier().getID() );
    }


    @Override
    public void createFormContent( IFormEditorPageSite _site ) {
        super.createFormContent( _site );
        tk = pageSite.getToolkit();
        pageSite.setFormTitle( "Biotop: " + biotop.objnr().get() );
        pageSite.getPageBody().setLayout( new FormLayout() );

        Section section = newSection( "Arten", false, null );
        ((Composite)section.getClient()).setLayout( new FormLayout() );

        section.setLayoutData( new SimpleFormData( SECTION_SPACING ).fill().top( 0, 0 ).bottom( 100 ).create() );
        
        pageSite.getPageBody().layout( true );
    }
    
}
