package com.razavy.saro.newsapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class NewsAdapter extends ArrayAdapter<NewsItem> {
    NewsAdapter(Context context, List<NewsItem> newsItems) {
        super(context, 0, newsItems);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.news_item, parent, false);
        }
        NewsItem currentItem = getItem(position);
        TextView newsType = listItemView.findViewById(R.id.news_type);
        newsType.setText(currentItem.getNewsTypeName());
        TextView newsText = listItemView.findViewById(R.id.news_text);
        newsText.setText(currentItem.getNewsTitle());
        TextView newsTime = listItemView.findViewById(R.id.newsPublishedAt);
        String publishedAt = (currentItem.getNewsPublishDate().replace("T", ", ")).replace("Z", "");
        newsTime.setText(publishedAt);
        TextView newsAuthor = listItemView.findViewById(R.id.newsAuthor);
        newsAuthor.setText(currentItem.getNewsAuthor());
        return listItemView;
    }
}
