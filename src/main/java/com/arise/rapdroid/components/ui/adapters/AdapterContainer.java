package com.arise.rapdroid.components.ui.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class AdapterContainer<T> {

    public ArrayAdapter<T> arrayAdapter;
    protected List<T> items = new ArrayList<>();

    public AdapterContainer(@NonNull Context context, int resource){
        arrayAdapter = new ArrayAdapter<>(context, resource);
    }

    public AdapterContainer(@NonNull Context context){
        arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
    }

    public ArrayAdapter<T> adapter(){
        return arrayAdapter;
    }

    public AdapterContainer<T> add(T item){
        items.add(item);
        arrayAdapter.notifyDataSetChanged();
        return this;
    }
}
