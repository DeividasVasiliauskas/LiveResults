package com.pokertools;

import org.json.JSONObject;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.text.SimpleDateFormat;

public class Main {
    public static void main(String[] args) {
        Database db = new Database();

        parseArgs(args, db);

    }

    public static void parseArgs(String[] args, Database db){

        for (int i = 0; i < args.length; i++){

            switch (args[i]){
                // if we find a proper flag, next argument is the value
                // Check for out of bounds

                case "-t":
                    try {
                        parseTimeFrame(args[i+1], db);
                    }
                    catch (ArrayIndexOutOfBoundsException e){
                        System.out.println("No value provided for flag -t");
                        parseTimeFrame("default", db);
                    }
                    break;
                case "-b":
                    try{
                        parseBuyin(args[i+1], db);
                    } catch (ArrayIndexOutOfBoundsException e){
                        parseBuyin("0", db);
                    }
                    break;
                case "-h":
                    db.setShowHours(true);
                    break;
                default:
                    parseTimeFrame("today", db);
                    parseBuyin("0", db);
                    db.setShowHours(false);

            }
        }
    }

    public static void parseTimeFrame(String timeFrame, Database db){
        switch (timeFrame){
            case "today":
            case  "month":
            case "year":
            case "a":
                db.setTimeFrame(timeFrame);
                break;
            default:
                db.setTimeFrame("today");
        }
    }

    public static void parseBuyin(String buyin, Database db){
        double amtBuy = 0;

        try {
            amtBuy = Double.parseDouble(buyin);
        } catch (NumberFormatException e) {
            amtBuy = 0;
        }
        if (amtBuy > 0){
            db.setBuyIn(amtBuy);
        } else {
            db.setBuyIn(0);
        }
    }

}