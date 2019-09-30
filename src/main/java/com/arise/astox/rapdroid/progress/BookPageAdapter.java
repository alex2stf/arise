package com.arise.astox.rapdroid.progress;

import android.hardware.SensorEvent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by alex on 1/9/2018.
 */

public abstract class BookPageAdapter extends android.support.v4.app.FragmentPagerAdapter {
    private PageFragment currentPage;

    public BookPageAdapter(FragmentManager fm) {
        super(fm);
    }

    private static final String TAG = BookPageAdapter.class.getSimpleName().toUpperCase();





    @Override
    public Fragment getItem(int position) {
//        Log.i(TAG, "getItem " + position);

        PageFragment fragment = null;
        try {
            fragment = (PageFragment) getClasses().get(position).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        fragment.setPageAdapter(this);
        fragment.setIndex(position);
        return fragment;
    }

    public abstract List<Class<? extends PageFragment>> getClasses();

    @Override
    public int getCount() {
        return getClasses().size();
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object o) {
        super.setPrimaryItem(container, position, o);
        if (o instanceof PageFragment){
            currentPage = (PageFragment) o;
            currentPage.start();
        }
    }

    @Override
    public void startUpdate(ViewGroup container) {
        super.startUpdate(container);
        if (currentPage != null){
//            Log.i(TAG, "startUpdate " + currentPage.getIndex());
            currentPage.start();
        }
    }

    public void onSensorChanged(SensorEvent sensorEvent) {
        if (currentPage != null){
            currentPage.onSensorChanged(sensorEvent);
        }


    }
}
