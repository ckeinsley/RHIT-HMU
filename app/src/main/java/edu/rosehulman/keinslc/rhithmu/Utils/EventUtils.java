package edu.rosehulman.keinslc.rhithmu.Utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

import edu.rosehulman.keinslc.rhithmu.Event;

/**
 * Created by keinslc on 1/16/2017.
 */

public class EventUtils {

    private static final String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thur", "Fri", "Sat"};
    private static final String[] monthsOfYear = {"Jan", "Feb", "Mar", "Apr", "Mat", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"};
    private static final long ONE_HOUR_IN_MILLIS = 3600000;
    public static final long ONE_DAY_IN_MILLIS = 24 * ONE_HOUR_IN_MILLIS;
    public static final long ONE_WEEK_IN_MILLIS = 7 * ONE_DAY_IN_MILLIS;

    public static void createDefaultEvents(List<Event> events) {
        Calendar startTime = Calendar.getInstance();
        Calendar endTime = Calendar.getInstance();
        endTime.setTimeInMillis(endTime.getTimeInMillis() + 3600000);
        events.add(new Event(0, "Test Event", "Library", "This is a testable event", "John, Susie, Sally, Rob", startTime, endTime));
    }

    public static String getJSONifiedString(List<Event> events) {
        String json = new Gson().toJson(events);
        JSONArray jsonArray = new JSONArray();
        try {
            jsonArray = new JSONArray(json);
        } catch (JSONException e) {
            // Eat it
        }
        return jsonArray.toString();
    }

    public static List<Event> getEventListfromJSON(String jsonString) {
        // Lol why are there nulls, IDK
        jsonString = jsonString.replace("null", "");
        // Some GSON Magic
        Type type = new TypeToken<List<Event>>() {
        }.getType();
        List<Event> inpList = new Gson().fromJson(jsonString, type);
        //Magical Debugging Junk
//        for (int i = 0; i < inpList.size(); i++) {
//            Event x = inpList.get(i);
//            Log.d("EVENT : ", x.toString());
//        }
        return inpList;
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
        return 17L;
    }

    /**
     * Parses Schedule Lookup page calendars
     */
    public static List<Event> parseScheduleLookupEvent(String pathToParse) throws FileNotFoundException {
        List<Event> events = new ArrayList<>();
        File file = new File(pathToParse);
        Scanner scanner = new Scanner(file);
        if (!scanner.hasNextLine()) {
            scanner.close();
            return events;
        }
        scanner.nextLine();
        Event e;
        String active;
        String t1;
        String t2;
        while (scanner.hasNextLine() && scanner.nextLine().equals("BEGIN:VEVENT")) {
            e = new Event();
            active = scanner.nextLine();
            // I use substring for speed since we know exactly what the preceding string is
            active = active.substring(8);
            // summmary is both name and description
            e.setName(active);
            e.setDescription(active);
            active = scanner.nextLine();
            // prof is invitee
            e.setInvitees(active.substring(37));
            active = scanner.nextLine();
            // location is location
            e.setLocation(active.substring(9));
            // parse calendars
            t1 = scanner.nextLine();
            t2 = scanner.nextLine();
            if (t2.charAt(0) == 'T') {
                e.setStartTime(parseICSDate(t1 + t2));
                e.setEndTime(parseICSDate(scanner.nextLine() + scanner.nextLine()));
            } else {
                e.setStartTime(parseICSDate(t1));
                e.setEndTime(parseICSDate(t2));
            }
            // add our new event
            e.setId(Long.valueOf(15));
            events.add(e);
            //throw away unused lines
            scanner.nextLine();
            scanner.nextLine();
            scanner.nextLine();

        }

        scanner.close();
        return events;
    }

    /**
     * Turns ICS DTSTART/DTEND:YYYYMMDD T HHMMSS into a Calendar object
     *
     * @param date
     * @return
     */
    public static Calendar parseICSDate(String date) {
        Calendar calendar = Calendar.getInstance();
        //DTSTART and DTEND are different lengths so no hardcoded substring lengths
        String value = date.split(":")[1];
        //Log.d("parseICSString","input: " + date + " value: " + value);
        calendar.set(
                Integer.parseInt(value.substring(0, 4)),
                Integer.parseInt(value.substring(4, 6)) - 1, //I'll have you know I spent a long time debugging this
                Integer.parseInt(value.substring(6, 8)),
                Integer.parseInt(value.substring(9, 11)),
                Integer.parseInt(value.substring(11, 13)),
                Integer.parseInt(value.substring(13, 15)));

//        Log.d("value", value.substring(0,4) + " " + value.substring(4,6) + " " + value.substring(6,8) + " " + value.substring(9,11) + " " + value.substring(11,13) + " " +
//                value.substring(13,15));
//        Log.d("Calendar", calendar.get(Calendar.YEAR) + " " + calendar.get(Calendar.MONTH) + " " + calendar.get(Calendar.DATE) + " " + calendar.get(Calendar.HOUR) + " " + calendar.get(Calendar.MINUTE) + " " + calendar.get(Calendar.SECOND));
//        Log.d("Time millis", "" + calendar.getTimeInMillis());
        return calendar;
    }

    /**
     * Creates 2 weeks worth of hour events and then eliminates events where conflicts occur
     * hypothetical efficiency is O(N) where N = number of events
     *
     */
    public static List<Event> match(List<List<Event>> events) {
        List<Event> Conflicts = new ArrayList<>();
        for (List<Event> e : events) {
            Conflicts.addAll(e);
        }
        List<Event> Possibles = generateTwoWeeks();
        List<Event> toRemove;
        for (Event event : Conflicts) {
            toRemove = new ArrayList<>();
            for (Event pos : Possibles) {
                if (isConflict(event, pos)) {
                    toRemove.add(pos);
                }
            }
            Possibles.remove(toRemove);
        }
        return Possibles;
    }

    /**
     * Determines if two events conflict in two comparisons or less
     *
     * @param e1
     * @param e2
     * @return
     */
    public static boolean isConflict(Event e1, Event e2) {
        return !(e1.getEndTimeInMilis() <= e2.getStartTimeInMilis() || e2.getEndTimeInMilis() <= e1.getStartTimeInMilis());

//        if(e1.getStartTimeInMilis() < e2.getStartTimeInMilis()){
//            if(e1.getEndTimeInMilis() <= e2.getStartTimeInMilis()){
//                return false;
//            }
//            return true;
//        } else {
//            if (e2.getEndTimeInMilis() <= e1.getStartTimeInMilis()){
//                return false;
//            }
//            return true;
//        }
    }

    /**
     * Generates two weeks of 30 minutes events starting tomorrow at midnight
     *
     * @return
     */
    public static List<Event> generateTwoWeeks() {
        List<Event> events = new ArrayList<>();
        Calendar onTheHour = Calendar.getInstance();
        Calendar onTheHalf = Calendar.getInstance();
        //rather than deal with weird days we just directly alter time in milis  after setting to midnight
        onTheHour.set(onTheHour.get(Calendar.YEAR), onTheHour.get(Calendar.MONTH), onTheHour.get(Calendar.DATE), 0, 0, 0);
        onTheHour.setTimeInMillis(onTheHour.getTimeInMillis() + 24 * ONE_HOUR_IN_MILLIS);
        onTheHalf.set(onTheHour.get(Calendar.YEAR), onTheHour.get(Calendar.MONTH), onTheHour.get(Calendar.DATE), 0, 30, 0);
        Event e;
        for (int i = 0; i < 336; i++) {
            e = new Event();
            e.setName("Possible Meeting Time");
            e.setStartTimeInMilis(onTheHour.getTimeInMillis() + i * ONE_HOUR_IN_MILLIS);
            e.setEndTimeInMilis(onTheHalf.getTimeInMillis() + i * ONE_HOUR_IN_MILLIS);
            events.add(e);
            e = new Event();
            e.setName("Possible Meeting Time");
            e.setStartTimeInMilis(onTheHalf.getTimeInMillis() + i * ONE_HOUR_IN_MILLIS);
            e.setEndTimeInMilis(onTheHour.getTimeInMillis() + (i + 1) * ONE_HOUR_IN_MILLIS);
            events.add(e);
        }

        return events;
    }
}