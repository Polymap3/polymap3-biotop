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
package org.polymap.biotop.model.test;

import java.io.File;

import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;

import org.polymap.core.qi4j.idgen.HRIdentityGeneratorService;
import org.polymap.rhei.data.entitystore.lucene.LuceneEntityStoreInfo;
import org.polymap.rhei.data.entitystore.lucene.LuceneEntityStoreQueryService;
import org.polymap.rhei.data.entitystore.lucene.LuceneEntityStoreService;

import org.polymap.biotop.model.AktivitaetValue;
import org.polymap.biotop.model.BiotopComposite;
import org.polymap.biotop.model.BiotoptypArtComposite;
import org.polymap.biotop.model.BiotoptypValue;
import org.polymap.biotop.model.PflanzeValue;
import org.polymap.biotop.model.PflanzenArtComposite;
import org.polymap.biotop.model.PilzeArtComposite;
import org.polymap.biotop.model.PilzeValue;
import org.polymap.biotop.model.TierArtComposite;
import org.polymap.biotop.model.TierValue;
import org.polymap.biotop.model.idgen.BiotopnummerGeneratorService;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BiotopRepositoryAssembler {

    Application                 app;

    UnitOfWorkFactory           uowf;

    Module                      module;


    public BiotopRepositoryAssembler() {
    }


    protected void setApp( Application app ) {
        this.app = app;
        this.module = app.findModule( "application-layer", "biotop-module" );
        this.uowf = module.unitOfWorkFactory();
    }


    public void assemble( ApplicationAssembly _app )
    throws Exception {
        // project layer / module
        LayerAssembly domainLayer = _app.layerAssembly( "application-layer" );
        ModuleAssembly domainModule = domainLayer.moduleAssembly( "biotop-module" );
        domainModule.addEntities(
                BiotopComposite.class,
                BiotoptypArtComposite.class,
                PflanzenArtComposite.class,
                PilzeArtComposite.class,
                TierArtComposite.class
        );
//        domainModule.addTransients(
//        );
        domainModule.addValues(
                AktivitaetValue.class,
                BiotoptypValue.class,
                PflanzeValue.class,
                PilzeValue.class,
                TierValue.class
        );

        // persistence: workspace/Lucene
        File moduleRoot = new File( "/home/falko/servers/workspace-biotop/data/org.polymap.biotop/" );

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

        // additional services
        domainModule.addServices( BiotopnummerGeneratorService.class )
                .identifiedBy( "biotopnummer" );
    }
}
