package com.nayanpote.voxnayanai;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnhancedAnimationManager {

    private Context context;
    private Random random;

    // Core Animation Components
    private AnimatorSet idleAnimationSet;
    private AnimatorSet listeningAnimationSet;
    private AnimatorSet processingAnimationSet;
    private AnimatorSet responseAnimationSet;

    // View References
    private ImageView ivVoxCore;
    private View backgroundGlow, pulseEffect, voiceButtonGlow, statusIndicator;
    private CardView statusBarCard, responseCard, listeningCard;

    // Animation States
    private boolean isAnimating = false;
    private boolean isPaused = false;
    private List<AnimatorSet> activeAnimations;

    public EnhancedAnimationManager(Context context) {
        this.context = context;
        this.random = new Random();
        this.activeAnimations = new ArrayList<>();
    }

    public void initializeViews(ImageView voxCore, View bgGlow, View pulse, View voiceGlow,
                                View status,CardView statusCard, CardView respCard,
                                CardView listenCard) {
        this.ivVoxCore = voxCore;
        this.backgroundGlow = bgGlow;
        this.pulseEffect = pulse;
        this.voiceButtonGlow = voiceGlow;
        this.statusIndicator = status;
        this.statusBarCard = statusCard;
        this.responseCard = respCard;
        this.listeningCard = listenCard;
    }

    // ============ ENTRANCE ANIMATIONS ============

    public void playEntranceAnimation() {
        AnimatorSet entranceSet = new AnimatorSet();
        List<Animator> entranceAnimators = new ArrayList<>();

        // Status bar slide in from top
        if (statusBarCard != null) {
            statusBarCard.setTranslationY(-200f);
            statusBarCard.setAlpha(0f);
            ObjectAnimator statusSlide = ObjectAnimator.ofFloat(statusBarCard, "translationY", -200f, 0f);
            ObjectAnimator statusFade = ObjectAnimator.ofFloat(statusBarCard, "alpha", 0f, 1f);
            statusSlide.setDuration(800);
            statusFade.setDuration(800);
            statusSlide.setInterpolator(new OvershootInterpolator(1.2f));
            entranceAnimators.add(statusSlide);
            entranceAnimators.add(statusFade);
        }

        // Response card slide in from bottom
        if (responseCard != null) {
            responseCard.setTranslationY(200f);
            responseCard.setAlpha(0f);
            ObjectAnimator responseSlide = ObjectAnimator.ofFloat(responseCard, "translationY", 200f, 0f);
            ObjectAnimator responseFade = ObjectAnimator.ofFloat(responseCard, "alpha", 0f, 1f);
            responseSlide.setDuration(800);
            responseFade.setDuration(800);
            responseSlide.setStartDelay(600);
            responseFade.setStartDelay(600);
            responseSlide.setInterpolator(new OvershootInterpolator(1.1f));
            entranceAnimators.add(responseSlide);
            entranceAnimators.add(responseFade);
        }

        entranceSet.playTogether(entranceAnimators);
        entranceSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startIdleAnimations();
            }
        });

        activeAnimations.add(entranceSet);
        entranceSet.start();
    }

    // ============ IDLE ANIMATIONS ============

    public void startIdleAnimations() {
        if (isPaused) return;

        stopIdleAnimations();
        idleAnimationSet = new AnimatorSet();
        List<Animator> idleAnimators = new ArrayList<>();

        // Core breathing animation
        if (ivVoxCore != null) {
            ObjectAnimator coreBreathing = ObjectAnimator.ofPropertyValuesHolder(ivVoxCore,
                    PropertyValuesHolder.ofFloat("scaleX", 1f, 1.05f, 1f),
                    PropertyValuesHolder.ofFloat("scaleY", 1f, 1.05f, 1f));
            coreBreathing.setDuration(3000);
            coreBreathing.setRepeatCount(ValueAnimator.INFINITE);
            coreBreathing.setInterpolator(new AccelerateDecelerateInterpolator());
            idleAnimators.add(coreBreathing);
        }

        // Background glow pulsing
        if (backgroundGlow != null) {
            ObjectAnimator glowPulse = ObjectAnimator.ofFloat(backgroundGlow, "alpha", 0.3f, 0.7f, 0.3f);
            glowPulse.setDuration(4000);
            glowPulse.setRepeatCount(ValueAnimator.INFINITE);
            glowPulse.setInterpolator(new AccelerateDecelerateInterpolator());
            idleAnimators.add(glowPulse);
        }

        idleAnimationSet.playTogether(idleAnimators);
        activeAnimations.add(idleAnimationSet);
        idleAnimationSet.start();
    }

    public void stopIdleAnimations() {
        if (idleAnimationSet != null && idleAnimationSet.isRunning()) {
            idleAnimationSet.cancel();
            activeAnimations.remove(idleAnimationSet);
        }
    }

    // ============ LISTENING MODE ANIMATIONS ============

    public void startListeningMode() {
        stopIdleAnimations();
        listeningAnimationSet = new AnimatorSet();
        List<Animator> listeningAnimators = new ArrayList<>();

        // Voice button glow
        if (voiceButtonGlow != null) {
            voiceButtonGlow.setVisibility(View.VISIBLE);
            ObjectAnimator glowAnimation = ObjectAnimator.ofFloat(voiceButtonGlow, "alpha", 0f, 1f, 0f);
            glowAnimation.setDuration(800);
            glowAnimation.setRepeatCount(ValueAnimator.INFINITE);
            glowAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
            listeningAnimators.add(glowAnimation);
        }

        listeningAnimationSet.playTogether(listeningAnimators);
        activeAnimations.add(listeningAnimationSet);
        listeningAnimationSet.start();
    }

    public void stopListeningMode() {
        if (listeningAnimationSet != null && listeningAnimationSet.isRunning()) {
            listeningAnimationSet.cancel();
            activeAnimations.remove(listeningAnimationSet);
        }
        if (voiceButtonGlow != null) {
            voiceButtonGlow.setVisibility(View.INVISIBLE);
        }
        startIdleAnimations();
    }

    // ============ PROCESSING ANIMATIONS ============

    public void playProcessingAnimation() {
        processingAnimationSet = new AnimatorSet();
        List<Animator> processingAnimators = new ArrayList<>();

        if (ivVoxCore != null) {
            ObjectAnimator coreProcessing = ObjectAnimator.ofPropertyValuesHolder(ivVoxCore,
                    PropertyValuesHolder.ofFloat("scaleX", 1f, 1.3f, 1f),
                    PropertyValuesHolder.ofFloat("scaleY", 1f, 1.3f, 1f));
            coreProcessing.setDuration(1500);
            coreProcessing.setInterpolator(new AnticipateOvershootInterpolator(1.5f));
            processingAnimators.add(coreProcessing);
        }

        processingAnimationSet.playTogether(processingAnimators);
        activeAnimations.add(processingAnimationSet);
        processingAnimationSet.start();
    }

    public void playThinkingAnimation() {
        if (backgroundGlow != null) {
            ObjectAnimator thinkingGlow = ObjectAnimator.ofFloat(backgroundGlow, "alpha", 0.3f, 1f, 0.3f);
            thinkingGlow.setDuration(2000);
            thinkingGlow.setRepeatCount(3);
            thinkingGlow.setInterpolator(new AccelerateDecelerateInterpolator());
            thinkingGlow.start();
        }
    }

    // ============ RESPONSE ANIMATIONS ============

    public void playResponseAnimation() {
        responseAnimationSet = new AnimatorSet();
        List<Animator> responseAnimators = new ArrayList<>();

        if (responseCard != null) {
            ObjectAnimator cardPulse = ObjectAnimator.ofPropertyValuesHolder(responseCard,
                    PropertyValuesHolder.ofFloat("scaleX", 1f, 1.02f, 1f),
                    PropertyValuesHolder.ofFloat("scaleY", 1f, 1.02f, 1f));
            cardPulse.setDuration(500);
            cardPulse.setInterpolator(new OvershootInterpolator(1.1f));
            responseAnimators.add(cardPulse);
        }

        if (ivVoxCore != null) {
            ObjectAnimator coreResponse = ObjectAnimator.ofPropertyValuesHolder(ivVoxCore,
                    PropertyValuesHolder.ofFloat("scaleX", 1f, 1.1f, 1f),
                    PropertyValuesHolder.ofFloat("scaleY", 1f, 1.1f, 1f));
            coreResponse.setDuration(600);
            coreResponse.setInterpolator(new BounceInterpolator());
            responseAnimators.add(coreResponse);
        }

        responseAnimationSet.playTogether(responseAnimators);
        responseAnimationSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startIdleAnimations();
            }
        });
        activeAnimations.add(responseAnimationSet);
        responseAnimationSet.start();
    }

    // ============ SPECIAL EFFECT ANIMATIONS ============

