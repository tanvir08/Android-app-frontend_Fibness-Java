package com.pes.fibness;

public class MyModel {

    private int image;
    private String title;
    private String metre;
    private String description;

    public MyModel(int image, String title,  String metre, String description) {
        this.image = image;
        this.title = title;
        this.metre = metre;
        this.description = description;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMetre() {
        return metre;
    }

    public void setMetre(String metre) {
        this.metre = metre;
    }
}
