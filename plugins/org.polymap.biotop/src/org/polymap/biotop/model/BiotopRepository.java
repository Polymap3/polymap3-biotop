/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and individual contributors as
 * indicated by the @authors tag.
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
package org.polymap.biotop.model;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import net.refractions.udig.catalog.CatalogPluginSession;
import net.refractions.udig.catalog.IService;

import org.geotools.feature.NameImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.value.ValueBuilder;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.model.CompletionException;
import org.polymap.core.model.ConcurrentModificationException;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.qi4j.Qi4jPlugin;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModuleAssembler;
import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.data.entityfeature.DefaultEntityProvider;

import org.polymap.biotop.model.constant.Status;
import org.polymap.biotop.model.idgen.BiotopnummerGeneratorService;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BiotopRepository
        extends QiModule {

    private static Log log = LogFactory.getLog( BiotopRepository.class );

    public static final String              NAMESPACE = "http://polymap.org/biotop";

    /**
     * Get or create the repository for the current user session.
     */
    public static final BiotopRepository instance() {
        return (BiotopRepository)Qi4jPlugin.Session.instance().module( BiotopRepository.class );
    }


    // instance *******************************************

    private OperationSaveListener       operationListener = new OperationSaveListener();
    
    /** Allow direct access for operations. */
    protected IService                  biotopService;
    

    protected BiotopRepository( QiModuleAssembler assembler ) {
        super( assembler );
        log.debug( "Initializing Biotop module..." );

        // for the global instance of the module (Qi4jPlugin.Session.globalInstance()) there
        // is no request context
        if (Polymap.getSessionDisplay() != null) {
            OperationSupport.instance().addOperationSaveListener( operationListener );
        }
        
        // the Biotop service
        try {
            final CoordinateReferenceSystem crs = CRS.decode( "EPSG:31468" );
            final ReferencedEnvelope bounds = new ReferencedEnvelope( 4000000, 5000000, 5000000, 6000000, crs );
            
            biotopService = new BiotopService( 
                    // BiotopComposite
                    new BiotopEntityProvider( this ),
                    // BiotoptypArtComposite
                    new DefaultEntityProvider( this, BiotoptypArtComposite.class, 
                            new NameImpl( BiotopRepository.NAMESPACE, "Biotoptyp" )) {
                        public ReferencedEnvelope getBounds() {
                            return bounds;
                        }
                        public CoordinateReferenceSystem getCoordinateReferenceSystem( String propName ) {
                            return crs;
                        }
                        public String getDefaultGeometry() {
                            throw new RuntimeException( "not yet implemented." );
                        }
                    },
                    // PflanzenArtComposite
                    new DefaultEntityProvider( this, PflanzenArtComposite.class, 
                            new NameImpl( BiotopRepository.NAMESPACE, "Pflanzenart" )) {
                        public ReferencedEnvelope getBounds() {
                            return bounds;
                        }
                        public CoordinateReferenceSystem getCoordinateReferenceSystem( String propName ) {
                            return crs;
                        }
                        public String getDefaultGeometry() {
                            throw new RuntimeException( "not yet implemented." );
                        }
                    });
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
        
        // register with catalog
        if (Polymap.getSessionDisplay() != null) {
            Polymap.getSessionDisplay().asyncExec( new Runnable() {
                public void run() {
                    CatalogPluginSession.instance().getLocalCatalog().add( biotopService );
                }
            });
        }
    }

    
    protected void done() {
        if (operationListener != null) {
            OperationSupport.instance().removeOperationSaveListener( operationListener );
            operationListener = null;
        }
        if (biotopService != null) {
            biotopService.dispose( new NullProgressMonitor() );
        }
        // check ThreadLocal;
        // code from http://blog.igorminar.com/2009/03/identifying-threadlocal-memory-leaks-in.html
        try {
            Thread thread = Thread.currentThread();

            Field threadLocalsField = Thread.class.getDeclaredField( "threadLocals" );
            threadLocalsField.setAccessible( true );

            Class threadLocalMapKlazz = Class.forName( "java.lang.ThreadLocal$ThreadLocalMap" );
            Field tableField = threadLocalMapKlazz.getDeclaredField( "table" );
            tableField.setAccessible( true );

            Object table = tableField.get( threadLocalsField.get( thread ) );

            int threadLocalCount = Array.getLength( table );
            StringBuilder sb = new StringBuilder();
            StringBuilder classSb = new StringBuilder();

            int leakCount = 0;

            for (int i = 0; i < threadLocalCount; i++) {
                Object entry = Array.get( table, i );
                if (entry != null) {
                    Field valueField = entry.getClass().getDeclaredField( "value" );
                    valueField.setAccessible( true );
                    Object value = valueField.get( entry );
                    if (value != null) {
                        classSb.append( value.getClass().getName() ).append( ", " );
                    }
                    else {
                        classSb.append( "null, " );
                    }
                    leakCount++;
                }
            }

            sb.append( "possible ThreadLocal leaks: " ).append( leakCount ).append( " of " ).append(
                    threadLocalCount ).append( " = [" ).append(
                    classSb.substring( 0, classSb.length() - 2 ) ).append( "] " );

            log.info( sb );
        }
        catch (Exception e) {
            log.warn( "", e );
        }
        
        super.done();
        
        log.info( "Running GC ..." );
        Runtime.getRuntime().gc();
    }


    public <T> Query<T> findEntities( Class<T> compositeType, BooleanExpression expression,
            int firstResult, int maxResults ) {
        // Lucene does not like Integer.MAX_VALUE!?
        maxResults = Math.min( maxResults, 1000000 );
        
        return super.findEntities( compositeType, expression, firstResult, maxResults );
    }
    

    public void applyChanges() 
    throws ConcurrentModificationException, CompletionException {
        try {
            // save changes
            uow.apply();
        }
        catch (ConcurrentEntityModificationException e) {
            throw new ConcurrentModificationException( e );
        }
        catch (UnitOfWorkCompletionException e) {
            throw new CompletionException( e );
        }
    }
    
    
    public <T> ValueBuilder<T> newValueBuilder( Class<T> type ) {
        return assembler.getModule().valueBuilderFactory().newValueBuilder( type );
    }
    
    
    public BiotopComposite newBiotop( final EntityCreator<BiotopComposite> creator )
    throws Exception {
        return newEntity( BiotopComposite.class, null, new EntityCreator<BiotopComposite>() {
            public void create( BiotopComposite instance )
            throws Exception {
                // objnr
                ServiceReference<BiotopnummerGeneratorService> service = 
                        assembler.getModule().serviceFinder().findService( BiotopnummerGeneratorService.class );
                instance.objnr().set( service.get().generate() );
                // status
                instance.status().set( Status.aktuell.id );

                if (creator != null) {
                    creator.create( instance );
                }
            }
        });
    }
    
}
