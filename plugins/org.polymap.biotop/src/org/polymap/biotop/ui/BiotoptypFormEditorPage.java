package org.polymap.biotop.ui;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;
import org.opengis.feature.type.PropertyDescriptor;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.Action;

import org.eclipse.ui.forms.widgets.Section;

import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.model.EntityType;
import org.polymap.core.project.ui.util.SimpleFormData;

import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.data.entityfeature.PropertyDescriptorAdapter;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.DefaultFormPageLayouter;
import org.polymap.rhei.form.IFormEditorPage;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.form.IFormEditorToolkit;

import org.polymap.biotop.model.BiotopComposite;
import org.polymap.biotop.model.BiotopRepository;
import org.polymap.biotop.model.BiotoptypArtComposite;
import org.polymap.biotop.model.BiotoptypValue;
import org.polymap.biotop.model.constant.Pflegezustand;

/**
 * @see BiotopFormPageProvider
 */
public class BiotoptypFormEditorPage
        implements IFormEditorPage {

    private Feature                 feature;

    private FeatureStore            fs;

    private BiotopComposite         biotop;

    IFormEditorPageSite             site;

    private IFormEditorToolkit      tk;

    private DefaultFormPageLayouter layouter;


    protected BiotoptypFormEditorPage( Feature feature, FeatureStore featureStore ) {
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
        return "Biotoptypen-Pflege";
    }

    public void createFormContent( IFormEditorPageSite _site ) {
        BiotopFormPageProvider.log.debug( "createFormContent(): feature= " + feature );
        site = _site;
        tk = site.getToolkit();
        layouter = new DefaultFormPageLayouter();

        site.setFormTitle( "Biotop: " + biotop.objnr().get() );
        FormLayout layout = new FormLayout();
        site.getPageBody().setLayout( layout );

        Section leftSection = createTypenSection( site.getPageBody() );
        leftSection.setLayoutData( new SimpleFormData( BiotopFormPageProvider.SECTION_SPACING )
                .left( 0 ).right( 100 ).top( 0, 0 ).bottom( 50 ).create() );

        Section pflegeSection = createPflegeSection( site.getPageBody() );
        pflegeSection.setLayoutData( new SimpleFormData( BiotopFormPageProvider.SECTION_SPACING )
                .left( 0 ).right( 100 ).top( leftSection ).bottom( 100 ).create() );
    }


    protected Section createPflegeSection( Composite parent ) {
        Section section = tk.createSection( parent, Section.TITLE_BAR /*| Section.TREE_NODE*/ );
        section.setText( "Pflege" );

        Composite client = tk.createComposite( section );
        client.setLayout( new FormLayout() );
        section.setClient( client );
        
        layouter.setFieldLayoutData( site.newFormField( client, 
                new PropertyAdapter( biotop.pflegezustand() ),
                new PicklistFormField( Pflegezustand.all ), null, "Pflegezustand" ) );

        Composite field = layouter.setFieldLayoutData( site.newFormField( client, 
                new PropertyAdapter( biotop.pflegeEntwicklung() ),
                new TextFormField(), null, "Pflege/Entwicklung" ) );
        ((FormData)field.getLayoutData()).height = 80;

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

        // entity types
        final BiotopRepository repo = BiotopRepository.instance();
        EntityType<BiotoptypValue> valueType = repo.entityType( BiotoptypValue.class );
        EntityType<BiotoptypArtComposite> compType = repo.entityType( BiotoptypArtComposite.class );

        // columns
        PropertyDescriptor prop = new PropertyDescriptorAdapter( valueType.getProperty( "biotoptypArtNr" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop )
                .setHeader( "Nummer" ));
//        prop = new PropertyDescriptorAdapter( valueType.getProperty( "unternummer" ) );
//        viewer.addColumn( new DefaultFeatureTableColumn( prop )
//                .setHeader( "Unternummer" ));
        prop = new PropertyDescriptorAdapter( compType.getProperty( "name" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop )
                .setHeader( "Name" ));
        prop = new PropertyDescriptorAdapter( valueType.getProperty( "flaechenprozent" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop )
                .setHeader( "Prozent" ));
        prop = new PropertyDescriptorAdapter( valueType.getProperty( "pflegerueckstand" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop )
                .setHeader( "Pflegerückstand" ));

        // content
        viewer.setContent( new LinkedCompositesContentProvider<BiotoptypValue,BiotoptypArtComposite> (
                biotop.biotoptypen().get(), valueType, compType ) {
                    protected BiotoptypArtComposite linkedElement( BiotoptypValue elm ) {
                        BiotoptypArtComposite template = QueryExpressions.templateFor( BiotoptypArtComposite.class );
                        BooleanExpression expr = QueryExpressions.eq( template.nummer(), elm.biotoptypArtNr().get() );
                        Query<BiotoptypArtComposite> matches = repo.findEntities( BiotoptypArtComposite.class, expr, 0 , 1 );
                        return matches.find();
                    }
        });
        viewer.setInput( biotop.biotoptypen().get() );
        return section;
    }

    public Action[] getEditorActions() {
        return null;
    }

}