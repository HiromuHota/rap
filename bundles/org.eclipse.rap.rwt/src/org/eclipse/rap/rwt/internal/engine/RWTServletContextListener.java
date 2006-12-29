/*******************************************************************************
 * Copyright (c) 2002-2006 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Innoopract Informationssysteme GmbH - initial API and implementation
 ******************************************************************************/

package org.eclipse.rap.rwt.internal.engine;

import java.text.MessageFormat;
import java.util.*;
import javax.servlet.*;
import org.eclipse.rap.rwt.internal.lifecycle.*;
import org.eclipse.rap.rwt.internal.widgets.WidgetAdapterFactory;
import org.eclipse.rap.rwt.resources.*;
import org.eclipse.rap.rwt.widgets.Display;
import org.eclipse.rap.rwt.widgets.Widget;
import com.w4t.W4TContext;
import com.w4t.engine.lifecycle.PhaseListener;

/**
 * TODO [rh] JavaDoc 
 */
public class RWTServletContextListener implements ServletContextListener {

  public static final String ENTRYPOINT_PARAM 
    = "org.eclipse.rap.rwt.entrypoint";
  public static final String RESOURCE_MANAGER_FACTORY_PARAM
    = "org.eclipse.rap.rwt.resourcemanagerfactory";
  public static final String ADAPTER_FACTORY_PARAM
    = "org.eclipse.rap.rwt.adapterFactories";
  public static final String PHASE_LISTENER_PARAM 
    = "com.w4t.engine.lifecycle.phaselistener";
  
  private static final String REGISTERED_ENTRY_POINTS
    = "org.eclipse.rap.rwt.registeredEntryPoints";
  private static final String REGISTERED_PHASE_LISTENERS 
    = "org.eclipse.rap.rwt.registeredPhaseListeners";
  
  ///////////////////////////////////////////
  // implementation of ServletContextListener

  public void contextInitialized( final ServletContextEvent evt ) {
    registerEntryPoints( evt.getServletContext() );
    registerResourceManagerFactory( evt.getServletContext() );
    registerAdapterFactories( evt.getServletContext() );
    registerPhaseListener( evt.getServletContext() );
  }

  public void contextDestroyed( final ServletContextEvent evt ) {
    deregisterEntryPoints( evt.getServletContext() );
    deregisterPhaseListeners( evt.getServletContext() );
  }
  
  
  ////////////////////////////////////////////////////////////
  // helping methods - entry point registration/deregistration
  
  private static void registerEntryPoints( final ServletContext context ) {
    Set registeredEntryPoints = new HashSet();
    String value = context.getInitParameter( ENTRYPOINT_PARAM );
    if( value != null ) {
      String[] entryPoints = value.split( "," );
      for( int i = 0; i < entryPoints.length; i++ ) {
        String entryPoint = entryPoints[ i ];
        String[] parts = entryPoint.trim().split( "#" );
        String className = parts[ 0 ];
        String entryPointName = EntryPointManager.DEFAULT;
        if( parts.length > 1 ) {
          entryPointName = parts[ 1 ];
        }
        try {
          Class clazz = Class.forName( className );
          EntryPointManager.register( entryPointName, clazz );
          registeredEntryPoints.add( entryPointName );
        } catch( Exception ex ) {
          String text = "Failed to register entry point ''{0}''.";
          Object[] args = new Object[] { entryPoint };
          String msg = MessageFormat.format( text, args );
          context.log( msg, ex );
        }
      }
    }
    setRegisteredEntryPoints( context, registeredEntryPoints );
  }
  
  private void deregisterEntryPoints( final ServletContext context ) {
    String[] entryPoints = getRegisteredEntryPoints( context );
    if( entryPoints != null ) {
      for( int i = 0; i < entryPoints.length; i++ ) {
        EntryPointManager.deregister( entryPoints[ i ] );
      }
    }
  }
  
  private static void setRegisteredEntryPoints( final ServletContext ctx, 
                                                final Set entryPoints ) 
  {
    String[] value = new String[ entryPoints.size() ];
    entryPoints.toArray( value );
    ctx.setAttribute( REGISTERED_ENTRY_POINTS, value );
  }
  
  private static String[] getRegisteredEntryPoints( final ServletContext ctx ) {
    return ( String[] )ctx.getAttribute( REGISTERED_ENTRY_POINTS );
  }

  //////////////////////////////////////////////////
  // Helping methods - resource manager registration 
  
