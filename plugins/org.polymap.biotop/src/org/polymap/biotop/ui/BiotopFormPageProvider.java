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

import static com.google.common.collect.Iterables.find;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.Principal;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueBuilder;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.forms.widgets.Section;

import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.data.entityfeature.PlainValuePropertyAdapter;
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.data.entityfeature.ValuePropertyAdapter;
import org.polymap.rhei.field.CheckboxFormField;
import org.polymap.rhei.field.DateTimeFormField;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.IFormFieldListener;
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

import org.polymap.biotop.BiotopPlugin;
import org.polymap.biotop.model.AktivitaetValue;
import org.polymap.biotop.model.BiotopComposite;
import org.polymap.biotop.model.BiotopRepository;
import org.polymap.biotop.model.BiotoptypArtComposite2;
import org.polymap.biotop.model.constant.Erhaltungszustand;
import org.polymap.biotop.model.constant.Pflegezustand;
import org.polymap.biotop.model.constant.Schutzstatus;
import org.polymap.biotop.model.constant.Status;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BiotopFormPageProvider
        implements IFormPageProvider {

    static Log log = LogFactory.getLog( BiotopFormPageProvider.class );

//    static final int                FIELD_OFFSET_H = 5;
//    static final int                FIELD_OFFSET_V = 1;
    static final int                SECTION_SPACING = DefaultFormPageLayouter.DEFAULT_SECTION_SPACING;


    public List<IFormEditorPage> addPages( FormEditor formEditor, Feature feature ) {
        log.debug( "addPages(): feature= " + feature );
        List<IFormEditorPage> result = new ArrayList();
        if (feature.getType().getName().getLocalPart().equalsIgnoreCase( "biotop" )) {
            result.add( new BaseFormEditorPage( feature, formEditor.getFeatureStore() ) );
            //result.add( new BiotoptypFormEditorPage( feature, formEditor.getFeatureStore() ) );
            result.add( new PflegeFormPage( feature, formEditor.getFeatureStore() ) );
            result.add( new ArtenFormPage( feature, formEditor.getFeatureStore() ) );
            result.add( new SBKArtenFormPage( feature, formEditor.getFeatureStore() ) );
//            result.add( new PflanzenFormPage( feature, formEditor.getFeatureStore() ) );
//            result.add( new PilzeFormPage( feature, formEditor.getFeatureStore() ) );
//            result.add( new TiereFormPage( feature, formEditor.getFeatureStore() ) );
            result.add( new StoerungFormPage( feature, formEditor.getFeatureStore() ) );
            result.add( new GefahrFormPage( feature, formEditor.getFeatureStore() ) );
            result.add( new WertFormPage( feature, formEditor.getFeatureStore() ) );
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

        private BiotopRepository        repo;
        
        private PropertyChangeListener  bekanntmachungListener;

        private IFormFieldListener      biotoptypListener;

        private IFormFieldListener      statusListener;

        private IFormFieldListener      statusListener2;


        protected BaseFormEditorPage( Feature feature, FeatureStore featureStore ) {
            this.feature = feature;
            this.fs = featureStore;
            this.repo = BiotopRepository.instance();
            this.biotop = repo.findEntity( BiotopComposite.class, feature.getIdentifier().getID() );
        }

        public void dispose() {
            log.debug( "..." );
            if (bekanntmachungListener != null && biotop != null) {
                biotop.removePropertyChangeListener( bekanntmachungListener );
                bekanntmachungListener = null;
            }
        }

        public String getId() {
            return getClass().getName();
        }

        public String getTitle() {
            return "Basisdaten";
        }

        public byte getPriority() {
            return Byte.MAX_VALUE;
        }

        public void createFormContent( IFormEditorPageSite _site ) {
            log.debug( "createFormContent(): feature= " + feature );
            site = _site;
            tk = site.getToolkit();
            layouter = new DefaultFormPageLayouter();

            site.setFormTitle( "Biotop: " + biotop.objnr().get() );
            site.setEditorTitle( "Biotop: " + biotop.objnr().get() );
            site.setActivePage( getId() );
            site.getPageBody().setLayout( new FormLayout() );

            // leftSection
            Section leftSection = createLeftSection( site.getPageBody() );
            leftSection.setLayoutData( new SimpleFormData( SECTION_SPACING )
                    .left( 0 ).right( 50 ).top( 0, 0 ).create() );

            // idsSection
            Section idsSection = createIdsSection( site.getPageBody() );
            idsSection.setLayoutData( new SimpleFormData( SECTION_SPACING )
                    .left( 50 ).right( 100 ).top( 0, 0 ).create() );

            // biotoptyp
            Section btSection = createBiotoptypSection( site.getPageBody() );
            btSection.setLayoutData( new SimpleFormData( SECTION_SPACING )
                    .left( 50 ).right( 100 ).top( idsSection ).create() );

            // pflege
            Section pflegeSection = createPflegeSection( site.getPageBody() );
            pflegeSection.setLayoutData( new SimpleFormData( SECTION_SPACING )
                    .left( 50 ).right( 100 ).top( btSection ).bottom( 100 ).create() );

            // status
            // XXX must be last; otherwise "bearbeitet" DateField is triggered by
            // events on save and shows modified state after save otherwise
            Section statusSection = createStatusSection( site.getPageBody() );
            statusSection.setLayoutData( new SimpleFormData( SECTION_SPACING )
                    .left( 0 ).right( 50 ).top( leftSection ).create() );

            // geometrySection
            Section geomSection = createGeometrySection( site.getPageBody() );
            geomSection.setLayoutData( new SimpleFormData( SECTION_SPACING )
                    .left( 0 ).right( 50 ).top( statusSection ).bottom( 100 ).create() );

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
            ((FormData)field.getLayoutData()).height = 100;

            layouter.setFieldLayoutData( site.newFormField( client, 
                    new PropertyAdapter( biotop.erhaltungszustand() ),
                    new PicklistFormField( Erhaltungszustand.all ), null, "Erhaltungszustand" ) );

            layouter.setFieldLayoutData( site.newFormField( client, 
                    new PropertyAdapter( biotop.schutzstatus() ),
                    new PicklistFormField( Schutzstatus.all ), null, "Schutzstatus" ) );

            layouter.setFieldLayoutData( site.newFormField( client, 
                    new PropertyAdapter( biotop.status() ),
                    new PicklistFormField( Status.all ), null, "Status" ) );

            layouter.setFieldLayoutData( site.newFormField( client, 
                    new PropertyAdapter( biotop.geprueft() ),
                    new CheckboxFormField(), null, "Geprüft" ) );

            AktivitaetValue bekanntmachung = biotop.bekanntmachung().get();
            final IFormField bField = new StringFormField();
            if (bekanntmachung != null && bekanntmachung.wann().get() != null) {
                createAktivitaet( client, biotop.bekanntmachung(), "published_", "Bekanntmachung", true );
            }
            else {
                layouter.setFieldLayoutData( site.newFormField( client, 
                        new PlainValuePropertyAdapter( "bekanntmachung", "-" ),
                        bField.setEnabled( false ), null, "Bekanntmachung" ) );                
            }
            
            // listen to Property changes
            biotop.addPropertyChangeListener( bekanntmachungListener =
                new PropertyChangeListener() {
                    @EventHandler(display=true)
                    public void propertyChange( PropertyChangeEvent ev ) {
                        AktivitaetValue bkm = biotop.bekanntmachung().get();
                        if (bkm != null && bkm.wann().get() != null) {
                            bField.setValue( bkm.wann().get().toString() );
                        }
                    }
                },
                new EventFilter<PropertyChangeEvent>() {
                    public boolean apply( PropertyChangeEvent ev ) {
                        return ev.getPropertyName().equals( biotop.bekanntmachung().qualifiedName().name() );
                    }
                });

            // listen to field changes
            site.addFieldListener( statusListener = new IFormFieldListener() {
                public void fieldChange( FormFieldEvent ev ) {
                    if (ev.getFieldName().equals( biotop.status().qualifiedName().name() )) { 
                        if (ev.getNewValue() != null 
                                && ev.getNewValue().equals( Status.archiviert.id )) {
                            Principal user = Polymap.instance().getUser();
                            Calendar now = Calendar.getInstance( Locale.GERMANY );
                            now.set( Calendar.MILLISECOND, 0 );

                            ValueBuilder<AktivitaetValue> builder = BiotopRepository.instance().newValueBuilder( AktivitaetValue.class );
                            AktivitaetValue prototype = builder.prototype();

                            prototype.wann().set( now.getTime() );
                            prototype.wer().set( user.getName() );
                            prototype.bemerkung().set( "" );
                            biotop.loeschung().set( builder.newInstance() );

//                            site.setFieldValue( "deleted_wann", now.getTime() );
//                            site.setFieldValue( "deleted_wer", user.getName() );
                        }
                    }
                }
            });

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
                    new StringFormField().setEnabled( false ), null, "Biotopnummer" ) );

            layouter.setFieldLayoutData( site.newFormField( client, 
                    new PropertyAdapter( biotop.objnr_sbk() ),
                    new StringFormField().setEnabled( false ), null, "Objekt-Nr. (SBK)" ) );

            layouter.setFieldLayoutData( site.newFormField( client, 
                    new PropertyAdapter( biotop.tk25() ),
                    new StringFormField().setEnabled( false ), null, "TK25-Nr." ) );

            layouter.setFieldLayoutData( site.newFormField( client, 
                    new PropertyAdapter( biotop.unr() ),
                    new StringFormField().setEnabled( false ), null, "Unter-Nr." ) );

            layouter.setFieldLayoutData( site.newFormField( client, 
                    new PropertyAdapter( biotop.objnr_landkreise() ),
                    new StringFormField(), null, "Alte LK-Nr." ) ).setToolTipText( "Alte Landkreisnummer" );

            return section;
        }

        
        protected Section createGeometrySection( Composite parent ) {
            Section section = tk.createSection( parent, Section.TITLE_BAR /*| Section.TREE_NODE*/ );
            section.setText( "GIS" );

            Composite client = tk.createComposite( section );
            client.setLayout( layouter.newLayout() );
            section.setClient( client );

            layouter.setFieldLayoutData( site.newFormField( client, 
                    new PropertyAdapter( biotop.flaeche() ),
                    new StringFormField().setEnabled( false ), new NumberValidator( Double.class, locale, 12, 2 ),
                    "Gesamtfläche (m²)" ) );

            layouter.setFieldLayoutData( site.newFormField( client, 
                    new PropertyAdapter( biotop.umfang() ),
                    new StringFormField().setEnabled( false ), new NumberValidator( Double.class, locale, 12, 2 ),
                    "Umfang/Länge (m)" ) );

            layouter.setFieldLayoutData( site.newFormField( client, 
                    new PropertyAdapter( biotop.numGeom() ),
                    new StringFormField().setEnabled( false ), new IntegerValidator(),
                    "Teilflächen" ) );
            return section;
        }

        
        protected Section createBiotoptypSection( Composite parent ) {
            Section section = tk.createSection( parent, Section.TITLE_BAR /*| Section.TREE_NODE*/ );
            section.setText( "Biotoptyp" );
            section.setExpanded( true );

            Composite client = tk.createComposite( section );
            client.setLayout( layouter.newLayout() );
            section.setClient( client );

            // biotoptyp picklist
            final String nummer = biotop.biotoptyp2ArtNr().get() != null ? biotop.biotoptyp2ArtNr().get() : "1";
                    
            final BiotoptypArtComposite2[] current = new BiotoptypArtComposite2[1];
            Map<String,String> nameNummer = Maps.transformValues( repo.btNamen(), new Function<BiotoptypArtComposite2,String>() {
                public String apply( BiotoptypArtComposite2 input ) {
                    if (nummer != null && input.nummer().get().equals( nummer )) {
                        current[0] = input;
                    }
                    return input.nummer().get();
                }
            });

            final PicklistFormField picklist = new PicklistFormField( nameNummer );
            picklist.setTextEditable( false );
//            picklist.addModifyListener( new ModifyListener() {
//                public void modifyText( ModifyEvent ev ) {
//                    try {
//                        site.reloadEditor();
//                    }
//                    catch (Exception e) {
//                        log.warn( "", e );
//                    }
//                }
//            });

            layouter.setFieldLayoutData( site.newFormField( client, 
                    new PropertyAdapter( biotop.biotoptyp2ArtNr() ),
                    picklist, null, "Biotoptyp" ) );

            layouter.setFieldLayoutData( site.newFormField( client, 
                    new PlainValuePropertyAdapter( "bezeichnung_2002", current[0] != null ? current[0].bezeichnung_2002().get() : "" ),
                    new StringFormField().setEnabled( false ), null, "Bezeichnung (2002)" ) );

            layouter.setFieldLayoutData( site.newFormField( client, 
                    new PlainValuePropertyAdapter( "nummer_2012", current[0] != null ? current[0].nummer_2012().get() : "" ),
                    new StringFormField().setEnabled( false ), null, "Nummer" ) );

            layouter.setFieldLayoutData( site.newFormField( client, 
                    new PlainValuePropertyAdapter( "code", current[0] != null ? current[0].code().get() : "" ),
                    new StringFormField().setEnabled( false ), null, "Code" ) );

            layouter.setFieldLayoutData( site.newFormField( client, 
                    new PlainValuePropertyAdapter( "code_2002", current[0] != null ? current[0].code_2002().get() : "" ),
                    new StringFormField().setEnabled( false ), null, "Code (2002)" ) );

            layouter.setFieldLayoutData( site.newFormField( client, 
                    new PlainValuePropertyAdapter( "schutz26", current[0] != null ? current[0].schutz26().get() : "" ),
                    new StringFormField().setEnabled( false ), null, "Schutz §26/§30" ) );

            layouter.setFieldLayoutData( site.newFormField( client, 
                    new PlainValuePropertyAdapter( "schutz26_2002", current[0] != null ? current[0].schutz26_2002().get() : "" ),
                    new StringFormField().setEnabled( false ), null, "Schutz §26 (2002)" ) );

            layouter.setFieldLayoutData( site.newFormField( client,
                    new PlainValuePropertyAdapter( "vwv", current[0] != null ? current[0].vwv().get() : "" ),
                    new StringFormField().setEnabled( false ), null, "VwV" ) );

            layouter.setFieldLayoutData( site.newFormField( client,
                    new PlainValuePropertyAdapter( "vwv_2002", current[0] != null ? current[0].vwv_2002().get() : "" ),
                    new StringFormField().setEnabled( false ), null, "VwV-Nummer (2002)" ) );
            
            // update fields
            biotoptypListener = new IFormFieldListener() {
                public void fieldChange( FormFieldEvent ev ) {
                    if (ev.getFormField() == picklist) {
                        final String nummerNeu = ev.getNewValue();
                        if (nummerNeu != null) {
                            try {
                                BiotoptypArtComposite2 biotoptyp = find( repo.btNamen().values(), new Predicate<BiotoptypArtComposite2>() {
                                    public boolean apply( BiotoptypArtComposite2 input ) {
                                        String inputNummer = input.nummer().get();
                                        return inputNummer.equals( nummerNeu );
                                    }
                                });
                                site.setFieldValue( "bezeichnung_2002", biotoptyp.bezeichnung_2002().get() );
                                site.setFieldValue( "nummer_2012", biotoptyp.nummer_2012().get() );
                                site.setFieldValue( "code", biotoptyp.code().get() );
                                site.setFieldValue( "code_2002", biotoptyp.code_2002().get() );
                                site.setFieldValue( "schutz26", biotoptyp.schutz26().get() );
                                site.setFieldValue( "schutz26_2002", biotoptyp.schutz26_2002().get() );
                                site.setFieldValue( "vwv", biotoptyp.vwv().get() );
                                site.setFieldValue( "vwv_2002", biotoptyp.vwv_2002().get() );
                            }
                            catch (Exception e) {
                                log.warn( "Keine Biotopart mit Nummer: " + nummerNeu + " (" + e + ")" );
                            }
                        }
                    }
                }
            };
            site.addFieldListener( biotoptypListener );
//            Polymap.getSessionDisplay().asyncExec( new Runnable() {
//                public void run() {
//                    biotoptypListener.fieldChange( new FormFieldEvent( null, this, null, picklist, 0, null, biotop.biotoptyp2ArtNr().get() ) );
//                }
//            });
            return section;
        }

        
        protected Section createPflegeSection( Composite parent ) {
            Section section = tk.createSection( parent, Section.TITLE_BAR /*| Section.TREE_NODE*/ );
            section.setText( "Pflege" );
            section.setExpanded( true );

            Composite client = tk.createComposite( section );
            client.setLayout( layouter.newLayout() );
            section.setClient( client );
            
            layouter.setFieldLayoutData( site.newFormField( client, 
                    new PropertyAdapter( biotop.pflegeZustand() ),
                    new PicklistFormField( Pflegezustand.all ), null, "Pflegezustand" ) );

            layouter.setFieldLayoutData( site.newFormField( client, 
                    new PropertyAdapter( biotop.pflegeBedarf() ),
                    new CheckboxFormField(), null, "Pflegebedarf" ) );

//            Composite field = layouter.setFieldLayoutData( site.newFormField( client, 
//                    new PropertyAdapter( biotop.pflegeEntwicklung() ),
//                    new TextFormField(), null, "Pflege/Entwicklung" ) );
//            ((FormData)field.getLayoutData()).height = 100;
            
            layouter.newLayout();
            
            return section;
        }

        
        protected Section createStatusSection( Composite parent ) {
            Section section = tk.createSection( parent, Section.TITLE_BAR /*| Section.TREE_NODE*/ );
            section.setText( "Erfasser/Bearbeiter" );

            Composite client = tk.createComposite( section );
            client.setLayout( layouter.newLayout() );
            section.setClient( client );
            
            createAktivitaet( client, biotop.erfassung(), "created_", "Erfasst (Wann/Wer)", true );
            createAktivitaet( client, biotop.bearbeitung(), "modified_", "Bearbeitet", true );
            if (biotop.status().get() == Status.archiviert.id) {
                createAktivitaet( client, biotop.loeschung(), "deleted_", "Gelöscht", false );
            }
            
            // listen to field changes
            site.addFieldListener( statusListener2 = new IFormFieldListener() {
                public void fieldChange( FormFieldEvent ev ) {
                    if (ev.getFieldName().endsWith( "wann" ) 
                            || ev.getFieldName().endsWith( "wer" )
                            || !site.isDirty()) {
                        return;
                    }
                    
                    Principal user = Polymap.instance().getUser();
                    Calendar now = Calendar.getInstance( Locale.GERMANY );
                    now.set( Calendar.MILLISECOND, 0 );
                    
//                    ValueBuilder<AktivitaetValue> builder = BiotopRepository.instance().newValueBuilder( AktivitaetValue.class );
//                    AktivitaetValue prototype = builder.prototype();
//                    
//                    prototype.wann().set( now.getTime() );
//                    prototype.wer().set( user.getName() );
//                    prototype.bemerkung().set( "" );
//                    biotop.bearbeitung().set( builder.newInstance() );
                    
                    site.setFieldValue( "modified_wann", now.getTime() );
                    site.setFieldValue( "modified_wer", user.getName() );
                }
            });
            layouter.newLayout();
            return section;
        }


        private List<IFormFieldListener> als = new ArrayList();
        
        private void createAktivitaet( Composite client, Property<AktivitaetValue> prop, String prefix, String label,
                boolean updatable ) {
            AktivitaetValue aktivitaet = prop.get();
            
            //assert aktivitaet != null;
            if (aktivitaet == null || aktivitaet.wann().get() == null) {
                log.warn( "Aktivität oder .wann().get() ist null: " + prefix );
                return;
            }
            Calendar wann = Calendar.getInstance( Locale.GERMANY );
            wann.setTime( aktivitaet.wann().get() );
            wann.set( Calendar.MILLISECOND, 0 );
            
            Composite field1 = layouter.setFieldLayoutData( site.newFormField( client, 
                    new ValuePropertyAdapter( prefix, aktivitaet.wann(), prop ),
                    new DateTimeFormField().setEnabled( updatable ), null, label ) );
            ((FormData)field1.getLayoutData()).right = new FormAttachment( 0, 230 );

            SimpleFormData formData = new SimpleFormData( (FormData)field1.getLayoutData() );
            Composite field2 = site.newFormField( client, 
                    new ValuePropertyAdapter( prefix, aktivitaet.wer(), prop ),
                    new StringFormField().setEnabled( updatable ), null, IFormFieldLabel.NO_LABEL );
            field2.setLayoutData( formData.left( field1 ).right( 100, -5 ).create() );
            field2.setToolTipText( aktivitaet.wer().get() + ": " + aktivitaet.bemerkung().get() );
            
            // nicht mehr nötig, wird in ValuePropertyAdapter behandelt
            // http://polymap.org/biotop/ticket/67
//            if (updatable) {
//                // XXX das ändert die Entity unmittelbar!
//                IFormFieldListener l = new IFormFieldListener() {
//                    public void fieldChange( FormFieldEvent ev ) {
//                        if (!ev.getFieldName().startsWith( prefix ) || ev.getNewValue() == null) {
//                            return;
//                        }
//                        // new value
//                        final ValueBuilder<AktivitaetValue> builder = repo.newValueBuilder( AktivitaetValue.class );
//
//                        // copy current state
//                        final AktivitaetValue prototype = builder.prototype();
//                        prop.get().state().visitProperties( new StateVisitor() {
//                            public void visitProperty( QualifiedName name, Object value ) {
//                                prototype.state().getProperty( name ).set( value );
//                            }
//                        });
//                        // "wann"
//                        if (ev.getFieldName().endsWith( "wann" )) {
//                            prototype.wann().set( (Date)ev.getNewValue() );
//                        }
//                        // "wer"
//                        else if (ev.getFieldName().endsWith( "wer" )) {
//                            prototype.wer().set( (String)ev.getNewValue() );
//                        }
//                        prop.set( builder.newInstance() );
//                    }
//                };
//                site.addFieldListener( l );
//                als.add( l );
//            }
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

}
