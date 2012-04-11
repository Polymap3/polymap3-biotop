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
package org.polymap.biotop.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.viewers.Viewer;

import org.polymap.core.data.ui.featuretable.IFeatureContentProvider;
import org.polymap.core.data.ui.featuretable.IFeatureTableElement;
import org.polymap.core.model.Composite;
import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;
import org.polymap.core.model.EntityType.Property;

/**
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class LinkedCompositesContentProvider<T extends Composite,L extends Composite>
        implements IFeatureContentProvider {

    private static Log log = LogFactory.getLog( LinkedCompositesContentProvider.class );

    private Iterable<T>         composites;

    private EntityType<T>       compositeType;

    private EntityType<L>       linkedCompositeType;


    public LinkedCompositesContentProvider(
            Iterable<T> composites,
            EntityType<T> compositeType,
            EntityType<L> linkedCompositeType ) {
        this.composites = composites;
        this.compositeType = compositeType;
        this.linkedCompositeType = linkedCompositeType;
    }


    public void dispose() {
    }


    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
    }


    public Object[] getElements( Object input ) {
        log.debug( "getElements(): input=" + input.getClass().getName() );
        List<IFeatureTableElement> result = new ArrayList();
        for (T composite : composites) {
            result.add( new FeatureTableElement( composite ) );
        }
        return result.toArray();
    }

    
    protected abstract L linkedElement( T element );

    
    /**
     *
     */
    protected class FeatureTableElement
            implements IFeatureTableElement {

        private T           composite;

        private L           linked;


        protected FeatureTableElement( T composite ) {
            this.composite = composite;
            this.linked = linkedElement( composite );
        }

        public T getComposite() {
            return composite;
        }

        public Object getValue( String name ) {
            try {
                log.debug( "getValue(): name=" + name );
                Property prop = compositeType.getProperty( name );
                if (prop != null) {
                    return prop.getValue( composite );
                }
                prop = linkedCompositeType.getProperty( name );
                if (prop != null) {
                    return prop.getValue( linked );
                }
                throw new RuntimeException( "No such property found: " + name );
            }
            catch (RuntimeException e) {
                throw e;
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }

        public String fid() {
            if (composite instanceof Entity) {
                return ((Entity)composite).id();
            }
            else {
                throw new RuntimeException( "Don't know how to build fid out of: " + composite );
            }
        }

    }

}
