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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import org.geotools.feature.NameImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.type.Name;
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

import org.polymap.core.catalog.model.CatalogRepository;
import org.polymap.core.model.CompletionException;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.qi4j.Qi4jPlugin;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModuleAssembler;
import org.polymap.core.qi4j.Qi4jPlugin.Session;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.entity.ConcurrentModificationException;

import org.polymap.rhei.data.entityfeature.DefaultEntityProvider;
import org.polymap.rhei.data.entitystore.lucene.LuceneEntityStoreService;
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
        return Qi4jPlugin.Session.instance().module( BiotopRepository.class );
    }


    // instance *******************************************

    private OperationSaveListener               operationListener = new OperationSaveListener();
    
    private Map<String,BiotoptypArtComposite>   btNamen;

    private Map<String,BiotoptypArtComposite>   btNummern;

    /** Allow direct access for operations. */
    protected BiotopService                     biotopService;
    
    public ServiceReference<BiotopnummerGeneratorService> biotopnummern;
    
    /**
     * 
     */
    public static class ArtEntityProvider
            extends DefaultEntityProvider {
        
        public ArtEntityProvider( QiModule repo, Class entityClass, Name entityName ) {
            super( repo, entityClass, entityName );
        }
        
        public ReferencedEnvelope getBounds() {
            return new ReferencedEnvelope( 4000000, 5000000, 5000000, 6000000, getCoordinateReferenceSystem( null ) );
        }
        
        public CoordinateReferenceSystem getCoordinateReferenceSystem( String propName ) {
            try {
                return CRS.decode( "EPSG:31468" );
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }
        
        public String getDefaultGeometry() {
            throw new RuntimeException( "not yet implemented." );
        }
    };
    

    public BiotopRepository( final QiModuleAssembler assembler ) {
        super( assembler );
        log.debug( "Initializing Biotop module..." );

        // for the global instance of the module (Qi4jPlugin.Session.globalInstance()) there
        // is no request context
        if (Polymap.getSessionDisplay() != null) {
            OperationSupport.instance().addOperationSaveListener( operationListener );
        }
        biotopnummern = assembler.getModule().serviceFinder().findService( BiotopnummerGeneratorService.class );
    }
    

    public void init( final Session session ) {
        try {            
            // build the queryProvider
            ServiceReference<LuceneEntityStoreService> storeService = assembler.getModule().serviceFinder().findService( LuceneEntityStoreService.class );
            LuceneEntityStoreService luceneStore = storeService.get();

            biotopService = new BiotopService(
                    // BiotopComposite
                    new BiotopEntityProvider( this ),
                    // Arten...
                    new ArtEntityProvider( this, BiotoptypArtComposite.class, 
                            new NameImpl( BiotopRepository.NAMESPACE, "Biotoptyp" ) ),
                    new ArtEntityProvider( this, PflanzenArtComposite.class, 
                            new NameImpl( BiotopRepository.NAMESPACE, "Pflanzenart" ) ),
                    new ArtEntityProvider( this, PilzArtComposite.class, 
                            new NameImpl( BiotopRepository.NAMESPACE, "Pilzart" ) ),
                    new ArtEntityProvider( this, TierArtComposite.class, 
                            new NameImpl( BiotopRepository.NAMESPACE, "Tierart" ) ),
                    new ArtEntityProvider( this, StoerungsArtComposite.class, 
                            new NameImpl( BiotopRepository.NAMESPACE, "Beeinträchtigungen" ) ),
                    new ArtEntityProvider( this, WertArtComposite.class, 
                            new NameImpl( BiotopRepository.NAMESPACE, "Wertbestimmend" ) )
                    );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
        
        // register with catalog        
//        if (Polymap.getSessionDisplay() != null) {
//            Polymap.getSessionDisplay().asyncExec( new Runnable() {
//                public void run() {
                    CatalogRepository catalogRepo = session.module( CatalogRepository.class );
                    catalogRepo.getCatalog().addTransient( biotopService );
//                    CatalogPluginSession.instance().getLocalCatalog().add( biotopService );
//                }
//            });
//        }
    }

    
    protected void dispose() {
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
        
        super.dispose();
        
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
    
    
    public BiotopComposite newBiotop( final EntityCreator<BiotopComposite> creator )
    throws Exception {
        return newEntity( BiotopComposite.class, null, new EntityCreator<BiotopComposite>() {
            public void create( BiotopComposite prototype )
            throws Exception {
                // objnr
                prototype.objnr().set( biotopnummern.get().generate() );
                // status
                prototype.status().set( Status.aktuell.id );
                
                // erfassung
                ValueBuilder<AktivitaetValue> builder = newValueBuilder( AktivitaetValue.class );
                AktivitaetValue _prototype = builder.prototype();
                _prototype.wann().set( new Date() );
                _prototype.wer().set( Polymap.instance().getUser().getName() );
                _prototype.bemerkung().set( "" );
                prototype.erfassung().set( builder.newInstance() );
                // bearbeitung
                prototype.bearbeitung().set( builder.newInstance() );

                // biotoptyp (zufällig)
                String randomBt = btNamen().values().iterator().next().nummer().get();
//                prototype.biotoptypArtNr().set( randomBt );
                
                if (creator != null) {
                    creator.create( prototype );
                }
            }
        });
    }


    public Map<String,BiotoptypArtComposite> btNamen() {
        if (btNamen == null) {
            Query<BiotoptypArtComposite> entities = findEntities( 
                    BiotoptypArtComposite.class, null, 0, 1000 );

            btNamen = new HashMap();
            for (BiotoptypArtComposite entity : entities) {
                btNamen.put( entity.name().get(), entity );
            }
        }
        return btNamen;
    }
    

    public BiotoptypArtComposite btForNummer( String nummer ) {
        if (btNummern == null) {
            Query<BiotoptypArtComposite> entities = findEntities( 
                    BiotoptypArtComposite.class, null, 0, 1000 );

            btNummern = new HashMap();
            for (BiotoptypArtComposite entity : btNamen().values()) {
                btNummern.put( entity.nummer().get(), entity );
            }
        }
        return btNummern.get( nummer );
    }
    

//    public <V extends ValueComposite,A extends Entity,C extends ValueArtComposite<V,A>> 
//            C createValueArt( 
//                    Class<C> cl, 
//                    V value, 
//                    ValueArtFinder<V,A> artFinder ) {
//        
//        C result = assembler.getModule().transientBuilderFactory().newTransient( cl );
//        result.init( value, artFinder );
//        return result;
//    }

}
