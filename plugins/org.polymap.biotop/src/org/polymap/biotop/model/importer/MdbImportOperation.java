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
package org.polymap.biotop.model.importer;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.biotop.model.AktivitaetValue;
import org.polymap.biotop.model.BiotopComposite;
import org.polymap.biotop.model.BiotopRepository;
import org.polymap.biotop.model.BiotoptypArtComposite;
import org.polymap.biotop.model.BiotoptypValue;
import org.polymap.biotop.model.PflanzeValue;
import org.polymap.biotop.model.PflanzenArtComposite;
import org.polymap.biotop.model.PilzeArtComposite;
import org.polymap.biotop.model.PilzeValue;
import org.polymap.biotop.model.TierArtComposite;
import org.polymap.biotop.model.TierValue;

import org.polymap.core.qi4j.QiModule.EntityCreator;
import org.polymap.core.qi4j.event.AbstractModelChangeOperation;
import org.polymap.core.runtime.SubMonitor;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MdbImportOperation
        extends AbstractModelChangeOperation
        implements IUndoableOperation {

    private static Log log = LogFactory.getLog( MdbImportOperation.class );

    private File                dbFile;

    private String[]            tableNames;


    protected MdbImportOperation( File dbFile, String[] tableNames ) {
        super( "MS-Access-Daten importieren" );
        this.dbFile = dbFile;
        this.tableNames = tableNames;
    }


    protected IStatus doExecute( IProgressMonitor monitor, IAdaptable info )
    throws Exception {
        monitor.beginTask( getLabel(), 100 );
        Database db = Database.open( dbFile );
        try {
            SubMonitor sub = null;
            
            // BiotopComposite
            sub = new SubMonitor( monitor, 10 );
            importBiotopdaten( db.getTable( "Biotopdaten" ), sub );
            
            // Biotoptyp
            sub = new SubMonitor( monitor, 10 );
            importEntity( db.getTable( "Referenz_Biotoptypen" ), sub, 
                    BiotoptypArtComposite.class, "Nr_Biotoptyp", null );

            sub = new SubMonitor( monitor, 10 );
            importValue( db.getTable( "Biotoptypen" ), sub, BiotoptypValue.class,
                    new ValueCallback<BiotoptypValue>() {
                        public void fillValue( BiotopComposite biotop, BiotoptypValue value ) {
                            Double current = biotop.biotoptypArtProzent().get();
                            Double neu = value.flaechenprozent().get();
                            if (biotop.biotoptypArtNr().get() == null
                                    || current == null || current.compareTo( neu ) < 0) {
                                biotop.biotoptypArtNr().set( value.biotoptypArtNr().get() );
                                biotop.biotoptypArtProzent().set( neu );
                            }
                            
                            Collection<BiotoptypValue> coll = biotop.biotoptypen().get();
                            coll.add( value );
                            biotop.biotoptypen().set( coll );
                        }
            });

            // Pflanzen
            sub = new SubMonitor( monitor, 10 );
            importEntity( db.getTable( "Referenz_Pflanzen" ), sub, 
                    PflanzenArtComposite.class, "Nr_Planze", null );

            sub = new SubMonitor( monitor, 10 );
            importValue( db.getTable( "Pflanzen" ), sub, PflanzeValue.class,
                    new ValueCallback<PflanzeValue>() {
                        public void fillValue( BiotopComposite biotop, PflanzeValue value ) {
                            Collection<PflanzeValue> coll = biotop.pflanzen().get();
                            coll.add( value );
                            biotop.pflanzen().set( coll );
                        }
            });

            // Moose/Flechten/Pilze
            sub = new SubMonitor( monitor, 10 );
            importEntity( db.getTable( "Referenz_Mo_Fle_pil" ), sub, 
                    PilzeArtComposite.class, "Nr_Art", null );

            sub = new SubMonitor( monitor, 10 );
            importValue( db.getTable( "Mo_Fle_Pil" ), sub, PilzeValue.class,
                    new ValueCallback<PilzeValue>() {
                        public void fillValue( BiotopComposite biotop, PilzeValue value ) {
                            Collection<PilzeValue> coll = biotop.pilze().get();
                            coll.add( value );
                            biotop.pilze().set( coll );
                        }
            });

            // Tiere
            sub = new SubMonitor( monitor, 10 );
            importEntity( db.getTable( "Referenz_Tiere" ), sub, 
                    TierArtComposite.class, "Nr_Tier", null );

            sub = new SubMonitor( monitor, 10 );
            importValue( db.getTable( "Tiere" ), sub, TierValue.class,
                    new ValueCallback<TierValue>() {
                        public void fillValue( BiotopComposite biotop, TierValue value ) {
                            Collection<TierValue> coll = biotop.tiere().get();
                            coll.add( value );
                            biotop.tiere().set( coll );
                        }
            });

        }
        finally {
            db.close();
            db = null;
        }

        return Status.OK_STATUS;
    }

    
    protected void importBiotopdaten( 
            Table table, IProgressMonitor monitor )
            throws Exception {
        monitor.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );

        final AnnotatedCompositeImporter importer = new AnnotatedCompositeImporter(
                BiotopComposite.class, table );
        
        // data rows
        Map<String,Object> row = null;
        while ((row = table.getNextRow()) != null) {
            for (BiotopComposite biotop : findBiotop( row )) {
                if (biotop != null) {
                    importer.fillEntity( biotop, row );
                    
                    // Erfassung
                    Date erfasst = (Date)row.get( "Erfassung" );
                    if (erfasst != null) {
                        ValueBuilder<AktivitaetValue> builder = BiotopRepository.instance().newValueBuilder( AktivitaetValue.class );
                        AktivitaetValue prototype = builder.prototype();
                        prototype.wann().set( erfasst );
                        prototype.wer().set( "SBK" );
                        prototype.bemerkung().set( "Import aus SBK" );
                        biotop.erfassung().set( builder.newInstance() );
                    }

                    // Eingabe -> Bearbeitung
                    Date eingabe = (Date)row.get( "Eingabe" );
                    if (eingabe != null) {
                        ValueBuilder<AktivitaetValue> builder = BiotopRepository.instance().newValueBuilder( AktivitaetValue.class );
                        AktivitaetValue prototype = builder.prototype();
                        prototype.wann().set( eingabe );
                        prototype.wer().set( "SBK" );
                        prototype.bemerkung().set( "Import aus SBK" );
                        biotop.bearbeitung().set( builder.newInstance() );
                    }
                }
                else {
                    //log.warn( "No Biotop found for: " + row );
                }
            }
            if (monitor.isCanceled()) {
                throw new RuntimeException( "Operation canceled." );
            }
            monitor.worked( 1 );
        }
    }


    /*
     * 
     */
    interface EntityCallback<T extends EntityComposite> {
        void fillEntity( T entity );
    }

    
    protected <T extends EntityComposite> void importEntity( 
            Table table, IProgressMonitor monitor,
            Class<T> type, String idColumn, final EntityCallback<T> callback )
            throws Exception {
        monitor.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );

        final AnnotatedCompositeImporter importer = new AnnotatedCompositeImporter( type, table );
        
        // data rows
        Map<String, Object> row = null;
        int count = 0;
        while ((row = table.getNextRow()) != null) {
            if (idColumn != null) {
                Object id = row.get( idColumn );
            }
            final Map<String, Object> builderRow = row;
            
            EntityCreator<T> creator = new EntityCreator<T>() {
                public void create( T builderInstance ) throws Exception {
                    importer.fillEntity( builderInstance, builderRow );
                    if (callback != null) {
                        callback.fillEntity( builderInstance );
                    }
                }
            };
            BiotopRepository.instance().newEntity( type, null, creator );
            if (monitor.isCanceled()) {
                throw new RuntimeException( "Operation canceled." );
            }
            if ((++count % 100) == 0) {
                monitor.worked( 100 );
                monitor.setTaskName( "Objekte: " + count );
            }
        }
        log.info( "Imported: " + type + " -> " + count );
        monitor.done();
    }


    /*
     * 
     */
    interface ValueCallback<T extends ValueComposite> {
        void fillValue( BiotopComposite entity, T value );
    }

    
    protected <T extends ValueComposite> void importValue( 
            Table table, IProgressMonitor monitor, Class<T> type,
            ValueCallback<T> callback )
            throws Exception {
        monitor.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );

        AnnotatedCompositeImporter importer = new AnnotatedCompositeImporter( type, table );
        
        // data rows
        Map<String, Object> row = null;
        int count = 0;
        while ((row = table.getNextRow()) != null) {
            for (BiotopComposite biotop : findBiotop( row )) {

                if (biotop == null) {
                    //log.warn( "    No Biotop found for: " + row );
                    continue;
                }

                ValueBuilder<T> builder = BiotopRepository.instance().newValueBuilder( type );
                T prototype = builder.prototype();

                importer.fillEntity( prototype, row );

                T instance = builder.newInstance();
                if (callback != null) {
                    callback.fillValue( biotop, instance );
                }
            }

            if (monitor.isCanceled()) {
                throw new RuntimeException( "Operation canceled." );
            }
            if ((++count % 100) == 0) {
                monitor.worked( 100 );
                monitor.setTaskName( "Objekte: " + count );
            }
        }
        log.info( "Imported: " + type + " -> " + count );
        monitor.done();
    }


    public static void printSchema( Table table ) {
        log.info( "Table: " + table.getName() );
        for (Column col : table.getColumns()) {
            log.info( "    column: " + col.getName() + " - " + col.getType() );
        }
    }


    private Iterable<BiotopComposite> findBiotop( Map<String, Object> row ) {
        BiotopRepository repo = BiotopRepository.instance();
        BiotopComposite template = QueryExpressions.templateFor( BiotopComposite.class );

        String objnr_sbk = row.get( "Objektnummer" ).toString();
        String tk25 = row.get( "TK25" ).toString();

        Query<BiotopComposite> matches = repo.findEntities( BiotopComposite.class,
                QueryExpressions.and( 
                        QueryExpressions.eq( template.objnr_sbk(), objnr_sbk ),
                        QueryExpressions.eq( template.tk25(), tk25 ) ),
                        0, 100 );
        return matches;

//        Iterator<BiotopComposite> it = matches.iterator();
//        BiotopComposite result = it.hasNext() ? it.next() : null;
//        return result;
    }


    private Object columnValue( Table table, Map<String,Object> row, String col ) {
        if (table.getColumn( col ) == null) {
            throw new IllegalArgumentException( "No such column: " + col );
        }
        return row.get( col );
    }

}
