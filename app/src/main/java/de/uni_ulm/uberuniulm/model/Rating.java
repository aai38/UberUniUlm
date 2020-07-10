package de.uni_ulm.uberuniulm.model;

public class Rating {

    private float stars;
    private String comment;

    public Rating(float stars, String comment) {
        this.stars = stars;
        this.comment = comment;
    }

    public float getStars() {
        return stars;
    }

    public void setStars(float stars) {
        this.stars = stars;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }




}
