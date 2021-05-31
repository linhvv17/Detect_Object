package com.kma.detectobject.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Detection {

    @SerializedName("class")
    @Expose
    private String _class;
    @SerializedName("confidence")
    @Expose
    private float confidence;

    public String getClass_() {
        return _class;
    }

    public void setClass_(String _class) {
        this._class = _class;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

}