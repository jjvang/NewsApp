package com.example.ojboba.newsapp;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by OjBoba on 1/26/2017.
 */

public class QueryUtils {


    private static final String LOG_TAG = MainActivity.class.getName();


// -----------------------------CREATE PUBLIC ASYNC TASK -------------------------------------------


//-----------------------PUBLIC METHOD TO ASSIGN QUERY TO URL---------------------------------------

        protected static List<Word> doInBackground(String urls) {
            // Create URL object
            URL url = createUrl(urls);
            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem making the HTTP request.", e);
            }

            // Extract relevant fields from the JSON response and create an {@link Event} object
            List<Word> newsArticles = extractFeatureFromJson(jsonResponse);

            // Return the {@link Event} object as the result fo the {@link BookAsyncTask}
            return newsArticles;
        }

        /**
         * Update the screen with the given earthquake (which was the result of the
         * {@link BookAsyncTask}).
         */

//-------------------------BUILD THE URL OBJECT-----------------------------------------------------
        /**
         * Returns new URL object from the given string URL.
         */
        private static URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, "Error with creating URL", exception);
                return null;
            }
            return url;
        }
//-------------------------PERFORM NETWORK REQUEST--------------------------------------------------
        /**
         * Make an HTTP request to the given URL and return a String as the response.
         */
        private static String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";
            if (url == null) {
                return jsonResponse;
            }
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();

                // Check if the jSon Response is good via "200"
                // if the jSon response is error, it does not GET any info
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                } else {
                    Log.e(LOG_TAG, "Error Response Code: " + urlConnection.getResponseCode());
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error getting the jSON Response, possible connection error", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    // function must handle java.io.IOException here
                    inputStream.close();
                }
            }
            return jsonResponse;
        }
//-----------------------CONVERT INPUT-STREAM TO STRING --------------------------------------------
        /**
         * Convert the {@link InputStream} into a String which contains the
         * whole JSON response from the server.
         */
        private static String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

//-------------------------PARSE JSON DATA----------------------------------------------------------
        // We parse the JSON data to GET the specific data for the API
        // We Modify the JSON Parsing method to a list of books from the web server response
        /**
         * Return a list of {@link Word} objects that has been built up from
         * parsing the given JSON response.
         */

        private static List<Word> extractFeatureFromJson(String newsJSON) {
            // If the JSON string is empty or null, then return early.
            if (TextUtils.isEmpty(newsJSON)) {
                return null;
            }

            // Create an empty ArrayList that we can start adding books to
            List<Word> newsDetail = new ArrayList<>();

            // Try to parse the JSON response string. If there's a problem with the way the JSON
            // is formatted, a JSONException exception object will be thrown.
            // Catch the exception so the app doesn't crash, and print the error message to the logs.
            try {

                // Create a JSONObject from the JSON response string
                JSONObject baseJsonResponse = new JSONObject(newsJSON);

                // Extract the JSONArray associated with the key called "items",
                JSONObject itemsObject = baseJsonResponse.getJSONObject("response");

                // Get a single book at position i within the list of books
                JSONArray itemsArray = itemsObject.getJSONArray("results");

                // For each book in the itemsArray, repeat till maximum
                for (int i = 0; i < itemsArray.length(); i++) {

                    JSONObject currentDetail = itemsArray.getJSONObject(i);
                    String sectionName = "";
                    if (currentDetail.has("sectionName")) {
                        // Extract the value for the book title called "title"
                        sectionName = currentDetail.getString("sectionName");
                    }
                    String articleName = "";
                    if (currentDetail.has("webTitle")) {
                        // Extract the value for the book title called "title"
                       articleName = currentDetail.getString("webTitle");
                    }
                    // Create a new {@link Word} object with the title and author
                    String url = "";
                    if (currentDetail.has("webUrl")){
                        url = currentDetail.getString("webUrl");
                    }

                    Word details = new Word(sectionName, articleName, url);

                    // Add the new {@link Word} to the list of books.
                    newsDetail.add(details);
                }

            } catch (JSONException e) {
                // If an error is thrown when executing any of the above statements in the "try" block,
                // catch the exception here, so the app doesn't crash. Print a log message
                // with the message from the exception.
                Log.e("QueryUtils", "Problem parsing the book JSON results", e);
            }

            // Return the list of bookDetails
            return newsDetail;
        }

//    }
}
