package de.uni_ulm.uberuniulm.model;

public class Message {
    private User person_messaging;

    public User getPerson_messaging() {
        return person_messaging;
    }

    public void setPerson_messaging(User person_messaging) {
        this.person_messaging = person_messaging;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    private String content;
}
