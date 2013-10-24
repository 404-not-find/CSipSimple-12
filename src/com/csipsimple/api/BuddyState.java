/**
 * Copyright (C) 2013 Duzy Chan <code@duzy.info>
 * This file is part of CSipSimple.
 *
 *  CSipSimple is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  If you own a pjsip commercial license you can also redistribute it
 *  and/or modify it under the terms of the GNU Lesser General Public License
 *  as an android library.
 *
 *  CSipSimple is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CSipSimple.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  This file and this file only is also released under Apache license as an API file
 */

package com.csipsimple.api;

import android.os.Parcel;
import android.os.Parcelable;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;

/**
 * Represents state of the buddy.
 */
public class BuddyState implements Parcelable 
{
    public static final String BUDDY_ID = "buddy_id";
    public static final String ID = "id";
    public static final String URI = "uri";
    public static final String CONTACT = "contact";
    public static final String STATUS = "status";
    public static final String STATUS_TEXT = "status_text";
    public static final String MONITOR_PRESENCE = "monitor_presence";
    public static final String SUB_STATE = "sub_state";
    public static final String SUB_STATE_NAME = "sub_state_name";
    public static final String SUB_TERM_CODE = "sub_term_code";
    public static final String SUB_TERM_REASON = "sub_term_reason";

    public int		id;
    public String	uri;
    public String 	contact;
    public int		status;
    public String	status_text;
    public boolean	monitor_pres;
    public int		sub_state;
    public String	sub_state_name;
    public int		sub_term_code;
    public String 	sub_term_reason;
    //pjrpid_element 	rpid;
    //pjsip_pres_status	pres_status;

    /**
     * Constructor for a buddy object <br/>
     */
    public BuddyState() {
        // Nothing to do in default constructor
    }

    /**
     * Construct from parcelable <br/>
     * Only used by {@link #CREATOR}
     * 
     * @param in parcelable to build from
     */
    private BuddyState(Parcel in) {
        id = in.readInt();
	uri = in.readString();
	contact = in.readString();
	status = in.readInt();
	status_text = in.readString();
	monitor_pres = (in.readInt() == 1);
	sub_state = in.readInt();
	sub_state_name = in.readString();
	sub_term_code = in.readInt();
	sub_term_reason = in.readString();
    }

    /**
     * Parcelable creator. So that it can be passed as an argument of the aidl
     * interface
     */
    public static final Parcelable.Creator<BuddyState> CREATOR = new Parcelable.Creator<BuddyState>() {
        public BuddyState createFromParcel(Parcel in) {
            return new BuddyState(in);
        }

        public BuddyState[] newArray(int size) {
            return new BuddyState[size];
        }
    };

    /**
     * @see Parcelable#describeContents()
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * @see Parcelable#writeToParcel(Parcel, int)
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(uri);
        dest.writeString(contact);
        dest.writeInt(status);
        dest.writeString(status_text);
        dest.writeInt(monitor_pres ? 1 : 0);
        dest.writeInt(sub_state);
        dest.writeString(sub_state_name);
        dest.writeInt(sub_term_code);
        dest.writeString(sub_term_reason);
    }

    public final void createFromCursor(Cursor c) {
	if (c != null) {
	    ContentValues v = new ContentValues();
	    DatabaseUtils.cursorRowToContentValues(c, v);
	    this.createFromValues(v);
	}
    }

    public final void createFromValues(ContentValues values) {
	id = values.getAsInteger(ID);
	uri = values.getAsString(URI);
	contact = values.getAsString(CONTACT);
	status = values.getAsInteger(STATUS);
	status_text = values.getAsString(STATUS_TEXT);
	monitor_pres = values.getAsBoolean(MONITOR_PRESENCE);
	sub_state = values.getAsInteger(SUB_STATE);
	sub_state_name = values.getAsString(SUB_STATE_NAME);
	sub_term_code = values.getAsInteger(SUB_TERM_CODE);
	sub_term_reason = values.getAsString(SUB_TERM_REASON);
    }

    public final ContentValues getAsValues() {
	ContentValues values = new ContentValues();
	values.put(ID, id);
	values.put(URI, uri);
	values.put(CONTACT, contact);
	values.put(STATUS, status);
	values.put(STATUS_TEXT, status_text);
	values.put(MONITOR_PRESENCE, monitor_pres);
	values.put(SUB_STATE, sub_state);
	values.put(SUB_STATE_NAME, sub_state_name);
	values.put(SUB_TERM_CODE, sub_term_code);
	values.put(SUB_TERM_REASON, sub_term_reason);
	return values;
    }
}