/*******************************************************************************
* Copyright (c) 2011, 2013 EclipseSource and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    EclipseSource - initial API and implementation
*******************************************************************************/
package org.eclipse.rap.rwt.internal.protocol;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.rap.rwt.internal.json.JsonArray;
import org.eclipse.rap.rwt.internal.json.JsonObject;
import org.eclipse.rap.rwt.internal.json.JsonValue;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rap.rwt.testfixture.Message;
import org.eclipse.rap.rwt.testfixture.Message.CallOperation;
import org.eclipse.rap.rwt.testfixture.Message.CreateOperation;
import org.eclipse.rap.rwt.testfixture.Message.DestroyOperation;
import org.eclipse.rap.rwt.testfixture.Message.ListenOperation;
import org.eclipse.rap.rwt.testfixture.Message.SetOperation;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class ClientObject_Test {

  private Shell shell;
  private String shellId;
  private IClientObject clientObject;

  @Before
  public void setUp() {
    Fixture.setUp();
    Fixture.fakeResponseWriter();
    Display display = new Display();
    shell = new Shell( display );
    shellId = WidgetUtil.getId( shell );
    clientObject = ClientObjectFactory.getClientObject( shell );
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testCreate() {
    clientObject.create( "rwt.widgets.Shell" );

    CreateOperation operation = ( CreateOperation )getMessage().getOperation( 0 );
    assertEquals( shellId, operation.getTarget() );
    assertEquals( "rwt.widgets.Shell", operation.getType() );
  }

  @Test
  public void testCreateIncludesSetProperties() {
    clientObject.create( "rwt.widgets.Shell" );
    clientObject.set( "foo", 23 );

    Message message = getMessage();
    assertEquals( 1, message.getOperationCount() );
    assertTrue( message.getOperation( 0 ) instanceof CreateOperation );
    assertEquals( new Integer( 23 ), message.getOperation( 0 ).getProperty( "foo" ) );
  }

  @Test
  public void testSet() {
    clientObject.set( "key", ( Object )"value" );
    clientObject.set( "key2", 2 );
    clientObject.set( "key3", 3.5 );
    clientObject.set( "key4", true );
    clientObject.set( "key5", "aString" );

    SetOperation operation = ( SetOperation )getMessage().getOperation( 0 );
    assertEquals( shellId, operation.getTarget() );
    assertEquals( "value", operation.getProperty( "key" ) );
    assertEquals( new Integer( 2 ), operation.getProperty( "key2" ) );
    assertEquals( new Double( 3.5 ), operation.getProperty( "key3" ) );
    assertEquals( Boolean.TRUE, operation.getProperty( "key4" ) );
    assertEquals( "aString", operation.getProperty( "key5" ) );
  }

  @Test
  public void testSet_withIntArray() {
    clientObject.set( "key", new int[]{ 1, 2, 3 } );

    SetOperation operation = ( SetOperation )getMessage().getOperation( 0 );
    JsonArray result = ( JsonArray )operation.getProperty( "key" );
    assertEquals( JsonValue.readFrom( "[1, 2, 3]" ), result );
  }

  @Test
  public void testCreatePropertyGetStyle() {
    clientObject.create( "rwt.widgets.Shell"  );
    clientObject.set( "style", new String[] { "PUSH", "BORDER" } );

    CreateOperation operation = ( CreateOperation )getMessage().getOperation( 0 );
    assertArrayEquals( new String[] { "PUSH", "BORDER" }, operation.getStyles() );
  }

  @Test
  public void testDestroy() {
    clientObject.destroy();

    DestroyOperation operation = ( DestroyOperation )getMessage().getOperation( 0 );
    assertEquals( shellId, operation.getTarget() );
  }

  @Test
  public void testAddListener() {
    clientObject.listen( "selection", true );
    clientObject.listen( "fake", true );

    ListenOperation operation = ( ListenOperation )getMessage().getOperation( 0 );
    assertEquals( shellId, operation.getTarget() );
    assertTrue( operation.listensTo( "selection" ) );
    assertTrue( operation.listensTo( "fake" ) );
  }

  @Test
  public void testRemoveListener() {
    clientObject.listen( "selection", false );
    clientObject.listen( "fake", false );
    clientObject.listen( "fake2", true );

    ListenOperation operation = ( ListenOperation )getMessage().getOperation( 0 );
    assertEquals( shellId, operation.getTarget() );
    assertFalse( operation.listensTo( "selection" ) );
    assertFalse( operation.listensTo( "fake" ) );
    assertTrue( operation.listensTo( "fake2" ) );
  }

  @Test
  public void testCall() {
    clientObject.call( "method", null );

    CallOperation operation = ( CallOperation )getMessage().getOperation( 0 );
    assertEquals( shellId, operation.getTarget() );
    assertEquals( "method", operation.getMethodName() );
  }

  @Test
  public void testCall_twice() {
    clientObject.call( "method", null );

    clientObject.call( "method2", new JsonObject().add( "key1", "a" ).add( "key2", 3 ) );

    CallOperation operation = ( CallOperation )getMessage().getOperation( 1 );
    assertEquals( shellId, operation.getTarget() );
    assertEquals( "method2", operation.getMethodName() );
    assertEquals( "a", operation.getProperty( "key1" ) );
    assertEquals( new Integer( 3 ), operation.getProperty( "key2" ) );
  }

  private Message getMessage() {
    ProtocolMessageWriter writer = ContextProvider.getProtocolWriter();
    return new Message( writer.createMessage() );
  }

}
