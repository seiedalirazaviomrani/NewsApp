package com.razavy.saro.newsapp;

public class NewsItem {
    private String mText,   // Title of News
            mTypeName,      // Type name of News
            mTypeId,        // Type Id of News
            mUrl,           // URL of News
            mAuthor,        // News Author
            mPublishedAt;   // News published date

    NewsItem(String newsText,
             String newsTypeName,
             String newsTypeId,
             String newsUrl,
             String newsAuthor,
             String newsPublishDate) {
        this.mText = newsText;
        this.mTypeName = newsTypeName;
        this.mTypeId = newsTypeId;
        this.mUrl = newsUrl;
        this.mAuthor = newsAuthor;
        this.mPublishedAt = newsPublishDate;
    }

    public String getNewsTitle() {
        return this.mText;
    }

    public void setNewsTitle(String newsTitle) {
        this.mText = newsTitle;
    }

    public String getNewsTypeName() {
        return this.mTypeName;
    }

    public void setNewsTypeName(String newsTypeName) {
        this.mTypeName = newsTypeName;
    }

    public String getNewsTypeId() {
        return this.mTypeId;
    }

    public void setNewsTypeId(String newsTypeId) {
        this.mTypeId = newsTypeId;
    }

    public String getNewsUrl() {
        return this.mUrl;
    }

    public void setNewsUrl(String newsUrl) {
        this.mUrl = newsUrl;
    }

    public String getNewsAuthor() {
        return mAuthor;
    }

    public void setNewsAuthor(String newsAuthor) {
        mAuthor = newsAuthor;
    }

    public String getNewsPublishDate() {
        return this.mPublishedAt;
    }

    public void setNewsPublishDate(String newsPublishDate) {
        this.mPublishedAt = newsPublishDate;
    }
}
