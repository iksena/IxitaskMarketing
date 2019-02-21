package com.ixitask.ixitask.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResponseSummaryRes {
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
        public List<SummaryRes> summaryResponses = null;

    }

    public class SummaryRes {

        @SerializedName("logid")
        @Expose
        public String logid;
        @SerializedName("hpid")
        @Expose
        public String hpid;
        @SerializedName("street_name")
        @Expose
        public String streetName;
        @SerializedName("owner")
        @Expose
        public String owner;
        @SerializedName("phone")
        @Expose
        public String phone;
        @SerializedName("activity_note")
        @Expose
        public String activityNote;
        @SerializedName("contactby")
        @Expose
        public String contactby;
        @SerializedName("contactdate")
        @Expose
        public String contactdate;

    }
}
