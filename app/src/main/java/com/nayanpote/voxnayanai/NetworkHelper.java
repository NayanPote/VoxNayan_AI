package com.nayanpote.voxnayanai;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

public class NetworkHelper {
    private static final String TAG = "NetworkHelper";

    // Free API endpoints
    private static final String WIKIPEDIA_API = "https://en.wikipedia.org/api/rest_v1/page/summary/";
    private static final String REST_COUNTRIES_API = "https://restcountries.com/v3.1/name/";
    private static final String JOKES_API = "https://official-joke-api.appspot.com/random_joke";
    private static final String CAT_FACTS_API = "https://catfact.ninja/fact";
    private static final String NUMBERS_API = "http://numbersapi.com/";
    private static final String QUOTABLE_API = "https://api.quotable.io/random";
    private static final String PROGRAMMING_QUOTES_API = "https://programming-quotes-api.herokuapp.com/quotes/random";
    private static final String OPEN_TRIVIA_API = "https://opentdb.com/api.php?amount=1&type=multiple";
    private static final String DOG_FACTS_API = "https://dog-api.kinduff.com/api/facts";
    private static final String ADVICE_API = "https://api.adviceslip.com/advice";
    private static final String DICTIONARY_API = "https://api.dictionaryapi.dev/api/v2/entries/en/";

    // Additional free APIs
    private static final String NEWS_API = "https://api.currentsapi.services/v1/latest-news?apiKey=YOUR_API_KEY";
    private static final String HACKER_NEWS_API = "https://hacker-news.firebaseio.com/v0/topstories.json";
    private static final String HACKER_NEWS_ITEM_API = "https://hacker-news.firebaseio.com/v0/item/";
    private static final String WEATHER_API = "https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&current=temperature_2m,weathercode";
    private static final String CRYPTO_API = "https://api.coinbase.com/v2/exchange-rates";
    private static final String JOKE_NINJA_API = "https://api.api-ninjas.com/v1/jokes";
    private static final String FACTS_API = "https://api.api-ninjas.com/v1/facts";
    private static final String QUOTES_NINJA_API = "https://api.api-ninjas.com/v1/quotes";
    private static final String RIDDLES_API = "https://riddles-api.vercel.app/random";
    private static final String ACTIVITIES_API = "https://www.boredapi.com/api/activity";
    private static final String GENDER_API = "https://api.genderize.io/?name=";
    private static final String NATIONALITY_API = "https://api.nationalize.io/?name=";
    private static final String AGE_API = "https://api.agify.io/?name=";
    private static final String UNIVERSITIES_API = "http://universities.hipolabs.com/search?name=";
    private static final String GITHUB_API = "https://api.github.com/users/";
    private static final String JSON_PLACEHOLDER_API = "https://jsonplaceholder.typicode.com/posts/";

    public interface NetworkCallback {
        void onSuccess(String result);
        void onError(String error);
    }

    public static void searchWikipedia(String query, NetworkCallback callback) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    String encodedQuery = URLEncoder.encode(params[0], "UTF-8");
                    String urlString = WIKIPEDIA_API + encodedQuery;

                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("User-Agent", "VoxAI/1.0");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    connection.disconnect();

