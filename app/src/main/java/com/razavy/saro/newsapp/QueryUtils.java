package com.razavy.saro.newsapp;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

class QueryUtils {

    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    private QueryUtils() {
    }

    static List<NewsItem> extractNews(String requestedUrl) {
        final String JSON_RESPONSE = "response";
        final String JSON_RESULTS = "results";
        final String NEWS_TYPE_ID = "sectionId";
        final String NEWS_TYPE_NAME = "sectionName";
        final String NEWS_DATE = "webPublicationDate";
        final String NEWS_TEXT = "webTitle";
        final String NEWS_URL = "webUrl";
        final String NEWS_REFERENCES = "references";
        final String NEWS_AUTHOR = "author";
        String newsText = "", newsTypeName = "", newsTypeId = "", newsDate = "", newsUrl = "", newsAuthor = "";

        List<NewsItem> newsItems = new ArrayList<>();

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpsConnection(convertToUrl(requestedUrl));
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error in making HTTPS connection.", e);
        }

        if (jsonResponse == null || jsonResponse.isEmpty()) {
            Log.e(LOG_TAG, "The JSON response is null or empty!");
            return null;
        }

        try {
            JSONObject rootObject = new JSONObject(jsonResponse);
            JSONObject responseObject = rootObject.getJSONObject(JSON_RESPONSE);
            JSONArray results = responseObject.optJSONArray(JSON_RESULTS);
            for (int i = 0; i < results.length(); i++) {
                JSONObject newsItem = results.getJSONObject(i);
                newsText = newsItem.optString(NEWS_TEXT);
                newsTypeId = newsItem.optString(NEWS_TYPE_ID);
                newsTypeName = newsItem.optString(NEWS_TYPE_NAME);
                newsDate = newsItem.optString(NEWS_DATE);
                newsUrl = newsItem.optString(NEWS_URL);
                JSONArray references = newsItem.optJSONArray(NEWS_REFERENCES);
                if (references.length() > 0)
                    for (int j = 0; j < references.length(); j++) {
                        JSONObject newsAuthorObject = references.getJSONObject(i);
                        newsAuthor = newsAuthorObject.optString(NEWS_AUTHOR);
                    }
                newsItems.add(new NewsItem(newsText, newsTypeName, newsTypeId, newsUrl, newsAuthor, newsDate));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Incorrect JSON format.", e);
            return null;
        }
        return newsItems;
    }

    private static URL convertToUrl(String requestedURL) {
        try {
            return new URL(requestedURL);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Incorrect URL format", e);
            return null;
        }
    }

    private static String convertStreamToString(InputStream stream) throws IOException {
        StringBuilder result = new StringBuilder();
        if (stream != null) {
            InputStreamReader streamReader = new InputStreamReader(stream, Charset.forName("UTF-8"));
            BufferedReader bufferedReader = new BufferedReader(streamReader);
            String line = bufferedReader.readLine();
            while (line != null) {
                result.append(line);
                line = bufferedReader.readLine();
            }
        }
        return result.toString();
    }

    private static String makeHttpsConnection(URL requestedURL) throws IOException {
        String result = null;
        InputStream inputStream = null;
        HttpsURLConnection httpsURLConnection = null;
        try {
            httpsURLConnection = (HttpsURLConnection) requestedURL.openConnection();
            httpsURLConnection.setConnectTimeout(10000);
            httpsURLConnection.setReadTimeout(50000);
            httpsURLConnection.setRequestMethod("GET");
            httpsURLConnection.connect();
            if (httpsURLConnection.getResponseCode() == 200) {
                inputStream = httpsURLConnection.getInputStream();
                result = convertStreamToString(inputStream);
            } else {
                Log.e(LOG_TAG, "HTTPS connection error. The error code is: "
                        + httpsURLConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "IO Exception in making HTTPS connection.", e);
        } finally {
            if (inputStream != null)
                inputStream.close();
            if (httpsURLConnection != null)
                httpsURLConnection.disconnect();
        }
        return result;
    }
}
