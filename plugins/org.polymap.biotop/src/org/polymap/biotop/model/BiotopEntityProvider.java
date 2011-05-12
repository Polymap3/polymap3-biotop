/*
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as indicated
 * by the @authors tag.
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

import org.geotools.feature.NameImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModule.EntityCreator;
import org.polymap.rhei.data.entityfeature.DefaultEntityProvider;
import org.polymap.rhei.data.entityfeature.EntityProvider;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BiotopEntityProvider
        extends DefaultEntityProvider<BiotopComposite>
        implements EntityProvider<BiotopComposite> {

    private static Log log = LogFactory.getLog( BiotopEntityProvider.class );


    public BiotopEntityProvider( QiModule repo ) {
        super( repo, BiotopComposite.class, new NameImpl( BiotopRepository.NAMESPACE, "Biotop" ) );
    }


    public BiotopComposite newEntity( final EntityCreator<BiotopComposite> creator )
    throws Exception {
        return repo.newEntity( BiotopComposite.class, null, new EntityCreator<BiotopComposite>() {
            public void create( BiotopComposite instance )
            throws Exception {
                // defaults
                // custom
                creator.create( instance );
            }
        });
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
        return "geom";
    }


    public ReferencedEnvelope getBounds() {
        return new ReferencedEnvelope( 4000000, 5000000, 5000000, 6000000, getCoordinateReferenceSystem( null ) );
    }

}
