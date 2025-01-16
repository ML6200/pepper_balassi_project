package com.example.pepper_test;

import com.aldebaran.qi.sdk.QiContext;

public class TanarChat extends ChatBotClient{
    public TanarChat(QiContext qiContext) {
        super(qiContext);
        setResourceId(R.raw.tanarok);
    }
}
