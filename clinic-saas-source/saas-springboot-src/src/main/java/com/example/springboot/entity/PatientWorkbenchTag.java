package com.example.springboot.entity;

public class PatientWorkbenchTag {
    private String text;
    private String type;
    private String kind;

    public PatientWorkbenchTag() {
    }

    public PatientWorkbenchTag(String text, String type, String kind) {
        this.text = text;
        this.type = type;
        this.kind = kind;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }
}
