package com.example.pepper_test;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.human.Human;
import com.aldebaran.qi.sdk.object.humanawareness.HumanAwareness;
import com.aldebaran.qi.sdk.object.humanawareness.HumanawarenessConverter;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {

    enum ActivePage
    {
        TANAR,
        FELVETELI,
        ISKOLA
    }

    private static final String TAG = "MainActivity";
    private Chat chat;
    private Future<Void> chatFuture;
    private ActivePage activePage = ActivePage.ISKOLA;
    private QiContext qiContext = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnTanar = findViewById(R.id.tanarok);
        Button btnFelvetel = findViewById(R.id.felveteli);
        Button btnIskola = findViewById(R.id.iskola);

        btnTanar.setOnClickListener(e->
        {
            setContentView(R.layout.tanar_chat);
            activePage = ActivePage.TANAR;
        });

        btnFelvetel.setOnClickListener(e->
        {

            activePage = ActivePage.FELVETELI;
        });

        btnIskola.setOnClickListener(e->
        {
            setContentView(R.layout.chat_bot_client);
            activePage = ActivePage.ISKOLA;
        });

        // Regisztrálja az aktivitást a QiSDK-hoz
        QiSDK.register(this, this);
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        Log.d(TAG, "Robot fókusz elnyerve.");
        ChatBotClient chatBotClient = new ChatBotClient(qiContext);

        HumanAwareness humanAwareness = qiContext.getHumanAwareness();

        humanAwareness.addOnHumansAroundChangedListener(e->{
            if (activePage == ActivePage.TANAR)
            {
                chatBotClient.setResourceId(R.raw.tanarok);
                chatBotClient.InitChatBot();
            } else if (activePage == ActivePage.ISKOLA)
            {
                chatBotClient.setResourceId(R.raw.proba);
                chatBotClient.InitChatBot();
            }
        });
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
