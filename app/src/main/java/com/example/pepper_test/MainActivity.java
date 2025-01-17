package com.example.pepper_test;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.humanawareness.HumanAwareness;
import com.aldebaran.qi.sdk.object.touch.Touch;
import com.aldebaran.qi.sdk.object.touch.TouchSensor;
import com.aldebaran.qi.sdk.object.touch.TouchState;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks
{
    enum ActivePage
    {
        TANAR,
        FELVETELI,
        ISKOLA,
        FO
    }

    private static final String TAG = "MainActivity";
    private ActivePage activePage = ActivePage.ISKOLA;
    private ChatBotController chatBotController;
    private QiContext qiContext = null;

    //UI elemek deklarálása
    private Button btnTanar;
    private Button btnFelvetel;
    private Button btnIskola;
    private Button btnVissza;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnTanar = findViewById(R.id.tanarok);
        btnFelvetel = findViewById(R.id.felveteli);
        btnIskola = findViewById(R.id.iskola);
        btnVissza = findViewById(R.id.vissza);

        btnVissza.setOnClickListener(e->
        {
            setContentView(R.layout.activity_main);
            activePage = ActivePage.FO;
        });

        btnTanar.setOnClickListener(e->
        {
            setContentView(R.layout.tanar_chat);
            activePage = ActivePage.TANAR;
        });

        btnFelvetel.setOnClickListener(e->
        {
            setContentView(R.layout.felveteli_diakok);
            activePage = ActivePage.FELVETELI;
        });

        btnIskola.setOnClickListener(e->
        {
            setContentView(R.layout.chat_bot_client);
            activePage = ActivePage.ISKOLA;
        });

        // Regisztrálja az aktivitást a Sepper sdk-hoz
        QiSDK.register(this, this);
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        HumanAwareness humanAwareness = qiContext.getHumanAwareness();

        Log.d(TAG, "Robot fókusz elnyerve.");

        chatBotController = new ChatBotController(qiContext);
        chatBotController.startConversation();

        //ha mozognak körülötte
        humanAwareness.addOnHumansAroundChangedListener(e->
        {
            if (activePage == ActivePage.TANAR)
            {
                chatBotController.changeTopic(Temak.TANAROK);
            }
            else if (activePage == ActivePage.ISKOLA)
            {
                chatBotController.changeTopic(Temak.ISKOLA);
            }
            else if (activePage == ActivePage.FELVETELI)
            {
                chatBotController.changeTopic(Temak.FELVI);
            }
        });
    }

    @Override
    public void onRobotFocusLost()
    {
        Log.d(TAG, "Robot fókusz elvesztve.");
        chatBotController.requestEndConversation();
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.e(TAG, "Robot fókusz visszautasítva: " + reason);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Aktivitás megsemmisítése.");

        // Aktivitás leregisztrálása
        QiSDK.unregister(this, this);
        chatBotController.requestEndConversation();
    }


    // -------------------------------USER-DEFINED----------------------------------
    private void initTouchSensors(QiContext qiContext)
    {
        Touch touch = qiContext.getTouch();
        TouchSensor touchSensor = touch.getSensor("Head/Touch");
        touchSensor.addOnStateChangedListener(new TouchSensor.OnStateChangedListener()
        {
            @Override
            public void onStateChanged(TouchState state)
            {

            }
        });
    }
}
