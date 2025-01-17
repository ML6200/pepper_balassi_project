package com.example.pepper_test;

import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.aldebaran.qi.sdk.object.conversation.TopicStatus;

import java.util.ArrayList;
import java.util.List;

public class ChatBotController
{
    private QiChatbot qiChatbot;
    private Chat chat;

    private Future<Void> chatFuture;
    private final QiContext qiContext;

    private Topic iskolaTopic;
    private Topic felviTopic;
    private Topic tanarTopic;

    private List<Topic> topics = new ArrayList<>();

    public ChatBotController(QiContext qiContext)
    {
        this.qiContext = qiContext;
        initializeTopics();
    }

    private void initializeTopics()
    {
        iskolaTopic = TopicBuilder.with(qiContext)
                .withResource(R.raw.proba)
                .build();

        felviTopic = TopicBuilder.with(qiContext)
                .withResource(R.raw.felveteli)
                .build();

        tanarTopic = TopicBuilder.with(qiContext)
                .withResource(R.raw.tanarok)
                .build();

        topics.add(iskolaTopic);
        topics.add(felviTopic);
        topics.add(tanarTopic);
    }

    private void changeTopic(Topic topic)
    {
        TopicStatus topicStatus = qiChatbot.topicStatus(topic);
        topicStatus.setEnabled(true);
    }

    public void changeTopic(Temak tema)
    {
        Topic topic = null;
        switch (tema)
        {
            case FELVI:
            {
                topic = felviTopic;
            } break;

            case ISKOLA:
            {
                topic = iskolaTopic;
            } break;
            case TANAROK:
            {
                topic = tanarTopic;
            } break;
        }

        changeTopic(topic);
    }

    public void startConversation()
    {
        try
        {
            // QiChatbot létrehozása a Topic-kal
            qiChatbot = QiChatbotBuilder.with(qiContext)
                    .withTopics(topics)
                    .build();

            // Chat létrehozása a QiChatbot-tal
            chat = ChatBuilder.with(qiContext)
                    .withChatbot(qiChatbot)
                    .build();

            // Chat indítása
            chatFuture = chat.async().run();

            chat.addOnNoPhraseRecognizedListener(() ->
            {
                SayBuilder.with(qiContext)
                        .withPhrase(
                                new Phrase("Sajnos ebben nem tudok segíteni. " +
                                        "Van valami más amiben segíthetek?"))
                        .build();
            });

            Log.d("", "Chat indítása sikeresen megtörtént.");
        } catch (Exception e)
        {
            Log.e("", "Hiba történt a chat indítása során: ", e);
        }
    }

    public void forceEndConversation()
    {
        chatFuture.cancel(true); // eroszakos leallitas
    }

    public void requestEndConversation()
    {
        if (chatFuture != null && !chatFuture.isDone()) {
            chatFuture.requestCancellation();
        }
    }
}

enum Temak
{
    ISKOLA,
    TANAROK,
    FELVI
}

