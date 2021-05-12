package utp.edu.manager.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class Message {

    @PrimaryKey(autoGenerate = true)
    public int id;

    private String text;
    private String number;
    private Date date;
    private MessageType messageType;

    public Message() {
    }

    public Message(String text, String number, Date date, MessageType messageType) {
        this.id = id;
        this.text = text;
        this.number = number;
        this.date = date;
        this.messageType = messageType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", number='" + number + '\'' +
                ", date=" + date +
                ", messageType=" + messageType +
                '}';
    }
}
