/*
 * polymap.org
 * Copyright 2017, Falko Bräutigam. All rights reserved.
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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.value.ValueBuilder;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.qi4j.QiModule.EntityCreator;
import org.polymap.core.qi4j.event.AbstractModelChangeOperation;
import org.polymap.core.runtime.SubMonitor;

import org.polymap.biotop.model.AktivitaetValue;
import org.polymap.biotop.model.BiotopComposite;
import org.polymap.biotop.model.BiotopRepository;
import org.polymap.biotop.model.BiotoptypArtComposite2;
import org.polymap.biotop.model.WertArtComposite;
import org.polymap.biotop.model.WertComposite;
import org.polymap.biotop.model.WertValue;
import org.polymap.biotop.model.constant.Pflegezustand;
import org.polymap.biotop.model.constant.Schutzstatus;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WaldbiotopeImportOperation
        extends AbstractModelChangeOperation
        implements IUndoableOperation {

    private static final Log log = LogFactory.getLog( WaldbiotopeImportOperation.class );

    /** The temporary dir to {@link #unpackZip(IProgressMonitor)} into. */
    private File                zipDir;
    
    private BiotopRepository    repo;
    
    private Map<String,BiotopComposite>  imported = new HashMap();


    protected WaldbiotopeImportOperation( File zipDir ) {
        super( "Waldbiotop-Daten importieren" );
        this.zipDir = zipDir;
        this.repo = BiotopRepository.instance();
    }


    protected IStatus doExecute( IProgressMonitor monitor, IAdaptable info )
            throws Exception {
        monitor.beginTask( getLabel(), 20 );

        try {
            importCsv( new SubMonitor( monitor, 10 ) );
            importShape( new SubMonitor( monitor, 10 ) );
        }
        catch (Exception e) {
            log.warn( "", e );
            repo.revertChanges();
        }
        return Status.OK_STATUS;
    }

    
    protected void importShape( IProgressMonitor monitor ) throws Exception {
        File shpFile = findFile( "shp" );
        ShapefileDataStore ds = new ShapefileDataStore( shpFile.toURI().toURL() );
        FeatureSource<SimpleFeatureType,SimpleFeature> fs = ds.getFeatureSource();
        FeatureCollection<SimpleFeatureType,SimpleFeature> coll = fs.getFeatures();
        FeatureIterator<SimpleFeature> it = coll.features();
        monitor.beginTask( "Shapefile Daten...", coll.size() );
        try {
            while (it.hasNext()) {
                SimpleFeature feature = it.next();
                //log.info( "Feature: " + feature );
                
                String ID_PS = feature.getAttribute( "ID_PS" ).toString();
                BiotopComposite entity = imported.get( ID_PS );
                Geometry geom = (Geometry)feature.getDefaultGeometry();
                assert entity.geom().get() == null;
                entity.geom().set( (MultiPolygon)geom );
                        
                monitor.worked( 1 );
            }
            monitor.done();
        }
        finally {
            it.close();
        }
    }
    
    
    protected void importCsv( IProgressMonitor monitor ) throws Exception {
        monitor.beginTask( "CSV Daten...", 7350 /*numOfLines*/ );

        File csvFile = findFile( "csv", "txt" );
        
        // process
        CsvPreference csvPrefs = CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE;
        InputStream in = new BufferedInputStream( new FileInputStream( csvFile ) );
        ICsvListReader csv = new CsvListReader( new InputStreamReader( in, "UTF-8" ), csvPrefs );
        try {
            String[] header = csv.getHeader( true );

            final DateFormat df = new SimpleDateFormat( "dd.MM.yyyy" );
            final WertArtComposite wertArtTemplate = QueryExpressions.templateFor( WertArtComposite.class );
            
            List<String> line = null;
            while ((line = csv.read()) != null) {
                int offset = -1;
                //String col = line.get( i + 0 );
                final String BID_SBK = line.get( offset + 1 );
                String Flaeche = line.get( offset + 2 ); 
                String ID_BT = line.get( offset + 3 );
                String NR_BIOTOPT_2012 = line.get( offset + 4 ); 
                final String ID_PS = line.get( offset + 5 );
                final String Biotop_Unternummer = line.get( offset + 6 );
                final String Objektnummer = line.get( offset + 7 );
                final String TK25 = line.get( offset + 8 );
                final String Pflegezustand_ = line.get( offset + 9 );
                final String Biotoptyp_Code = line.get( offset + 10 );
                final String Schutz = line.get( offset + 11 );
                final String Biotopname = line.get( offset + 12 );
                final String Beschreibung = line.get( offset + 13 );
                final Date erfasst_dat = df.parse( line.get( offset + 14 ) ); 
                final String erfasst_name = line.get( offset + 15 );
                final Date bearb_dat = df.parse( line.get( offset + 16 ) );
                final String bearb_name = line.get( offset + 17 );
                final String Pflege = line.get( offset + 18 );
                final String Wertbestimmend = line.get( offset + 19 );
                
                BiotopComposite entity = repo.newBiotop( new EntityCreator<BiotopComposite>() {
                    public void create( BiotopComposite proto ) throws Exception {
                        proto.waldbiotop().set( true );
                        
                        proto.objnr_sbk().set( Objektnummer );
                        proto.unr().set( Biotop_Unternummer );
                        proto.tk25().set( TK25 );

                        proto.name().set( Biotopname );
                        proto.beschreibung().set( Beschreibung );
                        proto.pflegeZustand().set( Pflegezustand_ != null ? Pflegezustand.all.forLabelOrSynonym( Pflegezustand_ ).id : null );
                        proto.pflegeEntwicklung().set( Pflege );
                        proto.schutzstatus().set( Schutzstatus.all.forLabelOrSynonym( Schutz ).id );

                        // erfassung
                        ValueBuilder<AktivitaetValue> builder = repo.newValueBuilder( AktivitaetValue.class );
                        AktivitaetValue erfasst = builder.prototype();
                        erfasst.wann().set( erfasst_dat );
                        erfasst.wer().set( erfasst_name );
                        erfasst.bemerkung().set( "" );
                        proto.erfassung().set( builder.newInstance() );
                        
                        // bearbeitung
                        AktivitaetValue bearbeitet = builder.prototype();
                        bearbeitet.wann().set( bearb_dat );
                        bearbeitet.wer().set( bearb_name );
                        bearbeitet.bemerkung().set( "" );
                        proto.bearbeitung().set( bearbeitet );
                        
                        // wertbestimmend
                        if (Wertbestimmend != null) {
                            String[] wba = Wertbestimmend.split( "[.]+[ ]*" );
                            for (String wertbestimmend : wba) {
                                WertArtComposite wertArt = repo.findEntities( WertArtComposite.class, QueryExpressions.eq( wertArtTemplate.name(), wertbestimmend ), 0, 1 ).find();
                                if (wertArt != null) {
                                    WertValue wertValue = WertComposite.newInstance( wertArt ).value();
                                    proto.werterhaltend().get().add( wertValue );
                                    //log.warn( "OK: " + wertbestimmend );
                                }
                                else {
                                    log.warn( "No WertArtComposite for: " + wertbestimmend );
                                }
                            }
                        }
                        
                        // biotoptyp
                        for (BiotoptypArtComposite2 input : repo.btNummern().values()) {
                            if (input.code().get().equals( Biotoptyp_Code )) {
                                proto.biotoptyp2ArtNr().set( input.nummer().get() );
                            }
                        }
                        if (proto.biotoptyp2ArtNr().get() == null) {
                            log.info( "Biotoptyp Code: " + Biotoptyp_Code);
                        }
                    }
                });
                if (imported.put( ID_PS, entity ) != null) {
                    throw new IllegalStateException( "ID_PS already exists: " + ID_PS );
                }
                monitor.worked( 1 );
            }
            monitor.done();
        }
        finally {
            csv.close();
        }
    }


    /**
     *
     * @return
     */
    protected File findFile( String... exts ) {
        File result = null;
        for (File f : zipDir.listFiles()) {
            String ext = FilenameUtils.getExtension( f.getName() );
            if (ArrayUtils.contains( exts, ext )) {
                if (result != null) {
                    throw new IllegalStateException( "ZIP enthält mehrere Files für die Endung: " + ArrayUtils.toString( exts ) );
                }
                result = f;
            }
        }
        if (result == null) {
            throw new IllegalStateException( "ZIP enthält kein File mit der Endung: " + ArrayUtils.toString( exts ) );
        }
        return result;
    }
    
}
