/*******************************************************************************
 * Copyright (c) 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.client;

import org.eclipse.rap.rwt.client.service.ClientService;
import org.eclipse.rap.rwt.client.service.JavaScriptExecutor;
import org.eclipse.rap.rwt.internal.widgets.JavaScriptExecutorImpl;
import org.eclipse.rap.rwt.testfixture.Fixture;
import junit.framework.TestCase;


public class WebClient_Test extends TestCase {

  private WebClient client;

  @Override
  protected void setUp() throws Exception {
    Fixture.setUp();
    client = new WebClient();
  }

  @Override
  protected void tearDown() throws Exception {
    Fixture.tearDown();
  }

  public void testGetInvalidService() {
    assertNull( client.getService( UnsupportedService.class ) );
  }

  public void testGetJavaScriptExecutorService() {
    ClientService service = client.getService( JavaScriptExecutor.class );
    assertTrue( service instanceof JavaScriptExecutorImpl );
  }

  public void testGetServiveTwice() {
    ClientService service1 = client.getService( JavaScriptExecutor.class );
    ClientService service2 = client.getService( JavaScriptExecutor.class );

    assertSame( service1, service2 );
  }

  //////////////////
  // Helping classes

  private class UnsupportedService implements ClientService {
  }
}