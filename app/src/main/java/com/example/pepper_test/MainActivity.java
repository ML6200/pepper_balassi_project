package com.example.pepper_test;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Topic;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "MainActivity";
    private Chat chat;
    private Future<Void> chatFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Regisztrálja az aktivitást a QiSDK-hoz
        QiSDK.register(this, this);
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        Log.d(TAG, "Robot fókusz elnyerve.");

        try {
            // A 'faq.qichat' fájl elérési útja az erőforrások között
            int resourceId = R.raw.proba;

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
            Log.d(TAG, "Chat indítása sikeresen megtörtént.");
        } catch (Exception e) {
            Log.e(TAG, "Hiba történt a chat indítása során: ", e);
            Toast.makeText(this, "Hiba történt a chat indítása során.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRobotFocusLost() {
        Log.d(TAG, "Robot fókusz elvesztve.");
        // Chat leállítása, ha a robot fókusz elveszett
        if (chatFuture != null && !chatFuture.isDone()) {
            chatFuture.requestCancellation();
        }
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.e(TAG, "Robot fókusz visszautasítva: " + reason);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Aktivitás megsemmisítése.");
        // Aktivitás leiratkozása a QiSDK-ról
        QiSDK.unregister(this, this);
        // Chat leállítása, ha az aktivitás megsemmisül
        if (chatFuture != null && !chatFuture.isDone()) {
            chatFuture.requestCancellation();
        }
    }
}
