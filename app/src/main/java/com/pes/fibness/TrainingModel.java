package com.pes.fibness;

import java.io.Serializable;

public class TrainingModel implements Serializable {

    private int id;
    private String name;
    private String desc;
    private int nLikes;
    private int nComment;


    public TrainingModel(int id, String name, String desc, int nLikes, int nComment) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.nLikes = nLikes;
        this.nComment = nComment;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }

    public int getnLikes() { return nLikes; }
    public void setnLikes(int nLikes) { this.nLikes = nLikes; }

    public int getnComment() { return nComment; }
    public void setnComment(int nComment) { this.nComment = nComment; }


}

