package com.arise.rapdroid.components.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.arise.rapdroid.components.ui.views.LIFolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NavView extends LinearLayout {

    LinearLayout leftMenu;
    ScrollView scrollView;
    FrameLayout page;
    private final Context context;
    ImageView mainBtn;
    private int implodedBtnRes;
    private int expandedBtnRes;
    List<LIFolder> liFolders = new ArrayList<>();


    public NavView(Context context) {
        super(context);

        this.context = context;

    }

    public NavView setMainButton(int implodedBtnRes, int expandedBtnRes) {
        this.implodedBtnRes = implodedBtnRes;
        this.expandedBtnRes = expandedBtnRes;
        mainBtn = new ImageButton(context);
        mainBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
        return this;
    }

    private boolean composed = false;

    public NavView compose(){
        if (composed){
            return this;
        }
        scrollView = new ScrollView(context);
        leftMenu = new LinearLayout(context);
        scrollView.addView(leftMenu, Layouts.matchParentMatchParent());
        leftMenu.setOrientation(VERTICAL);
        page = new FrameLayout(context);
        leftMenu.addView(mainBtn, Layouts.wrapContentWrapContent());
        this.addView(scrollView);
        this.addView(page);
        toggle();
        composed = true;
        return this;
    }


    Map<View, View> views = new HashMap<>();
    Map<View, View> menus = new HashMap<>();
    List<View> indexes = new ArrayList<>();


    public NavView addMenu(int implodedBtnRes, String text, View xx){

        if (xx == null){
            return this;
        }
        if (!composed){
            compose();
        }
        ImageButton button = new ImageButton(context);
        button.setImageResource(implodedBtnRes);
        LIFolder btn = new LIFolder(context)
                .setup(text, implodedBtnRes, null, button)
                .hideText();
        views.put(button, xx);
        menus.put(button, btn);
        indexes.add(button);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                for (View v: views.values()){
                    v.setVisibility(INVISIBLE);
                }
                for (View v: menus.values()){
                    v.setBackgroundColor(Color.GREEN);
                }
                views.get(view).setVisibility(VISIBLE);
                menus.get(view).setBackgroundColor(Color.RED);
                views.get(view).setBackgroundColor(Color.RED);
            }
        });
        liFolders.add(btn);
        leftMenu.addView(btn);
        page.addView(xx);
        requestLayout();
        return this;
    }

    public void show(int index){
        for (int i = 0; i < indexes.size(); i++){
            if (i == index){
                views.get(indexes.get(i)).setVisibility(VISIBLE);
                menus.get(indexes.get(i)).setBackgroundColor(Color.RED);
            }
            else {
                views.get(indexes.get(i)).setVisibility(INVISIBLE);
                menus.get(indexes.get(i)).setBackgroundColor(Color.GREEN);
            }
        }
    }

    boolean collapsed = true;
    private void toggle(){
        if (collapsed){
            scrollView.setLayoutParams( Layouts.matchParentMatchParent088f());
            page.setLayoutParams( Layouts.matchParentMatchParent012f());
            collapsed = false;
            mainBtn.setImageResource(implodedBtnRes);
            for(LIFolder lf: liFolders){
                lf.hideText();
            }
        } else {
            scrollView.setLayoutParams( Layouts.matchParentMatchParent012f());
            page.setLayoutParams( Layouts.matchParentMatchParent088f());
            collapsed = true;
            mainBtn.setImageResource(expandedBtnRes);
            for(LIFolder lf: liFolders){
                lf.showText();
            }
        }
    }


}
