package com.nayanpote.voxnayanai;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import androidx.core.app.NotificationCompat;
import java.util.ArrayList;
import java.util.Locale;

public class VoxService extends Service implements TextToSpeech.OnInitListener {

    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "VoxServiceChannel";

    private SpeechRecognizer backgroundSpeechRecognizer;
    private Intent backgroundSpeechIntent;
    private TextToSpeech backgroundTTS;
    private VoxAIProcessor aiProcessor;
    private boolean isServiceActive = true;
    private boolean isBackgroundListening = false;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        initializeBackgroundComponents();
        aiProcessor = new VoxAIProcessor(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, createNotification());
        startBackgroundListening();
        return START_STICKY; // Restart service if killed
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not a bound service
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Vox AI Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setDescription("Vox AI Background Service");
            serviceChannel.setShowBadge(false);
            serviceChannel.setSound(null, null);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Vox AI")
                .setContentText("Voice assistant is running in background")
                .setSmallIcon(R.drawable.logo4)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();
    }

    private void initializeBackgroundComponents() {
        // Initialize TTS for background responses
        backgroundTTS = new TextToSpeech(this, this);

        // Initialize speech recognizer for background wake word detection
        backgroundSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        backgroundSpeechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        backgroundSpeechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        backgroundSpeechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        backgroundSpeechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());
        backgroundSpeechIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        backgroundSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                isBackgroundListening = true;
            }

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                isBackgroundListening = false;
                if (isServiceActive) {
                    // Restart listening after a brief delay
                    new android.os.Handler().postDelayed(() -> {
                        if (isServiceActive) {
                            startBackgroundListening();
                        }
                    }, 1000);
                }
            }

            @Override
            public void onError(int error) {
                isBackgroundListening = false;
                // Restart listening after error (except for critical errors)
                if (error != SpeechRecognizer.ERROR_CLIENT &&
                        error != SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS && isServiceActive) {
                    new android.os.Handler().postDelayed(() -> {
                        if (isServiceActive) {
                            startBackgroundListening();
                        }
                    }, 2000);
                }
            }

            @Override
            public void onResults(Bundle results) {
                isBackgroundListening = false;
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spokenText = matches.get(0);
                    processBackgroundCommand(spokenText);
                }

                // Continue listening for wake words
                if (isServiceActive) {
                    new android.os.Handler().postDelayed(() -> {
                        if (isServiceActive) {
                            startBackgroundListening();
                        }
                    }, 500);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> partialMatches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (partialMatches != null && !partialMatches.isEmpty()) {
                    String partialText = partialMatches.get(0);
                    // Check for wake words in partial results for faster response
                    if (aiProcessor.isWakeWord(partialText)) {
                        // Stop current listening and process wake word immediately
                        backgroundSpeechRecognizer.stopListening();
                        processBackgroundCommand(partialText);
                    }
                }
            }

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void startBackgroundListening() {
        if (!isBackgroundListening && isServiceActive) {
            try {
                backgroundSpeechRecognizer.startListening(backgroundSpeechIntent);
            } catch (Exception e) {
                e.printStackTrace();
                // Retry after delay if there's an error
                new android.os.Handler().postDelayed(() -> {
                    if (isServiceActive) {
                        startBackgroundListening();
                    }
                }, 3000);
            }
        }
    }

    private void stopBackgroundListening() {
        isBackgroundListening = false;
        if (backgroundSpeechRecognizer != null) {
            backgroundSpeechRecognizer.stopListening();
        }
    }

    private void processBackgroundCommand(String command) {
        if (aiProcessor.isWakeWord(command)) {
            // Wake word detected - respond and open main app
            String response = "Yes, I'm here. Opening Vox AI.";
            speakBackgroundResponse(response);

            // Open main activity
            Intent mainIntent = new Intent(this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(mainIntent);

            return;
        }

        // Process other commands in background
        aiProcessor.processCommand(command, new VoxAIProcessor.AIResponseCallback() {
            @Override
            public void onResponse(String response) {
                speakBackgroundResponse(response);

                // For complex commands, suggest opening the main app
                if (response.contains("I need to") || response.contains("complex")) {
                    new android.os.Handler().postDelayed(() -> {
                        speakBackgroundResponse("Would you like me to open the full Vox interface for more options?");
                    }, 2000);
                }
            }

            @Override
            public void onError(String error) {
                speakBackgroundResponse("Sorry, I encountered an error while processing your request.");
            }
        });
    }

    private void speakBackgroundResponse(String text) {
        if (backgroundTTS != null) {
            backgroundTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = backgroundTTS.setLanguage(Locale.US);
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                backgroundTTS.setSpeechRate(0.9f);
                backgroundTTS.setPitch(1.0f);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceActive = false;

        stopBackgroundListening();

        if (backgroundSpeechRecognizer != null) {
            backgroundSpeechRecognizer.destroy();
        }

        if (backgroundTTS != null) {
            backgroundTTS.stop();
            backgroundTTS.shutdown();
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // Restart service when task is removed (app is swiped away)
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplicationContext().startForegroundService(restartServiceIntent);
        } else {
            getApplicationContext().startService(restartServiceIntent);
        }

        super.onTaskRemoved(rootIntent);
    }
}