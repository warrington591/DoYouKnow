package com.bloomfield.warrington.doyouknow;

import android.net.Uri;

import java.util.Date;

/**
 * Created by Warrington on 4/22/17.
 */

public class ChatMessage {
    private String messageText;
    private String messageUser;
    private String image;
    private long messageTime;

    public ChatMessage(String messageText, String messageUser, String imageUrl) {
        this.messageText = messageText;
        this.messageUser = messageUser;
        this.image =imageUrl;
        // Initialize to current time
        messageTime = new Date().getTime();
    }

    public ChatMessage(String messageText, String messageUser) {
        this.messageText = messageText;
        this.messageUser = messageUser;
        // Initialize to current time
        messageTime = new Date().getTime();
    }


    public ChatMessage(){

    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }
}
