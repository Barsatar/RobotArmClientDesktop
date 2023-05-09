package com.example.robotarmdesktop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

public class SocketManager implements Runnable {
    private String ip = null;
    private Integer port = null;
    private TCPSocket tcpSocket = null;
    private UDPSocket udpSocket = null;
    private Thread socketManagerThread = null;
    private boolean isWork = false;
    private final ArrayList<String> sendDataArrayTCPSocket = new ArrayList<>();
    private final ArrayList<String> receiveDataArrayTCPSocket = new ArrayList<>();
    private final ArrayList<String> sendDataArrayUDPSocket = new ArrayList<>();
    private final ArrayList<byte[]> receiveDataArrayUDPSocket = new ArrayList<>();

    public SocketManager(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
        this.tcpSocket = new TCPSocket(this.ip, this.port);
        this.udpSocket = new UDPSocket(this.ip, this.port);

        this.createSocketManagerThread();
    }

    @Override
    public void run() {
        this.isWork = true;

        while (isWork) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            //if (this.tcpSocket != null && this.tcpSocket.isNotNullSocket() && this.tcpSocket.isClosed()) {
            //     this.tcpSocket = new TCPSocket(this.ip, this.port);
            //}

            if (this.tcpSocket != null) {
                while (this.sendDataArrayTCPSocket.size() > 0) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    this.tcpSocket.sendDataArrayPushBack(this.sendDataArrayTCPSocket.remove(0).getBytes(StandardCharsets.UTF_8));
                }

                while (this.tcpSocket.getReceiveDataArraySize() > 0) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    this.receiveDataArrayTCPSocket.add(new String(this.tcpSocket.receiveDataArrayPopFront()));
                }
            }

            if (this.udpSocket != null) {
                while (this.sendDataArrayUDPSocket.size() > 0) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    this.udpSocket.sendDataArrayPushBack(this.sendDataArrayUDPSocket.remove(0).getBytes(StandardCharsets.UTF_8));
                }

                while (this.udpSocket.getReceiveDataArraySize() > 0) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    this.receiveDataArrayUDPSocket.add(this.udpSocket.receiveDataArrayPopFront());
                }
            }
        }
    }

    public void createSocketManagerThread() {
        this.socketManagerThread = new Thread(this::run);
        this.socketManagerThread.setPriority(Thread.NORM_PRIORITY);
        this.socketManagerThread.start();
    }

    public void sendDataArrayPushBackTCPSocket(String data) {
        this.sendDataArrayTCPSocket.add(data);
    }

    public String receiveDataArrayPopFrontTCPSocket() {
        String data = "";

        if (this.receiveDataArrayTCPSocket.size() > 0) {
            data = this.receiveDataArrayTCPSocket.remove(0);
        }

        return data;
    }

    public void sendDataArrayPushBackUDPSocket(String data) {
        this.sendDataArrayUDPSocket.add(data);
    }

    public byte[] receiveDataArrayPopFrontUDPSocket() {
        int bufferSize = 60000;
        byte[] data = new byte[bufferSize];

        if (this.receiveDataArrayUDPSocket.size() > 0) {
            data = this.receiveDataArrayUDPSocket.remove(0);
        }

        return data;
    }

    public Boolean isTCPSocketWorkState() {
        if ((this.tcpSocket == null) || (this.tcpSocket != null && this.tcpSocket.isNotNullSocket() && this.tcpSocket.isClosed())) {
            return false;
        }

        return true;
    }

    public Boolean isUDPSocketWorkState() {
        if (this.udpSocket == null) {
            return false;
        }

        return true;
    }

    public int getReceiveDataArrayUDPSocketSize() {
        return this.receiveDataArrayUDPSocket.size();
    }

    public int getReceiveDataArrayTCPSocketSize() {
        return this.receiveDataArrayTCPSocket.size();
    }
}
