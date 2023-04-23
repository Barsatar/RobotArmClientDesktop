package com.example.robotarmdesktop;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesManager {
    public String getValue(String properties_file_name, String key) {
        Properties properties = new Properties();

        try (FileInputStream in = new FileInputStream("src/main/resources/" + properties_file_name)) {
            properties.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return properties.getProperty(key);
    }

    public void setValue(String properties_file_name, String key, String value) {
        Properties properties = new Properties();

        try (FileInputStream in = new FileInputStream("src/main/resources/" + properties_file_name)) {
            properties.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (FileOutputStream out = new FileOutputStream("src/main/resources/" + properties_file_name)) {
            properties.setProperty(key, value);
            properties.store(out, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}