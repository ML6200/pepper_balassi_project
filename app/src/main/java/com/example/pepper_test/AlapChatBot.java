package com.example.pepper_test;

import com.aldebaran.qi.sdk.QiContext;

public class AlapChatBot extends ChatBotClient {
    public AlapChatBot(QiContext qiContext) {
        super(qiContext);
        setResourceId(R.raw.proba);
    }
}
