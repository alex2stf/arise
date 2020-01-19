package com.arise.rapdroid.components.ui.views;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.arise.rapdroid.components.ui.Layouts;

public class LIFolder extends LinearLayout {
    private final Context ctx;

    private Object data;


    public LIFolder(Context context) {
        super(context);
        this.ctx = context;
    }

    public LIFolder(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.ctx = context;
    }

    public LIFolder(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.ctx = context;
    }



    public LIFolder setup(String text){
        return setup(text, android.R.drawable.ic_menu_view, null);
    }

    public Object getData() {
        return data;
    }

    public LIFolder setData(Object data) {
        this.data = data;
        return this;
    }

    public LIFolder setup(String text, int iconResource, String description){
        ImageView imageView = new ImageView(ctx);
        imageView.setImageResource(iconResource);
        setPadding(10, 10, 10, 10);
        return setup(text, iconResource, description, imageView);
    }


    TextView textView;
    TextView descriptionView;
    ImageView imageView;
    LinearLayout rightCnt;

    public LIFolder hideText(){
        rightCnt.setVisibility(INVISIBLE);
        imageView.setLayoutParams(Layouts.matchParentWrapContent());
        return this;
    }

    public LIFolder showText(){
        rightCnt.setVisibility(VISIBLE);
        imageView.setLayoutParams(Layouts.matchParentMatchParent08f());
        return this;
    }

    public LIFolder setup(String text, int iconResource, String description, ImageView imageView){
        this.imageView = imageView;
        setOrientation(HORIZONTAL);
        addView(imageView, Layouts.matchParentMatchParent08f());

        rightCnt = new LinearLayout(ctx);

        if (description == null) {
            textView = new TextView(ctx);
            textView.setText(text);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                textView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
            }
            textView.setGravity(Gravity.CENTER_VERTICAL);
            rightCnt.addView(textView, Layouts.wrapContentMatchParent());
            textView.setTypeface(null, Typeface.BOLD_ITALIC);
        } else {
            SmartLayout smartLayout = new SmartLayout(ctx);
            textView = smartLayout.addTextView(text, View.TEXT_ALIGNMENT_TEXT_START);
            descriptionView = smartLayout.addTextView(description, TEXT_ALIGNMENT_TEXT_START);
            rightCnt.addView(smartLayout, Layouts.wrapContentMatchParent());

        }
        addView(rightCnt, Layouts.matchParentMatchParent02f());
        return this;
    }


}