                    JSONObject jsonObject = new JSONObject(response.toString());
                    if (jsonObject.has("extract")) {
                        return jsonObject.getString("extract");
                    } else {
                        return "No information found for " + params[0];
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Wikipedia search error", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError("Failed to fetch Wikipedia information");
                }
            }
        }.execute(query);
    }

    public static void searchCountryInfo(String countryName, NetworkCallback callback) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    String encodedCountry = URLEncoder.encode(params[0], "UTF-8");
                    String urlString = REST_COUNTRIES_API + encodedCountry;

                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    connection.disconnect();

                    JSONArray jsonArray = new JSONArray(response.toString());
                    if (jsonArray.length() > 0) {
                        JSONObject country = jsonArray.getJSONObject(0);
                        StringBuilder countryInfo = new StringBuilder();

                        String name = country.getJSONObject("name").getString("common");
                        countryInfo.append("Country: ").append(name);

                        if (country.has("capital") && country.getJSONArray("capital").length() > 0) {
                            countryInfo.append("\nCapital: ").append(country.getJSONArray("capital").getString(0));
                        }

                        if (country.has("population")) {
                            long population = country.getLong("population");
                            countryInfo.append("\nPopulation: ").append(String.format("%,d", population));
                        }

                        if (country.has("region")) {
                            countryInfo.append("\nRegion: ").append(country.getString("region"));
                        }

                        if (country.has("currencies")) {
                            JSONObject currencies = country.getJSONObject("currencies");
                            Iterator<String> currencyKeys = currencies.keys();
                            if (currencyKeys.hasNext()) {
                                String key = currencyKeys.next();
                                JSONObject currency = currencies.getJSONObject(key);
                                countryInfo.append("\nCurrency: ").append(currency.getString("name"));
                            }
                        }

                        if (country.has("languages")) {
                            JSONObject languages = country.getJSONObject("languages");
                            Iterator<String> langKeys = languages.keys();
                            if (langKeys.hasNext()) {
                                String key = langKeys.next();
                                countryInfo.append("\nLanguages: ").append(languages.getString(key));
                            }
                        }


                        return countryInfo.toString();
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Country search error", e);
                    return null;
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError("Failed to fetch country information");
                }
            }
        }.execute(countryName);
    }

    public static void getRandomJoke(NetworkCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    URL url = new URL(JOKES_API);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    connection.disconnect();

                    JSONObject jsonObject = new JSONObject(response.toString());
                    String setup = jsonObject.getString("setup");
                    String punchline = jsonObject.getString("punchline");

                    return setup + " " + punchline;

                } catch (Exception e) {
                    Log.e(TAG, "Joke fetch error", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError("Failed to fetch joke");
                }
            }
        }.execute();
    }

    public static void getCatFact(NetworkCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    URL url = new URL(CAT_FACTS_API);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    connection.disconnect();

                    JSONObject jsonObject = new JSONObject(response.toString());
                    return "Here's a cat fact: " + jsonObject.getString("fact");

                } catch (Exception e) {
                    Log.e(TAG, "Cat fact fetch error", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError("Failed to fetch cat fact");
                }
            }
        }.execute();
    }

    public static void getDogFact(NetworkCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    URL url = new URL(DOG_FACTS_API);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    connection.disconnect();

                    JSONObject jsonObject = new JSONObject(response.toString());
                    JSONArray facts = jsonObject.getJSONArray("facts");
                    if (facts.length() > 0) {
                        return "Here's a dog fact: " + facts.getString(0);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Dog fact fetch error", e);
                    return null;
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError("Failed to fetch dog fact");
                }
            }
        }.execute();
    }

    public static void getNumberFact(String number, NetworkCallback callback) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    String urlString = NUMBERS_API + params[0];
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    connection.disconnect();

                    return response.toString();

                } catch (Exception e) {
                    Log.e(TAG, "Number fact fetch error", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError("Failed to fetch number fact");
                }
            }
        }.execute(number);
    }

    public static void getRandomQuote(NetworkCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    URL url = new URL(QUOTABLE_API);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    connection.disconnect();

                    JSONObject jsonObject = new JSONObject(response.toString());
                    String quote = jsonObject.getString("content");
                    String author = jsonObject.getString("author");

                    return "\"" + quote + "\" - " + author;

                } catch (Exception e) {
                    Log.e(TAG, "Quote fetch error", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError("Failed to fetch quote");
                }
            }
        }.execute();
    }

    public static void getProgrammingQuote(NetworkCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    URL url = new URL(PROGRAMMING_QUOTES_API);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    connection.disconnect();

                    JSONObject jsonObject = new JSONObject(response.toString());
                    String quote = jsonObject.getString("en");
                    String author = jsonObject.getString("author");

                    return "Programming Quote: \"" + quote + "\" - " + author;

                } catch (Exception e) {
                    Log.e(TAG, "Programming quote fetch error", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError("Failed to fetch programming quote");
                }
            }
        }.execute();
    }

    public static void getTriviaQuestion(NetworkCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    URL url = new URL(OPEN_TRIVIA_API);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    connection.disconnect();

                    JSONObject jsonObject = new JSONObject(response.toString());
                    JSONArray results = jsonObject.getJSONArray("results");

                    if (results.length() > 0) {
                        JSONObject question = results.getJSONObject(0);
                        String category = question.getString("category");
                        String questionText = question.getString("question");
                        String correctAnswer = question.getString("correct_answer");

                        return "Trivia (" + category + "): " + questionText + "\nAnswer: " + correctAnswer;
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Trivia fetch error", e);
                    return null;
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError("Failed to fetch trivia question");
                }
            }
        }.execute();
    }

    public static void getAdvice(NetworkCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    URL url = new URL(ADVICE_API);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    connection.disconnect();

                    JSONObject jsonObject = new JSONObject(response.toString());
                    JSONObject slip = jsonObject.getJSONObject("slip");

                    return "Here's some advice: " + slip.getString("advice");

                } catch (Exception e) {
                    Log.e(TAG, "Advice fetch error", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError("Failed to fetch advice");
                }
            }
        }.execute();
    }

    public static void getDefinition(String word, NetworkCallback callback) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    String encodedWord = URLEncoder.encode(params[0], "UTF-8");
                    String urlString = DICTIONARY_API + encodedWord;

                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    connection.disconnect();

                    JSONArray jsonArray = new JSONArray(response.toString());
                    if (jsonArray.length() > 0) {
                        JSONObject wordObj = jsonArray.getJSONObject(0);
                        JSONArray meanings = wordObj.getJSONArray("meanings");

                        if (meanings.length() > 0) {
                            JSONObject meaning = meanings.getJSONObject(0);
                            String partOfSpeech = meaning.getString("partOfSpeech");
                            JSONArray definitions = meaning.getJSONArray("definitions");

                            if (definitions.length() > 0) {
                                String definition = definitions.getJSONObject(0).getString("definition");
                                return params[0] + " (" + partOfSpeech + "): " + definition;
                            }
                        }
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Definition fetch error", e);
                    return null;
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError("Failed to fetch definition");
                }
            }
        }.execute(word);
    }

    // NEW METHODS - Additional APIs

    public static void getHackerNewsStories(NetworkCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    URL url = new URL(HACKER_NEWS_API);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    connection.disconnect();

                    JSONArray stories = new JSONArray(response.toString());
                    if (stories.length() > 0) {
                        // Get first 3 story IDs
                        StringBuilder result = new StringBuilder("Top Tech News:\n");
                        for (int i = 0; i < Math.min(3, stories.length()); i++) {
                            int storyId = stories.getInt(i);
                            result.append("• Story ID: ").append(storyId).append("\n");
                        }
                        return result.toString();
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Hacker News fetch error", e);
                    return null;
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError("Failed to fetch news stories");
                }
            }
        }.execute();
    }

    public static void getCryptoRates(NetworkCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    URL url = new URL(CRYPTO_API);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    connection.disconnect();

                    JSONObject jsonObject = new JSONObject(response.toString());
                    JSONObject data = jsonObject.getJSONObject("data");
                    JSONObject rates = data.getJSONObject("rates");

                    StringBuilder result = new StringBuilder("Crypto Exchange Rates (USD):\n");
                    if (rates.has("BTC")) {
                        result.append("Bitcoin: $").append(rates.getString("BTC")).append("\n");
                    }
                    if (rates.has("ETH")) {
                        result.append("Ethereum: $").append(rates.getString("ETH")).append("\n");
                    }

                    return result.toString();

                } catch (Exception e) {
                    Log.e(TAG, "Crypto rates fetch error", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError("Failed to fetch crypto rates");
                }
            }
        }.execute();
    }

    public static void getRandomActivity(NetworkCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    URL url = new URL(ACTIVITIES_API);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    connection.disconnect();

                    JSONObject jsonObject = new JSONObject(response.toString());
                    String activity = jsonObject.getString("activity");
                    String type = jsonObject.getString("type");

                    return "Activity Suggestion (" + type + "): " + activity;

                } catch (Exception e) {
                    Log.e(TAG, "Activity fetch error", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError("Failed to fetch activity suggestion");
                }
            }
        }.execute();
    }

    public static void getNameInfo(String name, NetworkCallback callback) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    StringBuilder result = new StringBuilder("Name Analysis for ").append(params[0]).append(":\n");

                    // Gender prediction
                    URL genderUrl = new URL(GENDER_API + URLEncoder.encode(params[0], "UTF-8"));
                    HttpURLConnection genderConn = (HttpURLConnection) genderUrl.openConnection();
                    genderConn.setRequestMethod("GET");

                    BufferedReader genderReader = new BufferedReader(new InputStreamReader(genderConn.getInputStream()));
                    StringBuilder genderResponse = new StringBuilder();
                    String line;

                    while ((line = genderReader.readLine()) != null) {
                        genderResponse.append(line);
                    }
                    genderReader.close();
                    genderConn.disconnect();

                    JSONObject genderJson = new JSONObject(genderResponse.toString());
                    if (genderJson.has("gender") && !genderJson.isNull("gender")) {
                        result.append("Predicted Gender: ").append(genderJson.getString("gender"));
                        if (genderJson.has("probability")) {
                            result.append(" (").append(Math.round(genderJson.getDouble("probability") * 100)).append("% confidence)");
                        }
                        result.append("\n");
                    }

                    return result.toString();

                } catch (Exception e) {
                    Log.e(TAG, "Name info fetch error", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError("Failed to fetch name information");
                }
            }
        }.execute(name);
    }

    public static void searchUniversities(String query, NetworkCallback callback) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    String encodedQuery = URLEncoder.encode(params[0], "UTF-8");
                    String urlString = UNIVERSITIES_API + encodedQuery;

                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    connection.disconnect();

                    JSONArray universities = new JSONArray(response.toString());
                    if (universities.length() > 0) {
                        StringBuilder result = new StringBuilder("Universities matching '").append(params[0]).append("':\n");

                        for (int i = 0; i < Math.min(3, universities.length()); i++) {
                            JSONObject uni = universities.getJSONObject(i);
                            result.append("• ").append(uni.getString("name"));
                            if (uni.has("country")) {
                                result.append(" (").append(uni.getString("country")).append(")");
                            }
                            result.append("\n");
                        }

                        return result.toString();
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Universities search error", e);
                    return null;
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError("Failed to search universities");
                }
            }
        }.execute(query);
    }

    public static void getGitHubUserInfo(String username, NetworkCallback callback) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    String urlString = GITHUB_API + URLEncoder.encode(params[0], "UTF-8");

                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("User-Agent", "VoxAI/1.0");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    connection.disconnect();

                    JSONObject user = new JSONObject(response.toString());
                    StringBuilder result = new StringBuilder("GitHub User: ").append(user.getString("login")).append("\n");

                    if (user.has("name") && !user.isNull("name")) {
                        result.append("Name: ").append(user.getString("name")).append("\n");
                    }
                    if (user.has("public_repos")) {
                        result.append("Public Repos: ").append(user.getInt("public_repos")).append("\n");
                    }
                    if (user.has("followers")) {
                        result.append("Followers: ").append(user.getInt("followers")).append("\n");
                    }
                    if (user.has("bio") && !user.isNull("bio")) {
                        result.append("Bio: ").append(user.getString("bio")).append("\n");
                    }

                    return result.toString();

                } catch (Exception e) {
                    Log.e(TAG, "GitHub user fetch error", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError("Failed to fetch GitHub user info");
                }
            }
        }.execute(username);
    }

    public static void getRandomFact(NetworkCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    URL url = new URL(FACTS_API);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("X-Api-Key", "YOUR_API_KEY"); // Optional for api-ninjas

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    connection.disconnect();

                    JSONArray facts = new JSONArray(response.toString());
                    if (facts.length() > 0) {
                        JSONObject fact = facts.getJSONObject(0);
                        return "Random Fact: " + fact.getString("fact");
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Random fact fetch error", e);
                    return null;
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError("Failed to fetch random fact");
                }
            }
        }.execute();
    }

    public static void getRiddle(NetworkCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    URL url = new URL(RIDDLES_API);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    connection.disconnect();

                    JSONObject riddle = new JSONObject(response.toString());
                    String question = riddle.getString("riddle");
                    String answer = riddle.getString("answer");

                    return "Riddle: " + question + "\nAnswer: " + answer;

                } catch (Exception e) {
                    Log.e(TAG, "Riddle fetch error", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError("Failed to fetch riddle");
                }
            }
        }.execute();
    }

    // Utility methods
    public static void searchTechInfo(String query, NetworkCallback callback) {
        // Use Wikipedia for programming and technology information
        searchWikipedia(query + " programming technology", callback);
    }

    public static void getGeopoliticsInfo(String query, NetworkCallback callback) {
        // Use Wikipedia for geopolitics information
        searchWikipedia(query + " geopolitics politics", callback);
    }

    public static void getPersonInfo(String personName, NetworkCallback callback) {
        // Use Wikipedia for person information
        searchWikipedia(personName + " biography", callback);
    }

    public static void getHistoryInfo(String query, NetworkCallback callback) {
        // Use Wikipedia for historical information
        searchWikipedia(query + " history", callback);
    }

    public static void getScienceInfo(String query, NetworkCallback callback) {
        // Use Wikipedia for science information
        searchWikipedia(query + " science", callback);
    }

    // Generic web search fallback method
    public static void searchGeneral(String query, NetworkCallback callback) {
        // Use Wikipedia as primary source for general information
        searchWikipedia(query, callback);
    }
}