package com.pokertools;

import org.json.JSONObject;
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



        Database db = new Database();
        System.out.println(today);
        System.out.println(today.toLocalDate());

        //System.out.println(db.getDates(java.sql.Date.valueOf(today.toLocalDate())));



        ArrayList<java.sql.Date> dates = db.getDates(java.sql.Date.valueOf(today.toLocalDate()));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        System.out.println(dates.size());
        for (java.sql.Date date : dates) {
            System.out.println(sdf.format(date));
        }


    }
}