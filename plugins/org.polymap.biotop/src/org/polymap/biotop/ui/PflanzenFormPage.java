/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.biotop.ui;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;
import org.opengis.feature.type.PropertyDescriptor;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.Action;

import org.eclipse.ui.forms.widgets.Section;

import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.model.EntityType;
import org.polymap.core.project.ui.util.SimpleFormData;

import org.polymap.rhei.data.entityfeature.PropertyDescriptorAdapter;
import org.polymap.rhei.form.DefaultFormPageLayouter;
import org.polymap.rhei.form.IFormEditorPage;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.form.IFormEditorToolkit;

import org.polymap.biotop.model.BiotopComposite;
import org.polymap.biotop.model.BiotopRepository;
import org.polymap.biotop.model.PflanzeValue;
import org.polymap.biotop.model.PflanzenArtComposite;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PflanzenFormPage
        implements IFormEditorPage {

    static final int                SECTION_SPACING = BiotopFormPageProvider.SECTION_SPACING;

    private Feature                 feature;

    private FeatureStore            fs;

    private BiotopComposite         biotop;

    IFormEditorPageSite             site;

    private IFormEditorToolkit      tk;

    private DefaultFormPageLayouter layouter;


    protected PflanzenFormPage( Feature feature, FeatureStore featureStore ) {
        this.feature = feature;
        this.fs = featureStore;
        this.biotop = BiotopRepository.instance().findEntity(
                BiotopComposite.class, feature.getIdentifier().getID() );
    }

    public void dispose() {
    }

    public String getId() {
        return getClass().getName();
    }

    public String getTitle() {
        return "Pflanzen";
    }

    public void createFormContent( IFormEditorPageSite _site ) {
        site = _site;
        tk = site.getToolkit();
        layouter = new DefaultFormPageLayouter();

        //site.setFormTitle( "Biotop: " + biotop.objnr().get() );
        FormLayout layout = new FormLayout();
        site.getPageBody().setLayout( layout );

        Section pflanzenSection = createPflanzenSection( site.getPageBody() );
        pflanzenSection.setLayoutData( new SimpleFormData( SECTION_SPACING )
                .left( 0 ).right( 100 ).top( 0, 0 ).bottom( 100 ).create() );
    }


    protected Section createPflanzenSection( Composite parent ) {
        Section section = tk.createSection( parent, Section.TITLE_BAR | Section.TREE_NODE );
        section.setText( "Pflanzen" );
        section.setExpanded( true );

        Composite client = tk.createComposite( section );
        client.setLayout( new FormLayout() );
        section.setClient( client );

        FeatureTableViewer viewer = new FeatureTableViewer( client, SWT.NONE );
        viewer.getTable().setLayoutData( new SimpleFormData().fill().create() );

        // entity types
        final BiotopRepository repo = BiotopRepository.instance();
        final EntityType<PflanzeValue> valueType = repo.entityType( PflanzeValue.class );
        final EntityType<PflanzenArtComposite> compType = repo.entityType( PflanzenArtComposite.class );

        // columns
        PropertyDescriptor prop = new PropertyDescriptorAdapter( valueType.getProperty( "pflanzenArtNr" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop )
                 .setHeader( "Nummer" ));
        prop = new PropertyDescriptorAdapter( compType.getProperty( "name" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop )
                 .setHeader( "Name" ));
        prop = new PropertyDescriptorAdapter( valueType.getProperty( "menge" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop )
                 .setHeader( "Menge" ));
        prop = new PropertyDescriptorAdapter( valueType.getProperty( "mengenstatusNr" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop )
                 .setHeader( "MengenstatusNr" ));

        // content
        viewer.setContent( new LinkedCompositesContentProvider<PflanzeValue,PflanzenArtComposite>(
                biotop.pflanzen().get(), valueType, compType ) {
                    protected PflanzenArtComposite linkedElement( PflanzeValue elm ) {
                        PflanzenArtComposite template = QueryExpressions.templateFor( PflanzenArtComposite.class );
                        BooleanExpression expr = QueryExpressions.eq( template.nummer(), elm.pflanzenArtNr().get() );
                        Query<PflanzenArtComposite> matches = repo.findEntities( PflanzenArtComposite.class, expr, 0 , 1 );
                        return matches.find();
                    }
        });
        viewer.setInput( biotop.pflanzen().get() );

        return section;
    }

    public Action[] getEditorActions() {
        return null;
    }

}
