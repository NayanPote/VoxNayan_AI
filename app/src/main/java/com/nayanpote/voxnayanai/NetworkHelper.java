package com.nayanpote.voxnayanai;

import android.os.AsyncTask;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class NetworkHelper {

    public interface NetworkCallback {
        void onSuccess(String result);
        void onError(String error);
    }

    // Free Wikipedia API for general information
    public static void searchWikipedia(String query, NetworkCallback callback) {
        new AsyncTask<String, Void, String>() {
            private String errorMessage = null;

            @Override
            protected String doInBackground(String... params) {
                try {
                    String encodedQuery = URLEncoder.encode(params[0], "UTF-8");
                    String urlString = "https://en.wikipedia.org/api/rest_v1/page/summary/" + encodedQuery;

                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("User-Agent", "VoxAI/1.0 (https://github.com/voxai)");
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);

                    int responseCode = connection.getResponseCode();
                    if (responseCode == 200) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;

                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        JSONObject jsonObject = new JSONObject(response.toString());
                        if (jsonObject.has("extract") && !jsonObject.isNull("extract")) {
                            String extract = jsonObject.getString("extract");
                            if (extract.length() > 400) {
                                extract = extract.substring(0, 400) + "...";
                            }
                            return extract;
                        } else {
                            errorMessage = "No information found";
                        }
                    } else {
                        errorMessage = "API returned error code: " + responseCode;
                    }
                } catch (IOException e) {
                    errorMessage = "Network error: " + e.getMessage();
                } catch (JSONException e) {
                    errorMessage = "JSON parsing error: " + e.getMessage();
                } catch (Exception e) {
                    errorMessage = "Unexpected error: " + e.getMessage();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null && !result.trim().isEmpty()) {
                    callback.onSuccess(result);
                } else {
                    callback.onError(errorMessage != null ? errorMessage : "No information found");
                }
            }
        }.execute(query);
    }

    // Free REST Countries API for country information
    public static void searchCountryInfo(String countryName, NetworkCallback callback) {
        new AsyncTask<String, Void, String>() {
            private String errorMessage = null;

            @Override
            protected String doInBackground(String... params) {
                try {
                    String encodedQuery = URLEncoder.encode(params[0], "UTF-8");
                    String urlString = "https://restcountries.com/v3.1/name/" + encodedQuery;

                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);

                    int responseCode = connection.getResponseCode();
                    if (responseCode == 200) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;

                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        // Parse country information
                        return parseCountryInfo(response.toString());
                    } else {
                        errorMessage = "Country not found";
                    }
                } catch (Exception e) {
                    errorMessage = "Error fetching country information: " + e.getMessage();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError(errorMessage != null ? errorMessage : "Country information not found");
                }
            }
        }.execute(countryName);
    }

    // Free JokeAPI for entertainment
    public static void getRandomJoke(NetworkCallback callback) {
        new AsyncTask<Void, Void, String>() {
            private String errorMessage = null;

            @Override
            protected String doInBackground(Void... params) {
                try {
                    String urlString = "https://v2.jokeapi.dev/joke/Programming,Miscellaneous,Pun?blacklistFlags=nsfw,religious,political,racist,sexist,explicit&type=single";

                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);

                    int responseCode = connection.getResponseCode();
                    if (responseCode == 200) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;

                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        JSONObject jsonObject = new JSONObject(response.toString());
                        if (jsonObject.has("joke")) {
                            return jsonObject.getString("joke");
                        } else if (jsonObject.has("setup") && jsonObject.has("delivery")) {
                            return jsonObject.getString("setup") + " " + jsonObject.getString("delivery");
                        }
                    } else {
                        errorMessage = "Could not fetch joke";
                    }
                } catch (Exception e) {
                    errorMessage = "Error fetching joke: " + e.getMessage();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError(errorMessage != null ? errorMessage : "Could not get a joke right now");
                }
            }
        }.execute();
    }

    // Free Numbers API for fun facts
    public static void getNumberFact(String number, NetworkCallback callback) {
        new AsyncTask<String, Void, String>() {
            private String errorMessage = null;

            @Override
            protected String doInBackground(String... params) {
                try {
                    String urlString = "http://numbersapi.com/" + params[0];

                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);

                    int responseCode = connection.getResponseCode();
                    if (responseCode == 200) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;

                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        return response.toString();
                    } else {
                        errorMessage = "Could not fetch number fact";
                    }
                } catch (Exception e) {
                    errorMessage = "Error fetching number fact: " + e.getMessage();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null && !result.trim().isEmpty()) {
                    callback.onSuccess(result);
                } else {
                    callback.onError(errorMessage != null ? errorMessage : "Could not get number fact");
                }
            }
        }.execute(number);
    }

    // Free Cat Facts API
    public static void getCatFact(NetworkCallback callback) {
        new AsyncTask<Void, Void, String>() {
            private String errorMessage = null;

            @Override
            protected String doInBackground(Void... params) {
                try {
                    String urlString = "https://catfact.ninja/fact";

                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);

                    int responseCode = connection.getResponseCode();
                    if (responseCode == 200) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;

                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        JSONObject jsonObject = new JSONObject(response.toString());
                        if (jsonObject.has("fact")) {
                            return jsonObject.getString("fact");
                        }
                    } else {
                        errorMessage = "Could not fetch cat fact";
                    }
                } catch (Exception e) {
                    errorMessage = "Error fetching cat fact: " + e.getMessage();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess("Here's a cat fact: " + result);
                } else {
                    callback.onError(errorMessage != null ? errorMessage : "Could not get cat fact");
                }
            }
        }.execute();
    }

    private static String parseCountryInfo(String jsonResponse) throws JSONException {
        JSONObject jsonArray = new JSONObject("[" + jsonResponse + "]");
        JSONObject country = jsonArray.getJSONArray("").getJSONObject(0);

        StringBuilder info = new StringBuilder();

        if (country.has("name") && country.getJSONObject("name").has("common")) {
            info.append(country.getJSONObject("name").getString("common"));
        }

        if (country.has("capital")) {
            info.append(" has the capital ").append(country.getJSONArray("capital").getString(0));
        }

        if (country.has("population")) {
            long population = country.getLong("population");
            info.append(" with a population of ").append(String.format("%,d", population)).append(" people");
        }

        if (country.has("region")) {
            info.append(" located in ").append(country.getString("region"));
        }

        return info.toString();
    }
}