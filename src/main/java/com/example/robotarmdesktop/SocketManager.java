package com.example.robotarmdesktop;

public class SocketManager implements Runnable {
    private TCPSocket tcpSocket = null;
    private Thread socketManagerThread = null;
    private String ip = "";
    private String port = "";

    public SocketManager() {
        this.createSocketManagerThread();
    }

    @Override
    public void run() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        while (true) {
            if (this.tcpSocket != null && this.tcpSocket.isNotNullSocket() && this.tcpSocket.isClosed()) {
                this.createTCPSocket();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void createSocketManagerThread() {
        this.socketManagerThread = new Thread(this::run);
        this.socketManagerThread.setPriority(Thread.NORM_PRIORITY);
        this.socketManagerThread.start();
    }

    public void createTCPSocket() {
        this.tcpSocket = new TCPSocket(this.ip, this.port);
    }

    public void setConnectionData(String ip, String port) {
        this.ip = ip;
        this.port = port;
    }

    public void closeTCPSocket() {
        this.tcpSocket.closeInputStream();
        this.tcpSocket.closeOutputStream();
        this.tcpSocket.closeSocket();
    }
}
