package com.ixitask.ixitask.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResponseInstall {

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
        public List<Install> installs = null;

    }

    public class Install {

        @SerializedName("serviceid")
        @Expose
        public String serviceid;
        @SerializedName("woid")
        @Expose
        public String woid;
        @SerializedName("custid")
        @Expose
        public String custid;
        @SerializedName("custname")
        @Expose
        public String custname;
        @SerializedName("address")
        @Expose
        public String address;
        @SerializedName("tglservice")
        @Expose
        public String tglservice;
        @SerializedName("completedate")
        @Expose
        public String completedate;
        @SerializedName("tglreg")
        @Expose
        public String tglreg;
        @SerializedName("sonote")
        @Expose
        public Object sonote;
        @SerializedName("billamt")
        @Expose
        public int billamt;
        @SerializedName("sstatus")
        @Expose
        public String sstatus;

    }
}
