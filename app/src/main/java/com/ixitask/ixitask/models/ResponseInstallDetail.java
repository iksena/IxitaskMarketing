package com.ixitask.ixitask.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class ResponseInstallDetail {
    
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("status_message")
    @Expose
    private String statusMessage;
    @SerializedName("data")
    @Expose
    private Data data;

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

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public class Data {

        @SerializedName("records")
        @Expose
        private List<Record> records = null;

        public List<Record> getRecords() {
            return records;
        }

        public void setRecords(List<Record> records) {
            this.records = records;
        }

    }

    public class Record {

        @SerializedName("serviceid")
        @Expose
        private String serviceid;
        @SerializedName("woid")
        @Expose
        private String woid;
        @SerializedName("custid")
        @Expose
        private String custid;
        @SerializedName("custname")
        @Expose
        private String custname;
        @SerializedName("address")
        @Expose
        private String address;
        @SerializedName("phone")
        @Expose
        private String phone;
        @SerializedName("slot")
        @Expose
        private String slot;
        @SerializedName("completedate")
        @Expose
        private String completedate;
        @SerializedName("tglreg")
        @Expose
        private String tglreg;
        @SerializedName("sonote")
        @Expose
        private String sonote;
        @SerializedName("longitude")
        @Expose
        private String longitude;
        @SerializedName("lattitude")
        @Expose
        private String lattitude;
        @SerializedName("pics")
        @Expose
        private List<Pic> pics = null;
        @SerializedName("products")
        @Expose
        private List<Product> products = null;
        @SerializedName("prorate")
        @Expose
        private int prorate;
        @SerializedName("monthlyfee")
        @Expose
        private int monthlyfee;
        @SerializedName("installfee")
        @Expose
        private int installfee;
        @SerializedName("sstatus")
        @Expose
        private String sstatus;

        public String getServiceid() {
            return serviceid;
        }

        public Record setServiceid(String serviceid) {
            this.serviceid = serviceid;
            return this;
        }

        public String getWoid() {
            return woid;
        }

        public Record setWoid(String woid) {
            this.woid = woid;
            return this;
        }

        public String getCustid() {
            return custid;
        }

        public Record setCustid(String custid) {
            this.custid = custid;
            return this;
        }

        public String getCustname() {
            return custname;
        }

        public Record setCustname(String custname) {
            this.custname = custname;
            return this;
        }

        public String getAddress() {
            return address;
        }

        public Record setAddress(String address) {
            this.address = address;
            return this;
        }

        public String getPhone() {
            return phone;
        }

        public Record setPhone(String phone) {
            this.phone = phone;
            return this;
        }

        public String getSlot() {
            return slot;
        }

        public Record setSlot(String slot) {
            this.slot = slot;
            return this;
        }

        public String getCompletedate() {
            return completedate;
        }

        public Record setCompletedate(String completedate) {
            this.completedate = completedate;
            return this;
        }

        public String getTglreg() {
            return tglreg;
        }

        public Record setTglreg(String tglreg) {
            this.tglreg = tglreg;
            return this;
        }

        public String getSonote() {
            return sonote;
        }

        public Record setSonote(String sonote) {
            this.sonote = sonote;
            return this;
        }

        public String getLongitude() {
            return longitude;
        }

        public Record setLongitude(String longitude) {
            this.longitude = longitude;
            return this;
        }

        public String getLattitude() {
            return lattitude;
        }

        public Record setLattitude(String lattitude) {
            this.lattitude = lattitude;
            return this;
        }

        public List<Pic> getPics() {
            return pics;
        }

        public Record setPics(List<Pic> pics) {
            this.pics = pics;
            return this;
        }

        public List<Product> getProducts() {
            return products;
        }

        public String[] getProductsStr(){
            String[] productsStr = new String[products.size()];
            for (int i=0;i<products.size();i++){
                productsStr[i] = products.get(i).getProname();
            }
            return productsStr;
        }

        public Record setProducts(List<Product> products) {
            this.products = products;
            return this;
        }

        public int getProrate() {
            return prorate;
        }

        public Record setProrate(int prorate) {
            this.prorate = prorate;
            return this;
        }

        public int getMonthlyfee() {
            return monthlyfee;
        }

        public Record setMonthlyfee(int monthlyfee) {
            this.monthlyfee = monthlyfee;
            return this;
        }

        public int getInstallfee() {
            return installfee;
        }

        public Record setInstallfee(int installfee) {
            this.installfee = installfee;
            return this;
        }

        public String getSstatus() {
            return sstatus;
        }

        public Record setSstatus(String sstatus) {
            this.sstatus = sstatus;
            return this;
        }
    }

    public class Product {

        @SerializedName("proname")
        @Expose
        private String proname;

        public String getProname() {
            return proname;
        }

        public void setProname(String proname) {
            this.proname = proname;
        }

        @NonNull
        @Override
        public String toString() {
            return proname;
        }
    }

    public class Pic {

        @SerializedName("picture")
        @Expose
        private String picture;

        public String getPicture() {
            return picture;
        }

        public void setPicture(String picture) {
            this.picture = picture;
        }

    }
}
