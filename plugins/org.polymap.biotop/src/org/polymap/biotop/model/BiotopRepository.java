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

import net.refractions.udig.catalog.CatalogPluginSession;
import net.refractions.udig.catalog.IService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.value.ValueBuilder;

import org.polymap.core.model.CompletionException;
import org.polymap.core.model.ConcurrentModificationException;
import org.polymap.core.operation.IOperationSaveListener;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.qi4j.Qi4jPlugin;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModuleAssembler;
import org.polymap.core.qi4j.Qi4jPlugin.Session;
import org.polymap.core.runtime.Polymap;

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

    /**
     * The global instance used outside any user session.
     * 
     * @return A newly created {@link Session} instance. It is up to the caller
     *         to store and re-use if necessary.
     */
    public static final BiotopRepository globalInstance() {
        return (BiotopRepository)Qi4jPlugin.Session.globalInstance().module( BiotopRepository.class );
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
        biotopService = new BiotopService( 
                new BiotopEntityProvider( this ) );
        
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
    

    /**
     * 
     */
    class OperationSaveListener
    implements IOperationSaveListener {

        public void prepareSave( OperationSupport os )
        throws Exception {
        }

        public void save( OperationSupport os )
        throws Exception {
            log.debug( "..." );
            commitChanges();
        }
        
        public void revert( OperationSupport os ) {
            log.debug( "..." );
            discardChanges();
        }

    }

}
