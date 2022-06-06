package com.arise.astox.net.servers;

import com.arise.core.tools.Mole;
import com.arise.core.tools.ThreadUtil;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import java.io.IOException;
import java.util.Vector;

@Deprecated
public class BluetoothUtil {

    private static final Mole log = Mole.getInstance(BluetoothUtil.class);




    private static class BluetoothScanner implements Runnable {

        private final String[] names;
        private final String[] uids;

        private Vector<RemoteDevice> discoveredDevices = new Vector<>();

        private BluetoothScanner(String[] names, String[] uids){
            this.names = names;
            this.uids = uids;
        }


        void checkDevice(RemoteDevice device){
            String name;
            try {
                name = device.getFriendlyName(true);
            } catch (IOException e) {
                name = device.getBluetoothAddress();
            }

            if (uids != null && uids.length > 0){
                for (String s: uids){
                    if (name.indexOf(s) > -1){
                        discoveredDevices.add(device);
                        System.out.println("Adding device " + name);
                        return;
                    }
                }
                System.out.println("Ignore " + name);
            } else {
                System.out.println("Adding device " + name);
            }
        }


        @Override
        public void run() {
            final Object lock = new Object();
            LocalDevice local;
            try {
                local = LocalDevice.getLocalDevice();

            } catch (BluetoothStateException e) {
                e.printStackTrace();
                return;
            }
            log.info("This device's name: " + local.getFriendlyName());
            DiscoveryAgent agent = local.getDiscoveryAgent();



            DiscoveryListener deviceDiscoveryListener = new DiscoveryListener() {
                @Override
                public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass deviceClass) {
                    checkDevice(remoteDevice);
                }

                @Override
                public void servicesDiscovered(int i, ServiceRecord[] serviceRecords) {

                }

                @Override
                public void serviceSearchCompleted(int i, int i1) {

                }

                @Override
                public void inquiryCompleted(int i) {
                    synchronized (lock){
                        lock.notify();
                    }
                }
            };


            synchronized (lock){
                try {
                    agent.startInquiry(DiscoveryAgent.GIAC, deviceDiscoveryListener);
                } catch (BluetoothStateException e) {
                    e.printStackTrace();
                }
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (discoveredDevices.isEmpty()){
                return;
            }

            DiscoveryListener serviceDiscoveryListener = new DiscoveryListener() {
                @Override
                public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass deviceClass) {

                }

                @Override
                public void servicesDiscovered(int x, ServiceRecord[] serviceRecords) {
                    for (int i = 0; i < serviceRecords.length; i++) {
                        System.out.println("Service " + i);
                        String url = serviceRecords[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
                        if (url == null) {
                            continue;
                        }
                        System.out.println("Connection url " + url);
    //                    DataElement serviceName = services[i].getAttributeValue(0x0100);
    //                    if (serviceName != null) {
    //                        System.out.println("service " + serviceName.getValue() + " found " + url);
    //                    } else {
    //                        System.out.println("service found " + url);
    //                    }
    //
    //                    if(serviceName.getValue().equals("OBEX Object Push")){
    //                        sendMessageToDevice(url);
    //                    }
                    }
                }

                @Override
                public void serviceSearchCompleted(int i, int i1) {

                }

                @Override
                public void inquiryCompleted(int i) {
                    synchronized (lock){
                        lock.notify();
                    }
                }
            };


            int[] attrIDs = new int[]{0X0100};

            UUID buids[] = new UUID[uids.length];
            for (int i = 0; i < buids.length; i++){
                buids[i] = new UUID(java.util.UUID.fromString(uids[i]).toString().replaceAll("-", ""), false );
            }
//            UUID uuid = new UUID(java.util.UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66").toString().replaceAll("-", ""), false);

            for (RemoteDevice device: discoveredDevices){
                synchronized (lock){
                    try {
                        log.info("Search in " + device.getFriendlyName(false));
                    } catch (IOException e) {
                        log.info("Search in " + device.getBluetoothAddress());
                    }
                    try {
                        agent.searchServices(attrIDs, buids, device, serviceDiscoveryListener);
                    } catch (BluetoothStateException e) {
                        e.printStackTrace();
                    }
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }


        }
    }
}
