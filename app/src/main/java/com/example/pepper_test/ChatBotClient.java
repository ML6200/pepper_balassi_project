package com.example.pepper_test;

import android.util.Log;
import android.widget.Toast;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Topic;

public class ChatBotClient {
    private Chat chat;
    private Future<Void> chatFuture;
    private QiContext qiContext;

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    private int resourceId;

    public ChatBotClient(QiContext qiContext)
    {
        this.qiContext = qiContext;
    }

    public void InitChatBot() {
        Log.d("", "Robot fókusz elnyerve.");

        try {
            // Topic létrehozása a QiChat fájl betöltésével
            Topic topic = TopicBuilder.with(qiContext)
                    .withResource(resourceId)
                    .build();

            // QiChatbot létrehozása a Topic-kal
            QiChatbot qiChatbot = QiChatbotBuilder.with(qiContext)
                    .withTopic(topic)
                    .build();

            // Chat létrehozása a QiChatbot-tal
            chat = ChatBuilder.with(qiContext)
                    .withChatbot(qiChatbot)
                    .build();

            // Chat indítása
            chatFuture = chat.async().run();
            Log.d("", "Chat indítása sikeresen megtörtént.");
        } catch (Exception e) {
            Log.e("", "Hiba történt a chat indítása során: ", e);
        }
    }
}
