package com.pes.fibness;

import java.io.Serializable;

public class CommentModel implements Serializable {

    private int id;
    private int userId;
    private String username;
    private String date;
    private String text;

    public CommentModel(int id, int userId, String username, String date, String text) {
        this.id = id; this.userId = userId; this.username = username; this.date = date; this.text = text;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text;}

}
