package de.uni_ulm.uberuniulm.model;

import java.sql.Date;
import java.sql.Time;

public class BookedRide {

    private String userKey;

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public int getzIndex() {
        return zIndex;
    }

    public void setzIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    private int zIndex;

    public BookedRide(String userKey, int zIndex) {
        this.userKey = userKey;
        this.zIndex = zIndex;
    }
}


