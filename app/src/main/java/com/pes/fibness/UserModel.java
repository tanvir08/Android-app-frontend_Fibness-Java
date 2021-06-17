package com.pes.fibness;

import java.io.Serializable;

public class UserModel implements Serializable, Comparable<UserModel>{

    private int id;
    private String username;
    private Boolean blocked;

    public UserModel(int id, String username, Boolean blocked) {
        this.id = id;
        this.username = username;
        this.blocked = blocked;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getBlocked() { return blocked; }
    public void setBlocked(Boolean blocked) { this.blocked = blocked; }

    /*use to sort list by username*/
    @Override
    public int compareTo(UserModel userModel) {
        return this.username.compareTo(userModel.username);
    }
}
