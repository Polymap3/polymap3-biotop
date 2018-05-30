/* 
 * polymap.org
 * Copyright (C) 2018, Falko Bräutigam. All rights reserved.
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
package org.polymap.biotop.operations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.quote.AlwaysQuoteMode;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;

import com.google.common.base.Joiner;
import com.vividsolutions.jts.geom.MultiPolygon;

import org.eclipse.rwt.widgets.ExternalBrowser;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import org.polymap.core.data.operation.DefaultFeatureOperation;
import org.polymap.core.data.operation.DownloadServiceHandler;
import org.polymap.core.data.operation.DownloadServiceHandler.ContentProvider;
import org.polymap.core.data.operation.FeatureOperationExtension;
import org.polymap.core.data.operation.IFeatureOperationContext;
import org.polymap.core.data.util.Geometries;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.SubMonitor;
import org.polymap.core.security.SecurityUtils;

import org.polymap.biotop.model.AktivitaetValue;
import org.polymap.biotop.model.ArtdatenComposite;
import org.polymap.biotop.model.BiotopComposite;
import org.polymap.biotop.model.BiotopRepository;
import org.polymap.biotop.model.BiotoptypArtComposite;
import org.polymap.biotop.model.BiotoptypArtComposite2;
import org.polymap.biotop.model.FlurstueckComposite;
import org.polymap.biotop.model.GefahrArtComposite;
import org.polymap.biotop.model.GefahrValue;
import org.polymap.biotop.model.PflanzeValue;
import org.polymap.biotop.model.PflanzenArtComposite;
import org.polymap.biotop.model.PflegeArtComposite;
import org.polymap.biotop.model.PilzArtComposite;
import org.polymap.biotop.model.PilzValue;
import org.polymap.biotop.model.StoerungValue;
import org.polymap.biotop.model.StoerungsArtComposite;
import org.polymap.biotop.model.TierArtComposite;
import org.polymap.biotop.model.TierValue;
import org.polymap.biotop.model.WertArtComposite;
import org.polymap.biotop.model.WertValue;
import org.polymap.biotop.model.constant.Erhaltungszustand;
import org.polymap.biotop.model.constant.GefahrLevel;
import org.polymap.biotop.model.constant.Pflegezustand;
import org.polymap.biotop.model.constant.Schutzstatus;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ExportOperation
        extends DefaultFeatureOperation {

    private static final Log log = LogFactory.getLog( ExportOperation.class );

    public static final String      NULL = "null";
    
    public static final String      DELIM = ":";
    
    private CsvPreference           prefs;
    
    private Locale                  locale = Locale.GERMAN;
    
    private String                  charset = "UTF-8";

    private NumberFormat            nf = NumberFormat.getInstance( locale );

    private DateFormat              df = new SimpleDateFormat( "dd.MM.yyyy",  locale );

    private File                    zipDir;

    private BiotopRepository        repo;
    
    
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


    @Override
    public Status execute( IProgressMonitor monitor ) throws Exception {
        //int count = context.features().size();
        monitor.beginTask( context.adapt( FeatureOperationExtension.class ).getLabel(), 300 );

        prefs = new CsvPreference.Builder('"', ';', "\n")
                .surroundingSpacesNeedQuotes( true )
                .useQuoteMode( new AlwaysQuoteMode() )
                .build();        
        nf.setGroupingUsed( false );
        repo = BiotopRepository.instance();
        zipDir = Files.createTempDirectory( "biotop-export-" ).toFile();

        new CsvWriter().write( PflanzenArtComposite.class, "pflanzen", new SubMonitor( monitor, 10 ) );
        new CsvWriter().write( PilzArtComposite.class, "pilze", new SubMonitor( monitor, 10 ) );
        new CsvWriter().write( TierArtComposite.class, "tiere", new SubMonitor( monitor, 10 ) );
        new CsvWriter().write( StoerungsArtComposite.class, "störung", new SubMonitor( monitor, 10 ) );
        new CsvWriter().write( GefahrArtComposite.class, "gefährdung", new SubMonitor( monitor, 10 ) );
        new CsvWriter().write( PflegeArtComposite.class, "pflege", new SubMonitor( monitor, 10 ) );
        new CsvWriter().write( WertArtComposite.class, "wertbestimmend", new SubMonitor( monitor, 10 ) );
        new CsvWriter().write( BiotoptypArtComposite2.class, "biotoptyp", new SubMonitor( monitor, 10 ) );
        new CsvWriter().write( BiotoptypArtComposite.class, "biotoptyp-sbk", new SubMonitor( monitor, 10 ) );
        new CsvWriter().write( FlurstueckComposite.class, "flurstücke", new SubMonitor( monitor, 10 ) );
        new CsvWriter().write( ArtdatenComposite.class, "arten", new SubMonitor( monitor, 50 ) );
        
        // Shape
        ShapefileDataStore ds = new ShapefileDataStore( new File( zipDir, "biotop.shp" ).toURI().toURL() );
        SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
        ftb.setName( "Biotop" );
        ftb.add( "geom", MultiPolygon.class, Geometries.crs( "EPSG:31468" ) );
        ftb.add( "identity", String.class );
        final SimpleFeatureType shapeSchema = ftb.buildFeatureType();
        ds.createSchema( shapeSchema );
        final FeatureStore fs = (FeatureStore)ds.getFeatureSource();
        final List<SimpleFeature> biotopes = new ArrayList( 40000 );
        final SimpleFeatureBuilder b = new SimpleFeatureBuilder( shapeSchema );

        // BiotopComposite
        new CsvWriter() {

            @Override
            protected void writeHeader( Method prop ) {
                if (prop.getName().equals( "arten" )
                        || prop.getName().equals( "pflanzen" )
                        || prop.getName().equals( "tiere" )
                        || prop.getName().equals( "pilze" )
                        || prop.getName().equals( "stoerungen" )
                        || prop.getName().equals( "werterhaltend" )
                        || prop.getName().equals( "gefahr" )
                        || prop.getName().equals( "pflege" )
                        || prop.getName().equals( "flurstuecke" )) {
                    properties.add( prop );
                    header.add( prop.getName() );
                }
                else if (prop.getName().equals( "erfassung" )
                        || prop.getName().equals( "bearbeitung" )
                        || prop.getName().equals( "loeschung" )
                        || prop.getName().equals( "bekanntmachung" )) {
                    properties.add( prop );
                    header.add( prop.getName() + " (" + Joiner.on( DELIM ).join( "wann", "wer", "bemerkung" ) + ")" );
                }
                else {
                    super.writeHeader( prop );
                }
            }
            
            @Override
            protected void writeComposite( CsvListWriter csv, Composite c ) throws Exception {
                super.writeComposite( csv, c );
                
                b.set( "geom", ((BiotopComposite)c).geom().get() );
                b.set( "identity", ((BiotopComposite)c).id() );
                biotopes.add( b.buildFeature( ((BiotopComposite)c).id() ) );
            }

            @Override
            protected void writeProperty( List row, Method prop, Object value ) {
                // Konstanten
                if (prop.getName().equals( "gefahrLevel" )) {
                    row.add( value != null ? value + DELIM + GefahrLevel.all.forId( (Integer)value ).label : NULL );
                }
                else if (prop.getName().equals( "erhaltungszustand" )) {
                    row.add( value != null ? value + DELIM + Erhaltungszustand.all.forId( (Integer)value ).label : NULL );
                }
                else if (prop.getName().equals( "pflegeZustand" )) {
                    row.add( value != null ? value + DELIM + Pflegezustand.all.forId( (Integer)value ).label : NULL );
                }
                else if (prop.getName().equals( "schutzstatus" )) {
                    row.add( value != null ? value + DELIM + Schutzstatus.all.forId( (Integer)value ).label : NULL );
                }
                else if (prop.getName().equals( "status" )) {
                    row.add( value != null ? value + DELIM + org.polymap.biotop.model.constant.Status.all.forId( (Integer)value ).label : NULL );
                }
                else if (prop.getName().equals( "arten" )) {
                    row.add( Joiner.on( DELIM ).join( (Collection)value ) );
                }
                // identity
                else if (prop.getName().equals( "pflege" )) {
                    StringBuilder ids = new StringBuilder( 256 );
                    for (PflegeArtComposite pflege : (ManyAssociation<PflegeArtComposite>)value ) {
                        ids.append( ids.length() > 0 ? DELIM : "" ).append( pflege.id() );
                    }
                    row.add( ids.toString() );
                }
                else if (prop.getName().equals( "flurstuecke" )) {
                    StringBuilder ids = new StringBuilder( 256 );
                    for (FlurstueckComposite fl : (ManyAssociation<FlurstueckComposite>)value ) {
                        ids.append( ids.length() > 0 ? DELIM : "" ).append( fl.id() );
                    }
                    row.add( ids.toString() );
                }
                // nummer
                else if (prop.getName().equals( "pflanzen" )) {
                    StringBuilder ids = new StringBuilder( 256 );
                    for (PflanzeValue v : (Collection<PflanzeValue>)value ) {
                        ids.append( ids.length() > 0 ? DELIM : "" ).append( v.pflanzenArtNr() );
                    }
                    row.add( ids.toString() );
                }
                else if (prop.getName().equals( "tiere" )) {
                    StringBuilder ids = new StringBuilder( 256 );
                    for (TierValue v : (Collection<TierValue>)value ) {
                        ids.append( ids.length() > 0 ? DELIM : "" ).append( v.tierArtNr() );
                    }
                    row.add( ids.toString() );
                }
                else if (prop.getName().equals( "pilze" )) {
                    StringBuilder ids = new StringBuilder( 256 );
                    for (PilzValue v : (Collection<PilzValue>)value ) {
                        ids.append( ids.length() > 0 ? DELIM : "" ).append( v.artNr() );
                    }
                    row.add( ids.toString() );
                }
                else if (prop.getName().equals( "gefahr" )) {
                    StringBuilder ids = new StringBuilder( 256 );
                    for (GefahrValue v : (Collection<GefahrValue>)value ) {
                        ids.append( ids.length() > 0 ? DELIM : "" ).append( v.artNr() );
                    }
                    row.add( ids.toString() );
                }
                else if (prop.getName().equals( "werterhaltend" )) {
                    StringBuilder ids = new StringBuilder( 256 );
                    for (WertValue v : (Collection<WertValue>)value ) {
                        ids.append( ids.length() > 0 ? DELIM : "" ).append( v.artNr() );
                    }
                    row.add( ids.toString() );
                }
                else if (prop.getName().equals( "stoerungen" )) {
                    StringBuilder ids = new StringBuilder( 256 );
                    for (StoerungValue v : (Collection<StoerungValue>)value ) {
                        ids.append( ids.length() > 0 ? DELIM : "" ).append( v.artNr() );
                    }
                    row.add( ids.toString() );
                }
                // Aktivitaeten
                else if (prop.getName().equals( "erfassung" )) {
                    AktivitaetValue a = (AktivitaetValue)value;
                    row.add( Joiner.on( DELIM ).useForNull( NULL ).join( format( a.wann().get() ), a.wer().get(), a.bemerkung().get() ) );
                }
                else if (prop.getName().equals( "bearbeitung" )) {
                    AktivitaetValue a = (AktivitaetValue)value;
                    row.add( Joiner.on( DELIM ).useForNull( NULL ).join( format( a.wann().get() ), a.wer().get(), a.bemerkung().get() ) );
                }
                else if (prop.getName().equals( "loeschung" )) {
                    AktivitaetValue a = (AktivitaetValue)value;
                    row.add( Joiner.on( DELIM ).useForNull( NULL ).join( format( a.wann().get() ), a.wer().get(), a.bemerkung().get() ) );
                }
                else if (prop.getName().equals( "bekanntmachung" )) {
                    AktivitaetValue a = (AktivitaetValue)value;
                    row.add( Joiner.on( DELIM ).useForNull( NULL ).join( format( a.wann().get() ), a.wer().get(), a.bemerkung().get() ) );
                }
                else {
                    super.writeProperty( row, prop, value );
                }
            }

            private String format( Date date ) {
                return date != null ? df.format( date ) : NULL;
            }
        }.write( BiotopComposite.class, "biotop", new SubMonitor( monitor, 120 ) );
        
        // write shapefile
        monitor.subTask( "Shapefile..." );
        fs.addFeatures( DataUtilities.collection( biotopes ) );
        monitor.worked( 10 );
        
        // write ZIP file
        monitor.subTask( "Zip..." );
        final File zip = File.createTempFile( "biotop-export-", ".zip" );
        try (
            ZipOutputStream out = new ZipOutputStream( new FileOutputStream( zip ) );
        ){
            for (File f : zipDir.listFiles()) {
                ZipEntry entry = new ZipEntry( f.getName() );
                out.putNextEntry( entry );
                try (FileInputStream in = new FileInputStream( f )) {
                    IOUtils.copy( in, out );
                }
                out.closeEntry();
                monitor.worked( 1 );
            }
        }

        openDownload( zip );
        
        monitor.done();
        return Status.OK;
    }


    /**
     * 
     */
    protected class CsvWriter {
        
        protected List<Method>  properties = new ArrayList( 32 );
        
        protected List<String>  header = new ArrayList( properties.size() );
        

        public void write( Class<? extends Composite> type, String name, IProgressMonitor monitor ) throws Exception {
            Query<? extends Composite> query = repo.findEntities( type, null, 0, -1 );
            monitor.beginTask( StringUtils.capitalize( name ), 30000 ); //(int)query.count() );
        
            // CSV
            File f = new File( zipDir, name + ".csv" );
            try ( 
                Writer out = new BufferedWriter( new FileWriterWithEncoding( f, charset ) );
                CsvListWriter csv = new CsvListWriter( out, prefs );
            ){
                // header
                monitor.subTask( "Kopf..." );
                for (Method m : type.getMethods()) {
                    writeHeader( m );
                }
                csv.writeHeader( header.toArray( new String[header.size()] ) );
        
                // entities
                monitor.subTask( "Daten..." );
                int worked = 0;
                for (Composite c : query) {
                    writeComposite( csv, c );
                    if (++worked % 300 == 0) {
                        monitor.subTask( "Daten... (" + worked + ")" );
                        monitor.worked( 300 );
                    }
                    if (monitor.isCanceled()) {
                        throw new OperationCanceledException();
                    }
//                    if (worked >= 500) {
//                        break;
//                    }
                }
            }
            monitor.done();
        }


        protected void writeHeader( Method prop ) {
            if (Property.class.isAssignableFrom( prop.getReturnType() ) && !prop.getName().startsWith( "_" )) {
                ParameterizedType type = (ParameterizedType)prop.getGenericReturnType();
                Type valueType = type.getActualTypeArguments()[0];
                if (valueType instanceof Class) {
                    if (String.class.isAssignableFrom( (Class)valueType )
                            || Number.class.isAssignableFrom( (Class)valueType )
                            || Date.class.isAssignableFrom( (Class)valueType )
                            || Boolean.class.isAssignableFrom( (Class)valueType )) {
                        properties.add( prop );
                        header.add( prop.getName() );
                    }
                }
            }
        }

        protected void writeComposite( CsvListWriter csv, Composite c ) throws Exception {
            List row = new ArrayList( properties.size() );
            for (Method m : properties) {
                Object prop = m.invoke( c, ArrayUtils.EMPTY_OBJECT_ARRAY );
                Object value = null;
                if (prop instanceof Property) {
                    value = ((Property)prop).get();
                }
                else if (prop instanceof ManyAssociation) {
                    value = prop;
                }
                else {
                    throw new RuntimeException( "" + prop );
                }
                writeProperty( row, m, value );
            }
            csv.write( row );
        }
        
        protected void writeProperty( List row, Method prop, Object value ) {
            if (value == null) {
                row.add( NULL );
            }
            else if (value instanceof String) {
                row.add( value );
            }
            else if (value instanceof Number) {
                row.add( nf.format( value ) );
            }
            else if (value instanceof Date) {
                row.add( df.format( value ) );
            }
            else if (value instanceof Boolean) {
                row.add( value.toString() );
            }
            else {
                throw new RuntimeException( "Type: " + value );
                //row.add( value.toString() );
            }
        }
    }


    protected List biotop( BiotopComposite biotop ) {
        List result = new ArrayList( 30 );
        
        return result;
    }
    
    
    protected void openDownload( final File f ) {
        Polymap.getSessionDisplay().asyncExec( new Runnable() {
            public void run() {
                String url = DownloadServiceHandler.registerContent( new ContentProvider() {
                    @Override public String getContentType() {
                        return "application/zip";
                    }
                    @Override public String getFilename() {
                        return f.getName();
                    }
                    @Override public InputStream getInputStream() throws Exception {
                        return new BufferedInputStream( new FileInputStream( f ) );
                    }
                    @Override public boolean done( boolean success ) {
                        f.delete();
                        return true;
                    }
                });
                
                log.info( "CSV: download URL: " + url );

                ExternalBrowser.open( "download_window", url,
                        ExternalBrowser.NAVIGATION_BAR | ExternalBrowser.STATUS );
            }
        });
    }
    
}
