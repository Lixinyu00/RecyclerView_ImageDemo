# RecyclerView_ImageDemo
Recyclerview实现瀑布流图片显示，包含Volley、Glide用法
本项目CSDN博客文章：http://blog.csdn.net/qq_30128497/article/details/77450939
###RecyclerView简介：
RecyclerView是一个像listview和GridView合体的View，功能比较强大，使用起来还比较简单。
想具体学习RecyclerView的话可以到鸿洋大佬的博客中学习
[Android RecyclerView 使用完全解析 体验艺术般的控件](http://blog.csdn.net/lmj623565791/article/details/45059587)

RecyclerView中提供了几个方法可以设置View的效果，以下几条总结言简意赅：
你想要控制其显示的方式，请通过布局管理器LayoutManager
你想要控制Item间的间隔（可绘制），请通过ItemDecoration
你想要控制Item增删的动画，请通过ItemAnimator
你想要控制点击、长按事件，请自己写
###正题：实现网络图片瀑布流
![这里写图片描述](http://img.blog.csdn.net/20170821154301489?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvcXFfMzAxMjg0OTc=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
*图1图片瀑布流效果展示*


![](http://img.blog.csdn.net/20170821153618893?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvcXFfMzAxMjg0OTc=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
*图2标题栏隐藏及下拉刷新*

此练习设计大体思路：
数据为gank.io的福利接口，地址为http://gank.io/api/data/%E7%A6%8F%E5%88%A9/10/1
①将资源URL用Volley进行请求返回资源Json字段 
②通过FastJson将Json中的资源解析出来
③添加RecyclerView控件，并将LayoutManager设置为StaggeredGridLayoutManager（交错网格布局）
④给RecyclerView添加Adapter，在Adapter中将资源填入每一个Item中利用Glide显示图片
**activity_main.xml**
在布局中添加控件，RecyclerView是我们的主角，其他的控件是做下拉刷新及标题栏隐藏的见图2，可忽略。
```
<?xml version="1.0" encoding="utf-8"?>
<android.support.design.widget.CoordinatorLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/main_content"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fitsSystemWindows="true">

    <android.support.design.widget.AppBarLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <android.support.v7.widget.Toolbar
            android:id="@+id/toolbar_main"
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize"
            app:layout_scrollFlags="scroll">
        </android.support.v7.widget.Toolbar>
    </android.support.design.widget.AppBarLayout>

    <android.support.v4.widget.SwipeRefreshLayout
        android:id="@+id/swipeRefreshLayout"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_behavior="@string/appbar_scrolling_view_behavior">

        <android.support.v7.widget.RecyclerView
            android:id="@+id/recyclerview_main"
            android:layout_width="match_parent"
            android:layout_height="match_parent"/>
    </android.support.v4.widget.SwipeRefreshLayout>
</android.support.design.widget.CoordinatorLayout>
```

**MainActivity：**
在Activity中设置RecyclerView的布局管理及添加适配器
```
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
    }

    private void initViews() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview_main);
        //设置RecyclerView的视图布局
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
```
**RecyclerAdapter**
RecyclerView适配器，在适配器中先进行了JSON字段的解析，然后在onBindViewHolder中通过Glide加载图片。
```
package com.example.user.recyclerview_imagedemo;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LiXinyu
 * @date 2017/8/16 15:38.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

    private Context mContext;
    private String mJson;
    private List<ImageBean.Result> mDatas;
    private List<Integer> mHeight;
    private List<String> urls;
    private onRecyclerItemClickListener listener;

    public RecyclerAdapter(Context context, String datas) {
        mContext = context;
        mJson = datas;
        initdata();
    }

    private void initdata() {
        ImageBean imageBean = JSON.parseObject(mJson, ImageBean.class);
        mDatas = imageBean.getResults();
        mHeight=new ArrayList<>();
        urls=new ArrayList<>();
    }

//  初始化view视图，将item视图添加到RecyclerView中
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycler_image, null);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }
//  添加数据到View中
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        String url;
        url = mDatas.get(position).getUrl();
        if (mHeight.size()<=mDatas.size()){
            mHeight.add((int)(400+Math.random()*300));
            urls.add(url);
        }
        ViewGroup.LayoutParams layoutParams = holder.imageView.getLayoutParams();
        layoutParams.height = mHeight.get(position);
        holder.imageView.setLayoutParams(layoutParams);
        Glide.with(mContext).load(url).into(holder.imageView);
        if(listener!=null){
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v,position);
                }
            });
        }
    }
    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public void addItem(String str){
        List<ImageBean.Result> result;
        ImageBean imageBean = JSON.parseObject(str, ImageBean.class);
        result= imageBean.getResults();
        mDatas.addAll(result);
        notifyDataSetChanged();
    }
    public interface onRecyclerItemClickListener{
        void onItemClick(View view,int pos);
    }
    public void setonRecyclerItemClickListener(onRecyclerItemClickListener listener){
        this.listener=listener;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public MyViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image_recycler);
        }
    }
}

```
关于Volley及Json就不贴出来了 没什么特殊的，Volley中写了一个onResponse的回调监听解决了数据的传输问题。


