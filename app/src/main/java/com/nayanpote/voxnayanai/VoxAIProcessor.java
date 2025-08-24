package com.nayanpote.voxnayanai;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class VoxAIProcessor {

    private Context context;
    private String[] wakeWords = {
            "hey vox", "hi vox", "hello vox", "vox",
            "ok vox", "wake up vox", "vox wake up"
    };

    private String[] greetingResponses = {
            "Hello! How can I assist you today?",
            "Hi there! What can I do for you?",
            "Greetings! I'm here to help.",
            "Hello! Ready to assist you.",
            "Hi! How may I help you today?"
    };

    public interface AIResponseCallback {
        void onResponse(String response);
        void onError(String error);
    }

    public VoxAIProcessor(Context context) {
        this.context = context;
    }

    public boolean isWakeWord(String input) {
        if (input == null) return false;
        String lowerInput = input.toLowerCase().trim();

        for (String wakeWord : wakeWords) {
            if (lowerInput.contains(wakeWord)) {
                return true;
            }
        }
        return false;
    }

    public void processCommand(String command, AIResponseCallback callback) {
        if (TextUtils.isEmpty(command)) {
            callback.onError("Empty command received");
            return;
        }

        String lowerCommand = command.toLowerCase().trim();

        // Handle system commands first
        if (handleSystemCommands(lowerCommand, callback)) {
            return;
        }

        // Handle app commands
        if (handleAppCommands(lowerCommand, callback)) {
            return;
        }

        // Handle information queries with enhanced API support
        if (handleInformationQueries(lowerCommand, callback)) {
            return;
        }

        // Handle entertainment commands
        if (handleEntertainmentCommands(lowerCommand, callback)) {
            return;
        }

        // Handle general conversation
        handleGeneralConversation(lowerCommand, callback);
    }

    private boolean handleSystemCommands(String command, AIResponseCallback callback) {
        if (command.contains("volume up") || command.contains("increase volume")) {
            adjustVolume(true);
            callback.onResponse("Volume increased.");
            return true;
        }

        if (command.contains("volume down") || command.contains("decrease volume") || command.contains("lower volume")) {
            adjustVolume(false);
            callback.onResponse("Volume decreased.");
            return true;
        }

        if (command.contains("what time") || command.contains("current time") || command.contains("time is")) {
            String currentTime = getCurrentTime();
            callback.onResponse("The current time is " + currentTime);
            return true;
        }

        if (command.contains("what date") || command.contains("today's date") || command.contains("current date")) {
            String currentDate = getCurrentDate();
            callback.onResponse("Today is " + currentDate);
            return true;
        }

        if (command.contains("battery") || command.contains("battery level")) {
            String batteryInfo = getBatteryInfo();
            callback.onResponse(batteryInfo);
            return true;
        }

        if (command.contains("wifi") || command.contains("internet") || command.contains("connection")) {
            String networkInfo = getNetworkInfo();
            callback.onResponse(networkInfo);
            return true;
        }

        return false;
    }

    private boolean handleAppCommands(String command, AIResponseCallback callback) {
        if (command.contains("open") || command.contains("launch") || command.contains("start")) {
            String appName = extractAppName(command);
            if (!TextUtils.isEmpty(appName)) {
                if (openApp(appName)) {
                    callback.onResponse("Opening " + appName + ".");
                } else {
                    callback.onResponse("Sorry, I couldn't find or open " + appName + ".");
                }
                return true;
            }
        }

        if (command.contains("play music") || command.contains("music") || command.contains("songs")) {
            openMusicApp();
            callback.onResponse("Opening music player.");
            return true;
        }

        if (command.contains("camera") || command.contains("take photo") || command.contains("take picture")) {
            openCamera();
            callback.onResponse("Opening camera.");
            return true;
        }

        if (command.contains("call") || command.contains("phone") || command.contains("dial")) {
            openDialer();
            callback.onResponse("Opening phone dialer.");
            return true;
        }

        if (command.contains("calculator") || command.contains("calculate")) {
            openCalculator();
            callback.onResponse("Opening calculator.");
            return true;
        }

        if (command.contains("browser") || command.contains("internet") || command.contains("chrome") || command.contains("firefox")) {
            openBrowser();
            callback.onResponse("Opening web browser.");
            return true;
        }

        return false;
    }

    private boolean handleInformationQueries(String command, AIResponseCallback callback) {
        if (!isNetworkAvailable()) {
            callback.onResponse("Sorry, I need an internet connection to search for information.");
            return true;
        }

        if (command.contains("what is") || command.contains("tell me about") ||
                command.contains("define") || command.contains("explain") || command.contains("who is")) {

            String query = extractQuery(command);
            if (!TextUtils.isEmpty(query)) {
                if (query.contains("country") || isCountryQuery(query)) {
                    NetworkHelper.searchCountryInfo(query, new NetworkHelper.NetworkCallback() {
                        @Override
                        public void onSuccess(String result) {
                            callback.onResponse(result);
                        }

                        @Override
                        public void onError(String error) {
                            // Fallback to Wikipedia
                            searchWikipediaInfo(query, callback);
                        }
                    });
                } else {
                    searchWikipediaInfo(query, callback);
                }
                return true;
            }
        }

        if (command.contains("weather") || command.contains("temperature")) {
            callback.onResponse("For accurate weather information, I recommend checking your weather app or asking me to open it. Weather services require location access which I don't currently have.");
            return true;
        }

        if (command.contains("news") || command.contains("latest news")) {
            callback.onResponse("For the latest news, I can open your news app. Would you like me to do that?");
            return true;
        }

        return false;
    }

    private boolean handleEntertainmentCommands(String command, AIResponseCallback callback) {
        if (command.contains("tell me a joke") || command.contains("joke") || command.contains("funny")) {
            NetworkHelper.getRandomJoke(new NetworkHelper.NetworkCallback() {
                @Override
                public void onSuccess(String result) {
                    callback.onResponse(result);
                }

                @Override
                public void onError(String error) {
                    String[] jokes = {
                            "Why don't scientists trust atoms? Because they make up everything!",
                            "Why did the AI go to therapy? It had too many deep learning issues!",
                            "What do you call a robot that takes the long way around? R2-Detour!"
                    };
                    Random random = new Random();
                    callback.onResponse(jokes[random.nextInt(jokes.length)]);
                }
            });
            return true;
        }

        if (command.contains("cat fact") || command.contains("tell me about cats")) {
            NetworkHelper.getCatFact(new NetworkHelper.NetworkCallback() {
                @Override
                public void onSuccess(String result) {
                    callback.onResponse(result);
                }

                @Override
                public void onError(String error) {
                    callback.onResponse("Here's a cat fact: Cats spend 70% of their lives sleeping, which is 13-16 hours a day.");
                }
            });
            return true;
        }

        if (command.contains("number") && (command.contains("fact") || command.contains("trivia"))) {
            String number = extractNumber(command);
            if (number != null) {
                NetworkHelper.getNumberFact(number, new NetworkHelper.NetworkCallback() {
                    @Override
                    public void onSuccess(String result) {
                        callback.onResponse(result);
                    }

                    @Override
                    public void onError(String error) {
                        callback.onResponse("I couldn't fetch a fact about that number right now.");
                    }
                });
                return true;
            }
        }

        if (command.contains("roll dice") || command.contains("dice roll") || command.contains("random number")) {
            Random random = new Random();
            int dice = random.nextInt(6) + 1;
            callback.onResponse("I rolled a " + dice + " for you!");
            return true;
        }

        if (command.contains("flip coin") || command.contains("coin flip") || command.contains("heads or tails")) {
            Random random = new Random();
            String result = random.nextBoolean() ? "Heads" : "Tails";
            callback.onResponse("The coin landed on " + result + "!");
            return true;
        }

        return false;
    }

    private void handleGeneralConversation(String command, AIResponseCallback callback) {
        if (command.contains("hello") || command.contains("hi") || command.contains("hey")) {
            Random random = new Random();
            String response = greetingResponses[random.nextInt(greetingResponses.length)];
            callback.onResponse(response);
            return;
        }

        if (command.contains("how are you") || command.contains("how do you do")) {
            String[] responses = {
                    "I'm functioning perfectly and ready to assist you!",
                    "All systems operational and ready to help!",
                    "I'm doing great! How can I help you today?",
                    "Running smoothly and eager to assist!"
            };
            Random random = new Random();
            callback.onResponse(responses[random.nextInt(responses.length)]);
            return;
        }

        if (command.contains("what can you do") || command.contains("help") || command.contains("capabilities")) {
            String capabilities = "I can help you with: opening apps, controlling volume, " +
                    "telling time and date, searching for information, telling jokes, " +
                    "providing facts, rolling dice, flipping coins, and much more! " +
                    "Just ask me naturally and I'll do my best to help.";
            callback.onResponse(capabilities);
            return;
        }

        if (command.contains("thank you") || command.contains("thanks")) {
            String[] responses = {
                    "You're welcome! Happy to help.",
                    "My pleasure! Anything else I can do?",
                    "Glad I could help!",
                    "You're very welcome!"
            };
            Random random = new Random();
            callback.onResponse(responses[random.nextInt(responses.length)]);
            return;
        }

        if (command.contains("goodbye") || command.contains("bye") || command.contains("see you")) {
            String[] responses = {
                    "Goodbye! Have a great day!",
                    "See you later! Take care!",
                    "Bye! I'll be here when you need me.",
                    "Farewell! Have a wonderful day!"
            };
            Random random = new Random();
            callback.onResponse(responses[random.nextInt(responses.length)]);
            return;
        }

        if (command.contains("who are you") || command.contains("what are you")) {
            callback.onResponse("I'm Vox, your AI assistant. I'm here to help you with various tasks, " +
                    "answer questions, and make your life easier. Think of me as your personal digital companion!");
            return;
        }

        if (command.contains("who made you") || command.contains("who is your developer")) {
            callback.onResponse("Vox Ai Assistant is developed by super smart man nayan pote. " +
                    "he made me with full dedication");
            return;
        }

        if (command.contains("who is nayan pote") || command.contains("you know nayan pote")) {
            callback.onResponse("A very good friend of mine. he made me. and he is still working. to make me better");
            return;
        }

        if (command.contains("love you") || command.contains("like you")) {
            callback.onResponse("That's very kind of you! I'm happy to be your helpful assistant. " +
                    "Is there anything specific I can help you with today?");
            return;
        }

        // Default responses for unrecognized commands
        String[] defaultResponses = {
                "I'm not sure I understand that. Could you please rephrase?",
                "I didn't quite get that. Can you try saying it differently?",
                "I'm still learning. Could you be more specific?",
                "Sorry, I don't understand that command yet. Try asking me something else!",
                "Hmm, I'm not sure about that. Can you ask me in a different way?"
        };

        Random random = new Random();
        String response = defaultResponses[random.nextInt(defaultResponses.length)];
        callback.onResponse(response);
    }

    private void searchWikipediaInfo(String query, AIResponseCallback callback) {
        NetworkHelper.searchWikipedia(query, new NetworkHelper.NetworkCallback() {
            @Override
            public void onSuccess(String result) {
                callback.onResponse(result);
            }

            @Override
            public void onError(String error) {
                callback.onResponse("I couldn't find specific information about that. " +
                        "You might want to search online for more details.");
            }
        });
    }

    // Helper methods
    private void adjustVolume(boolean increase) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            int direction = increase ? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_LOWER;
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI);
        }
    }

    private String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return timeFormat.format(calendar.getTime());
    }

    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    private String getBatteryInfo() {
        return "You can check your battery level in the status bar or settings.";
    }

    private String getNetworkInfo() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return "You are connected to WiFi.";
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                return "You are connected to mobile data.";
            }
        }
        return "No internet connection detected.";
    }

    private String extractAppName(String command) {
        String[] commonWords = {"open", "launch", "start", "the", "app", "application"};
        String cleanCommand = command;

        for (String word : commonWords) {
            cleanCommand = cleanCommand.replace(word, "").trim();
        }

        return cleanCommand;
    }

    private boolean openApp(String appName) {
        try {
            PackageManager pm = context.getPackageManager();
            List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

            for (ApplicationInfo app : apps) {
                String name = pm.getApplicationLabel(app).toString().toLowerCase();
                if (name.contains(appName.toLowerCase())) {
                    Intent launchIntent = pm.getLaunchIntentForPackage(app.packageName);
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(launchIntent);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void openMusicApp() {
        Intent intent = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Intent musicIntent = new Intent(Intent.ACTION_VIEW);
            musicIntent.setType("audio/*");
            musicIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(musicIntent);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openDialer() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openCalculator() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_APP_CALCULATOR);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            // Try alternative method
            openApp("calculator");
        }
    }

    private void openBrowser() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String extractQuery(String command) {
        String[] prefixes = {"what is", "tell me about", "define", "explain", "who is", "where is"};
        String query = command;

        for (String prefix : prefixes) {
            if (command.contains(prefix)) {
                query = command.substring(command.indexOf(prefix) + prefix.length()).trim();
                break;
            }
        }

        return query;
    }

    private String extractNumber(String command) {
        String[] words = command.split(" ");
        for (String word : words) {
            try {
                Integer.parseInt(word);
                return word;
            } catch (NumberFormatException e) {
                // Continue searching
            }
        }
        return "42"; // Default number
    }

    private boolean isCountryQuery(String query) {
        String[] countryKeywords = {"country", "nation", "capital", "population", "currency"};
        String lowerQuery = query.toLowerCase();
        for (String keyword : countryKeywords) {
            if (lowerQuery.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}