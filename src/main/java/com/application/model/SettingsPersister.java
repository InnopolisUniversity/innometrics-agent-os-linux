package com.application.model;

import com.application.utils.DialogsAndAlert;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SettingsPersister {
    private volatile JSONObject cache;
    private final Path p;

    public SettingsPersister(Path settingsfile){
        this.p = Paths.get("/opt/datacollectorlinux/lib/app/config.json");
        if (!Files.exists(settingsfile)){
            try {
                Files.createFile(this.p);
            } catch (IOException e) {
                //DialogsAndAlert.errorToDevTeam(e,"Config file does not exist or corrupted");
            }
        }
        this.cache = create(this.p);
    }

    private synchronized JSONObject create(Path settingsFile) {
        try {
            JSONObject json = null;
            if (Files.exists(settingsFile)) {
                FileReader reader = new FileReader(settingsFile.toString());
                JSONParser jsonParser = new JSONParser();
                json = (JSONObject) jsonParser.parse(reader);
            } else {
                Files.createFile(p);
                json = new JSONObject();
                Writer writer = new FileWriter(settingsFile.toString());
                json.writeJSONString(writer);
                writer.close();
            }
            if (!Files.isWritable(p)) {
                DialogsAndAlert.warning("Non-writable settings file");
                return new JSONObject();
            }
            return json;
        } catch (IOException | ParseException e) {
            return new JSONObject();
        }
    }
    private synchronized void commit() {
        if (this.p == null)
            return;

        try {
            Writer writer = new FileWriter(this.p.toString());
            this.cache.writeJSONString(writer);
            writer.close();
        } catch (IOException ex) {
            //DialogsAndAlert.errorToDevTeam(ex,"Committing to Json");
        }
    }

    public synchronized String get(String key) {
        return (String) this.cache.getOrDefault(key,"Null");
    }
    public synchronized void putSetting(String key, String value) {
        this.cache.put(key,value);
        //this.commit();
    }

    public synchronized void updateSettings(Model m){

        LocalDate today = LocalDate.now();
        today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        this.cache.put("tokenDate", today.toString());
        this.cache.put("token", m.getLoggedInSessionToken());
        this.cache.put("username", m.getUsername());
        this.cache.put("loginUsername", m.getLoginUsername());

        this.commit();
    }

    public void cleanup() {
        try {
            JSONObject json = new JSONObject();
            Writer writer = new FileWriter(p.toString());
            json.writeJSONString(writer);
            writer.close();
        }catch (IOException ex){
            DialogsAndAlert.Infomation("Logout not successful");
        }
    }

    public JSONObject getAllSettingsJson(){
        return this.cache;
    }
}
