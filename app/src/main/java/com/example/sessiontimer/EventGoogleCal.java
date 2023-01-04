package com.example.sessiontimer;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class EventGoogleCal {

    private static ContentResolver cr;
    private String[] allCals;

    public EventGoogleCal(Context context) {
        cr = context.getContentResolver();








        //listAllAccounts(context);
        listAllCalendars();
        //showEvents(12);
        //long test = getTotalMins("Work Session",12);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -7);
        //long[] testarr = getLastWeekMins(calendar.getTime(),"Work Session",12);
        //createEvent(12, 0, 0);


    }

    public String[] getCalendarsArr() {
        return allCals;
    }

    public void createEvent(long calID, long startMillis, long endMillis, String evTitle) {

        /*
        long calID = 12;
        long startMillis = 0;
        long endMillis = 0;

        Calendar beginTime = Calendar.getInstance();
        beginTime.set(2021, 0, 29, 7, 30);
        startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.set(2021, 0, 29, 8, 45);
        endMillis = endTime.getTimeInMillis();
        */


        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startMillis);
        values.put(CalendarContract.Events.DTEND, endMillis);
        values.put(CalendarContract.Events.TITLE, evTitle);
        values.put(CalendarContract.Events.DESCRIPTION, "Group workout mit Felix");
        values.put(CalendarContract.Events.CALENDAR_ID, calID);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, "Europe/Berlin");
        Uri uriEv = cr.insert(CalendarContract.Events.CONTENT_URI, values);

        // get the event ID that is the last element in the Uri
        long eventID = Long.parseLong(uriEv.getLastPathSegment());


        Log.i("felix", "Event created: "+eventID);


        //Redirect to Calendar App
        Uri urii = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, 1088);
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(urii);
        //startActivity(intent);
    }

    /**
     * Returns the number of lorem ipsum
     * represented by this <tt>Lorem</tt> object.
     *
     * @param beginDate day in past TODO remove this
     * @return  Summary array with durations of last weeks work session
     *          in minutes including today.
     */
    public long[] getLastWeekMins(Date beginDate, String titleFilter, int calendarLocalId) {
        long[] lastWeekMins = new long[7];
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(beginDate);
        calendar.set(Calendar.HOUR_OF_DAY, 1);
        calendar.set(Calendar.MINUTE, 1);
        long startMills = calendar.getTimeInMillis();

        calendar.setTime(new Date());
        long endMills = calendar.getTimeInMillis();

        int dowToday = calendar.get(Calendar.DAY_OF_WEEK);

        ContentUris.appendId(builder, startMills);
        ContentUris.appendId(builder, endMills);
        Cursor eventCursor = cr.query(builder.build(), new String[]{CalendarContract.Instances.TITLE,
                        CalendarContract.Instances.BEGIN, CalendarContract.Instances.END, CalendarContract.Instances.DESCRIPTION, CalendarContract.Instances.EVENT_ID},
                CalendarContract.Instances.CALENDAR_ID + " = ?", new String[]{String.valueOf(calendarLocalId)}, null);

        while (eventCursor.moveToNext()) {
            final String title = eventCursor.getString(0);
            if (title.equals(titleFilter)) {
                final Date begin = new Date(eventCursor.getLong(1));
                final Date end = new Date(eventCursor.getLong(2));
                final String description = eventCursor.getString(3);
                final int identifier = eventCursor.getInt(4);
                //Log.i("felix", "id:" + identifier + " Title: " + title + "\tDescription: " + description + "\tBegin: " + begin + "\tEnd: " + end);

                calendar.setTime(begin);
                int dowDelta = dowToday - calendar.get(Calendar.DAY_OF_WEEK);
                if (dowDelta < 0) {
                    dowDelta = (dowDelta * -1)-1;
                    lastWeekMins[dowDelta] += (eventCursor.getLong(2) - eventCursor.getLong(1))/60000;
                } else {
                    lastWeekMins[6 - dowDelta] += (eventCursor.getLong(2) - eventCursor.getLong(1))/60000;
                }

            }
        }
        Log.i("felix", "lastweekmins array: "+ Arrays.toString(lastWeekMins));

        return lastWeekMins;
    }

    public long getTotalMins(String titleFilter, int calendarLocalId) {
        long totalMin = 0;
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();

        Calendar beginTimeE = Calendar.getInstance();
        beginTimeE.set(2021, Calendar.JANUARY, 29, 8, 0);
        long startMills = beginTimeE.getTimeInMillis();

        Calendar endTimeE = Calendar.getInstance();
        //endTimeE.set(2021, Calendar.JANUARY, 29, 20, 0);
        long endMills = endTimeE.getTimeInMillis();

        ContentUris.appendId(builder, startMills);
        ContentUris.appendId(builder, endMills);

        Cursor eventCursor = cr.query(builder.build(), new String[]{CalendarContract.Instances.TITLE,
                        CalendarContract.Instances.BEGIN, CalendarContract.Instances.END, CalendarContract.Instances.DESCRIPTION, CalendarContract.Instances.EVENT_ID},
                CalendarContract.Instances.CALENDAR_ID + " = ?", new String[]{String.valueOf(calendarLocalId)}, null);

        while (eventCursor.moveToNext()) {
            final String title = eventCursor.getString(0);
            if (title.equals(titleFilter)) {
                final Date begin = new Date(eventCursor.getLong(1));
                final Date end = new Date(eventCursor.getLong(2));
                final String description = eventCursor.getString(3);
                final int identifier = eventCursor.getInt(4);

                //Log.i("felix", "id:" + identifier + "Title: " + title + "\tDescription: " + description + "\tBegin: " + begin + "\tEnd: " + end);

                final long deltaT = eventCursor.getLong(2) - eventCursor.getLong(1);
                //Log.i("felix", "delta t: " + (deltaT / 60000));
                totalMin += (deltaT / 60000);
            }
        }
    Log.i("felix", "--------------------"+totalMin/60+"-h---------EOL------------------------");

        return totalMin;
    }

    private void showEvents(int calendarLocalId) {
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();

        Calendar beginTimeE = Calendar.getInstance();
        beginTimeE.set(2021, Calendar.JANUARY, 29, 8, 0);
        long startMills = beginTimeE.getTimeInMillis();

        Calendar endTimeE = Calendar.getInstance();
        endTimeE.set(2021, Calendar.JANUARY, 29, 20, 0);
        long endMills = endTimeE.getTimeInMillis();

        ContentUris.appendId(builder, startMills);
        ContentUris.appendId(builder, endMills);

        Cursor eventCursor = cr.query(builder.build(), new String[]{CalendarContract.Instances.TITLE,
                        CalendarContract.Instances.BEGIN, CalendarContract.Instances.END, CalendarContract.Instances.DESCRIPTION, CalendarContract.Instances.EVENT_ID},
                CalendarContract.Instances.CALENDAR_ID + " = ?", new String[]{String.valueOf(calendarLocalId)}, null);

        while (eventCursor.moveToNext()) {
            final String title = eventCursor.getString(0);
            final Date begin = new Date(eventCursor.getLong(1));
            final Date end = new Date(eventCursor.getLong(2));
            final String description = eventCursor.getString(3);
            final int identifier = eventCursor.getInt(4);

            //Log.i("felix", "id:"+identifier+"Title: " + title + "\tDescription: " + description + "\tBegin: " + begin + "\tEnd: " + end);
        }
    }

    /**
     * List all accounts currently signed in on the device.
     *
     * @param context
     * @return Array with all account names that could be found
     */
    public CharSequence[] listAllAccounts(Context context) {
        AccountManager manager = AccountManager.get(context);
        Account[] accounts = manager.getAccounts();
        //Account[] accounts = manager.getAccountsByType(null);
        List<String> username = new LinkedList<String>();
        Log.i("felix", "Found "+accounts.length+" accounts.");
        for (Account account : accounts) {
            username.add(account.name);
            Log.i("felix", "account: "+account.name+" - "+account.type);
        }
        return username.toArray(new CharSequence[username.size()]);
    }

    public void listAllCalendars() {
        List<String> callist = new ArrayList<String>();
        // Run query
        Cursor cur = null;

        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String selection;
        String[] selectionArgs;
        boolean onlyOwned = false;
        if (onlyOwned) {
            selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                    + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                    + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";
            selectionArgs = new String[] {"flexus11@web.de", "com.google", "flexus11@web.de"};
        } else {
            selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                    + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?))";
            selectionArgs = new String[] {"flexus11@web.de", "com.google"};
        }

        // Submit the query and get a Cursor object back.
        cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);



        // Use the cursor to step through the returned records
        while (cur.moveToNext()) {
            long calID = 0;
            String displayName = null;
            String accountName = null;
            String ownerName = null;

            // Get the field values
            calID = cur.getLong(PROJECTION_ID_INDEX);
            displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
            accountName = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX);
            ownerName = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX);

            // Do something with the values...

            Log.i("felix", "Field values: "+calID+" - "+displayName+" - "+accountName+ " - "+ownerName);
            callist.add(displayName);
        }
        allCals = callist.toArray(new String[0]);
    }

    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.
    public static final String[] EVENT_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;
}
