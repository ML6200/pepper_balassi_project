package com.szkkr.pepiai;
import android.os.Bundle;
import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.conversation.Say;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.models.response.OllamaAsyncResultStreamer;
import io.github.ollama4j.types.OllamaModelType;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "MainActivity";
    private ExecutorService executorService;
    private BlockingQueue<String> ttsQueue;
    private volatile boolean isRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        executorService = Executors.newFixedThreadPool(2); // One for streaming, one for TTS
        ttsQueue = new LinkedBlockingQueue<>();
        isRunning = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false; // Stop the TTS processing thread
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    void runner(QiContext qiContext) {
        // Thread for streaming tokens
        executorService.execute(() -> {
            try {
                String host = "http://localhost:11434/";
                OllamaAPI ollamaAPI = new OllamaAPI(host);
                ollamaAPI.setRequestTimeoutSeconds(60);

                String prompt = "Mi magyarország fővárosa és mit kell tudni róla?";
                OllamaAsyncResultStreamer streamer = ollamaAPI.generateAsync(OllamaModelType.MISTRAL, prompt, false);

                int pollIntervalMilliseconds = 1000;

                while (isRunning) {
                    String tokens = streamer.getStream().poll();

                    if (tokens != null && !tokens.isEmpty()) {
                        // Add tokens to the TTS queue
                        ttsQueue.put(tokens);
                    }

                    if (!streamer.isAlive()) {
                        break;
                    }

                    Thread.sleep(pollIntervalMilliseconds);
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "Streaming interrupted", e);
                Thread.currentThread().interrupt(); // Restore the interrupted status
            } catch (Exception e) {
                Log.e(TAG, "Error during streaming", e);
            }
        });

        // Thread for processing TTS tasks
        executorService.execute(() -> {
            while (isRunning) {
                try {
                    String phrase = ttsQueue.take(); // Block until a token is available
                    say(qiContext, phrase);
                } catch (InterruptedException e) {
                    Log.e(TAG, "TTS processing interrupted", e);
                    Thread.currentThread().interrupt(); // Restore the interrupted status
                }
            }
        });
    }

    private void say(QiContext qiContext, String phrase) {
        if (qiContext != null && phrase != null && !phrase.isEmpty()) {
            Say say = SayBuilder
                    .with(qiContext)
                    .withText(phrase)
                    .build();
            say.run(); // This is synchronous and will block until TTS is done
        }
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        runner(qiContext);
    }

    @Override
    public void onRobotFocusLost() {
        // Clean up resources if needed
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.w(TAG, "Robot focus refused: " + reason);
    }
}
