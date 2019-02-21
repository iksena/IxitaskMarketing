package com.ixitask.ixitask.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResponseHomepass {
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("status_message")
    @Expose
    private String statusMessage;
    @SerializedName("data")
    @Expose
    private DataHomepass data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public DataHomepass getData() {
        return data;
    }

    public void setData(DataHomepass data) {
        this.data = data;
    }

    public class DataHomepass{

        @SerializedName("records")
        @Expose
        private List<Homepass> homepasses;
        public List<Homepass> getHomepasses() {
            return homepasses;
        }
        public void setHomepasses(List<Homepass> homepasses) {
            this.homepasses = homepasses;
        }
    }

    public static class Homepass {

        @SerializedName("hpid")
        @Expose
        private String hpid;
        @SerializedName("street_name")
        @Expose
        private String streetName;
        @SerializedName("complex")
        @Expose
        private String complex;
        @SerializedName("owner")
        @Expose
        private String owner;
        @SerializedName("phone")
        @Expose
        private String phone;
        @SerializedName("market")
        @Expose
        private String market;
        @SerializedName("node")
        @Expose
        private String node;
        @SerializedName("open")
        @Expose
        private String open;
        @SerializedName("contactby")
        @Expose
        private String contactby;
        @SerializedName("contactdate")
        @Expose
        private String contactdate;

        public String getHpid() {
            return hpid;
        }

        public void setHpid(String hpid) {
            this.hpid = hpid;
        }

        public String getStreetName() {
            return streetName;
        }

        public void setStreetName(String streetName) {
            this.streetName = streetName;
        }

        public String getComplex() {
            return complex;
        }

        public void setComplex(String complex) {
            this.complex = complex;
        }

        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            this.owner = owner;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getMarket() {
            return market;
        }

        public void setMarket(String market) {
            this.market = market;
        }

        public String getNode() {
            return node;
        }

        public void setNode(String node) {
            this.node = node;
        }

        public String getOpen() {
            return open;
        }

        public void setOpen(String open) {
            this.open = open;
        }

        public String getContactby() {
            return contactby;
        }

        public void setContactby(String contactby) {
            this.contactby = contactby;
        }

        public String getContactdate() {
            return contactdate;
        }

        public void setContactdate(String contactdate) {
            this.contactdate = contactdate;
        }

    }
}



