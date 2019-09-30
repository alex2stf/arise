package com.arise.astox.net.servers;

import java.io.IOException;


// srw-rw---- at /var/run/sdp
public class BluetoothServer {
	private static final String RESPONSE = "Greetings from serverland";


	public static void main(String[] args) throws IOException {

//		java.util.UUID MY_UUID_SECURE =
//			java.util.UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
//		System.out.println(MY_UUID_SECURE.getMostSignificantBits());
//		// display local device address and name
//		LocalDevice localDevice = LocalDevice.getLocalDevice();
//		System.out.println("Address: " + localDevice.getBluetoothAddress());
//		System.out.println("Name: " + localDevice.getFriendlyName());
//
//		UUID uuid = new UUID(java.util.UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66").toString().replaceAll("-", ""), false);
//		String connectionString = "btspp://localhost:" + uuid + ";name=Sample SPP Server";
//
//		System.out.println(connectionString);
//
//		// open server url
//		StreamConnectionNotifier streamConnNotifier = (StreamConnectionNotifier) Connector.open(connectionString);
//
//
//		System.out.println("\nServer Started. Waiting for clients to connect...");
//		while (true) {
//			StreamConnection connection = streamConnNotifier.acceptAndOpen();
//
//
//
//			new Thread(new Runnable() {
//				@Override
//				public void run() {
//					RemoteDevice dev = null;
//					try {
//						dev = RemoteDevice.getRemoteDevice(connection);
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//					System.out.println("Remote device address: " + dev.getBluetoothAddress());
//					try {
//						System.out.println("Remote device name: " + dev.getFriendlyName(true));
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//
//
//
//					OutputStream outStream = null;
//					try {
//						outStream = connection.openOutputStream();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//
//
//				// read string from spp client
//					InputStream inStream = null;
//					try {
//						inStream = connection.openInputStream();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//
//					while (true) {
//
//						System.out.println("loop mf");
//						try {
//							StringBuilder sb = new StringBuilder();
//							int readed;
//							byte[] buf = new byte[1024];
//							inStream.read(buf);
//							sb.append(new String(buf, "UTF-8"));
//							System.out.println("readed " + sb.toString());
//
//							System.out.println("Message from mobile device: " + sb.toString());
//
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//
//
//					}
//				}
//			}).start();
//		}
//
////		AbstractServer server = new BluecoveServer();

	}
}