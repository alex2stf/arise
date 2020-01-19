package com.arise.rapdroid.components.ui.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class ListViewAdapter extends BaseAdapter implements ListAdapter {
    private List<View> views = new ArrayList<>();

    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public Object getItem(int i) {
        return views.get(i);
    }

    @Override
    public long getItemId(int i) {
        return views.get(i).getId();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = views.get(i);
        return view;
    }

    public ListViewAdapter add(View view){
        views.add(view);
        return this;
    }

    public void addAndNotify(View view){
        add(view);
        this.notifyDataSetChanged();
    }


    public void setViews(List<View> views) {
        this.views = views;
        this.notifyDataSetChanged();
    }

    public void clear(){
        views.clear();
        this.notifyDataSetChanged();
    }

    public void forEach(Consumer consumer) {
        for (View view: views){
            consumer.consume(view);
        }
    }

    public interface Consumer {
        void consume(View view);
    }
}
