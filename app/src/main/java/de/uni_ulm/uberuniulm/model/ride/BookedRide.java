package de.uni_ulm.uberuniulm.model.ride;

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

    public boolean isRated() {
        return isRated;
    }

    public void setRated(boolean rated) {
        isRated = rated;
    }

    private boolean isRated;

    public BookedRide(String userKey, int zIndex) {
        this.userKey = userKey;
        this.zIndex = zIndex;
    }
}


