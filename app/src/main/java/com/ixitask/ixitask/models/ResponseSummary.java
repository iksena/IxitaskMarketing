package com.ixitask.ixitask.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResponseSummary {
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
        public List<Summary> summaries = null;

    }

    public class Summary {

        @SerializedName("hpscid")
        @Expose
        public String hpscid;
        @SerializedName("hpscnote")
        @Expose
        public String hpscnote;
        @SerializedName("hpsnum")
        @Expose
        public String hpsnum;

    }
}
