package com.pokertools;

import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class Database {

    private JsonReader jsonReader = new JsonReader();
    private String playerName;
    private String user;
    private String dbName;
    private String password;
    private String host;
    private String port;
    private String delta;
    private int pid;


    public Database() {
        this.jsonReader = new JsonReader();
        JSONObject a = jsonReader.readJsonFile("src/main/resources/config.json");
        this.playerName = a.get("P_NAME").toString();
        this.user = a.get("USER").toString();
        this.dbName = a.get("DB_NAME").toString();
        this.password = a.get("PASSWORD").toString();
        this.host = a.get("HOST").toString();
        this.port = a.get("PORT").toString();
        this.delta = a.get("DELTA").toString();
        this.pid = getPid(this.playerName);

    }

    public Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:postgresql://"
                    + this.host + ":" + this.port + "/" + this.dbName, this.user, this.password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    public void close(Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getPid(String playerName) {
        Connection conn = connect();
        int pid = 0;
        try {
            String query = "SELECT id_player FROM player WHERE player_name = " + playerName +  "LIMIT 1";
            pid = conn.createStatement().executeQuery(query).getInt(1);

        }
        catch (SQLException e) {
            e.printStackTrace();
    }
        close(conn);
        return pid;
    }

    public int getPid() {
        return this.pid;
    }


}
