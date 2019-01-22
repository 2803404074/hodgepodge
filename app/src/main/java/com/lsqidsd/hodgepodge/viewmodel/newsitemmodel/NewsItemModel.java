package com.lsqidsd.hodgepodge.viewmodel.newsitemmodel;

import android.content.Context;
import android.content.Intent;
import android.databinding.BindingAdapter;
import android.databinding.ObservableInt;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.lsqidsd.hodgepodge.bean.NewsItem;
import com.lsqidsd.hodgepodge.bean.NewsTop;
import com.lsqidsd.hodgepodge.http.OnSuccessAndFaultListener;
import com.lsqidsd.hodgepodge.http.OnSuccessAndFaultSub;
import com.lsqidsd.hodgepodge.http.RetrofitServiceManager;
import com.lsqidsd.hodgepodge.utils.TimeUtil;
import com.lsqidsd.hodgepodge.view.WebViewActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.Observable;
import io.reactivex.observers.DisposableObserver;

public class NewsItemModel<T> {
    private String url;
    private ItemNewsDataListener newsDataListener;
    private ItemNewsDataListener01 newsDataListener01;
    private Context context;
    public ObservableInt progressVisibility = new ObservableInt(View.VISIBLE);
    public ObservableInt lineVisibility = new ObservableInt(View.GONE);
    private List<NewsItem.DataBean> dataBeans = new ArrayList<>();
    private NewsItem.DataBean dataBean;
    private List<NewsTop> newsTopList = new ArrayList<>();

    public NewsItemModel(Context context, T dataBean) {
        this.context = context;
        this.dataBean = (NewsItem.DataBean) dataBean;
    }

    public NewsItemModel(Context context, List<NewsItem.DataBean> dataBeans) {
        this.context = context;
        this.dataBeans = dataBeans;
    }

    public NewsItemModel(String url, Context context, ItemNewsDataListener listener) {
        this.newsDataListener = listener;
        this.url = url;
        this.context = context;
    }

    public String getAuthor() {
        return dataBean.getSource();
    }

    public void click(View view) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra("url", getUrl());
        context.startActivity(intent);
    }

    public String getUrl() {
        return dataBean.getUrl();
    }

    public String getTime() {
        return TimeUtil.formatTime(dataBean.getPublish_time());
    }

    @BindingAdapter({"imageUrl"})
    public static void setImageView(ImageView imageView, String imageUrl) {
        if (!TextUtils.isEmpty(imageUrl)) {
            Picasso.get().load(imageUrl).into(imageView);
        }
    }

    public String getImageUrl() {
        return dataBean.getBimg();
    }

    public String getTitle() {
        return dataBean.getTitle();
    }

    public void getMoreData(int page, ItemNewsDataListener listener) {
        this.newsDataListener = listener;
        getNewsData(page);
    }

    public void getNewsData(int page) {
        getMainViewData(new OnSuccessAndFaultSub(new OnSuccessAndFaultListener() {
            @Override
            public void onSuccess(Object result) {
                NewsItem newsItem = (NewsItem) result;
                if (newsDataListener != null) {
                    if (newsItem.getData().size() > 0) {
                        for (NewsItem.DataBean dataBeann : newsItem.getData()) {
                            dataBeans.add(dataBeann);
                        }
                        newsDataListener.dataBeanChange(dataBeans);
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                progressVisibility.set(View.GONE);
                            }
                        }, 1000);
                    } else {
                        lineVisibility.set(View.VISIBLE);
                        progressVisibility.set(View.GONE);
                    }
                }
            }

            @Override
            public void onFault(String errorMsg) {
                progressVisibility.set(View.GONE);
                lineVisibility.set(View.VISIBLE);
            }
        }), page);
    }

    private void getMainViewData(DisposableObserver<NewsItem> subscriber, int page) {
        Observable<NewsItem> observable = RetrofitServiceManager.getInstance().getHttpApi().getMainNews(page);
        RetrofitServiceManager.getInstance().toSubscribe(observable, subscriber);
    }

    public void getTopNews() {
        Observable<List<NewsTop>> observable = RetrofitServiceManager.getInstance().getHttpApi().getTop();
        RetrofitServiceManager.getInstance().toSubscribe(observable, new OnSuccessAndFaultSub<>(new OnSuccessAndFaultListener() {
            @Override
            public void onSuccess(Object o) {
                newsTopList = (List<NewsTop>) o;
                for (NewsTop newsTop1 : newsTopList) {
                    Log.e("title", newsTop1.getTitle());
                }
                if (newsDataListener01 != null) {
                    newsDataListener01.dataTopBeanChange(newsTopList);
                }
            }

            @Override
            public void onFault(String errorMsg) {

            }
        }));
    }

    public interface ItemNewsDataListener {
        void dataBeanChange(List<NewsItem.DataBean> dataBeans);
    }

    public interface ItemNewsDataListener01 {
        void dataTopBeanChange(List<NewsTop> dataBeans);
    }
}