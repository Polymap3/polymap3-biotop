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

import java.util.Map.Entry;

import java.io.File;
import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.recordstore.IRecordState;
import org.polymap.core.runtime.recordstore.SimpleQuery;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordStore;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LoadStatesTest {

    public static void main( String[] args ) 
    throws Exception {
        LuceneRecordStore store = new LuceneRecordStore( 
                new File( "/home/falko/servers/workspace-biotop/data/org.polymap.biotop/" ), false );
        
        SimpleQuery query = new SimpleQuery()
                .eq( "type", "org.polymap.biotop.model.BiotopComposite" )
                .setMaxResults( 100000 );
        loadStates( store, query );
        loadStates( store, query );
        
        store.close();
    }

    /**
     *
     * @param store
     * @throws Exception
     */
    private static void loadStates( LuceneRecordStore store, SimpleQuery query )
    throws Exception {
        Timer timer = new Timer();
        int states = 0;
        int properties = 0;
        int strings = 0;
        for (IRecordState state : store.find( query )) {
            states++;
            for (Entry<String,Object> property : state) {
                properties++;
                if (property.getValue() instanceof String) {
                    strings++;
                }
            }
        }
        System.out.println( "Load time: " + timer.elapsedTime() + "ms; states: " + states + "; properties: " + properties );
    }
}
