package edu.rosehulman.keinslc.rhithmu;

import android.os.Parcel;
import android.os.Parcelable;

import com.alamkanak.weekview.WeekViewEvent;

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
    private String mDescription;
    private String mInvitees;

    public Event() {
        setId(-1);
    }

    public Event(long id, String name, String location, String description, String invitees, Calendar start, Calendar end) {
        super(id, name, location, start, end);
        mDescription = description;
        mInvitees = invitees;
    }

    protected Event(Parcel in) {
        mDescription = in.readString();
        mInvitees = in.readString();
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public String getInvitees() {
        return mInvitees;
    }

    public void setInvitees(String mInvitees) {
        this.mInvitees = mInvitees;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mDescription);
        dest.writeString(mInvitees);
    }

    @Override
    public String toString() {
        return "Event{" +
                "mName='" + getName() + '\'' +
                "mStartTime='" + getStartTime().getTime().toString() + '\'' +
                "mEndTime='" + getEndTime().getTime().toString() + '\'' +
                "mDescription='" + mDescription + '\'' +
                ", mInvitees='" + mInvitees + '\'' +
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
