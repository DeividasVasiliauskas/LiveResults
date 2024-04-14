package com.pokertools;

import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonReader {

    public JSONObject readJsonFile(String filename) {
        JSONObject jsonObject = null;
        try {
            String content = new String(Files.readAllBytes(Paths.get(filename)));
            return new JSONObject(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
