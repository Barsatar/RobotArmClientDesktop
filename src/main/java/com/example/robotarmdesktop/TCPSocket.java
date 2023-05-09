package com.example.robotarmdesktop;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class TCPSocket {
    private String ip = null;
    private Integer port = null;
    private Socket socket = null;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private Thread socketThread = null;
    private Thread sendDataListenerThread = null;
    private Thread receiveDataListenerThread = null;
    private Thread testConnectionThread = null;
    private boolean isWork = false;
    private ArrayList<byte[]> sendDataArray = new ArrayList<>();
    private ArrayList<byte[]> receiveDataArray = new ArrayList<>();

    public TCPSocket(String ip, Integer port) {
        this.ip = ip;
        this.port = port;

        this.createSocketThread();
    }

    public void run() {
        this.isWork = true;

        this.createSocket();
        this.createOutputStream();
        this.createInputStream();

        this.createSendDataListenerThread();
        this.createReceiveDataListenerThread();
        this.createTestConnectionThread();
    }

    public void createSocket() {
        try {
            this.socket = new Socket(this.ip, this.port);
        } catch (IOException e) {
            this.isWork = false;
        }
    }

    public void createOutputStream() {
        try {
            this.outputStream = this.socket.getOutputStream();
        } catch (IOException | NullPointerException e) {
            this.isWork = false;
        }
    }

    public void createInputStream() {
        try {
            this.inputStream = this.socket.getInputStream();
        } catch (IOException | NullPointerException e) {
            this.isWork = false;
        }
    }

    public void closeSocket() {
        try {
            if (this.socket != null) {
                this.socket.close();
            }
        } catch (IOException e) {
            this.isWork = true;
        }
    }

    public void closeInputStream() {
        try {
            if (this.inputStream != null) {
                this.inputStream.close();
            }
        } catch (IOException e) {
            this.isWork = true;
            this.closeSocket();
        }
    }

    public void closeOutputStream() {
        try {
            if (this.outputStream != null) {
                this.outputStream.close();
            }
        } catch (IOException e) {
            this.isWork = true;
            this.closeSocket();
        }
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

    public void createTestConnectionThread() {
        this.testConnectionThread = new Thread(this::testConnection);
        this.testConnectionThread.setPriority(Thread.NORM_PRIORITY);
        this.testConnectionThread.start();
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

            if (this.removeTestConnectionData(new String(this.trimData(data))).length() > 0) {
                this.receiveDataArray.add(data);
            }
        }
    }

    public void testConnection() {
        while (this.isWork) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            this.sendData("RA_TestConnection".getBytes(StandardCharsets.UTF_8));
        }
    }

    public synchronized void sendData(byte[] data) {
        try {
            this.outputStream.write(data);
            this.outputStream.flush();

            if (this.removeTestConnectionData(new String(this.trimData(data))).length() != 0) {
                System.out.println("RAD_TCPSocket_SendData: OK (" + new String(data) + ").");
            }
        } catch (IOException e) {
            this.isWork = false;
            this.closeOutputStream();
            this.closeInputStream();
            this.closeSocket();
        }
    }

    public synchronized byte[] receiveData() {
        int bufferSize = 1024;
        byte[] data = new byte[bufferSize];

        try {
            this.inputStream.read(data);

            if (this.removeTestConnectionData(new String(this.trimData(data))).length() != 0) {
                System.out.println("RAD_TCPSocket_ReceiveData: OK (" + new String(data) + ").");
            }
        } catch (IOException e) {
            this.isWork = false;
            this.closeOutputStream();
            this.closeInputStream();
            this.closeSocket();
        }

        return this.removeTestConnectionData(new String(this.trimData(data))).getBytes(StandardCharsets.UTF_8);
    }

    public  byte[] trimData(byte[] data) {
        int outDataSize = data.length;

        for (int i = data.length - 1; i >= 0; --i, --outDataSize) {
            if (data[i] != 0) {
                break;
            }
        }

        byte[] outData = new byte[outDataSize];

        for (int i = 0; i < outDataSize; ++i) {
            outData[i] = data[i];
        }

        return outData;
    }

    public String removeTestConnectionData(String data) {
        return data.replace("RA_TestConnection", "");
    }

    public boolean isNotNullSocket() {
        if (this.socket != null) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isClosed() {
        return this.socket.isClosed();
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