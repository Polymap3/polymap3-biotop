/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.biotop.model.test;

import java.util.Collection;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.spi.structure.ApplicationSPI;

import org.polymap.core.model.Composite;
import org.polymap.core.model.EntityType;
import org.polymap.core.model.EntityType.CollectionProperty;
import org.polymap.core.model.EntityType.Property;
import org.polymap.core.runtime.Timer;

import org.polymap.biotop.model.BiotopComposite;
import org.polymap.biotop.model.PflanzeValue;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LoadEntitiesTest {

    public static void main( String[] args )
    throws Exception {
        BiotopRepositoryAssembler assembler = assemble();
        
        UnitOfWork uow = assembler.module.unitOfWorkFactory().newUnitOfWork();
        QueryBuilder<BiotopComposite> builder = assembler.module.queryBuilderFactory().newQueryBuilder( BiotopComposite.class );
        Query<BiotopComposite> query = builder.newQuery( uow ).maxResults( 100000 ).firstResult( 0 );
        load( uow, query );
        load( uow, query );
    }
    
    
    protected static void load( UnitOfWork uow, Query<BiotopComposite> query )
    throws Exception {
        Timer timer = new Timer();
        
        int entities = 0;
        int properties = 0;
        int strings = 0;
        for (BiotopComposite biotop : query) {
            entities++;
            EntityType<BiotopComposite> type = biotop.getEntityType();
            for (Property prop : type.getProperties()) {
                if (prop instanceof CollectionProperty) {
                    EntityType<Composite> collType = ((CollectionProperty)prop).getComplexType();
                    for (Property p2 : collType.getProperties()) {
                        properties++;
                        if (prop.getValue( biotop ) instanceof String) {
                            strings++;
                        }                        
                    }
                }
                else {
                    properties++;
                    if (prop.getValue( biotop ) instanceof String) {
                        strings++;
                    }
                }
            }
            Collection<PflanzeValue> pflanzen = biotop.pflanzen().get();
            for (PflanzeValue v : pflanzen) {
                properties++;
                if ((v.menge().get()) instanceof Integer) {
                    strings++;
                }
                properties++;
                if ((v.mengenstatusNr().get()) instanceof Double) {
                    strings++;
                }
                properties++;
                if ((v.pflanzenArtNr().get()) instanceof String) {
                    strings++;
                }
            }
        }
        System.out.println( "Load time: " + timer.elapsedTime() + "ms; entities: " + entities + "; properties: " + properties );
    }
        
    
    protected static BiotopRepositoryAssembler assemble() 
    throws Exception {
        final BiotopRepositoryAssembler assembler = new BiotopRepositoryAssembler();
        Energy4Java qi4j = new Energy4Java();
        
        ApplicationSPI application = qi4j.newApplication( new ApplicationAssembler() {
            public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
            throws AssemblyException {
                ApplicationAssembly app = applicationFactory.newApplicationAssembly();
                try {
                    assembler.assemble( app );
                }
                catch (Exception e) {
                    throw new RuntimeException( e );
                }
                return app;
            }
        } );
        
        assembler.setApp( application );
        
        application.activate();
        return assembler;
    }
}
