package com.company;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class JsonSettings {
    private static final Logger log = LoggerFactory.getLogger(JsonSettings.class);

    public String userName;
    public String password;
    public String host;
    public String port;
    public String sid;
    public String driver;
    public String defaultSysPassword;
    public String defaultUserName;
    public String defaultPassword;
    public String queueTable;
    public String queueName;
    public Integer sendCount;
    public Integer threadsCount;
    public Integer threadLatency;

    public JsonSettings() {
        // default settings
        this.userName = "jmsuser";
        this.password = "jmsuser";
        this.host = "localhost";
        this.port = "1521";
        this.sid = "xe";
        this.driver = "thin";
        this.defaultSysPassword = "123";
        this.defaultUserName = "jmsuser";
        this.defaultPassword = "jmsuser";
        this.queueTable = "sample_aqtbl";
        this.queueName = "sample_aq";
        this.sendCount = 200;
        this.threadsCount = 1;
        this.threadLatency = 100;
    }

    private static JsonSettings setDefaultValues(JsonSettings settings) {
        JsonSettings defaultSettings = new JsonSettings();
        if (settings.userName == null)              settings.userName = defaultSettings.userName;
        if (settings.password == null)              settings.password = defaultSettings.password;
        if (settings.host == null)                  settings.host = defaultSettings.host;
        if (settings.port == null)                  settings.port = defaultSettings.port;
        if (settings.sid == null)                   settings.sid = defaultSettings.sid;
        if (settings.driver == null)                settings.driver = defaultSettings.driver;
        if (settings.defaultSysPassword == null)    settings.defaultSysPassword = defaultSettings.defaultSysPassword;
        if (settings.defaultUserName == null)       settings.defaultUserName = defaultSettings.defaultUserName;
        if (settings.defaultPassword == null)       settings.defaultPassword = defaultSettings.defaultPassword;
        if (settings.queueTable == null)            settings.queueTable = defaultSettings.queueTable;
        if (settings.queueName == null)             settings.queueName = defaultSettings.queueName;
        if (settings.sendCount == null)             settings.sendCount = defaultSettings.sendCount;
        if (settings.threadsCount == null)           settings.threadsCount = defaultSettings.threadsCount;
        if (settings.threadLatency == null)         settings.threadLatency = defaultSettings.threadLatency;
        return settings;
    }

    public static JsonSettings loadSettings(String settingsFilePath) {
        JsonSettings settings = new JsonSettings();
        File settingsFile = new File(settingsFilePath);
        if (settingsFile.exists() && !settingsFile.isDirectory()) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (BufferedReader br = new BufferedReader(new FileReader(settingsFilePath))) {
                settings = gson.fromJson(br, JsonSettings.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            log.info("File is not exist: " + settingsFile + ". It will be created.");
            saveSettings(settings, settingsFilePath);
        }
        return setDefaultValues(settings);
    }

    public static void saveSettings(JsonSettings settings, String settingsFilePath) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(settings);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(settingsFilePath))) {
            writer.write(jsonString);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
