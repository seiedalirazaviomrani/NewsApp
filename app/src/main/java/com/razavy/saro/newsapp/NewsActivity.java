package com.razavy.saro.newsapp;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class NewsActivity extends AppCompatActivity implements LoaderCallbacks<List<NewsItem>> {

    private static final String LOG_TAG = NewsActivity.class.getSimpleName();
    private static final String MY_API = "PLEASE INSERT YOUR API CODE HERE!";
    private static final String BASE_URL = "https://content.guardianapis.com/search";
    private static final int LOADER_ID = 1;
    private NewsAdapter mAdapter;
    private ProgressBar mProgressBar;
    private TextView emptyView;
    private boolean isQuery = false;
    private String searchKeyword = "";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent openSettingActivity = new Intent(this, SettingsActivity.class);
            startActivity(openSettingActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        emptyView = findViewById(R.id.emptyView);
        mProgressBar = findViewById(R.id.progress_circular);
        mAdapter = new NewsAdapter(this, new ArrayList<NewsItem>());
        ListView listView = findViewById(R.id.newsListView);
        listView.setAdapter(mAdapter);

        final LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(LOADER_ID, null, NewsActivity.this);

        SearchView searchView = (SearchView) findViewById(R.id.search_bar);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) {
                    isQuery = true;
                    searchKeyword = query;
                    loaderManager.restartLoader(LOADER_ID, null, NewsActivity.this);
                    return true;
                } else
                    return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        listView.setEmptyView(findViewById(R.id.emptyView));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NewsItem currentItem = mAdapter.getItem(position);
                Uri newsUri = Uri.parse(currentItem.getNewsUrl());
                Intent openWebsite = new Intent(Intent.ACTION_VIEW, newsUri);
                startActivity(openWebsite);
            }
        });
    }

    @Override
    public Loader<List<NewsItem>> onCreateLoader(int id, Bundle args) {
        Uri baseUri = Uri.parse(BASE_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        if (isQuery) {
            uriBuilder.appendQueryParameter("q", searchKeyword);
        }
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String numberOfNews = sharedPrefs.getString(getString(R.string.settings_news_numbers_key), getString(R.string.settings_news_numbers_default_value));
        String orderBy = sharedPrefs.getString(getString(R.string.settings_order_key), getString(R.string.settings_order_default_value));
        String fromDate = sharedPrefs.getString(getString(R.string.setting_date_key), getString(R.string.setting_date_default_value));

        uriBuilder.appendQueryParameter("api-key", MY_API);
        uriBuilder.appendQueryParameter(getString(R.string.settings_news_numbers_key), numberOfNews);
        uriBuilder.appendQueryParameter(getString(R.string.settings_order_key), orderBy);
        uriBuilder.appendQueryParameter(getString(R.string.setting_date_key), fromDate);
        uriBuilder.appendQueryParameter("show-references", "author");

        Log.i(LOG_TAG, "URL = " + uriBuilder.toString());

        return new NewsAsyncTaskLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<NewsItem>> loader, List<NewsItem> newsItems) {
        mAdapter.clear();
        mProgressBar.setVisibility(View.GONE);
        emptyView.setText(getString(R.string.i_m_alone_here));
        emptyView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.baseline_sentiment_dissatisfied_24);
        if (newsItems != null && !newsItems.isEmpty())
            mAdapter.addAll(newsItems);
        else if (isQuery && checkNetworkConnectivity()) {
            emptyView.setText(getString(R.string.news_not_found));
            emptyView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.baseline_warning_24);
        } else if (!checkNetworkConnectivity()) {
            emptyView.setText(getString(R.string.no_network_connection));
            emptyView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.baseline_signal_wifi_off_24);
        } else if (!checkInternetConnectivity()) {
            emptyView.setText(getString(R.string.no_internet_connection));
            emptyView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.baseline_cloud_off_24);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<NewsItem>> loader) {
        mAdapter.clear();
    }

    private static class NewsAsyncTaskLoader extends AsyncTaskLoader<List<NewsItem>> {
        private String mUrl;

        NewsAsyncTaskLoader(@NonNull Context context, String url) {
            super(context);
            this.mUrl = url;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Nullable
        @Override
        public List<NewsItem> loadInBackground() {
            if (mUrl == null)
                return null;
            else
                return QueryUtils.extractNews(mUrl);
        }
    }

    private boolean checkNetworkConnectivity() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private boolean checkInternetConnectivity() {
        try {
            InetAddress ipAddr = InetAddress.getByName("content.guardianapis.com");
            return !ipAddr.equals("");
        } catch (Exception e) {
            return false;
        }
    }
}
