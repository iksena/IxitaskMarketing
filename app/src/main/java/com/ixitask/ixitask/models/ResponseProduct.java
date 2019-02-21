package com.ixitask.ixitask.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import androidx.annotation.NonNull;

public class ResponseProduct {
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
        public List<Product> products = null;

    }

    public class Product {

        @SerializedName("pro_id")
        @Expose
        public int proId;
        @SerializedName("pro_name")
        @Expose
        public String proName;
        @SerializedName("pro_bundle")
        @Expose
        public String proBundle;
        @SerializedName("pro_price")
        @Expose
        public int proPrice;
        @SerializedName("pro_install")
        @Expose
        public int proInstall;

        @NonNull
        @Override
        public String toString() {
            return proName;
        }
    }
}
