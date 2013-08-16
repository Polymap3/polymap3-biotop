package org.polymap.biotop;

import org.osgi.framework.BundleContext;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.polymap.core.ImageRegistryHelper;

/**
 * The activator class controls the plug-in life cycle
 */
public class BiotopPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.polymap.biotop";

	// The shared instance
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
