package com.snizl.android.libraries;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.snizl.android.R;

import java.util.concurrent.ExecutionException;

public class AspectRatioImageView extends ImageView {
    private Context context;

    public AspectRatioImageView(Context context) {
        super(context);
        this.context = context;
    }

    public AspectRatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(getDrawable() != null){
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = width * getDrawable().getIntrinsicHeight() / getDrawable().getIntrinsicWidth();

            setMeasuredDimension(width, height);
        }
        else{
            setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void setUrl(Context context, String url){
        this.context = context;
        Glide.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .skipMemoryCache(true)
                .crossFade()
                .into(this);
    }

    public void setUri(Context context, Uri uri){
        this.context = context;
        Glide.with(context)
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .skipMemoryCache(true)
                .crossFade()
                .into(this);
    }

    public void setResId(Context context, int resId){
        this.context = context;
        Glide.with(context)
                .load(resId)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .skipMemoryCache(true)
                .crossFade()
                .into(this);
    }

    public void setImage(Bitmap result){
        this.setImageBitmap(result);
    }
}
