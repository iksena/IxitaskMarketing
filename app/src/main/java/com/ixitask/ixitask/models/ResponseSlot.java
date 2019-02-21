package com.ixitask.ixitask.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResponseSlot {
    @SerializedName("status")
    @Expose
    public String status;
    @SerializedName("status_message")
    @Expose
    public String statusMessage;
    @SerializedName("data")
    @Expose
    public Data data;

    public class Data {

        @SerializedName("records")
        @Expose
        public List<Slot> slots = null;

    }

    public class Slot {

        @SerializedName("slotid")
        @Expose
        public String slotid;
        @SerializedName("slotname")
        @Expose
        public String slotname;
        @SerializedName("availableslot")
        @Expose
        public String availableslot;

    }
}
