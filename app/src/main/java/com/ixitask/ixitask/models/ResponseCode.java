package com.ixitask.ixitask.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResponseCode {
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("status_message")
    @Expose
    private String statusMessage;
    @SerializedName("data")
    @Expose
    private DataCode data;

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

    public DataCode getData() {
        return data;
    }

    public void setData(DataCode data) {
        this.data = data;
    }

    public class DataCode{

        @SerializedName("records")
        @Expose
        private List<ResCode> resCodes = null;

        public List<ResCode> getResCodes() {
            return resCodes;
        }

        public void setResCodes(List<ResCode> resCodes) {
            this.resCodes = resCodes;
        }

    }

    public class ResCode{

        @SerializedName("rid")
        @Expose
        private String rid;
        @SerializedName("rcode")
        @Expose
        private String rcode;

        public String getRid() {
            return rid;
        }

        public void setRid(String rid) {
            this.rid = rid;
        }

        public String getRcode() {
            return rcode;
        }

        public void setRcode(String rcode) {
            this.rcode = rcode;
        }

        @Override
        public String toString() {
            return rcode;
        }
    }
}




