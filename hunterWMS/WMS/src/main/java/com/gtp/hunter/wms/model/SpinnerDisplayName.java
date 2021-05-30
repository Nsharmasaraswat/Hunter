package com.gtp.hunter.wms.model;

public class SpinnerDisplayName {

    private String display;
    private String name;

    public SpinnerDisplayName(String name, String display) {
        this.name = name;
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.display;
    }
}
