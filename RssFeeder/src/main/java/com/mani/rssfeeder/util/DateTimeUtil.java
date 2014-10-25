package com.mani.rssfeeder.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by maniselvaraj on 29/9/14.
 */
public class DateTimeUtil {

    public static long getTimeInMillis(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EE, dd MMM yyyy HH:mm:ss");
        try {
            Date date = dateFormat.parse(dateString);
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(TimeZone.getDefault());
            cal.setTimeInMillis(date.getTime());
            return cal.getTimeInMillis();
        } catch(Exception e) {
            return -1;
        }
    }


    public static String getFormattedString(long timeInMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd");
        Date date = new Date(timeInMillis);
        return dateFormat.format(date);
    }

}