  private static void registerResourceManagerFactory(
    final ServletContext context )
  {
    String factoryName 
      = context.getInitParameter( RESOURCE_MANAGER_FACTORY_PARAM );
    if( factoryName != null ) {
      try {
        Class clazz = Class.forName( factoryName );
        IResourceManagerFactory factory;
        factory = ( IResourceManagerFactory )clazz.newInstance();
        ResourceManager.register( factory );
      } catch( final Exception ex ) {
        String text = "Failed to register resource manager factory ''{0}''.";
        Object[] args = new Object[] { factoryName };
        String msg = MessageFormat.format( text, args );
        context.log( msg, ex );
      }      
    } else {
      ResourceManager.register( new DefaultResourceManagerFactory() );
    }
  }
  
  /////////////////////////////////////////////////
  // Helping methods - adapter factory registration
  
  private static void registerAdapterFactories( final ServletContext context ) {
    String initParam = context.getInitParameter( ADAPTER_FACTORY_PARAM );
    if( initParam != null ) {
      String[] factoryParams = initParam.split( "," );
      for( int i = 0; i < factoryParams.length; i++ ) {
        String[] classNames = factoryParams[ i ].trim().split( "#" );
        if( classNames.length != 2 ) {
          Object[] param = new Object[] { factoryParams[ i ] };
          String text = "''{0}'' is not a valid factory-adaptable pair.";
          String msg = MessageFormat.format( text, param );
          context.log( msg );
        } else {
          try {
            Class factoryClass = Class.forName( classNames[ 0 ] );
            Class adaptableClass = Class.forName( classNames[ 1 ] );
            AdapterFactoryRegistry.add( factoryClass, adaptableClass );
          } catch( final Throwable thr ) {
            Object[] param = new Object[] { factoryParams[ i ] };
            String text;
            text = "Could not register the factory-adaptable ''{0}'' pair.";
            String msg = MessageFormat.format( text, param );
            context.log( msg, thr );
          }
        }        
      }
    } else {
      AdapterFactoryRegistry.add( LifeCycleAdapterFactory.class, 
                                  Widget.class );
      AdapterFactoryRegistry.add( LifeCycleAdapterFactory.class, 
                                  Display.class );
      AdapterFactoryRegistry.add( WidgetAdapterFactory.class, 
                                  Widget.class );
      AdapterFactoryRegistry.add( WidgetAdapterFactory.class, 
                                  Display.class );
    }
  }
  
  ///////////////////////////////////////////////////////////////
  // Helping methods - phase listener registration/deregistration

  private static void registerPhaseListener( final ServletContext context ) {
    List phaseListeners = new ArrayList();
    String initParam = context.getInitParameter( PHASE_LISTENER_PARAM );
    if( initParam != null ) {
      String[] listenerNames = initParam.split( "," );
      for( int i = 0; i < listenerNames.length; i++ ) {
        String className = listenerNames[ i ].trim();
        try {
          Class clazz = Class.forName( className );
          PhaseListener listener = ( PhaseListener )clazz.newInstance();
          phaseListeners.add( listener );
        } catch( Throwable thr ) {
          String text = "Failed to register phase listener ''{0}''.";
          String msg = MessageFormat.format( text, new Object[] { className } );
          context.log( msg, thr );
        }
      }
    } else {
      phaseListeners.add( new PreserveWidgetsPhaseListener() );
      phaseListeners.add( new CurrentPhase.Listener() );
    }
    PhaseListener[] registeredListeners;
    registeredListeners = new PhaseListener[ phaseListeners.size() ];
    phaseListeners.toArray( registeredListeners );
    for( int i = 0; i < registeredListeners.length; i++ ) {
      PhaseListenerRegistry.add( registeredListeners[ i ] );
    }
    context.setAttribute( REGISTERED_PHASE_LISTENERS, registeredListeners );
  }

  private void deregisterPhaseListeners( final ServletContext servletContext ) {
    PhaseListener[] listeners = getRegisteredPhaseListeners( servletContext );
    if( listeners != null ) {
      for( int i = 0; i < listeners.length; i++ ) {
        W4TContext.getLifeCycle().removePhaseListener( listeners[ i ] );
      }
    }
  }
  
  private static PhaseListener[] getRegisteredPhaseListeners( 
    final ServletContext ctx ) 
  {
    return ( PhaseListener[] )ctx.getAttribute( REGISTERED_PHASE_LISTENERS );
  }
}