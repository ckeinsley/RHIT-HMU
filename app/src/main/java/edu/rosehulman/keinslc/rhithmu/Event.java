package edu.rosehulman.keinslc.rhithmu;

import android.os.Parcel;
import android.os.Parcelable;

import com.alamkanak.weekview.WeekViewEvent;

import java.util.Calendar;

/**
 * Created by keinslc on 1/15/2017.
 */

public class Event extends WeekViewEvent implements Parcelable {

    private String mDescription;
    private String mInvitees;

    public Event(long id, String name, String location, String description, String invitees, Calendar start, Calendar end) {
        super(id, name, location, start, end);
        mDescription = description;
        mInvitees = invitees;
    }

    protected Event(Parcel in) {
        mDescription = in.readString();
        mInvitees = in.readString();
    }

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mDescription);
        dest.writeString(mInvitees);
    }
}
