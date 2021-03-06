package io.nats.client;

import static io.nats.client.ConnectionImpl.DEFAULT_BUF_SIZE;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

public class ProtocolTest {

	@Rule
	public TestCasePrinterRule pr = new TestCasePrinterRule(System.out);

	final static String defaultConnect = "CONNECT {\"verbose\":false,\"pedantic\":false,\"ssl_required\":false,\"name\":\"\",\"lang\":\"java\",\"version\":\"0.3.0-SNAPSHOT\"}\r\n";
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMockServerIO() {
		try (TCPConnectionMock conn = new TCPConnectionMock())
		{
			conn.open("localhost", 2222, 200);
			assertTrue(conn.isConnected());

			OutputStream bw = conn.getWriteBufferedStream(
					ConnectionImpl.DEFAULT_STREAM_BUF_SIZE);
			assertNotNull(bw);

			BufferedReader br = conn.getBufferedInputStreamReader();
			assertNotNull (br);

			System.err.println("Reading from mock connection inputStream");
			String s = br.readLine().trim();
			System.err.println("<= " + s);

			assertEquals("INFO strings not equal.", TCPConnectionMock.defaultInfo.trim(), s);

			bw.write(defaultConnect.getBytes());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			fail(e1.getMessage());
		}
	}

	@Test
	public void testMockServerConnection() {
		try (TCPConnectionMock mock = new TCPConnectionMock())
		{
			ConnectionFactory cf = new ConnectionFactory();
			try (Connection c = cf.createConnection(mock)) {
				assertTrue(!c.isClosed());

				try (SyncSubscription sub = c.subscribeSync("foo")) {
					c.publish("foo", "Hello".getBytes());
					Message m = sub.nextMessage();
					System.err.println("Msg = " + m);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					c.close();
				}	
			} catch (IOException | TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				fail(e.getMessage());
			} finally {
				mock.close();
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Test
	public void testPingTimer() {
		try (TCPConnectionMock mock = new TCPConnectionMock())
		{
			ConnectionFactory cf = new ConnectionFactory();
			cf.setPingInterval(2000);
			try (Connection c = cf.createConnection(mock)) {
				assertTrue(!c.isClosed());
				try {Thread.sleep(5000); } catch (InterruptedException e) {}
			} catch (IOException | TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				fail(e.getMessage());
			} 
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Test
	public void testProcessErr() {
		byte[] argBufBase = new byte[DEFAULT_BUF_SIZE];
		ByteBuffer argBufStream = ByteBuffer.wrap(argBufBase);

		try (TCPConnectionMock mock = new TCPConnectionMock())
		{
			ConnectionFactory cf = new ConnectionFactory();
			try (ConnectionImpl c = cf.createConnection(mock)) {
				assertTrue(!c.isClosed());
				c.processErr(argBufStream);
				assertTrue(c.isClosed());
			} catch (IOException | TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				fail(e.getMessage());
			} 
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	@Test
	public void testVerbose() {
		try (TCPConnectionMock mock = new TCPConnectionMock())
		{
			ConnectionFactory cf = new ConnectionFactory();
			cf.setVerbose(true);
			try (ConnectionImpl c = cf.createConnection(mock)) {
//			try (ConnectionImpl c = cf.createConnection()) {
				assertTrue(!c.isClosed());
				SyncSubscription s = c.subscribeSync("foo");
				c.flush();
				c.close();
				assertTrue(c.isClosed());
			} catch (IOException | TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				fail(e.getMessage());
			} 
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			fail(e1.getMessage());			
		}
	}
	
	@Test
	public void testProcessErrStaleConnection() {
		byte[] argBufBase = new byte[DEFAULT_BUF_SIZE];
		ByteBuffer argBufStream = ByteBuffer.wrap(argBufBase);
		argBufStream.put(ConnectionImpl.STALE_CONNECTION.getBytes());
		try (TCPConnectionMock mock = new TCPConnectionMock())
		{
			ConnectionFactory cf = new ConnectionFactory();
			try (ConnectionImpl c = cf.createConnection(mock)) {
				assertTrue(!c.isClosed());
				c.processErr(argBufStream);

			} catch (IOException | TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				fail(e.getMessage());
			} 
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Test
	public void testGetConnectedId() {
		try (TCPConnectionMock mock = new TCPConnectionMock())
		{
			final String expectedId = "a1c9cf0c66c3ea102c600200d441ad8e";
			try (Connection c = new ConnectionFactory().createConnection(mock)) {
				assertTrue(!c.isClosed());
				assertEquals("Wrong server ID", c.getConnectedId(),expectedId);
				c.close();
				assertNull("Should have returned NULL", c.getConnectedId());
			} catch (IOException | TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				fail(e.getMessage());
			} 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testServerToClientPingPong() {
		try {Thread.sleep(500);} catch (InterruptedException e) {}
		try (TCPConnectionMock mock = new TCPConnectionMock())
		{
			try (Connection c = new ConnectionFactory().createConnection(mock)) {
				assertFalse(c.isClosed());
				Thread.sleep(500);
				mock.sendPing();
			} catch (IOException | TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				fail(e.getMessage());
			} 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test //(expected=ConnectionClosedException.class)
	public void testServerParseError() throws Exception {
		try (TCPConnectionMock mock = new TCPConnectionMock())
		{
			try (ConnectionImpl c = new ConnectionFactory().createConnection(mock)) {
				assertTrue(!c.isClosed());
				byte[] data = "Hello\r\n".getBytes();
				c.sendProto(data, data.length);
				System.err.println("Sent malformed control");
				Thread.sleep(100);
				assertTrue(c.isClosed());
			} catch (Exception e) {
				throw (e);
			} finally {
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
			throw(e);
		}
	}

	//	@Test
	public void testServerInfo() {
		try (TCPConnectionMock mock = new TCPConnectionMock())
		{
			//			final String expectedInfo = "INFO {\"server_id\":\"a1c9cf0c66c3ea102c600200d441ad8e\","
			//					+ "\"version\":\"0.7.2\",\"go\":\"go1.4.2\",\"host\":\"0.0.0.0\",\"port\":4222,"
			//					+ "\"auth_required\":false,\"ssl_required\":false,\"max_payload\":1048576}\r\n";

			try (ConnectionImpl c = new ConnectionFactory().createConnection(mock)) {
				assertTrue(!c.isClosed());

				String expected = TCPConnectionMock.defaultInfo;				
				ServerInfo info = c.getConnectedServerInfo();

				assertEquals("Wrong server INFO", expected, info);

			} catch (IOException | TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				fail(e.getMessage());
			} 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testSendConnectException() {
		try (TCPConnectionMock mock = new TCPConnectionMock())
		{
			mock.setBadWriter(true);
			try (ConnectionImpl c = new ConnectionFactory().createConnection(mock)) {
				fail("Shouldn't have connected.");
			} catch (IOException | TimeoutException e) {
				assertTrue(e instanceof IOException);
				assertEquals("Error sending connect protocol message", e.getMessage());
				System.err.println("Success.");
			} 
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testReadOpException() {
		try (TCPConnectionMock mock = new TCPConnectionMock())
		{
			mock.setBadReader(true);
			try (ConnectionImpl c = new ConnectionFactory().createConnection(mock)) {
				fail("Shouldn't have connected.");
			} catch (IOException | TimeoutException e) {
				String name = e.getClass().getName();
				assertTrue("Got " + name + " instead of ConnectionException", 
						e instanceof ConnectionException);
				assertEquals("nats: Connection read error.", e.getMessage());
				System.err.println("Success.");
			} 
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}
	
	@Test
	public void testConnectNullPong() {
		try (TCPConnectionMock mock = new TCPConnectionMock())
		{
			mock.setSendNullPong(true);
			try (ConnectionImpl c = new ConnectionFactory().createConnection(mock)) {
				fail("Shouldn't have connected.");
			} catch (IOException | TimeoutException e) {
				assertTrue(e instanceof IOException);
				assertTrue(e.getMessage().startsWith("nats: Unexpected protocol line:"));
				System.err.println("Success.");
			} 
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testErrOpConnectionEx() {
		try (TCPConnectionMock mock = new TCPConnectionMock())
		{
			mock.setSendGenericError(true);
			ConnectionFactory cf = new ConnectionFactory();
			try (ConnectionImpl c = new ConnectionFactory().createConnection(mock)) {
				fail("Shouldn't have connected.");
			} catch (IOException | TimeoutException e) {
				assertTrue(e instanceof ConnectionException);
				assertEquals("nats: 'Generic error message.'", e.getMessage());
				System.err.println("Success.");
			} 
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}

	@Test
	public void testErrOpReconnectEx() {
		// TODO fix this test
		try (TCPConnectionMock mock = new TCPConnectionMock())
		{
			ConnectionFactory cf = new ConnectionFactory();
			cf.setMaxReconnect(1);
//			boolean exThrown = false;
			try (ConnectionImpl c = cf.createConnection(mock)) {
				assertFalse(c.isClosed());
				c.setReconnectedEventHandler(new ReconnectedEventHandler() {
					@Override
					public void onReconnect(ConnectionEvent event) {
						System.err.println("Reconnected");
					}
				});
				Thread.sleep(500);
				mock.setSendGenericError(true);
				System.err.println("Restarting server");
				mock.bounce();
				System.err.println("Restarted server");
				while (c.isReconnecting()) {
					Thread.sleep(500);
				}
				assertTrue(c.isClosed());
				Exception e = c.getLastError();
//				assertTrue(e instanceof IOException);
//				assertTrue(e instanceof ConnectionException);
//				e.printStackTrace();
//				assertEquals("nats: 'Generic error message.'", e.getMessage());
//				System.err.println("Success.");

			} catch (IOException | TimeoutException e) {
//				exThrown = true;
				assertTrue(e instanceof ConnectionException);
				assertEquals("'Generic error message.'", e.getMessage());
				System.err.println("Success.");
			} finally {
			}
//			assertTrue(exThrown);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}

	@Test
	public void testErrOpAuthorization() {
		try (TCPConnectionMock mock = new TCPConnectionMock())
		{
			mock.setSendAuthorizationError(true);
			try (ConnectionImpl c = new ConnectionFactory().createConnection(mock)) {
				fail("Shouldn't have connected.");
			} catch (IOException | TimeoutException e) {
				assertTrue(e instanceof AuthorizationException);
				assertEquals("'nats: Authorization Failed'", e.getMessage());
				System.err.println("Success.");
			} 
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testReadOpNull() {
		try (TCPConnectionMock mock = new TCPConnectionMock())
		{
			mock.setCloseStream(true);
			try (ConnectionImpl c = new ConnectionFactory().createConnection(mock)) {
				fail("Shouldn't have connected.");
			} catch (IOException | TimeoutException e) {
				assertTrue(e instanceof IOException);
				assertEquals("nats: Connection read error.", e.getMessage());
				System.err.println("Success.");
			} 
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testNoInfoSent() {
		try (TCPConnectionMock mock = new TCPConnectionMock())
		{
			mock.setNoInfo(true);
			try (ConnectionImpl c = new ConnectionFactory().createConnection(mock)) {
				fail("Shouldn't have connected.");
			} catch (IOException | TimeoutException e) {
				assertTrue(e instanceof IOException);
				assertEquals("Protocol exception, INFO not received", e.getMessage());
				System.err.println("Success.");
			} 
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testTlsMismatchServer() {
		try (TCPConnectionMock mock = new TCPConnectionMock())
		{
			mock.setTlsRequired(true);
			try (ConnectionImpl c = new ConnectionFactory().createConnection(mock)) {
				fail("Shouldn't have connected.");
			} catch (IOException | TimeoutException e) {
				assertTrue(e instanceof ConnectionException);
				Throwable cause = e.getCause();
				assertNotNull("Exception cause should not be NULL", cause);
				assertTrue(cause instanceof SecureConnectionRequiredException);
				assertNotNull(cause.getMessage());
				assertEquals("A secure connection is required by the server.",
						cause.getMessage());
				System.err.println("Success.");			} 
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testTlsMismatchClient() {
		try (TCPConnectionMock mock = new TCPConnectionMock())
		{
			ConnectionFactory cf = new ConnectionFactory();
			cf.setSecure(true);
			try (ConnectionImpl c = cf.createConnection(mock)) {
				fail("Shouldn't have connected.");
			} catch (IOException | TimeoutException e) {
				assertTrue(e instanceof ConnectionException);
				Throwable cause = e.getCause();
				assertNotNull("Exception cause should not be NULL", cause);
				assertTrue(cause instanceof SecureConnectionWantedException);
				assertNotNull(cause.getMessage());
				assertEquals("A secure connection is required by the client.",
						cause.getMessage());
				System.err.println("Success.");
			} 
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

}
