package edu.rosehulman.keinslc.rhithmu;

import android.os.Parcel;
import android.os.Parcelable;

import com.alamkanak.weekview.WeekViewEvent;
import com.google.firebase.database.Exclude;

import java.util.Calendar;

import edu.rosehulman.keinslc.rhithmu.Utils.EventUtils;

/**
 * Created by keinslc on 1/15/2017.
 */

public class Event extends WeekViewEvent implements Parcelable {

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };
    private String key;
    private String description;
    private String invitees;
    private long mStartTimeInMilis;
    private long mEndTimeInMilis;


    public Event() {
        this(-1, "", "", "", "", Calendar.getInstance(), Calendar.getInstance());
    }

    public Event(long id, String name, String location, String description, String invitees, Calendar start, Calendar end) {
        this.description = description;
        this.invitees = invitees;
        start.setTimeInMillis(mStartTimeInMilis);
        end.setTimeInMillis(mEndTimeInMilis);
        setStartTime(start);
        setEndTime(end);
        setName(name);
        setId(id);
        setLocation(location);
    }

    protected Event(Parcel in) {
        description = in.readString();
        invitees = in.readString();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String mDescription) {
        this.description = mDescription;
    }

    public String getInvitees() {
        return invitees;
    }

    public void setInvitees(String mInvitees) {
        this.invitees = mInvitees;
    }

    public long getEndTimeInMilis() {
        return mEndTimeInMilis;
    }

    public void setEndTimeInMilis(long mEndTimeInMilis) {
        this.mEndTimeInMilis = mEndTimeInMilis;
        Calendar cal = getEndTime();
        cal.setTimeInMillis(mEndTimeInMilis);
        setEndTime(cal);
    }


    public long getStartTimeInMilis() {
        return mStartTimeInMilis;
    }

    public void setStartTimeInMilis(long mStartTimeInMilis) {
        this.mStartTimeInMilis = mStartTimeInMilis;
        Calendar cal = getStartTime();
        cal.setTimeInMillis(mStartTimeInMilis);
        setStartTime(cal);
    }

    @Exclude
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Exclude
    @Override
    public Calendar getEndTime() {
        return super.getEndTime();
    }

    @Override
    public void setEndTime(Calendar endTime) {
        super.setEndTime((Calendar) endTime.clone());
        mEndTimeInMilis = endTime.getTimeInMillis();
    }

    @Exclude
    @Override
    public Calendar getStartTime() {
        return super.getStartTime();
    }

    @Override
    public void setStartTime(Calendar startTime) {
        super.setStartTime((Calendar) startTime.clone());
        mStartTimeInMilis = startTime.getTimeInMillis();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(description);
        dest.writeString(invitees);
    }

    @Override
    public String toString() {
        return "Event{" +
                "mName='" + getName() + '\'' +
                "mStartTime='" + getStartTime().getTime().toString() + '\'' +
                "mEndTime='" + getEndTime().getTime().toString() + '\'' +
                "description='" + description + '\'' +
                ", invitees='" + invitees + '\'' +
                '}';
    }

    public String niceToStringNoName() {
        String output = "From " + EventUtils.getDateStringFromCalendar(getStartTime()) + ' ' + EventUtils.getTimeStringFromCalendar(getStartTime()) +
                "\nuntil " + EventUtils.getDateStringFromCalendar(getEndTime()) + ' ' + EventUtils.getTimeStringFromCalendar(getEndTime()) + '\n';
        if (!getLocation().isEmpty()) {
            output += "at " + getLocation() + '\n';
        }
        if (!getInvitees().isEmpty()) {
            output += "with " + getInvitees() + '\n';
        }
        if (!getDescription().isEmpty()) {
            output += getDescription();
        }
        return output;
    }
}
