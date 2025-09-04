package com.nayanpote.voxnayanai;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NeuralNetworkView extends View {

    private static final int MAX_NODES = 150;
    private static final float NODE_SIZE = 5f; // Slightly smaller for more nodes
    private static final float CONNECTION_ALPHA_SPEED = 0.6f;
    private static final float FLOAT_SPEED = 1.5f; // Speed of floating motion
    private static final float FLOAT_RANGE = 30f; // Range of floating motion
    private static final float MIN_CONNECTION_DISTANCE = 200f; // Maximum distance for connections

    private List<NeuralNode> nodes;
    private List<NeuralConnection> connections;
    private Paint nodePaint;
    private Paint connectionPaint;
    private Paint glowPaint;
    private Random random;
    private boolean isAnimating = false;
    private int viewWidth, viewHeight;
    private long startTime;

    // Animation thread
    private Thread animationThread;
    private boolean isRunning = false;

    public NeuralNetworkView(Context context) {
        super(context);
        init();
    }

    public NeuralNetworkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NeuralNetworkView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        nodes = new ArrayList<>();
        connections = new ArrayList<>();
        random = new Random();
        startTime = System.currentTimeMillis();

        // Initialize paints
        nodePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nodePaint.setStyle(Paint.Style.FILL);

        connectionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        connectionPaint.setStyle(Paint.Style.STROKE);
        connectionPaint.setStrokeWidth(3.0f);

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.FILL);

        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        initializeNeuralNetwork();
    }

    private void initializeNeuralNetwork() {
        nodes.clear();
        connections.clear();

        // Create nodes with floating properties - allow placement beyond view bounds
        for (int i = 0; i < MAX_NODES; i++) {
            // Extended range for initial positioning - can start outside view
            float x = -100 + random.nextFloat() * (viewWidth + 200);
            float y = -100 + random.nextFloat() * (viewHeight + 200);
            NeuralNodeType type = NeuralNodeType.values()[random.nextInt(NeuralNodeType.values().length)];

            // Create floating parameters
            float floatOffsetX = random.nextFloat() * FLOAT_RANGE - FLOAT_RANGE / 2;
            float floatOffsetY = random.nextFloat() * FLOAT_RANGE - FLOAT_RANGE / 2;
            float floatSpeedX = (random.nextFloat() - 0.5f) * FLOAT_SPEED;
            float floatSpeedY = (random.nextFloat() - 0.5f) * FLOAT_SPEED;

            nodes.add(new NeuralNode(x, y, type, floatOffsetX, floatOffsetY, floatSpeedX, floatSpeedY));
        }

        // Connect all nodes to create a full neural network
        createFullyConnectedNetwork();
    }

    private void createFullyConnectedNetwork() {
        // Connect each node to nearby nodes (not all to all to avoid visual clutter)
        for (int i = 0; i < nodes.size(); i++) {
            NeuralNode nodeA = nodes.get(i);

            // Sort other nodes by distance and connect to closest ones
            List<NeuralNode> nearbyNodes = new ArrayList<>();

            for (int j = 0; j < nodes.size(); j++) {
                if (i != j) {
                    NeuralNode nodeB = nodes.get(j);
                    float distance = calculateDistance(nodeA, nodeB);

                    if (distance < MIN_CONNECTION_DISTANCE) {
                        nearbyNodes.add(nodeB);
                    }
                }
            }

            // Connect to nearby nodes
            for (NeuralNode nodeB : nearbyNodes) {
                // Avoid duplicate connections
                boolean connectionExists = false;
                for (NeuralConnection existing : connections) {
                    if ((existing.nodeA == nodeA && existing.nodeB == nodeB) ||
                            (existing.nodeA == nodeB && existing.nodeB == nodeA)) {
                        connectionExists = true;
                        break;
                    }
                }

                if (!connectionExists) {
                    connections.add(new NeuralConnection(nodeA, nodeB));
                }
            }
        }

        // Ensure minimum connectivity - add some random long-distance connections
        int additionalConnections = Math.min(20, MAX_NODES / 3);
        for (int i = 0; i < additionalConnections; i++) {
            NeuralNode nodeA = nodes.get(random.nextInt(nodes.size()));
            NeuralNode nodeB = nodes.get(random.nextInt(nodes.size()));

            if (nodeA != nodeB) {
                boolean connectionExists = false;
                for (NeuralConnection existing : connections) {
                    if ((existing.nodeA == nodeA && existing.nodeB == nodeB) ||
                            (existing.nodeA == nodeB && existing.nodeB == nodeA)) {
                        connectionExists = true;
                        break;
                    }
                }

                if (!connectionExists) {
                    connections.add(new NeuralConnection(nodeA, nodeB));
                }
            }
        }
    }

    private float calculateDistance(NeuralNode nodeA, NeuralNode nodeB) {
        return (float) Math.sqrt(
                Math.pow(nodeA.baseX - nodeB.baseX, 2) + Math.pow(nodeA.baseY - nodeB.baseY, 2)
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isAnimating || nodes.isEmpty()) return;

        // Draw connections first (behind nodes)
        drawConnections(canvas);

        // Draw nodes on top
        drawNodes(canvas);
    }

    private void drawConnections(Canvas canvas) {
        for (NeuralConnection connection : connections) {
            // Calculate current positions
            float x1 = connection.nodeA.getCurrentX();
            float y1 = connection.nodeA.getCurrentY();
            float x2 = connection.nodeB.getCurrentX();
            float y2 = connection.nodeB.getCurrentY();

            // Only draw connections if at least one node is visible or close to visible area
            if (!isConnectionVisible(x1, y1, x2, y2)) {
                continue;
            }

            // Animate connection strength
            float alpha = connection.strength * 580f; // Reduced max alpha for subtlety
            int color = getConnectionColor(connection);

            connectionPaint.setColor(color);
            connectionPaint.setAlpha((int) Math.max(20, alpha));

            // Draw connection line with slight curve
            drawConnectionLine(canvas, x1, y1, x2, y2, connection);
        }
    }

    private boolean isConnectionVisible(float x1, float y1, float x2, float y2) {
        // Extended bounds to include connections that extend beyond view
        float margin = MIN_CONNECTION_DISTANCE;
        return (x1 >= -margin || x2 >= -margin) &&
                (x1 <= viewWidth + margin || x2 <= viewWidth + margin) &&
                (y1 >= -margin || y2 >= -margin) &&
                (y1 <= viewHeight + margin || y2 <= viewHeight + margin);
    }

    private void drawConnectionLine(Canvas canvas, float x1, float y1, float x2, float y2, NeuralConnection connection) {
        // Create slightly curved connection path for organic feel
        Path connectionPath = new Path();
        connectionPath.moveTo(x1, y1);

        // Calculate control points for subtle curve
        float midX = (x1 + x2) / 2;
        float midY = (y1 + y2) / 2;

        // Add slight curvature based on connection strength and time
        float time = (System.currentTimeMillis() - startTime) * 0.001f;
        float offset = connection.strength * 15f * (float) Math.sin(time * 0.5f + connection.phaseOffset);
        float controlX = midX + offset * (random.nextFloat() - 0.5f) * 0.5f;
        float controlY = midY + offset * (random.nextFloat() - 0.5f) * 0.5f;

        connectionPath.quadTo(controlX, controlY, x2, y2);
        canvas.drawPath(connectionPath, connectionPaint);

        // Draw signal flow for highly active connections
        if (connection.strength > 0.6f) {
            drawSignalFlow(canvas, x1, y1, x2, y2, connection);
        }
    }

    private void drawSignalFlow(Canvas canvas, float x1, float y1, float x2, float y2, NeuralConnection connection) {
        float time = (System.currentTimeMillis() + connection.flowOffset) % 3000 / 3000f;
        float signalX = x1 + (x2 - x1) * time;
        float signalY = y1 + (y2 - y1) * time;

        glowPaint.setColor(ContextCompat.getColor(getContext(), R.color.ai_neural_signal));
        glowPaint.setAlpha((int) (connection.strength * 200));
        canvas.drawCircle(signalX, signalY, 2.5f, glowPaint);
    }

    private void drawNodes(Canvas canvas) {
        for (NeuralNode node : nodes) {
            float currentX = node.getCurrentX();
            float currentY = node.getCurrentY();

            // Only draw nodes that are visible or close to the visible area
            float margin = NODE_SIZE * 4; // Small margin to smoothly fade in/out nodes
            if (currentX < -margin || currentX > viewWidth + margin ||
                    currentY < -margin || currentY > viewHeight + margin) {
                continue; // Skip drawing this node if it's too far outside
            }

            // Draw node glow with pulsing effect
            if (node.activity > 0.4f) {
                float glowSize = NODE_SIZE * (2f + node.activity * 0.5f);
                glowPaint.setColor(getNodeGlowColor(node.type));
                glowPaint.setAlpha((int) (node.activity * 80));
                canvas.drawCircle(currentX, currentY, glowSize, glowPaint);
            }

            // Draw node core
            nodePaint.setColor(getNodeColor(node.type));
            nodePaint.setAlpha((int) (120 + node.activity * 135));
            canvas.drawCircle(currentX, currentY, NODE_SIZE, nodePaint);

            // Draw node center highlight
            nodePaint.setColor(0xFFFFFFFF); // Pure white core
            nodePaint.setAlpha((int) (200 + node.activity * 55));
            canvas.drawCircle(currentX, currentY, NODE_SIZE * 0.3f, nodePaint);

            // Draw activity pulse rings
            if (node.activity > 0.8f) {
                drawActivityPulse(canvas, currentX, currentY, node);
            }
        }
    }

    private void drawActivityPulse(Canvas canvas, float x, float y, NeuralNode node) {
        float pulseRadius = NODE_SIZE + (node.pulsePhase * NODE_SIZE * 3);

        connectionPaint.setColor(getNodeColor(node.type));
        connectionPaint.setAlpha((int) ((1f - node.pulsePhase) * node.activity * 150));
        connectionPaint.setStrokeWidth(3f);
        connectionPaint.setStyle(Paint.Style.STROKE);

        canvas.drawCircle(x, y, pulseRadius, connectionPaint);

        connectionPaint.setStyle(Paint.Style.STROKE);
        connectionPaint.setStrokeWidth(1.0f);
    }

    private int getNodeColor(NeuralNodeType type) {
        switch (type) {
            case INPUT:
                return ContextCompat.getColor(getContext(), R.color.ai_neural_input);
            case PROCESSING:
                return ContextCompat.getColor(getContext(), R.color.ai_neural_processing);
            case OUTPUT:
                return ContextCompat.getColor(getContext(), R.color.ai_neural_output);
            case MEMORY:
                return ContextCompat.getColor(getContext(), R.color.ai_neural_memory);
            default:
                return ContextCompat.getColor(getContext(), R.color.ai_primary_cyan);
        }
    }

    private int getNodeGlowColor(NeuralNodeType type) {
        switch (type) {
            case INPUT:
                return ContextCompat.getColor(getContext(), R.color.ai_glow_green);
            case PROCESSING:
                return ContextCompat.getColor(getContext(), R.color.ai_glow_cyan);
            case OUTPUT:
                return ContextCompat.getColor(getContext(), R.color.ai_glow_blue);
            case MEMORY:
                return ContextCompat.getColor(getContext(), R.color.ai_glow_purple);
            default:
                return ContextCompat.getColor(getContext(), R.color.ai_glow_cyan);
        }
    }

    private int getConnectionColor(NeuralConnection connection) {
        if (connection.nodeA.type == NeuralNodeType.INPUT ||
                connection.nodeB.type == NeuralNodeType.INPUT) {
            return ContextCompat.getColor(getContext(), R.color.ai_connection_input);
        } else if (connection.nodeA.type == NeuralNodeType.OUTPUT ||
                connection.nodeB.type == NeuralNodeType.OUTPUT) {
            return ContextCompat.getColor(getContext(), R.color.ai_connection_output);
        } else {
            return ContextCompat.getColor(getContext(), R.color.ai_neural_connection);
        }
    }

    private void updateNeuralNetwork() {
        float currentTime = (System.currentTimeMillis() - startTime) * 0.001f;

        // Safely iterate over a copy of nodes
        for (NeuralNode node : new ArrayList<>(nodes)) {
            // Update floating position - no boundary restrictions
            node.updateFloatingPosition(currentTime);

            // Simulate complex neural activity with multiple wave patterns
            float activityWave1 = (float) Math.sin(currentTime * 1.5f + node.baseX * 0.008f);
            float activityWave2 = (float) Math.sin(currentTime * 0.8f + node.baseY * 0.006f);
            float activityWave3 = (float) Math.sin(currentTime * 2.2f + node.nodeId * 0.1f);

            node.activity = 0.4f + 0.6f * (activityWave1 * 0.4f + activityWave2 * 0.3f + activityWave3 * 0.3f);
            node.activity = Math.max(0.1f, Math.min(1.0f, node.activity));

            // Update pulse phase for highly active nodes
            if (node.activity > 0.8f) {
                node.pulsePhase += 0.06f;
                if (node.pulsePhase >= 1f) node.pulsePhase = 0f;
            }
        }

        // Safely iterate over a copy of connections
        for (NeuralConnection connection : new ArrayList<>(connections)) {
            // Calculate dynamic distance
            float currentDistance = (float) Math.sqrt(
                    Math.pow(connection.nodeA.getCurrentX() - connection.nodeB.getCurrentX(), 2) +
                            Math.pow(connection.nodeA.getCurrentY() - connection.nodeB.getCurrentY(), 2)
            );

            // Connection strength based on activity and proximity
            float activityInfluence = (connection.nodeA.activity + connection.nodeB.activity) / 2f;
            float distanceInfluence = Math.max(0.1f, 1f - (currentDistance / MIN_CONNECTION_DISTANCE));
            float targetStrength = activityInfluence * distanceInfluence;

            connection.strength += (targetStrength - connection.strength) * CONNECTION_ALPHA_SPEED;
            connection.strength = Math.max(0.05f, Math.min(1f, connection.strength));
        }
    }


    public void startNeuralAnimation() {
        if (isRunning) return;

        isAnimating = true;
        isRunning = true;
        startTime = System.currentTimeMillis();

        animationThread = new Thread(() -> {
            while (isRunning) {
                updateNeuralNetwork();
                post(this::invalidate);

                try {
                    Thread.sleep(25); // ~40 FPS for faster, smoother animation
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        animationThread.start();
    }

    public void stopNeuralAnimation() {
        isAnimating = false;
        isRunning = false;

        if (animationThread != null) {
            animationThread.interrupt();
            try {
                animationThread.join(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void setNeuralActivity(float intensity) {
        // Boost neural activity based on intensity (0.0 to 1.0)
        for (NeuralNode node : nodes) {
            node.baseActivity = 0.3f + intensity * 0.7f;
        }
    }

    // Enhanced Neural Network Classes
    private static class NeuralNode {
        float baseX, baseY; // Original position
        float floatOffsetX, floatOffsetY; // Current floating offset
        float floatSpeedX, floatSpeedY; // Floating speed
        float floatPhaseX, floatPhaseY; // Phase for floating motion
        float activity, baseActivity, pulsePhase;
        NeuralNodeType type;
        int nodeId;
        private static int nodeCounter = 0;

        NeuralNode(float x, float y, NeuralNodeType type, float floatOffsetX, float floatOffsetY,
                   float floatSpeedX, float floatSpeedY) {
            this.baseX = x;
            this.baseY = y;
            this.type = type;
            this.floatOffsetX = floatOffsetX;
            this.floatOffsetY = floatOffsetY;
            this.floatSpeedX = floatSpeedX;
            this.floatSpeedY = floatSpeedY;
            this.floatPhaseX = (float) (Math.random() * Math.PI * 2);
            this.floatPhaseY = (float) (Math.random() * Math.PI * 2);
            this.activity = 0.5f;
            this.baseActivity = 0.5f;
            this.pulsePhase = 0f;
            this.nodeId = nodeCounter++;
        }

        void updateFloatingPosition(float time) {
            // Smooth floating motion using sine waves - no boundary restrictions
            floatOffsetX = FLOAT_RANGE * (float) Math.sin(time * floatSpeedX + floatPhaseX);
            floatOffsetY = FLOAT_RANGE * (float) Math.sin(time * floatSpeedY + floatPhaseY);

            // Optional: Add gradual drift to make nodes explore different areas
            baseX += (float) Math.sin(time * 0.1f + nodeId * 0.01f) * 0.1f;
            baseY += (float) Math.cos(time * 0.15f + nodeId * 0.01f) * 0.1f;
        }

        float getCurrentX() {
            return baseX + floatOffsetX;
        }

        float getCurrentY() {
            return baseY + floatOffsetY;
        }
    }

    private static class NeuralConnection {
        NeuralNode nodeA, nodeB;
        float strength;
        float phaseOffset;
        long flowOffset;

        NeuralConnection(NeuralNode nodeA, NeuralNode nodeB) {
            this.nodeA = nodeA;
            this.nodeB = nodeB;
            this.strength = 0.2f + (float) Math.random() * 0.5f;
            this.phaseOffset = (float) (Math.random() * Math.PI * 2);
            this.flowOffset = (long) (Math.random() * 3000);
        }
    }

    private enum NeuralNodeType {
        INPUT, PROCESSING, OUTPUT, MEMORY
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopNeuralAnimation();
    }
}