package com.nayanpote.voxnayanai;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final int REQUEST_RECORD_AUDIO = 1001;
    private static final int REQUEST_OVERLAY_PERMISSION = 1002;

    // UI Components
    private TextView tvResponse, tvStatus;
    private ImageButton btnVoice, btnSettings, btnChat, btnPower;
    private LinearLayout listeningIndicator;
    private View statusIndicator, outerRing, middleRing, pulseEffect;

    // Core Components
    private TextToSpeech textToSpeech;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private VoxAIProcessor aiProcessor;
    private SharedPreferences preferences;

    // State Variables
    private boolean isListening = false;
    private boolean isTTSInitialized = false;
    private boolean isVoxActive = true;
    private String userName = "User";

    // Animation Components
    private AnimatorSet pulseAnimation;
    private AnimatorSet listeningAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        initializePreferences();
        initializeTextToSpeech();
        initializeSpeechRecognizer();
        initializeAIProcessor();
        initializeAnimations();
        setupClickListeners();

        checkPermissions();
        startVoxService();

        // Welcome message
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            String welcomeMessage = "Hello " + userName + ", I am Vox. Your AI assistant is ready to help.";
            updateResponse(welcomeMessage);
            speakText(welcomeMessage);
        }, 1000);
    }

    private void initializeViews() {
        tvResponse = findViewById(R.id.tvResponse);
        tvStatus = findViewById(R.id.tvStatus);
        btnVoice = findViewById(R.id.btnVoice);
        btnSettings = findViewById(R.id.btnSettings);
        btnChat = findViewById(R.id.btnChat);
        btnPower = findViewById(R.id.btnPower);
        listeningIndicator = findViewById(R.id.listeningIndicator);
        statusIndicator = findViewById(R.id.statusIndicator);
        outerRing = findViewById(R.id.outerRing);
        middleRing = findViewById(R.id.middleRing);
        pulseEffect = findViewById(R.id.pulseEffect);
    }

    private void initializePreferences() {
        preferences = getSharedPreferences("VoxPreferences", Context.MODE_PRIVATE);
        userName = preferences.getString("user_name", "User");
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(this, this);
    }

    private void initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                showListening(true);
            }

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {
                animateListeningWaves(rmsdB);
            }

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                showListening(false);
            }

            @Override
            public void onError(int error) {
                showListening(false);
                isListening = false;
                handleSpeechError(error);
            }

            @Override
            public void onResults(Bundle results) {
                showListening(false);
                isListening = false;
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spokenText = matches.get(0);
                    processVoiceCommand(spokenText);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> partialMatches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (partialMatches != null && !partialMatches.isEmpty()) {
                    updateResponse("You said: " + partialMatches.get(0));
                }
            }

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void initializeAIProcessor() {
        aiProcessor = new VoxAIProcessor(this);
    }

    private void initializeAnimations() {
        // Pulse animation for AI core
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(pulseEffect, "scaleX", 0.8f, 1.2f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(pulseEffect, "scaleY", 0.8f, 1.2f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(pulseEffect, "alpha", 0.3f, 0.8f);

        // Add repeat + reverse on each animator
        scaleX.setRepeatMode(ValueAnimator.REVERSE);
        scaleX.setRepeatCount(ValueAnimator.INFINITE);

        scaleY.setRepeatMode(ValueAnimator.REVERSE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);

        alpha.setRepeatMode(ValueAnimator.REVERSE);
        alpha.setRepeatCount(ValueAnimator.INFINITE);

        // AnimatorSet just groups them
        pulseAnimation = new AnimatorSet();
        pulseAnimation.playTogether(scaleX, scaleY, alpha);
        pulseAnimation.setDuration(1000);
        pulseAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseAnimation.start();

        // Outer ring rotation
        ObjectAnimator rotateOuter = ObjectAnimator.ofFloat(outerRing, "rotation", 0f, 360f);
        rotateOuter.setDuration(10000);
        rotateOuter.setRepeatCount(ValueAnimator.INFINITE);
        rotateOuter.start();

        // Middle ring rotation (opposite direction)
        ObjectAnimator rotateMiddle = ObjectAnimator.ofFloat(middleRing, "rotation", 360f, 0f);
        rotateMiddle.setDuration(8000);
        rotateMiddle.setRepeatCount(ValueAnimator.INFINITE);
        rotateMiddle.start();
    }


    private void setupClickListeners() {
        btnVoice.setOnClickListener(v -> toggleListening());

        btnSettings.setOnClickListener(v -> showSettingsDialog());

        btnChat.setOnClickListener(v -> {
            // TODO: Implement chat history
            Toast.makeText(this, "Chat history feature coming soon", Toast.LENGTH_SHORT).show();
        });

        btnPower.setOnClickListener(v -> toggleVoxPower());
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
            }
        }
    }

    private void startVoxService() {
        Intent serviceIntent = new Intent(this, VoxService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void toggleListening() {
        if (!isVoxActive) {
            updateResponse("Vox is currently offline. Please activate Vox first.");
            return;
        }

        if (isListening) {
            stopListening();
        } else {
            startListening();
        }
    }

    private void startListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            isListening = true;
            speechRecognizer.startListening(speechRecognizerIntent);
            animatePulse(true);
        } else {
            Toast.makeText(this, "Audio permission required", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopListening() {
        isListening = false;
        speechRecognizer.stopListening();
        showListening(false);
        animatePulse(false);
    }

    private void showListening(boolean listening) {
        listeningIndicator.setVisibility(listening ? View.VISIBLE : View.GONE);
        if (listening) {
            startListeningAnimation();
        } else {
            stopListeningAnimation();
        }
    }

    private void startListeningAnimation() {
        if (listeningAnimation != null && listeningAnimation.isRunning()) {
            return;
        }

        View[] waves = {
                findViewById(R.id.wave1),
                findViewById(R.id.wave2),
                findViewById(R.id.wave3),
                findViewById(R.id.wave4),
                findViewById(R.id.wave5)
        };

        listeningAnimation = new AnimatorSet();
        ArrayList<Animator> animators = new ArrayList<>(); // Use Animator, not ObjectAnimator

        for (int i = 0; i < waves.length; i++) {
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(waves[i], "scaleY", 0.5f, 2.0f);
            scaleY.setDuration(600);
            scaleY.setRepeatCount(ValueAnimator.INFINITE);
            scaleY.setRepeatMode(ValueAnimator.REVERSE);
            scaleY.setStartDelay(i * 100); // offset each wave
            animators.add(scaleY);
        }

        listeningAnimation.playTogether(animators);
        listeningAnimation.start();
    }


    private void stopListeningAnimation() {
        if (listeningAnimation != null) {
            listeningAnimation.cancel();
        }
    }

    private void animateListeningWaves(float rmsdB) {
        // Animate waves based on audio level
        float normalizedLevel = Math.min(rmsdB / 10.0f, 1.0f);
        // Implementation for wave animation based on audio level
    }

    private void animatePulse(boolean start) {
        if (start) {
            pulseEffect.setVisibility(View.VISIBLE);
            pulseAnimation.start();
        } else {
            pulseAnimation.cancel();
            pulseEffect.setVisibility(View.INVISIBLE);
        }
    }

    private void processVoiceCommand(String command) {
        updateResponse("Processing: " + command);

        // Check for wake words
        if (aiProcessor.isWakeWord(command)) {
            String response = "Yes " + userName + ", I'm listening. How can I help you?";
            updateResponse(response);
            speakText(response);
            return;
        }

        // Process the command with AI
        aiProcessor.processCommand(command, new VoxAIProcessor.AIResponseCallback() {
            @Override
            public void onResponse(String response) {
                runOnUiThread(() -> {
                    updateResponse(response);
                    speakText(response);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    updateResponse("Sorry, I encountered an error: " + error);
                    speakText("Sorry, I encountered an error while processing your request.");
                });
            }
        });
    }

    private void updateResponse(String response) {
        tvResponse.setText(response);
    }

    private void speakText(String text) {
        if (isTTSInitialized && textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void handleSpeechError(int errorCode) {
        String errorMessage;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                errorMessage = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                errorMessage = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                errorMessage = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                errorMessage = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                errorMessage = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                errorMessage = "No speech input matched";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                errorMessage = "Recognition service busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                errorMessage = "Server error";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                errorMessage = "No speech input";
                break;
            default:
                errorMessage = "Speech recognition error";
                break;
        }

        updateResponse("Speech Error: " + errorMessage);
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Vox Settings");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_settings, null);
        EditText etUserName = dialogView.findViewById(R.id.etUserName);
        etUserName.setText(userName);

        builder.setView(dialogView);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = etUserName.getText().toString().trim();
            if (!newName.isEmpty()) {
                userName = newName;
                preferences.edit().putString("user_name", userName).apply();
                updateResponse("Settings saved. Hello " + userName + "!");
                speakText("Settings saved. Hello " + userName);
            }
        });
        builder.setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private void toggleVoxPower() {
        isVoxActive = !isVoxActive;

        if (isVoxActive) {
            statusIndicator.setBackgroundResource(R.drawable.status_indicator_active);
            tvStatus.setText("ACTIVE");
            tvStatus.setTextColor(getResources().getColor(R.color.vox_green));
            updateResponse("Vox is now online and ready to assist.");
            speakText("Vox is now online and ready to assist " + userName);
        } else {
            statusIndicator.setBackgroundResource(R.drawable.status_indicator_inactive);
            tvStatus.setText("OFFLINE");
            tvStatus.setTextColor(getResources().getColor(R.color.vox_red));
            updateResponse("Vox is now offline.");
            speakText("Vox is going offline. Goodbye " + userName);
            stopListening();
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Text-to-Speech language not supported", Toast.LENGTH_SHORT).show();
            } else {
                isTTSInitialized = true;
                // Set voice parameters for a more AI-like sound
                textToSpeech.setSpeechRate(0.9f);
                textToSpeech.setPitch(1.0f);
            }
        } else {
            Toast.makeText(this, "Text-to-Speech initialization failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Audio permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Audio permission is required for voice commands", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (pulseAnimation != null) {
            pulseAnimation.cancel();
        }
        if (listeningAnimation != null) {
            listeningAnimation.cancel();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isListening) {
            stopListening();
        }
    }
}