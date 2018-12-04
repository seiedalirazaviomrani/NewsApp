package com.razavy.saro.newsapp;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class NewsActivity extends AppCompatActivity implements LoaderCallbacks<List<NewsItem>> {

    private static final String MY_API = "PLEASE INSERT YOUR API CODE HERE!";
    private static final String SAMPLE_QUERY = "https://content.guardianapis.com/search?api-key=" + MY_API + "&show-references=author";
    private static final int LOADER_ID = 1;
    private NewsAdapter mAdapter;
    private ProgressBar mProgressBar;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        emptyView = findViewById(R.id.emptyView);
        mProgressBar = findViewById(R.id.progress_circular);
        mAdapter = new NewsAdapter(this, new ArrayList<NewsItem>());
        ListView listView = findViewById(R.id.newsListView);
        listView.setAdapter(mAdapter);

        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(LOADER_ID, null, NewsActivity.this);

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
        return new NewsAsyncTaskLoader(this, SAMPLE_QUERY);
    }

    @Override
    public void onLoadFinished(Loader<List<NewsItem>> loader, List<NewsItem> newsItems) {
        mAdapter.clear();
        mProgressBar.setVisibility(View.GONE);
        emptyView.setText(getString(R.string.i_m_alone_here));
        emptyView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.baseline_sentiment_dissatisfied_24);
        if (newsItems != null && !newsItems.isEmpty())
            mAdapter.addAll(newsItems);
        else if (!checkNetworkConnectivity()) {
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
}
