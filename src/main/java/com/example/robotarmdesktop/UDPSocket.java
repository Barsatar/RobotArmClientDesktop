package com.example.robotarmdesktop;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class UDPSocket {
    private String ip = null;
    private Integer port = null;
    private DatagramSocket socket = null;
    private InetAddress address = null;
    private Thread socketThread = null;
    private Thread sendDataListenerThread = null;
    private Thread receiveDataListenerThread = null;
    private boolean isWork = false;
    private ArrayList<byte[]> sendDataArray = new ArrayList<>();
    private ArrayList<byte[]> receiveDataArray = new ArrayList<>();

    public UDPSocket(String ip, Integer port) {
        this.ip = ip;
        this.port = port;

        this.createSocketThread();
    }

    public void run() {
        this.isWork = true;

        this.createSocket();

        this.createSendDataListenerThread();
        this.createReceiveDataListenerThread();
    }

    public void createSocket() {
        try {
            this.address = InetAddress.getByName(this.ip);
            this.socket = new DatagramSocket(this.port);
        } catch (IOException e) {
            this.isWork = false;
        }
    }

    public void closeSocket() {
        this.socket.close();
    }

    public void createSocketThread() {
        this.socketThread = new Thread(this::run);
        this.socketThread.setPriority(Thread.NORM_PRIORITY);
        this.socketThread.start();
    }

    public void createSendDataListenerThread() {
        this.sendDataListenerThread = new Thread(this::sendDataListener);
        this.sendDataListenerThread.setPriority(Thread.NORM_PRIORITY);
        this.sendDataListenerThread.start();
    }

    public void createReceiveDataListenerThread() {
        this.receiveDataListenerThread = new Thread(this::receiveDataListener);
        this.receiveDataListenerThread.setPriority(Thread.NORM_PRIORITY);
        this.receiveDataListenerThread.start();
    }

    public void sendDataListener() {
        while (this.isWork) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (this.sendDataArray.size() > 0) {
                this.sendData(this.sendDataArray.remove(0));
            }
        }
    }

    public void receiveDataListener() {
        byte[] data;

        while (this.isWork) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            data = this.receiveData();

            if (data.length > 0) {
                this.receiveDataArray.add(data);
            }
        }
    }

    public void sendData(byte[] data) {
        try {
            DatagramPacket packet = new DatagramPacket(data, data.length, this.address, this.port);
            this.socket.send(packet);

            System.out.println("RAD_UDPSocket_SendData: OK (" + new String(data) + ").");
        } catch (IOException e) {
            this.isWork = false;
            this.closeSocket();
        }
    }

    public synchronized byte[] receiveData() {
        int bufferSize = 40960;
        byte[] data = new byte[bufferSize];

        try {
            DatagramPacket packet = new DatagramPacket(new byte[bufferSize], bufferSize);
            this.socket.receive(packet);
            data = packet.getData();

            System.out.println("RAD_UDPSocket_ReceiveData: OK (" + data.length + ").");
        } catch (IOException e) {
            this.isWork = false;
            this.closeSocket();
        }

        return data;
    }

    public void sendDataArrayPushBack(byte[] data) {
        this.sendDataArray.add(data);
    }

    public byte[] receiveDataArrayPopFront() {
        byte[] data = new byte[0];

        if (this.receiveDataArray.size() > 0) {
            data = this.receiveDataArray.remove(0);
        }

        return data;
    }

    public int getReceiveDataArraySize() {
        return this.receiveDataArray.size();
    }
}
