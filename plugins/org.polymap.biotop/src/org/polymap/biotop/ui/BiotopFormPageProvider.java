/*
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
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
package org.polymap.biotop.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;
import org.opengis.feature.type.PropertyDescriptor;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.forms.widgets.Section;

import org.polymap.biotop.BiotopPlugin;
import org.polymap.biotop.model.BiotopComposite;
import org.polymap.biotop.model.BiotopRepository;
import org.polymap.biotop.model.BiotoptypValue;
import org.polymap.biotop.model.constant.Erhaltungszustand;
import org.polymap.biotop.model.constant.Status;

import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.model.EntityType;
import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.data.entityfeature.CompositesFeatureContentProvider;
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.data.entityfeature.PropertyDescriptorAdapter;
import org.polymap.rhei.field.IntegerValidator;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.DefaultFormPageLayouter;
import org.polymap.rhei.form.FormEditor;
import org.polymap.rhei.form.IFormEditorPage;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.form.IFormEditorToolkit;
import org.polymap.rhei.form.IFormPageProvider;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BiotopFormPageProvider
        implements IFormPageProvider {

    private static Log log = LogFactory.getLog( BiotopFormPageProvider.class );

    static final int                FIELD_OFFSET_H = 5;
    static final int                FIELD_OFFSET_V = 1;
    static final int                SECTION_SPACING = 8;


    public List<IFormEditorPage> addPages( FormEditor formEditor, Feature feature ) {
        log.debug( "addPages(): feature= " + feature );
        List<IFormEditorPage> result = new ArrayList();
        if (feature.getType().getName().getLocalPart().equalsIgnoreCase( "biotop" )) {
            result.add( new BaseFormEditorPage( feature, formEditor.getFeatureStore() ) );
            result.add( new TypFormEditorPage( feature, formEditor.getFeatureStore() ) );
            result.add( new PflanzenFormPage( feature, formEditor.getFeatureStore() ) );
        }
        return result;
    }


    /**
     * The standard page.
     */
    public static class BaseFormEditorPage
            implements IFormEditorPage {

        private Feature                 feature;

        private FeatureStore            fs;

        private BiotopComposite         biotop;

        IFormEditorPageSite             site;

        private IFormEditorToolkit      tk;

        private DefaultFormPageLayouter layouter;
        
        private Locale                  locale = Locale.GERMAN;


        protected BaseFormEditorPage( Feature feature, FeatureStore featureStore ) {
            this.feature = feature;
            this.fs = featureStore;
            this.biotop = BiotopRepository.instance().findEntity(
                    BiotopComposite.class, feature.getIdentifier().getID() );
        }

        public void dispose() {
            log.debug( "..." );
        }

        public String getId() {
            return getClass().getName();
        }

        public String getTitle() {
            return "Basisdaten";
        }

        public void createFormContent( IFormEditorPageSite _site ) {
            log.debug( "createFormContent(): feature= " + feature );
            site = _site;
            tk = site.getToolkit();
            layouter = new DefaultFormPageLayouter();

            site.setFormTitle( "Biotop: " + StringUtils.abbreviate( biotop.name().get(), 25 ) );
            site.setActivePage( getId() );
            FormLayout layout = new FormLayout();
            site.getPageBody().setLayout( layout );

            // leftSection
            Section leftSection = createLeftSection( site.getPageBody() );
            leftSection.setLayoutData( new SimpleFormData( SECTION_SPACING )
                    .left( 0 ).right( 50 ).top( 0, 0 ).create() );

            // idsSection
            Section idsSection = createIdsSection( site.getPageBody() );
            idsSection.setLayoutData( new SimpleFormData( SECTION_SPACING )
                    .left( 50 ).right( 100 ).top( 0, 0 ).create() );

            // statusSection
            Section statusSection = createStatusSection( site.getPageBody() );
            statusSection.setLayoutData( new SimpleFormData( SECTION_SPACING )
                    .left( 0 ).right( 50 ).top( leftSection ).create() );

            layouter.newLayout();
        }

        protected Section createLeftSection( Composite parent ) {
            Section section = tk.createSection( parent, Section.TITLE_BAR /*| Section.TREE_NODE*/ );
            section.setText( "Basisdaten" );
            section.setExpanded( true );

            Composite client = tk.createComposite( section );
            client.setLayout( layouter.newLayout() );
            section.setClient( client );

            // properties
            layouter.setFieldLayoutData( site.newFormField( client, 
                    new PropertyAdapter( biotop.name() ),
                    new StringFormField(), null, "Name" ) );

            Composite field = layouter.setFieldLayoutData( site.newFormField( client, 
                    new PropertyAdapter( biotop.beschreibung() ),
                    new TextFormField(), null, "Beschreibung" ) );
            ((FormData)field.getLayoutData()).height = 80;

            layouter.setFieldLayoutData( site.newFormField( client, 
                    new PropertyAdapter( biotop.erhaltungszustand() ),
                    new PicklistFormField( Erhaltungszustand.all ), null, "Erhaltungszustand" ) );

            layouter.setFieldLayoutData( site.newFormField( client, 
                    new PropertyAdapter( biotop.numGeom() ),
                    new StringFormField(), new IntegerValidator(),
                    "Teilflächen" ) ).setEnabled( false );

            layouter.setFieldLayoutData( site.newFormField( client, 
                    new PropertyAdapter( biotop.flaeche() ),
                    new StringFormField(), new NumberValidator( Double.class, locale, 12, 2 ),
                    "Gesamtfläche (m²)" ) ).setEnabled( false );

            layouter.setFieldLayoutData( site.newFormField( client, 
                    new PropertyAdapter( biotop.umfang() ),
                    new StringFormField(), new NumberValidator( Double.class, locale, 12, 2 ),
                    "Umfang/Länge (m)" ) ).setEnabled( false );

            return section;
        }

        protected Section createIdsSection( Composite parent ) {
            Section section = tk.createSection( parent, Section.TITLE_BAR /*| Section.TREE_NODE*/ );
            section.setText( "IDs" );
            section.setExpanded( true );

            Composite client = tk.createComposite( section );
            client.setLayout( layouter.newLayout() );
            section.setClient( client );

            // properties
            layouter.setFieldLayoutData( site.newFormField( client, 
                    new PropertyAdapter( biotop.objnr() ),
                    new StringFormField(), null, "Biotopnummer" ) ).setEnabled( false );

            layouter.setFieldLayoutData( site.newFormField( client, 
                    new PropertyAdapter( biotop.objnr_landkreise() ),
                    new StringFormField(), null, "Objekt-Nr. (LKs)" ) );

            layouter.setFieldLayoutData( site.newFormField( client, 
                    new PropertyAdapter( biotop.objnr_sbk() ),
                    new StringFormField(), null, "Objekt-Nr. (SBK)" ) );

            layouter.setFieldLayoutData( site.newFormField( client, 
                    new PropertyAdapter( biotop.tk25() ),
                    new StringFormField(), null, "TK25 Nr." ) );
            return section;
        }

        protected Section createStatusSection( Composite parent ) {
            Section section = tk.createSection( parent, Section.TITLE_BAR /*| Section.TREE_NODE*/ );
            section.setText( "Status" );

            Composite client = tk.createComposite( section );
            client.setLayout( layouter.newLayout() );
            section.setClient( client );

            // properties
            layouter.setFieldLayoutData( site.newFormField( client, 
                    new PropertyAdapter( biotop.status() ),
                    new PicklistFormField( Status.all ), null, "Status" ) );

//            AktivitaetValue loeschung = biotop.loeschung().get();
//            field = site.newFormField( client, new PropertyAdapter( loeschung.bemerkung() ),
//                    new TextFormField(), null, "Begründung" );
//            layouter.setFieldLayoutData( field, 80 );
//            field.setEnabled( Status.nicht_aktuell.equals( biotop.status().get() ));

            return section;
        }

        public Action[] getEditorActions() {
            // zoom flurstuecke
            ImageDescriptor icon = BiotopPlugin.imageDescriptorFromPlugin( 
                    BiotopPlugin.PLUGIN_ID, "icons/find_flurstuecke.gif" );
            Action action1 = new Action( "mit Flurstücken/Eigentümern verschneiden", icon ) {

                public String getToolTipText() {
                    return "verschneiden mit Flurstücken/Eigentümern";
                }

                public void runWithEvent( Event ev ) {
                    Shell shell = PolymapWorkbench.getShellToParentOn();
                    MessageDialog.openInformation( shell, "Information", "Keine Flurstücksinformationen verfügbar." );
                }
            };
            return new Action[] { action1 };
        }

    }


    /**
     * The standard page for {@link AntragComposite}.
     */
    public static class TypFormEditorPage
            implements IFormEditorPage {

        private Feature                 feature;

        private FeatureStore            fs;

        private BiotopComposite         biotop;

        IFormEditorPageSite             site;

        private IFormEditorToolkit      tk;

        private DefaultFormPageLayouter layouter;


        protected TypFormEditorPage( Feature feature, FeatureStore featureStore ) {
            this.feature = feature;
            this.fs = featureStore;
            this.biotop = BiotopRepository.instance().findEntity(
                    BiotopComposite.class, feature.getIdentifier().getID() );
        }

        public void dispose() {
            log.debug( "..." );
        }

        public String getId() {
            return getClass().getName();
        }

        public String getTitle() {
            return "Typ/Schutz/Pflege";
        }

        public void createFormContent( IFormEditorPageSite _site ) {
            log.debug( "createFormContent(): feature= " + feature );
            site = _site;
            tk = site.getToolkit();
            layouter = new DefaultFormPageLayouter();

            //site.setFormTitle( "Biotop: " + biotop.objnr().get() );
            FormLayout layout = new FormLayout();
            site.getPageBody().setLayout( layout );

            Section leftSection = createTypenSection( site.getPageBody() );
            leftSection.setLayoutData( new SimpleFormData( SECTION_SPACING )
                    .left( 0 ).right( 100 ).top( 0, 0 ).bottom( 50 ).create() );

            Section statusSection = createStatusSection( site.getPageBody() );
            statusSection.setLayoutData( new SimpleFormData( SECTION_SPACING )
                    .left( 0 ).right( 50 ).top( leftSection ).bottom( 100 ).create() );

        }

        protected Section createStatusSection( Composite parent ) {
            Section section = tk.createSection( parent, Section.TITLE_BAR /*| Section.TREE_NODE*/ );
            section.setText( "Schutzstatus" );

            Composite client = tk.createComposite( section );
            client.setLayout( new FormLayout() );
            section.setClient( client );

            return section;
        }

        protected Section createTypenSection( Composite parent ) {
            Section section = tk.createSection( parent, Section.TITLE_BAR /*| Section.TREE_NODE*/ );
            section.setText( "Biotoptypen" );

            Composite client = tk.createComposite( section );
            client.setLayout( new FormLayout() );
            section.setClient( client );

            FeatureTableViewer viewer = new FeatureTableViewer( client, SWT.NONE );
            viewer.getTable().setLayoutData( new SimpleFormData().fill().create() );

//            EntityType biotopType = BiotopRepository.instance().entityType( BiotopComposite.class );
//            CollectionProperty biotoptypenProp = (CollectionProperty)biotopType.getProperty( "biotoptypen" );
//            EntityType biotoptypType = biotoptypenProp.getComplexType();

            // columns
            final EntityType<BiotoptypValue> type = 
                    BiotopRepository.instance().entityType( BiotoptypValue.class );
//            for (Property prop : biotoptypType.getProperties()) {
//                viewer.addColumn( new PropertyDescriptorAdapter( prop ), true );
//            }

            PropertyDescriptor prop = new PropertyDescriptorAdapter( type.getProperty( "biotoptypArtNr" ) );
            viewer.addColumn( new DefaultFeatureTableColumn( prop )
                     .setHeader( "Nummer" ));
            prop = new PropertyDescriptorAdapter( type.getProperty( "unternummer" ) );
            viewer.addColumn( new DefaultFeatureTableColumn( prop )
                     .setHeader( "Unternummer" ));
            prop = new PropertyDescriptorAdapter( type.getProperty( "flaechenprozent" ) );
            viewer.addColumn( new DefaultFeatureTableColumn( prop )
                     .setHeader( "Prozent" ));
            prop = new PropertyDescriptorAdapter( type.getProperty( "pflegerueckstand" ) );
            viewer.addColumn( new DefaultFeatureTableColumn( prop )
                     .setHeader( "Pflegerückstand" ));

            // content
            viewer.setContent( new CompositesFeatureContentProvider(
                    biotop.biotoptypen().get(), type ) );

//            viewer.setContent( new IFeatureContentProvider() {
//                public Object[] getElements( Object input ) {
//                    log.info( "getElements(): input=" + input );
//                    List<IFeatureTableElement> result = new ArrayList();
//
//                    for (final BiotoptypValue biotoptyp : biotop.biotoptypen().get()) {
//                        result.add( new IFeatureTableElement() {
//                            public Object getValue( String name )
//                            throws Exception {
//                                return biotoptypType.getProperty( name ).getValue( biotoptyp );
//                            }
//                        });
//                    }
//                    return result.toArray();
//                }
//                public void inputChanged( Viewer _viewer, Object oldInput, Object newInput ) {
//                }
//                public void dispose() {
//                }
//            });
            viewer.setInput( biotop.biotoptypen().get() );

            return section;
        }

        public Action[] getEditorActions() {
            return null;
        }

    }

}
