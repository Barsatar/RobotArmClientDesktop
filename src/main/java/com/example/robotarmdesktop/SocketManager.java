package com.example.robotarmdesktop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;
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
            if (this.tcpSocket != null && this.tcpSocket.isNotNullSocket() && this.tcpSocket.isClosed()) {
                this.tcpSocket = new TCPSocket(this.ip, this.port);
            }

            if (this.tcpSocket != null) {
                while (this.sendDataArrayTCPSocket.size() > 0) {
                    this.tcpSocket.sendDataArrayPushBack(this.sendDataArrayTCPSocket.remove(0).getBytes(StandardCharsets.UTF_8));
                }

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                while (this.tcpSocket.getReceiveDataArraySize() > 0) {
                    this.receiveDataArrayTCPSocket.add(new String(this.tcpSocket.receiveDataArrayPopFront()));
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

    public String receiveDataArrayByCommandTCPSocket(String commandType, String command) {
        String data = "";
        JsonMapper mapper = new JsonMapper();

        for (int i = 0; i < this.receiveDataArrayTCPSocket.size(); ++i) {
            try {
                Map map = mapper.readValue(this.receiveDataArrayTCPSocket.get(i), Map.class);

                if (map.get("command_type").equals(commandType) && map.get("command").equals(command)) {
                    data = this.receiveDataArrayTCPSocket.remove(i);
                    break;
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        return data;
    }
}
