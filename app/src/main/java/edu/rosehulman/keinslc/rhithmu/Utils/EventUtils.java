package edu.rosehulman.keinslc.rhithmu.Utils;

import java.util.Calendar;
import java.util.List;

import edu.rosehulman.keinslc.rhithmu.Event;

/**
 * Created by keinslc on 1/16/2017.
 */

public class EventUtils {

    private static final String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thur", "Fri", "Sat"};
    private static final String[] monthsOfYear = {"Jan", "Feb", "Mar", "Apr", "Mat", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"};

    public static void createDefaultEvents(List<Event> events) {
        Calendar startTime = Calendar.getInstance();
        Calendar endTime = Calendar.getInstance();
        endTime.setTimeInMillis(endTime.getTimeInMillis() + 3600000);
        events.add(new Event(0, "Test Event", "Library", "This is a testable event", "John, Susie, Sally, Rob", startTime, endTime));
    }

    // Hilariously, days are 1 based and months are 0 based... Good job java calendar
    public static String getDateStringFromCalendar(Calendar calendar) {
        String output = "";
        output += (daysOfWeek[calendar.get(Calendar.DAY_OF_WEEK) - 1]);
        output += (", ");
        output += (monthsOfYear[calendar.get(Calendar.MONTH)]);
        output += (' ');
        output += (calendar.get(Calendar.DAY_OF_MONTH));
        output += (", ");
        output += (calendar.get(Calendar.YEAR));
        return output;
    }

    public static String getTimeStringFromCalendar(Calendar calendar) {
        String output = "";
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour == 0) {
            output += "12";
        } else {
            output += (hour > 12 ? "" + (hour - 12) : "" + hour);
        }
        output += ":";
        int minute = calendar.get(Calendar.MINUTE);
        if (minute < 10) {
            output += "0" + minute;
        } else {
            output += minute;
        }
        output += " ";
        output += (hour >= 12 ? "PM" : "AM");
        return output;
    }

    public static Long getNewId() {
        //TODO: Guarantee that no two IDs are the same
        return Long.valueOf(17);
    }
}