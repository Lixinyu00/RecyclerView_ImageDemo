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
    private String TAG = "recycler";
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


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycler_image, null);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

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
