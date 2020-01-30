package com.arise.rapdroid.components.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NavView extends LinearLayout {

    LinearLayout leftMenu;
    ScrollView scrollView;
    FrameLayout pageContainer;
    private final Context context;

    private int menuSelectedColor = Color.GREEN;
    private int menuReleasedColor = Color.YELLOW;

    public NavView(Context context) {
        super(context);
        this.context = context;
    }


    public NavView setSelectedColor(int color){
        menuSelectedColor = color;
        return this;
    }

    public NavView setReleasedColor(int color){
        menuReleasedColor = color;
        return this;
    }

    public NavView addButton(int resource, OnClickListener onClickListener) {
        compose();
        ImageButton imageButton = new ImageButton(context);
        imageButton.setOnClickListener(onClickListener);
        imageButton.setImageResource(resource);
        leftMenu.addView(imageButton, Layouts.wrapContentWrapContent());
        return this;
    }


    public NavView compose(){
        if (scrollView == null && leftMenu == null) {
            scrollView = new ScrollView(context);
            leftMenu = new LinearLayout(context);
            scrollView.addView(leftMenu, Layouts.matchParentMatchParent());
            leftMenu.setOrientation(VERTICAL);
            leftMenu.setPadding(0, 0, 0, 0);
            pageContainer = new FrameLayout(context);
            scrollView.setBackgroundColor(menuReleasedColor);
            this.addView(scrollView, Layouts.wrapContentMatchParent());
            this.addView(pageContainer);
        }
        return this;
    }


    private final OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View current) {
            for (Container c: pages){
                if (c.menu == current){
                   c.enable();
                }
                else {
                   c.disable();
                }
            }
        }
    };



    private List<Container> pages = new ArrayList<>();

    public NavView addMenu(int selectedRes, int releasedRes, String id, View rightPage){
        if (rightPage == null){
            return this;
        }
        compose();
        ImageButton button = new ImageButton(context);
        button.setImageResource(selectedRes);
        rightPage.setPadding(10, 10, 10, 10);
        leftMenu.addView(button);
        pageContainer.addView(rightPage);
        pages.add(new Container(button, rightPage, selectedRes, releasedRes));
        button.setOnClickListener(onClickListener);
        show(0);
        requestLayout();
        return this;
    }




    public void show(int index){
        for (int i = 0; i < pages.size(); i++){
            if (i == index){
                pages.get(i).enable();
            }
            else {
                pages.get(i).disable();
            }
        }
    }



    private class Container {
        final ImageButton menu;
        final View page;
        private final int selected;
        private final int released;


        private Container(ImageButton menu, View page, int selected, int released) {
            this.menu = menu;
            this.page = page;
            this.selected = selected;
            this.released = released;
        }

        public void enable() {
            menu.setBackgroundColor(menuSelectedColor);
            menu.setImageResource(selected);
            page.setBackgroundColor(menuSelectedColor);
            page.setVisibility(VISIBLE);
        }

        public void disable() {
            menu.setBackgroundColor(Color.TRANSPARENT);
            menu.setImageResource(released);
            page.setVisibility(INVISIBLE);
        }
    }





}
