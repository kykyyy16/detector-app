package com.example.tilapia_white_spot_disease_detection.Domain;

public class TrendSDomain {

    private String title;
    private String subtitle;
    private String picAddress;

    public TrendSDomain(String title, String subtitle, String picAddress) {
        this.title = title;
        this.subtitle = subtitle;
        this.picAddress = picAddress;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getPicAddress() {
        return picAddress;
    }

    public void setPicAddress(String picAddress) {
        this.picAddress = picAddress;
    }
}
