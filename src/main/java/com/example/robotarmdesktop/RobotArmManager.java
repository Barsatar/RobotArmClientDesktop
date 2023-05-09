package com.example.robotarmdesktop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import javafx.scene.image.Image;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;

public class RobotArmManager implements Runnable {
    private SocketManager socketManager = null;
    private Thread robotArmManagerThread = null;
    private boolean isWork = false;
    private ArrayList<Module> modules = new ArrayList<>();
    private Boolean isRecording = false;
    private Boolean isFrame = false;
    private ArrayList<byte[]> framePartsArray = new ArrayList<>();
    private ArrayList<String> recordingCommands = new ArrayList<>();
    private ArrayList<Map> getModulesConfigurationArray = new ArrayList<>();
    private ArrayList<Map> getCurrentModulesConfigurationArray = new ArrayList<>();
    private ArrayList<Map> detectObjectsArray = new ArrayList<>();

    public RobotArmManager() {
        this.createRobotArmManagerThread();
    }

    @Override
    public void run() {
        this.isWork = true;

        while (this.isWork) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (this.socketManager != null && this.socketManager.getReceiveDataArrayUDPSocketSize() > 0) {
                this.framePartsArray.add(this.socketManager.receiveDataArrayPopFrontUDPSocket());
            }

            if (this.socketManager != null && this.socketManager.getReceiveDataArrayTCPSocketSize() > 0) {
                String data = this.socketManager.receiveDataArrayPopFrontTCPSocket();

                try {
                    JsonMapper mapper = new JsonMapper();
                    Map map = mapper.readValue(data, Map.class);

                    if (map.get("command_type").equals("answer") && map.get("command").equals("get_modules_configuration")) {
                        this.getModulesConfigurationArray.add(map);
                    }

                    if (map.get("command_type").equals("answer") && map.get("command").equals("get_current_modules_configuration")) {
                        this.getCurrentModulesConfigurationArray.add(map);
                    }

                    if (map.get("command_type").equals("answer") && map.get("command").equals("detect_objects")) {
                        this.detectObjectsArray.add(map);
                    }
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void createRobotArmManagerThread() {
        this.robotArmManagerThread = new Thread(this::run);
        this.robotArmManagerThread.setPriority(Thread.NORM_PRIORITY);
        this.robotArmManagerThread.start();
    }

    public void start(String ip, Integer port) {
        this.socketManager = new SocketManager(ip, port);

        Map robotArmConfiguration = this.getModulesConfiguration();
        Map currentModulesConfiguration = this.getCurrentModulesConfiguration();

        for (Object moduleIndex: robotArmConfiguration.keySet()) {
            Map configuration = (Map) robotArmConfiguration.get(moduleIndex);
            Map currentConfiguration = (Map) currentModulesConfiguration.get(moduleIndex);

            String id = configuration.get("id").toString();
            String type = configuration.get("type").toString();
            String minAngle = configuration.get("min_angle").toString();
            String maxAngle = configuration.get("max_angle").toString();
            String minSpeedLevel= configuration.get("min_speed_level").toString();
            String maxSpeedLevel= configuration.get("max_speed_level").toString();
            String currentAngle = currentConfiguration.get("angle").toString();
            String currentSpeedLevel = currentConfiguration.get("speed_level").toString();
            String angle = currentConfiguration.get("angle").toString();
            String speedLevel = currentConfiguration.get("speed_level").toString();

            this.modules.add(new Module(id, type, minAngle, maxAngle, minSpeedLevel, maxSpeedLevel, currentAngle, currentSpeedLevel, angle, speedLevel));
        }
    }

    public Image detectObjectsCommand(String threshold) {
        this.socketManager.sendDataArrayPushBackUDPSocket("RAD_UDPSocket_Init");
        this.framePartsArray.clear();

        String command = "{\"socket\":\"tcp\", \"command_type\":\"detection\", \"command\":\"detect_objects\", \"data\":{\"threshold\":" + threshold + "}}";
        this.socketManager.sendDataArrayPushBackTCPSocket(command);

        int countOfFrameParts = 0;
        int lastPartSize = 0;

        while (true) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (this.detectObjectsArray.size() > 0) {
                Map data = (Map) this.detectObjectsArray.remove(0).get("data");
                countOfFrameParts = (int) data.get("count_of_frame_parts");
                lastPartSize = (int) data.get("last_part_size");

                break;
            }
        }

        while (true) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (this.framePartsArray.size() == countOfFrameParts) {
                File file = new File("src/main/resources/images/detection.png");

                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(file);

                    for (byte[] framePart: this.framePartsArray) {
                        fileOutputStream.write(framePart);
                    }

                    fileOutputStream.close();

                    return new Image(file.toURI().toString());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void sendManualControlCommand() {
        String modulesConfiguration = "";

        for (Module module: this.modules) {
            modulesConfiguration += "\"" + module.id + "\": {\"id\":" + module.id + ", \"angle\":" + module.angle + ", \"speed_level\":" + module.speedLevel + "}, ";
        }

        String command = "{\"socket\":\"tcp\", \"command_type\":\"control\", \"command\":\"manual_control\", \"data\":{" + modulesConfiguration.substring(0, modulesConfiguration.length() - 2) + "}}";

        if (this.isRecording) {
            String recordingCommand = "{\"command\": \"manual_control\", \"data\":{" + modulesConfiguration.substring(0, modulesConfiguration.length() - 2) + "}}";
            this.recordingCommands.add(recordingCommand);
        }

        this.socketManager.sendDataArrayPushBackTCPSocket(command);
    }

    public void sendNeuralNetworkControlCommand(String x, String y, String z, String alpha, String theta, String psi) {
        String command = "{\"socket\":\"tcp\", \"command_type\":\"control\", \"command\":\"neural_network_control\", \"data\":{\"x\":" + x + ", \"y\":" + y + ", \"z\":" + z + ", \"alpha\":" + alpha + ", \"theta\":" + theta + ", \"psi\":" + psi + "}}";

        if (this.isRecording) {
            String recordingCommand = "{\"command\": \"neural_network_control\", \"data\":{\"x\":" + x + ", \"y\":" + y + ", \"z\":" + z + ", \"alpha\":" + alpha + ", \"theta\":" + theta + ", \"psi\":" + psi + "}}";
            this.recordingCommands.add(recordingCommand);
        }

        this.socketManager.sendDataArrayPushBackTCPSocket(command);
    }

    public Map getModulesConfiguration() {
        String command = "{\"socket\":\"tcp\", \"command_type\":\"request\", \"command\":\"get_modules_configuration\", \"data\":{}}";
        this.socketManager.sendDataArrayPushBackTCPSocket(command);

        while (true) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (this.getModulesConfigurationArray.size() > 0) {
                return (Map) this.getModulesConfigurationArray.remove(0).get("data");
            }
        }
    }

    public Map getCurrentModulesConfiguration() {
        String command = "{\"socket\":\"tcp\", \"command_type\":\"request\", \"command\":\"get_current_modules_configuration\", \"data\": {}}";
        this.socketManager.sendDataArrayPushBackTCPSocket(command);

        while (true) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (this.getCurrentModulesConfigurationArray.size() > 0) {
                return (Map) this.getCurrentModulesConfigurationArray.remove(0).get("data");
            }
        }
    }

    public void updateModule(String id, String angle, String speedLevel) {
        for (int i = 0; i < this.modules.size(); ++i) {
            if (id == this.modules.get(i).id) {
                this.modules.get(i).angle = angle;
                this.modules.get(i).speedLevel = speedLevel;

                break;
            }
        }
    }

    public ArrayList<Module> getModules() {
        return this.modules;
    }

    public void startRecordingScript() {
        this.isRecording = true;
        this.recordingCommands.clear();
    }

    public void finishRecordingScript() {
        this.isRecording = false;
    }

    public void runScript() {
        if (this.recordingCommands.size() > 0 && !this.isRecording) {
            String recordingCommands = "";

            for (int i = 0; i < this.recordingCommands.size(); ++i) {
                recordingCommands += "\"" + i + "\": " + this.recordingCommands.get(i) + ", ";
            }

            String command = "{\"socket\":\"tcp\", \"command_type\":\"control\", \"command\":\"script\", \"data\":{" + recordingCommands.substring(0, recordingCommands.length() - 2) + "}}";

            this.socketManager.sendDataArrayPushBackTCPSocket(command);
        }
    }

    public Boolean getTCPSocketState() {
        return this.socketManager.isTCPSocketWorkState();
    }

    public Boolean getUDPSocketState() {
        return this.socketManager.isUDPSocketWorkState();
    }
}