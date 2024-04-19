package com.pokertools;

import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import java.time.LocalDateTime;



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

    // args

    private String timeFrame;
    private double buyIn;
    private boolean showHours;


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

        this.timeFrame = timeFrame;
        this.buyIn = buyIn;
        this.showHours = showHours;

    }

    public void showStats(){
        // Prints out the stats
        String buyIn;
        Timestamp today;
        Timestamp month = Timestamp.valueOf(LocalDateTime.now().withDayOfMonth(1).withHour(4).withMinute(0).
                withSecond(0).withNano(0));
        Timestamp year = Timestamp.valueOf(LocalDateTime.now().withDayOfYear(1).withHour(4).withMinute(0).
                withSecond(0).withNano(0));

        // we need to account time when we go past 00:00
        if (LocalDateTime.now().getHour() < 10) {
            today = Timestamp.valueOf(LocalDateTime.now().withHour(4).withMinute(0).withSecond(0).minusDays(1));
        }else {
            today = Timestamp.valueOf(LocalDateTime.now().withHour(4).withMinute(0).withSecond(0));
        }


        if (this.buyIn > 0){buyIn = String.valueOf(this.buyIn) + "s:"; }else{buyIn = "";}

        switch (this.timeFrame){
            case "today":

                int todayPlayed = this.tourneysPlayed(this.playerName, today, this.buyIn);
                double chEV = this.avgEvChipsWon(this.playerName, today, this.buyIn);
                double netWon = this.netWon(this.playerName, today, this.buyIn);
                double evWon = this.evDealWon(todayPlayed, chEV, this.buyIn);

                System.out.printf(buyIn + "Today played: %s Chip Ev: %s Net Won: %s Ev Won: %s",
                        todayPlayed, chEV, netWon, evWon);
                break;
            case "month":


                System.out.println(buyIn + "This Month played: Chip Ev: Net Won: Ev Won:");
                break;
            case "year":
                System.out.println(buyIn + "This Year played: Chip Ev: Net Won: Ev Won:");
                break;
            case "a":
                System.out.println(buyIn + "All Time played: Chip Ev: Net Won: Ev Won:");
                break;
            default:
                System.out.println(buyIn + "Today played: Chip Ev: Net Won: Ev Won:");

        }
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
            String query = "SELECT id_player FROM player WHERE player_name = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, playerName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                pid = rs.getInt(1);
            }

        }
        catch (SQLException e) {
            e.printStackTrace();
    }
        close(conn);
        return pid;
    }

    public List<java.sql.Date[]> getDates(){
        Connection conn = connect();
        List<java.sql.Date[]> dates = new ArrayList<>();
        try {
            String query = "SELECT date_start, date_end FROM tourney_summary WHERE date_start > '2021-01-01'";
            ResultSet rs = conn.createStatement().executeQuery(query);
            while (rs.next()) {
                java.sql.Date dateStart = rs.getDate(1);
                java.sql.Date dateEnd = rs.getDate(2);
                dates.add(new java.sql.Date[]{dateStart, dateEnd});
            }

        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        close(conn);
        return dates;
    }

    public int tourneysPlayed(String playerName, Timestamp date, double buyin){
        Connection conn = connect();
        int cnt = 0;
        try {
            String query = "SELECT COUNT(tourney_summary.id_tourney) " +
                    "FROM tourney_summary INNER JOIN tourney_results " +
                    "ON tourney_summary.id_tourney = tourney_results.id_tourney " +
                    "WHERE amt_buyin = ? AND id_player_real = ? AND tourney_summary.date_start > ?";

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setDouble(1, buyin);
            pstmt.setInt(2, getPid(playerName));
            pstmt.setTimestamp(3, date);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                cnt = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        close(conn);
        return cnt;
    }

    public double getRakeback(String name, String date, double buyin){
        Connection conn = connect();
        double rakeback = 0;
        try {
            String query = "SELECT SUM(amt_fee) " +
                    "FROM tourney_summary " +
                    "WHERE amt_buyin = ? AND id_player_real = ? AND ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setDouble(1, buyin);
            pstmt.setInt(2, getPid(name));
            pstmt.setString(3, date);


            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                rakeback = (rs.getDouble(1) * 4 * 3.5) / 69;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        close(conn);
        return rakeback;

    }

    public double  avgEvChipsWon(String  name, Timestamp date, double buy){
        Connection conn = connect();
        double ev = 0;
        try {
            String query = "SELECT SUM(amt_expected_won)/COUNT(DISTINCT tourney_summary.id_tourney) " +
                    "FROM tourney_hand_player_statistics INNER JOIN tourney_summary " +
                    "ON tourney_hand_player_statistics.id_tourney = tourney_summary.id_tourney " +
                    "WHERE tourney_summary.amt_buyin = ? AND id_player_real = ? and tourney_summary.date_start > ?";

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setDouble(1, buy);
            pstmt.setInt(2, getPid(name));
            pstmt.setTimestamp(3, date);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                ev = rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        close(conn);
        return Math.round(ev * 100.0) / 100.0;
    }

    public double evChipsWon(double buy, String name, String date){
        Connection conn = connect();
        double ev = 0;
        try {
            String query = "SELECT SUM(amt_expected_won) " +
                    "FROM tourney_hand_player_statistics INNER JOIN tourney_summary " +
                    "ON tourney_hand_player_statistics.id_tourney = tourney_summary.id_tourney " +
                    "WHERE tourney_summary.amt_buyin = ? AND id_player_real = ? AND ?";

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setDouble(1, buy);
            pstmt.setInt(2, getPid(name));
            pstmt.setString(3, date);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                ev = rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        close(conn);
        return ev;
    }

    public double netWon(String name, Timestamp date, double buy){
        Connection conn = connect();
        double net = 0;
        try {
            String query = "SELECT SUM(tourney_results.amt_won - (tourney_summary.amt_buyin + amt_fee)) " +
                    "FROM tourney_summary INNER JOIN tourney_results " +
                    "ON tourney_results.id_tourney = tourney_summary.id_tourney " +
                    "WHERE tourney_summary.amt_buyin = ? AND id_player = ? AND tourney_summary.date_start > ?";

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setDouble(1, buy);
            pstmt.setInt(2, getPid(name));
            pstmt.setTimestamp(3, date);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                net = rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        close(conn);
        return net;
    }

    public double amtExpectedWonByTid(String name, java.sql.Date dateStart, int tid){
        Connection conn = connect();
        double amt = 0;
        try {
            String query = "SELECT SUM(amt_expected_won) " +
                    "FROM tourney_hand_player_statistics INNER JOIN tourney_summary " +
                    "ON tourney_hand_player_statistics.id_tourney = tourney_summary.id_tourney " +
                    "WHERE id_player_real = ? AND date_start = ? AND tourney_summary.id_tourney = ?";

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, getPid(name));
            pstmt.setDate(2, dateStart);
            pstmt.setInt(3, tid);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                amt = rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        close(conn);
        return amt;
    }

    public double showdownByTid(String name, java.sql.Date dateStart, int tid){
        Connection conn = connect();
        double amt = 0;
        try {
            String query = "SELECT SUM(amt_won) " +
                    "FROM tourney_results INNER JOIN tourney_summary " +
                    "ON tourney_results.id_tourney = tourney_summary.id_tourney " +
                    "WHERE id_player_real = ? AND date_start = ? AND tourney_summary.id_tourney = ? " +
                    "AND flg_showdown = 't'";

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, getPid(name));
            pstmt.setDate(2, dateStart);
            pstmt.setInt(3, tid);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                amt = rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        close(conn);
        return amt;
    }

    public double nonShowdownByTid(String name, java.sql.Date dateStart, int tid){
        Connection conn = connect();
        double amt = 0;
        try {
            String query = "SELECT SUM(amt_won) " +
                    "FROM tourney_results INNER JOIN tourney_summary " +
                    "ON tourney_results.id_tourney = tourney_summary.id_tourney " +
                    "WHERE id_player_real = ? AND date_start = ? AND tourney_summary.id_tourney = ? " +
                    "AND flg_showdown = 'f'";

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, getPid(name));
            pstmt.setDate(2, dateStart);
            pstmt.setInt(3, tid);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                amt = rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        close(conn);
        return amt;
    }

    public double amtChipsWonByTid(String name, java.sql.Date dateStart, int tid){
        Connection conn = connect();
        double amt = 0;
        try {
            String query = "SELECT SUM(amt_won) " +
                    "FROM tourney_results INNER JOIN tourney_summary " +
                    "ON tourney_results.id_tourney = tourney_summary.id_tourney " +
                    "WHERE id_player_real = ? AND date_start = ? AND tourney_summary.id_tourney = ?";

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, getPid(name));
            pstmt.setDate(2, dateStart);
            pstmt.setInt(3, tid);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                amt = rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        close(conn);
        return amt;
    }

    public ArrayList<Integer> getTids(java.sql.Date dateStart){
        Connection conn = connect();
        ArrayList<Integer> tids = new ArrayList<>();
        try {
            String query = "SELECT id_tourney " +
                    "FROM tourney_summary " +
                    "WHERE date_start = ? " +
                    "ORDER BY date_start ASC";

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setDate(1, dateStart);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tids.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        close(conn);
        return tids;
    }

    // ALGO FOR HOURS WORKED

    public ArrayList<java.sql.Date> getDates(java.sql.Date dateStart){
        Connection conn = connect();
        ArrayList<java.sql.Date> dates = new ArrayList<>();
        try {
            String query = "SELECT date_start, date_end " +
                    "FROM tourney_summary " +
                    "WHERE date_start > ? " +
                    "ORDER BY date_start ASC";

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setDate(1, dateStart);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                dates.add(rs.getDate(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        close(conn);
        return dates;
    }

    public ArrayList<java.sql.Timestamp> getDates(java.sql.Timestamp dateStart){
        Connection conn = connect();
        ArrayList<java.sql.Timestamp> dates = new ArrayList<>();
        try {
            String query = "SELECT date_start " +
                    "FROM tourney_summary " +
                    "WHERE date_start > ? " +
                    "ORDER BY date_start ASC";

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setTimestamp(1, dateStart);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                dates.add(rs.getTimestamp(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        close(conn);
        return dates;
    }

    public double evDealWon(int cnt_tourneys, double avgChEv, double buyin){
        double evWon = (((buyin * 2.742) / 1500) * avgChEv - (buyin * 0.086)) * cnt_tourneys;
        return Math.round(evWon * 100.0) / 100.0;
    }

    public int getPid() {
        return this.pid;
    }

    public JsonReader getJsonReader() {
        return jsonReader;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getUser() {
        return user;
    }

    public String getDbName() {
        return dbName;
    }

    public String getPassword() {
        return password;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getDelta() {
        return delta;
    }

    public double getBuyIn() {
        return buyIn;
    }

    public void setBuyIn(double buyIn) {
        this.buyIn = buyIn;
    }

    public boolean isShowHours() {
        return showHours;
    }

    public void setShowHours(boolean showHours) {
        this.showHours = showHours;
    }

    public String getTimeFrame() {
        return timeFrame;
    }

    public void setTimeFrame(String timeFrame) {
        this.timeFrame = timeFrame;
    }
}
