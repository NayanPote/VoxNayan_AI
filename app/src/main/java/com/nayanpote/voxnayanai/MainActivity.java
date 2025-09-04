package com.nayanpote.voxnayanai;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.ImageDecoder;
import android.graphics.drawable.AnimatedImageDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final int REQUEST_RECORD_AUDIO = 1001;
    private static final int REQUEST_OVERLAY_PERMISSION = 1002;

    // UI Components
    private TextView tvResponse, tvStatus, tvNetworkStatus, tvWakeWordStatus;
    private ImageButton btnVoice, btnSettings, btnlens, btnPower;
    private CardView listeningCard, statusBarCard, responseCard;
    private LinearLayout listeningIndicator;
    private View statusIndicator, backgroundGlow, pulseEffect, voiceButtonGlow;
    private ImageView ivVoxCore;

    // Custom Views
    private NeuralNetworkView neuralNetwork;
    private EnhancedWaveView waveVisualization;

    // Core Components
    private TextToSpeech textToSpeech;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private VoxAIProcessor aiProcessor;
    private SharedPreferences preferences;

    // Animation Components
    private EnhancedAnimationManager animationManager;

    // State Variables
    private boolean isListening = false;
    private boolean isTTSInitialized = false;
    private boolean isVoxActive = true;
    private boolean isNetworkConnected = false;
    private String userName = "User";

    // Network Monitor
    private BroadcastReceiver networkReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        initializeCustomViews();
        initializePreferences();
        initializeNetworkMonitor();
        initializeTextToSpeech();
        initializeSpeechRecognizer();
        initializeAIProcessor();
        initializeAnimationManager();
        setupClickListeners();

        checkPermissions();
        checkNetworkStatus();
        startVoxService();
        startEntranceAnimations();

        // Welcome message
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            String welcomeMessage = "Hello " + userName + ",  Neural networks initialized and ready to assist.";
            updateResponse(welcomeMessage);
            speakText(welcomeMessage);
            animationManager.startIdleAnimations();
        }, 2000);

        setupAnimatedLogo();
    }

    private void initializeViews() {
        // Text Views
        tvResponse = findViewById(R.id.tvResponse);
        tvStatus = findViewById(R.id.tvStatus);
        tvNetworkStatus = findViewById(R.id.tvNetworkStatus);
        tvWakeWordStatus = findViewById(R.id.tvWakeWordStatus);

        // Buttons
        btnVoice = findViewById(R.id.btnVoice);
        btnSettings = findViewById(R.id.btnSettings);
        btnPower = findViewById(R.id.btnPower);
        btnlens = findViewById(R.id.btnlens);

        // Cards and Containers
        listeningCard = findViewById(R.id.listeningCard);
        statusBarCard = findViewById(R.id.statusBarCard);
        responseCard = findViewById(R.id.responseCard);
        listeningIndicator = findViewById(R.id.listeningIndicator);

        // Visual Effects
        statusIndicator = findViewById(R.id.statusIndicator);
        backgroundGlow = findViewById(R.id.backgroundGlow);
        pulseEffect = findViewById(R.id.pulseEffect);
        voiceButtonGlow = findViewById(R.id.voiceButtonGlow);

        ivVoxCore = findViewById(R.id.ivVoxCore);

    }

    private void initializeCustomViews() {
        neuralNetwork = findViewById(R.id.neuralNetwork);
        waveVisualization = findViewById(R.id.waveVisualization);

        // Initialize neural network
        if (neuralNetwork != null) {
            neuralNetwork.startNeuralAnimation();
        }
    }

    private void initializePreferences() {
        preferences = getSharedPreferences("VoxPreferences", Context.MODE_PRIVATE);
        userName = preferences.getString("user_name", "User");
    }

    private void initializeNetworkMonitor() {
        networkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                checkNetworkStatus();
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, filter);
    }

    private void checkNetworkStatus() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean wasConnected = isNetworkConnected;
        isNetworkConnected = activeNetwork != null && activeNetwork.isConnected();

        updateNetworkStatusUI();

        // Notify user about network status change
        if (wasConnected && !isNetworkConnected) {
            updateResponse("Network disconnected. Switching to offline mode with limited functionality.");
            speakText("Network disconnected. Operating in offline mode.");
            animationManager.playNetworkErrorAnimation();
        } else if (!wasConnected && isNetworkConnected) {
            updateResponse("Network connected. Full neural network functionality restored.");
            speakText("Network connected. All systems operational.");
            animationManager.playNetworkConnectedAnimation();
        }
    }

    private void updateNetworkStatusUI() {
        if (tvNetworkStatus != null) {
            if (isNetworkConnected) {
                tvNetworkStatus.setText("ONLINE");
                tvNetworkStatus.setTextColor(getResources().getColor(R.color.ai_success_green));
                animationManager.animateStatusIndicator(tvNetworkStatus, true);
            } else {
                tvNetworkStatus.setText("OFFLINE");
                tvNetworkStatus.setTextColor(getResources().getColor(R.color.ai_error_red));
                animationManager.animateStatusIndicator(tvNetworkStatus, false);

                // Stop listening when going offline
                if (isListening) {
                    stopListening();
                    updateResponse("Network connection lost. Voice recognition paused.");
                }
            }
        }
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
                animationManager.startListeningMode();
            }

            @Override
            public void onBeginningOfSpeech() {
                animationManager.onSpeechDetected();
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                if (waveVisualization != null) {
                    waveVisualization.updateAmplitude(rmsdB);
                }
                animationManager.updateVoiceLevel(rmsdB);
            }

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                showListening(false);
                animationManager.stopListeningMode();
            }

            @Override
            public void onError(int error) {
                showListening(false);
                isListening = false;
                animationManager.playErrorAnimation();
                handleSpeechError(error);
            }

            @Override
            public void onResults(Bundle results) {
                showListening(false);
                isListening = false;
                animationManager.stopListeningMode();
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spokenText = matches.get(0);
                    animationManager.playProcessingAnimation();
                    processVoiceCommand(spokenText);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> partialMatches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (partialMatches != null && !partialMatches.isEmpty()) {
                    updateResponse("Processing: " + partialMatches.get(0));
                    animationManager.updateProcessingText();
                }
            }

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void initializeAIProcessor() {
        aiProcessor = new VoxAIProcessor(this);
    }

    private void initializeAnimationManager() {
        animationManager = new EnhancedAnimationManager(this);
        animationManager.initializeViews(
               ivVoxCore,
                backgroundGlow, pulseEffect, voiceButtonGlow, statusIndicator,
                statusBarCard, responseCard, listeningCard
        );
    }

    private void setupClickListeners() {
        btnVoice.setOnClickListener(v -> {
            animationManager.playButtonClickAnimation(v);
            toggleListening();
        });

        btnSettings.setOnClickListener(v -> {
            animationManager.playButtonClickAnimation(v);
            showSettingsDialog();
        });

        btnlens.setOnClickListener(v -> {
            Intent intent = new Intent(this, nayanlens.class);
            startActivity(intent);

            animationManager.playButtonClickAnimation(v);
        });

        btnPower.setOnClickListener(v -> {
            animationManager.playButtonClickAnimation(v);
            toggleVoxPower();
        });
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
        serviceIntent.putExtra("user_name", userName);
        serviceIntent.putExtra("is_active", isVoxActive);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void startEntranceAnimations() {
        animationManager.playEntranceAnimation();
    }

    private void setupAnimatedLogo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                ImageDecoder.Source source = ImageDecoder.createSource(getResources(), R.drawable.animated_logo);
                AnimatedImageDrawable gifDrawable = (AnimatedImageDrawable) ImageDecoder.decodeDrawable(source);
                ivVoxCore.setImageDrawable(gifDrawable);
                gifDrawable.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void toggleListening() {
        if (!isVoxActive) {
            updateResponse("Vox neural network is offline. Please activate the system first.");
            speakText("Neural network is offline. Please activate me first.");
            animationManager.playWarningAnimation();
            return;
        }

        if (!isNetworkConnected) {
            updateResponse("No network connection detected. Voice recognition requires internet connectivity.");
            speakText("Network connection required for voice recognition.");
            animationManager.playNetworkErrorAnimation();
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
            animationManager.startVoiceInputMode();
            updateResponse("Neural network listening...");
        } else {
            Toast.makeText(this, "Audio permission required for voice input", Toast.LENGTH_SHORT).show();
            animationManager.playErrorAnimation();
        }
    }

    private void stopListening() {
        isListening = false;
        speechRecognizer.stopListening();
        showListening(false);
        animationManager.stopVoiceInputMode();
        updateResponse("Voice input terminated.");
    }

    private void showListening(boolean listening) {
        listeningCard.setVisibility(listening ? View.VISIBLE : View.GONE);
        if (listening) {
            animationManager.showListeningIndicator();
            if (waveVisualization != null) {
                waveVisualization.startListening();
            }
        } else {
            animationManager.hideListeningIndicator();
            if (waveVisualization != null) {
                waveVisualization.stopListening();
            }
        }
    }

    private void processVoiceCommand(String command) {
        updateResponse("Neural processing: " + command);
        animationManager.playThinkingAnimation();

        // Check for wake words
        if (aiProcessor.isWakeWord(command)) {
            String response = "Yes " + userName + ", neural networks are active. How may I assist you?";
            updateResponse(response);
            speakText(response);
            animationManager.playResponseAnimation();
            return;
        }

        // Process the command with AI
        aiProcessor.processCommand(command, new VoxAIProcessor.AIResponseCallback() {
            @Override
            public void onResponse(String response) {
                runOnUiThread(() -> {
                    updateResponse(response);
                    speakText(response);
                    animationManager.playResponseAnimation();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    updateResponse("Neural processing error: " + error);
                    speakText("I encountered an error while processing your request.");
                    animationManager.playErrorAnimation();
                });
            }
        });
    }

    private void updateResponse(String response) {
        tvResponse.setText(response);
        animationManager.animateResponseText();
    }

    private void speakText(String text) {
        if (isTTSInitialized && textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            animationManager.playSpeakingAnimation();
        }
    }

    private void handleSpeechError(int errorCode) {
        String errorMessage;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                errorMessage = "Audio recording error detected";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                errorMessage = "Client side error occurred";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                errorMessage = "Insufficient permissions for audio";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                errorMessage = "Network connectivity error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                errorMessage = "Network timeout occurred";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                errorMessage = "No speech pattern matched";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                errorMessage = "Recognition service busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                errorMessage = "Server error encountered";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                errorMessage = "No speech input detected";
                break;
            default:
                errorMessage = "Unknown speech recognition error";
                break;
        }

        updateResponse("Speech Error: " + errorMessage);
        speakText("I encountered a speech recognition error. Please try again.");
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.EnhancedDialogTheme);
        builder.setTitle("Vox AI Neural Settings");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_settings, null);
        EditText etUserName = dialogView.findViewById(R.id.etUserName);
        etUserName.setText(userName);

        builder.setView(dialogView);
        builder.setPositiveButton("Save Configuration", (dialog, which) -> {
            String newName = etUserName.getText().toString().trim();
            if (!newName.isEmpty()) {
                userName = newName;
                preferences.edit().putString("user_name", userName).apply();
                updateResponse("Neural configuration updated. Hello " + userName + "!");
                speakText("Configuration saved. Hello " + userName);
                animationManager.playSuccessAnimation();

                // Update service with new user name
                Intent serviceIntent = new Intent(this, VoxService.class);
                serviceIntent.putExtra("user_name", userName);
                serviceIntent.putExtra("is_active", isVoxActive);
                startService(serviceIntent);
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
        animationManager.animateDialog(dialog);
    }


    private void toggleVoxPower() {
        isVoxActive = !isVoxActive;
        animationManager.playPowerToggleAnimation(isVoxActive);

        if (isVoxActive) {
            statusIndicator.setBackgroundResource(R.drawable.enhanced_status_active);
            tvStatus.setText("ACTIVE");
            tvStatus.setTextColor(getResources().getColor(R.color.ai_success_green));
            updateResponse("Vox AI neural network is now online and fully operational.");
            speakText("Neural network activated. All systems operational, " + userName);
            animationManager.playActivationAnimation();
        } else {
            statusIndicator.setBackgroundResource(R.drawable.enhanced_status_inactive);
            tvStatus.setText("STANDBY");
            tvStatus.setTextColor(getResources().getColor(R.color.ai_error_red));
            updateResponse("Vox AI neural network entering standby mode.");
            speakText("Neural network entering standby mode. Goodbye " + userName);
            stopListening();
            animationManager.playDeactivationAnimation();
        }

        // Update service status
        Intent serviceIntent = new Intent(this, VoxService.class);
        serviceIntent.putExtra("user_name", userName);
        serviceIntent.putExtra("is_active", isVoxActive);
        startService(serviceIntent);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Text-to-Speech language not supported", Toast.LENGTH_SHORT).show();
            } else {
                isTTSInitialized = true;
                textToSpeech.setSpeechRate(0.9f);
                textToSpeech.setPitch(1.0f);
                animationManager.playTTSInitializedAnimation();
            }
        } else {
            Toast.makeText(this, "Text-to-Speech initialization failed", Toast.LENGTH_SHORT).show();
            animationManager.playErrorAnimation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Audio permission granted", Toast.LENGTH_SHORT).show();
                animationManager.playSuccessAnimation();
            } else {
                Toast.makeText(this, "Audio permission required for voice commands", Toast.LENGTH_LONG).show();
                animationManager.playWarningAnimation();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (networkReceiver != null) {
            try {
                unregisterReceiver(networkReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }

        if (animationManager != null) {
            animationManager.cleanup();
        }

        if (neuralNetwork != null) {
            neuralNetwork.stopNeuralAnimation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isListening) {
            stopListening();
        }
        if (animationManager != null) {
            animationManager.pauseAnimations();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNetworkStatus();
        if (animationManager != null) {
            animationManager.resumeAnimations();
        }
    }
}