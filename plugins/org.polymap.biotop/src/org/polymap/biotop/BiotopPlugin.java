/* 
 * polymap.org
 * Copyright (C) 2012-2015, Polymap GmbH. All rights reserved.
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
package org.polymap.biotop;

import org.osgi.framework.BundleContext;

import org.apache.commons.lang.time.FastDateFormat;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.polymap.core.ImageRegistryHelper;

/**
 * The activator class controls the plug-in life cycle.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BiotopPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.polymap.biotop";

	public static final FastDateFormat df = FastDateFormat.getInstance( "dd.MM.yyyy" );

	private static BiotopPlugin plugin;
	
	
	public static BiotopPlugin getDefault() {
    	return plugin;
    }

	// instance *******************************************

	private ImageRegistryHelper        images = new ImageRegistryHelper( this );
	

	public void start( BundleContext context ) throws Exception {
		super.start( context );
		plugin = this;
	}

	public void stop( BundleContext context ) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public Image imageForDescriptor( ImageDescriptor descriptor, String key ) {
	    return images.image( descriptor, key );
    }

    public Image imageForName( String resName ) {
        return images.image( resName );
    }

    public ImageDescriptor imageDescriptor( String path ) {
        return images.imageDescriptor( path );
    }

}
