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
package org.polymap.biotop.model;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.lucene.LuceneEntityStoreInfo;
import org.qi4j.entitystore.lucene.LuceneEntityStoreQueryService;
import org.qi4j.entitystore.lucene.LuceneEntityStoreService;

import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModuleAssembler;
import org.polymap.core.qi4j.idgen.HRIdentityGeneratorService;
import org.polymap.core.runtime.Polymap;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BiotopRepositoryAssembler
        extends QiModuleAssembler {

    private static Log log = LogFactory.getLog( BiotopRepositoryAssembler.class );

    private Application                 app;

    private UnitOfWorkFactory           uowf;

    private Module                      module;


    public BiotopRepositoryAssembler() {
    }


    protected void setApp( Application app ) {
        this.app = app;
        this.module = app.findModule( "application-layer", "biotop-module" );
        this.uowf = module.unitOfWorkFactory();
    }


    public QiModule newModule() {
        return new BiotopRepository( this );
    }


    public void assemble( ApplicationAssembly _app )
            throws Exception {
        log.info( "Assembling: org.polymap.biotop ..." );

        // project layer / module
        LayerAssembly domainLayer = _app.layerAssembly( "application-layer" );
        ModuleAssembly domainModule = domainLayer.moduleAssembly( "biotop-module" );
        domainModule.addEntities(
                BiotopComposite.class
        );
//        domainModule.addTransients(
//                CreateCatalogEntryOperation.class,
//                AntragImportOperation.class,
//                PersonImportOperation.class,
//                BearbschrittImportOperation.class,
//                VermesserImportOperation.class,
//                GemarkungImportOperation.class,
//                TeilantragOperation.class,
//                BelegImportOperation.class,
//                BelegPosImportOperation.class,
//                BelegPosDataImportOperation.class
//        );
        domainModule.addValues(
                AktivitaetValue.class,
                BiotoptypValue.class
        );

//        domainModule.addServices( FactoryService.class )
//                .visibleIn( Visibility.application );

        // persistence: workspace/Lucene
        File root = new File( Polymap.getWorkspacePath().toFile(), "data" );

        File moduleRoot = new File( root, "org.polymap.biotop" );
        moduleRoot.mkdir();

        domainModule.addServices( LuceneEntityStoreService.class )
                .setMetaInfo( new LuceneEntityStoreInfo( moduleRoot ) )
                .instantiateOnStartup()
                .identifiedBy( "lucene-repository" );

        // indexer
        domainModule.addServices( LuceneEntityStoreQueryService.class )
                //.visibleIn( indexingVisibility )
                //.setMetaInfo( namedQueries )
                .instantiateOnStartup();

        domainModule.addServices( HRIdentityGeneratorService.class );

//        domainModule.addServices( AntragsnummerGeneratorService.class )
//                .identifiedBy( "antragsnummer" );
    }


    public void createInitData()
            throws Exception {
    }


    public Module getModule() {
        return module;
    }

}
