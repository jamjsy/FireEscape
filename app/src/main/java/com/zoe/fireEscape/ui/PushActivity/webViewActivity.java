package com.ZOE.FireEscape.ui.PushActivity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.ZOE.FireEscape.entity.HistoryUrl;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import com.ZOE.FireEscape.R;
import com.ZOE.FireEscape.Utils.ActivityCollector;
import com.ZOE.FireEscape.adapter.WebViewAdapter;

/**
 * Created by aiyuan on 2017/2/20
 */
public class webViewActivity extends AppCompatActivity {
    private static final String TAG = "webViewActivity";
    private SwipeRefreshLayout swipeReflush;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        ActivityCollector.addActivity(this);
        swipeReflush = (SwipeRefreshLayout)findViewById(R.id.swipe_reflush);
        swipeReflush.setColorSchemeResources(R.color.colorPrimary);
        swipeReflush.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showHistory();
            }
        });
        showHistory();
    }

    private void showHistory() {
        ListView listView = (ListView)findViewById(R.id.list_view);
        List<String> webViews = new ArrayList<>();
        List<HistoryUrl> historyUrls = DataSupport.findAll(HistoryUrl.class);
        for(HistoryUrl historyUrl : historyUrls){
            webViews.add(historyUrl.getUrlPath());
        }
        WebViewAdapter webViewAdapter = new WebViewAdapter(this, webViews);
        listView.setAdapter(webViewAdapter);
        swipeReflush.setRefreshing(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