//    public void playPulseEffect() {
//        if (pulseEffect != null) {
//            pulseEffect.setVisibility(View.VISIBLE);
//            pulseEffect.setScaleX(0.5f);
//            pulseEffect.setScaleY(0.5f);
//            pulseEffect.setAlpha(1f);
//
//            ObjectAnimator pulseAnim = ObjectAnimator.ofPropertyValuesHolder(pulseEffect,
//                    PropertyValuesHolder.ofFloat("scaleX", 0.5f, 2f),
//                    PropertyValuesHolder.ofFloat("scaleY", 0.5f, 2f),
//                    PropertyValuesHolder.ofFloat("alpha", 1f, 0f));
//            pulseAnim.setDuration(800);
//            pulseAnim.setInterpolator(new AccelerateInterpolator());
//            pulseAnim.addListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    pulseEffect.setVisibility(View.INVISIBLE);
//                }
//            });
//            pulseAnim.start();
//        }
//    }

    // ============ STATE-SPECIFIC ANIMATIONS ============

    public void playActivationAnimation() {
        AnimatorSet activationSet = new AnimatorSet();
        List<Animator> activationAnimators = new ArrayList<>();

        if (statusIndicator != null) {
            ObjectAnimator statusPulse = ObjectAnimator.ofFloat(statusIndicator, "alpha", 0f, 1f);
            statusPulse.setDuration(1000);
            statusPulse.setInterpolator(new BounceInterpolator());
            activationAnimators.add(statusPulse);
        }

//        playPulseEffect();

        if (!activationAnimators.isEmpty()) {
            activationSet.playTogether(activationAnimators);
            activationSet.start();
        }
    }

    public void playDeactivationAnimation() {
        stopAllAnimations();

        if (statusIndicator != null) {
            ObjectAnimator statusFade = ObjectAnimator.ofFloat(statusIndicator, "alpha", 1f, 0.3f);
            statusFade.setDuration(500);
            statusFade.setInterpolator(new DecelerateInterpolator());
            statusFade.start();
        }
    }

    public void playErrorAnimation() {
        if (ivVoxCore != null) {
            ObjectAnimator errorShake = ObjectAnimator.ofFloat(ivVoxCore, "translationX", 0f, -10f, 10f, -5f, 5f, 0f);
            errorShake.setDuration(400);
            errorShake.start();
        }
    }

    public void playWarningAnimation() {
        if (backgroundGlow != null) {
            ObjectAnimator warningFlash = ObjectAnimator.ofFloat(backgroundGlow, "alpha", 0.3f, 0.8f, 0.3f);
            warningFlash.setDuration(300);
            warningFlash.setRepeatCount(2);
            warningFlash.start();
        }
    }

    public void playSuccessAnimation() {
//        playPulseEffect();
        if (ivVoxCore != null) {
            ObjectAnimator successBounce = ObjectAnimator.ofPropertyValuesHolder(ivVoxCore,
                    PropertyValuesHolder.ofFloat("scaleX", 1f, 1.2f, 1f),
                    PropertyValuesHolder.ofFloat("scaleY", 1f, 1.2f, 1f));
            successBounce.setDuration(600);
            successBounce.setInterpolator(new BounceInterpolator());
            successBounce.start();
        }
    }

    // ============ NETWORK STATUS ANIMATIONS ============

    public void playNetworkConnectedAnimation() {
        playSuccessAnimation();
    }

    public void playNetworkErrorAnimation() {
        playErrorAnimation();
    }

    // ============ VOICE RELATED ANIMATIONS ============

    public void startVoiceInputMode() {
        startListeningMode();
    }

    public void stopVoiceInputMode() {
        stopListeningMode();
    }

    public void onSpeechDetected() {
        if (ivVoxCore != null) {
            ObjectAnimator speechDetected = ObjectAnimator.ofFloat(ivVoxCore, "alpha", 1f, 0.7f, 1f);
            speechDetected.setDuration(200);
            speechDetected.start();
        }
    }

    public void updateVoiceLevel(float rmsdB) {
        if (ivVoxCore != null) {
            float scale = 1f + (rmsdB / 100f);
            scale = Math.max(1f, Math.min(1.3f, scale));
            ivVoxCore.setScaleX(scale);
            ivVoxCore.setScaleY(scale);
        }
    }

    public void playSpeakingAnimation() {
        if (ivVoxCore != null) {
            ObjectAnimator speakingPulse = ObjectAnimator.ofPropertyValuesHolder(ivVoxCore,
                    PropertyValuesHolder.ofFloat("scaleX", 1f, 1.05f, 1f),
                    PropertyValuesHolder.ofFloat("scaleY", 1f, 1.05f, 1f));
            speakingPulse.setDuration(500);
            speakingPulse.setRepeatCount(ValueAnimator.INFINITE);
            speakingPulse.setInterpolator(new AccelerateDecelerateInterpolator());
            speakingPulse.start();
        }
    }

    // ============ UI INTERACTION ANIMATIONS ============

    public void playButtonClickAnimation(View view) {
        ObjectAnimator clickAnim = ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 0.95f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 0.95f, 1f));
        clickAnim.setDuration(150);
        clickAnim.setInterpolator(new OvershootInterpolator(1.5f));
        clickAnim.start();
    }

    public void animateResponseText() {
        if (responseCard != null) {
            ObjectAnimator textAnim = ObjectAnimator.ofFloat(responseCard, "alpha", 0.5f, 1f);
            textAnim.setDuration(300);
            textAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            textAnim.start();
        }
    }

    public void animateStatusIndicator(View view, boolean isActive) {
        ObjectAnimator statusAnim = ObjectAnimator.ofFloat(view, "alpha",
                isActive ? 0.5f : 1f, isActive ? 1f : 0.5f);
        statusAnim.setDuration(300);
        statusAnim.start();
    }

    // ============ DIALOG ANIMATIONS ============

    public void animateDialog(AlertDialog dialog) {
        if (dialog.getWindow() != null) {
            View decorView = dialog.getWindow().getDecorView();
            decorView.setScaleX(0.8f);
            decorView.setScaleY(0.8f);
            decorView.setAlpha(0f);

            ObjectAnimator dialogAnim = ObjectAnimator.ofPropertyValuesHolder(decorView,
                    PropertyValuesHolder.ofFloat("scaleX", 0.8f, 1f),
                    PropertyValuesHolder.ofFloat("scaleY", 0.8f, 1f),
                    PropertyValuesHolder.ofFloat("alpha", 0f, 1f));
            dialogAnim.setDuration(300);
            dialogAnim.setInterpolator(new OvershootInterpolator(1.1f));
            dialogAnim.start();
        }
    }

    // ============ LISTENING INDICATOR ANIMATIONS ============

    public void showListeningIndicator() {
        if (listeningCard != null) {
            listeningCard.setAlpha(0f);
            listeningCard.setScaleY(0.8f);
            listeningCard.setVisibility(View.VISIBLE);

            ObjectAnimator showAnim = ObjectAnimator.ofPropertyValuesHolder(listeningCard,
                    PropertyValuesHolder.ofFloat("alpha", 0f, 1f),
                    PropertyValuesHolder.ofFloat("scaleY", 0.8f, 1f));
            showAnim.setDuration(300);
            showAnim.setInterpolator(new OvershootInterpolator(1.2f));
            showAnim.start();
        }
    }

    public void hideListeningIndicator() {
        if (listeningCard != null) {
            ObjectAnimator hideAnim = ObjectAnimator.ofPropertyValuesHolder(listeningCard,
                    PropertyValuesHolder.ofFloat("alpha", 1f, 0f),
                    PropertyValuesHolder.ofFloat("scaleY", 1f, 0.8f));
            hideAnim.setDuration(200);
            hideAnim.setInterpolator(new AccelerateInterpolator());
            hideAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    listeningCard.setVisibility(View.GONE);
                }
            });
            hideAnim.start();
        }
    }

    // ============ POWER STATE ANIMATIONS ============

    public void playPowerToggleAnimation(boolean isActive) {
        if (isActive) {
            playActivationAnimation();
        } else {
            playDeactivationAnimation();
        }
    }

    public void playTTSInitializedAnimation() {
        if (ivVoxCore != null) {
            ObjectAnimator ttsReady = ObjectAnimator.ofFloat(ivVoxCore, "alpha", 1f, 0.5f, 1f);
            ttsReady.setDuration(400);
            ttsReady.setRepeatCount(1);
            ttsReady.start();
        }
    }

    // ============ PROCESSING TEXT UPDATES ============

    public void updateProcessingText() {
        if (responseCard != null) {
            ObjectAnimator processingTextAnim = ObjectAnimator.ofFloat(responseCard, "alpha", 0.7f, 1f);
            processingTextAnim.setDuration(200);
            processingTextAnim.start();
        }
    }

    // ============ CLEANUP AND CONTROL METHODS ============

    public void pauseAnimations() {
        isPaused = true;
        for (AnimatorSet animSet : activeAnimations) {
            if (animSet != null && animSet.isRunning()) {
                animSet.pause();
            }
        }
    }

    public void resumeAnimations() {
        isPaused = false;
        for (AnimatorSet animSet : activeAnimations) {
            if (animSet != null && animSet.isPaused()) {
                animSet.resume();
            }
        }
        // Restart idle animations if no other animations are running
        if (activeAnimations.isEmpty()) {
            startIdleAnimations();
        }
    }

    public void stopAllAnimations() {
        for (AnimatorSet animSet : activeAnimations) {
            if (animSet != null && animSet.isRunning()) {
                animSet.cancel();
            }
        }
        activeAnimations.clear();

        if (idleAnimationSet != null && idleAnimationSet.isRunning()) {
            idleAnimationSet.cancel();
        }
        if (listeningAnimationSet != null && listeningAnimationSet.isRunning()) {
            listeningAnimationSet.cancel();
        }
        if (processingAnimationSet != null && processingAnimationSet.isRunning()) {
            processingAnimationSet.cancel();
        }
        if (responseAnimationSet != null && responseAnimationSet.isRunning()) {
            responseAnimationSet.cancel();
        }
    }

    public void cleanup() {
        stopAllAnimations();
        context = null;
        random = null;

        // Clear view references
        ivVoxCore = null;
        backgroundGlow = null;
        pulseEffect = null;
        voiceButtonGlow = null;
        statusIndicator = null;
        statusBarCard = null;
        responseCard = null;
        listeningCard = null;
    }

    // ============ UTILITY METHODS ============

    private float getRandomFloat(float min, float max) {
        return min + (max - min) * random.nextFloat();
    }

    private int getRandomInt(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }

    public boolean isAnimating() {
        return isAnimating;
    }

    public void setAnimating(boolean animating) {
        isAnimating = animating;
    }
}