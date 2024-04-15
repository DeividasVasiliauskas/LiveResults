package com.pokertools;

import org.json.JSONObject;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;

public class Main {
    public static void main(String[] args) {

        JsonReader jsonReader = new JsonReader();
        JSONObject config = jsonReader.readJsonFile("src/main/resources/config.json");

        List<TimeCalculator.TimeInterval> intervals = new ArrayList<>(); // load with data from database (date start, date end)

        int delta = config.getInt("DELTA");

        double RAKE = 0.35;
        double VIPBONUS = 3.5;

        LocalDateTime today = LocalDateTime.now().withHour(4).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime month = LocalDateTime.now().withDayOfMonth(1).withHour(4).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime year = LocalDateTime.now().withDayOfYear(1).withHour(4).withMinute(0).withSecond(0).withNano(0);

        Timestamp todayTimestamp = Timestamp.valueOf(today);

        Database db = new Database();

        for (int i = 0; i < 1000; i++){

            String name = db.getPlayerName();
            double buyIn = 0.93;

            int cntTourneys = db.cntTodayPlayed(name, todayTimestamp, buyIn);
            double chEv = db.avgEvChipsWon(name, todayTimestamp, buyIn);
            double netWon = db.netWon(name, todayTimestamp, buyIn);
            double evDealWon = db.evDealWon(cntTourneys, chEv, buyIn);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();

            }
            System.out.printf("Today Played: %s Today ChEV: %s NET Won: %s EV Won: %s ",
                    cntTourneys, chEv, netWon, evDealWon);
        }


    }
}