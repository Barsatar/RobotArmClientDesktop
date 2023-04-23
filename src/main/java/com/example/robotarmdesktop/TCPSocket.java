package com.example.robotarmdesktop;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class TCPSocket implements Runnable {
    private Socket socket = null;
    private Thread socketThread = null;
    private Thread sendDataListenerThread = null;
    private Thread receiveDataListenerThread = null;
    private Thread testConnectionThread = null;
    private OutputStream outputStream = null;
    private InputStream inputStream = null;
    private String ip = "";
    private String port = "";
    private boolean errorStatus = false;
    private ArrayList<byte[]> sendDataArray = new ArrayList<>();
    private ArrayList<byte[]> receiveDataArray = new ArrayList<>();

    public TCPSocket(String ip, String port) {
        this.ip = ip;
        this.port = port;
        this.createSocketThread();
    }

    @Override
    public void run() {
        this.errorStatus = false;

        this.createSocket();
        if (this.errorStatus) {
            return;
        }

        this.createInputStream();
        if (this.errorStatus) {
            this.closeSocket();
            return;
        }

        this.createOutputStream();
        if (this.errorStatus) {
            this.closeInputStream();
            this.closeSocket();
            return;
        }

        this.createSendDataListenerThread();
        this.createReceiveDataListenerThread();
        this.createTestConnectionThread();

        while (true) {
            if (this.errorStatus) {
                try {
                    this.sendDataListenerThread.join();
                    this.receiveDataListenerThread.join();
                    this.testConnectionThread.join();
                } catch (InterruptedException e) {
                    System.out.println("RAD_TCPSocket_Run: Error(" + e + ").");
                }

                break;
            }
        }

        this.closeInputStream();
        this.closeOutputStream();
        this.closeSocket();
    }

    public void sendDataListener() {
        while (true) {
            if (this.sendDataArray.size() > 0) {
                this.sendData(this.sendDataArray.remove(0));
            }

            if (this.errorStatus) {
                break;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void receiveDataListener() {
        byte[] data;

        while (true) {
            data = this.receiveData();

            if (this.errorStatus) {
                break;
            }

            if (this.removeTestConnectionData(new String(this.trimData(data))).length() != 0) {
                this.receiveDataArray.add(data);
            }
        }
    }

    public void testConnection() {
        while (true) {
            this.sendData("RA_TestConnection".getBytes(StandardCharsets.UTF_8));

            if (this.errorStatus) {
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
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
            System.out.println("RAD_TCPSocket_SendData: Error(" + e + ").");
            this.errorStatus = true;
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
            System.out.println("RAD_TCPSocket_ReceiveData: Error(" + e + ").");
            this.errorStatus = true;
        }

        return data;
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

    public void createSocket() {
        try {
            this.socket = new Socket(this.ip, Integer.parseInt(this.port));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.out.println("RAD_TCPSocket_CreateSocket: Error(" + e + ").");
            this.errorStatus = true;
        }
    }

    public void createInputStream() {
        try {
            this.inputStream = this.socket.getInputStream();
        } catch (IOException e) {
            System.out.println("RAD_TCPSocket_CreateInputStream: Error(" + e + ").");
            this.errorStatus = true;
        }
    }

    public void createOutputStream() {
        try {
            this.outputStream = this.socket.getOutputStream();
        } catch (IOException e) {
            System.out.println("RAD_TCPSocket_CreateOutputStream: Error(" + e + ").");
            this.errorStatus = true;
        }
    }

    public void closeSocket() {
        try {
            this.socket.close();
        } catch (IOException e) {
            System.out.println("RAD_TCPSocket_CloseSocket: Error(" + e + ").");
            this.errorStatus = true;
        }
    }

    public void closeInputStream() {
        try {
            this.inputStream.close();
        } catch (IOException e) {
            System.out.println("RAD_TCPSocket_CloseInputStream: Error(" + e + ").");
            this.errorStatus = true;
        }
    }

    public void closeOutputStream() {
        try {
            this.outputStream.close();
        } catch (IOException e) {
            System.out.println("RAD_TCPSocket_CloseOutputStream: Error(" + e + ").");
            this.errorStatus = true;
        }
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
}