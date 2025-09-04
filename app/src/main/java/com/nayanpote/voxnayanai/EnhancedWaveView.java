package com.nayanpote.voxnayanai;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class EnhancedWaveView extends View {

    private static final int WAVE_POINTS = 50;
    private static final float WAVE_SPEED = 0.05f;
    private static final float MAX_AMPLITUDE = 30f;

    private List<Float> waveHeights;
    private Paint wavePaint;
    private Paint glowPaint;
    private Paint particlePaint;
    private Path wavePath;
    private Path glowPath;

    private float currentAmplitude = 0f;
    private float targetAmplitude = 0f;
    private float waveOffset = 0f;
    private boolean isListening = false;
    private int viewWidth, viewHeight;

    // Animation thread
    private Thread animationThread;
    private boolean isRunning = false;

    // Wave particles
    private List<WaveParticle> particles;

    public EnhancedWaveView(Context context) {
        super(context);
        init();
    }

    public EnhancedWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EnhancedWaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        waveHeights = new ArrayList<>();
        particles = new ArrayList<>();

        // Initialize wave heights
        for (int i = 0; i < WAVE_POINTS; i++) {
            waveHeights.add(0f);
        }

        // Initialize paints
        wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wavePaint.setStyle(Paint.Style.STROKE);
        wavePaint.setStrokeWidth(2f);
        wavePaint.setStrokeCap(Paint.Cap.ROUND);
        wavePaint.setStrokeJoin(Paint.Join.ROUND);

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeWidth(4f);
        glowPaint.setStrokeCap(Paint.Cap.ROUND);
        glowPaint.setStrokeJoin(Paint.Join.ROUND);

        particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        particlePaint.setStyle(Paint.Style.FILL);

        wavePath = new Path();
        glowPath = new Path();

        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        setupGradients();
        initializeParticles();
    }

    private void setupGradients() {
        // Wave gradient
        LinearGradient waveGradient = new LinearGradient(
                0, 0, viewWidth, 0,
                new int[]{
                        ContextCompat.getColor(getContext(), R.color.ai_wave_start),
                        ContextCompat.getColor(getContext(), R.color.ai_wave_middle),
                        ContextCompat.getColor(getContext(), R.color.ai_wave_end)
                },
                new float[]{0f, 0.5f, 1f},
                Shader.TileMode.CLAMP
        );
        wavePaint.setShader(waveGradient);

        // Glow gradient
        LinearGradient glowGradient = new LinearGradient(
                0, 0, viewWidth, 0,
                new int[]{
                        ContextCompat.getColor(getContext(), R.color.ai_wave_glow_start),
                        ContextCompat.getColor(getContext(), R.color.ai_wave_glow_end)
                },
                new float[]{0f, 1f},
                Shader.TileMode.CLAMP
        );
        glowPaint.setShader(glowGradient);
    }

    private void initializeParticles() {
        particles.clear();
        for (int i = 0; i < 20; i++) {
            particles.add(new WaveParticle(
                    (float) Math.random() * viewWidth,
                    viewHeight / 2f + ((float) Math.random() - 0.5f) * 20f,
                    (float) Math.random() * 2f + 0.5f
            ));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (viewWidth == 0 || viewHeight == 0) return;

        float centerY = viewHeight / 2f;

        // Draw multiple wave layers for depth
        drawWaveLayer(canvas, centerY, 1f, 0f);           // Main wave
        drawWaveLayer(canvas, centerY, 0.6f, 0.3f);       // Secondary wave
        drawWaveLayer(canvas, centerY, 0.3f, 0.6f);       // Tertiary wave

        // Draw glow effect
        if (currentAmplitude > 0.1f) {
            drawGlowLayer(canvas, centerY);
        }

        // Draw wave particles
        if (isListening) {
            drawWaveParticles(canvas);
        }

        // Draw frequency bars
        if (currentAmplitude > 0.2f) {
            drawFrequencyBars(canvas);
        }
    }

    private void drawWaveLayer(Canvas canvas, float centerY, float amplitudeFactor, float phaseOffset) {
        wavePath.reset();

        float stepX = (float) viewWidth / (WAVE_POINTS - 1);
        boolean firstPoint = true;

        for (int i = 0; i < WAVE_POINTS; i++) {
            float x = i * stepX;

            // Create smooth wave using multiple sine waves
            float wave1 = (float) Math.sin((x * 0.02f) + waveOffset + phaseOffset) * currentAmplitude;
            float wave2 = (float) Math.sin((x * 0.015f) + waveOffset * 1.3f + phaseOffset) * currentAmplitude * 0.5f;
            float wave3 = (float) Math.sin((x * 0.025f) + waveOffset * 0.8f + phaseOffset) * currentAmplitude * 0.3f;

            float waveHeight = (wave1 + wave2 + wave3) * amplitudeFactor;
            float y = centerY + waveHeight;

            if (firstPoint) {
                wavePath.moveTo(x, y);
                firstPoint = false;
            } else {
                wavePath.lineTo(x, y);
            }
        }

        // Set alpha based on amplitude factor
        int alpha = (int) (255 * amplitudeFactor * (0.3f + currentAmplitude / MAX_AMPLITUDE * 0.7f));
        wavePaint.setAlpha(alpha);
        canvas.drawPath(wavePath, wavePaint);
    }

    private void drawGlowLayer(Canvas canvas, float centerY) {
        glowPath.reset();

        float stepX = (float) viewWidth / (WAVE_POINTS - 1);
        boolean firstPoint = true;

        for (int i = 0; i < WAVE_POINTS; i++) {
            float x = i * stepX;
            float waveHeight = (float) Math.sin((x * 0.02f) + waveOffset) * currentAmplitude * 0.8f;
            float y = centerY + waveHeight;

            if (firstPoint) {
                glowPath.moveTo(x, y);
                firstPoint = false;
            } else {
                glowPath.lineTo(x, y);
            }
        }

        glowPaint.setAlpha((int) (100 * currentAmplitude / MAX_AMPLITUDE));
        canvas.drawPath(glowPath, glowPaint);
    }

    private void drawWaveParticles(Canvas canvas) {
        for (WaveParticle particle : particles) {
            float alpha = particle.life * (currentAmplitude / MAX_AMPLITUDE) * 255f;
            particlePaint.setColor(ContextCompat.getColor(getContext(), R.color.ai_wave_particle));
            particlePaint.setAlpha((int) Math.max(30, alpha));

            canvas.drawCircle(particle.x, particle.y, particle.size, particlePaint);
        }
    }

    private void drawFrequencyBars(Canvas canvas) {
        int barCount = 10;
        float barWidth = viewWidth / (barCount * 2f);
        float spacing = barWidth * 0.5f;

        particlePaint.setColor(ContextCompat.getColor(getContext(), R.color.ai_frequency_bar));

        for (int i = 0; i < barCount; i++) {
            float x = spacing + i * (barWidth + spacing);
            float barHeight = (float) Math.random() * currentAmplitude * 0.8f + 5f;

            float alpha = currentAmplitude / MAX_AMPLITUDE * 255f;
            particlePaint.setAlpha((int) Math.max(50, alpha));

            canvas.drawRect(x, viewHeight / 2f - barHeight / 2f,
                    x + barWidth, viewHeight / 2f + barHeight / 2f,
                    particlePaint);
        }
    }

    private void updateWave() {
        // Smoothly interpolate amplitude
        currentAmplitude += (targetAmplitude - currentAmplitude) * 0.1f;

        // Update wave offset for animation
        waveOffset += WAVE_SPEED;
        if (waveOffset >= Math.PI * 2) {
            waveOffset -= Math.PI * 2;
        }

        // Update wave particles
        updateWaveParticles();
    }

    private void updateWaveParticles() {
        for (WaveParticle particle : particles) {
            // Move particle
            particle.x += particle.speed;

            // Add wave motion to Y
            particle.y = viewHeight / 2f +
                    (float) Math.sin(particle.x * 0.01f + waveOffset) * currentAmplitude * 0.3f +
                    ((float) Math.random() - 0.5f) * 10f;

            // Update life
            particle.life -= 0.01f;

            // Reset particle if it goes off screen or dies
            if (particle.x > viewWidth + 10 || particle.life <= 0) {
                particle.x = -10f;
                particle.y = viewHeight / 2f + ((float) Math.random() - 0.5f) * 20f;
                particle.life = 1f;
                particle.speed = (float) Math.random() * 2f + 0.5f;
            }
        }
    }

    public void updateAmplitude(float rmsdB) {
        // Convert RMS dB to amplitude (0-100 range typical for RMS)
        float normalizedAmplitude = Math.max(0, Math.min(100, rmsdB + 50)) / 100f;
        targetAmplitude = normalizedAmplitude * MAX_AMPLITUDE;
    }

    public void setAmplitude(float amplitude) {
        targetAmplitude = Math.max(0, Math.min(1, amplitude)) * MAX_AMPLITUDE;
    }

    public void startListening() {
        if (isRunning) return;

        isListening = true;
        isRunning = true;
        targetAmplitude = MAX_AMPLITUDE * 0.3f; // Base activity level

        animationThread = new Thread(() -> {
            while (isRunning) {
                updateWave();
                post(this::invalidate);

                try {
                    Thread.sleep(16); // ~60 FPS
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        animationThread.start();
    }

    public void stopListening() {
        isListening = false;
        isRunning = false;
        targetAmplitude = 0f;

        if (animationThread != null) {
            animationThread.interrupt();
            try {
                animationThread.join(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void setIntensity(float intensity) {
        // Set base intensity for idle animation
        targetAmplitude = intensity * MAX_AMPLITUDE;
    }

    // Wave particle class
    private static class WaveParticle {
        float x, y, speed, size, life;

        WaveParticle(float x, float y, float speed) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.size = 2f + (float) Math.random() * 3f;
            this.life = 1f;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopListening();
    }
}