package com.pes.fibness;

import java.io.Serializable;

public class ExerciseModel implements Serializable {

    private int id;
    private String title;
    private String desc;
    private int numRep;
    private int numSerie;
    private int numRest;

    public ExerciseModel(int id, String title, String desc, int numRep, int numSerie, int numRest) {
        this.id = id; this.title = title; this.desc = desc; this.numRep = numRep; this.numSerie = numSerie; this.numRest = numRest;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDesc() { return desc; }
    public int getNumRep() { return numRep; }
    public int getNumSerie() { return numSerie;}
    public int getNumRest() { return numRest; }
}
