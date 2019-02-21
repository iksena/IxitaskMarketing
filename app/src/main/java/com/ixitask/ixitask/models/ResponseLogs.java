package com.ixitask.ixitask.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResponseLogs {
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("status_message")
    @Expose
    private String statusMessage;
    @SerializedName("data")
    @Expose
    private DataLogs data;

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

    public DataLogs getData() {
        return data;
    }

    public void setData(DataLogs data) {
        this.data = data;
    }

    public class DataLogs{

        @SerializedName("records")
        @Expose
        private List<Log> logs = null;

        public List<Log> getLogs() {
            return logs;
        }

        public void setLogs(List<Log> logs) {
            this.logs = logs;
        }

    }

    public class Log{

        @SerializedName("logid")
        @Expose
        private String logid;
        @SerializedName("activity_note")
        @Expose
        private String activityNote;
        @SerializedName("contactby")
        @Expose
        private String contactby;
        @SerializedName("contactdate")
        @Expose
        private String contactdate;

        public String getLogid() {
            return logid;
        }

        public void setLogid(String logid) {
            this.logid = logid;
        }

        public String getActivityNote() {
            return activityNote;
        }

        public void setActivityNote(String activityNote) {
            this.activityNote = activityNote;
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




