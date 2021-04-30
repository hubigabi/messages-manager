package utp.edu.sms_manger.model;

import java.util.Date;

public class Sms {

    private String message;
    private String number;
    private Date date;

    public Sms(String message, String number, Date date) {
        this.message = message;
        this.number = number;
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
