package com.example.user.recyclerview_imagedemo;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private final String url = "http://gank.io/api/data/%E7%A6%8F%E5%88%A9/10/1";
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshlayout;
    private RecyclerAdapter myAdapter;
    private StaggeredGridLayoutManager myLayoutManager;
    private int lastVisibleItem;
    private int page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initListener();
        initDatas();
        Log.e("13456", "onCreate: "+android.os.Process.myTid() );
    }

    private void initViews() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview_main);

        myLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(myLayoutManager);

        swipeRefreshlayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshlayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorRefresh);
    }

    private void initListener() {

        swipeRefreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //do something you like
                swipeRefreshlayout.setRefreshing(false);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == recyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 2 >= myAdapter.getItemCount()) {
                    setImage("http://gank.io/api/data/%E7%A6%8F%E5%88%A9/10/" + (++page));
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int[] positions = myLayoutManager.findLastVisibleItemPositions(null);
                lastVisibleItem = Math.max(positions[0], positions[1]);
            }
        });
    }

    private void initDatas() {
        setImage(url);
    }

    private void setImage(String url) {
        VolleyUtil volleyutil = new VolleyUtil(this, url,
                new VolleyUtil.VolleyCallBack() {
                    @Override
                    public void onSuccess(String result) {
                        if (myAdapter == null) {
                            myAdapter = new RecyclerAdapter(getApplicationContext(), result);
                            myAdapter.setonRecyclerItemClickListener(new RecyclerAdapter.onRecyclerItemClickListener() {
                                @Override
                                public void onItemClick(View view, int pos) {
                                    Log.e("78978979", "onItemClick: " + pos);
                                }
                            });
                            recyclerView.setAdapter(myAdapter);
                        } else {
                            myAdapter.addItem(result);
                        }

                    }
                });
    }
}