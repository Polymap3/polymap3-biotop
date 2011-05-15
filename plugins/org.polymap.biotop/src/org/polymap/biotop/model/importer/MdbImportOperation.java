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

import java.util.Iterator;
import java.util.Map;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.value.ValueBuilder;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.polymap.biotop.model.BiotopComposite;
import org.polymap.biotop.model.BiotopRepository;
import org.polymap.biotop.model.BiotoptypValue;

import org.polymap.core.qi4j.event.AbstractModelChangeOperation;

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
            // Biotopdaten
//            SubProgressMonitor sub = new SubProgressMonitor( monitor, 10 );
//            importBiotopdaten( db.getTable( "Biotopdaten" ), sub );
            SubProgressMonitor sub = new SubProgressMonitor( monitor, 10 );
            importBiotoptypen( db.getTable( "Biotoptypen" ), sub );
        }
        finally {
            db.close();
            db = null;
        }

        return Status.OK_STATUS;
    }


    protected void importBiotopdaten( Table table, IProgressMonitor monitor )
    throws Exception {
        monitor.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );
        printSchema( table );

        // fail if the colums does not exit
        Column beschreibung = table.getColumn( "Biotopbeschreibung" );
        Column bemerkungen = table.getColumn( "Bemerkungen" );

        // data rows
        Map<String, Object> row = null;
        while ((row = table.getNextRow()) != null) {
            String objnr_sbk = (String)row.get( "Objektnummer" );
            BiotopComposite biotop = findBiotop( objnr_sbk );

            if (biotop == null) {
                log.info( "    No entity found for: " + objnr_sbk );
                continue;
            }
            //log.info( "    Entity found: " + biotop );
            biotop.beschreibung().set( (String)row.get( beschreibung.getName() ) );
            biotop.bemerkungen().set( (String)row.get( bemerkungen.getName() ) );
            monitor.worked( 1 );
        }
        monitor.done();
    }


    protected void importBiotoptypen( Table table, IProgressMonitor monitor )
    throws Exception {
        monitor.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );
        printSchema( table );

        // data rows
        Map<String, Object> row = null;
        while ((row = table.getNextRow()) != null) {
            String objnr_sbk = (String)columnValue( table, row, "Objektnummer" );
            BiotopComposite biotop = findBiotop( objnr_sbk );

            if (biotop == null) {
                log.info( "    No entity found for: " + objnr_sbk );
                continue;
            }

            ValueBuilder<BiotoptypValue> builder = BiotopRepository.instance().newValueBuilder( BiotoptypValue.class );
            BiotoptypValue prototype = builder.prototype();
            prototype.objnr_sbk().set( objnr_sbk );
            prototype.nummer().set( ((Number)columnValue( table, row, "Nr_Biotoptyp" )).intValue() );
            prototype.unternummer().set( (String)columnValue( table, row, "Biotop_Unternummer" ) );
            Object value = columnValue( table, row, "Pflegerückstand" );
            if (value != null) {
                prototype.pflegerueckstand().set( ((Number)value).intValue() );
            }
            value = columnValue( table, row, "Biotoptyp_Länge" );
            if (value != null) {
                prototype.laenge().set( ((Number)value).doubleValue() );
            }
            value = columnValue( table, row, "Biotoptyp_Breite" );
            if (value != null) {
                prototype.laenge().set( ((Number)value).doubleValue() );
            }
            value = columnValue( table, row, "Biotoptyp_Flächenprozent" );
            if (value != null) {
                prototype.flaechenprozent().set( ((Number)value).doubleValue() );
            }

            BiotoptypValue biotoptyp = builder.newInstance();
            biotop.biotoptypen().get().add( biotoptyp );
            // force update
            biotop.bid().set( biotop.bid().get() );
            log.info( "Biotoptyp added: " + biotop );
            monitor.worked( 1 );
        }
        monitor.done();
    }


    private void printSchema( Table table ) {
        log.info( "Table: " + table.getName() );
        for (Column col : table.getColumns()) {
            log.info( "    column: " + col.getName() + " - " + col.getType() );
        }
    }


    private BiotopComposite findBiotop( String objnr_sbk ) {
        BiotopRepository repo = BiotopRepository.instance();
        BiotopComposite template = QueryExpressions.templateFor( BiotopComposite.class );

        Query<BiotopComposite> matches = repo.findEntities( BiotopComposite.class,
                QueryExpressions.eq( template.objnr_sbk(), objnr_sbk ), 0, 1 );

        Iterator<BiotopComposite> it = matches.iterator();
        BiotopComposite result = it.hasNext() ? it.next() : null;
        return result;
    }


    private Object columnValue( Table table, Map<String,Object> row, String col ) {
        if (table.getColumn( col ) == null) {
            throw new IllegalArgumentException( "No such column: " + col );
        }
        return row.get( col );
    }

}
