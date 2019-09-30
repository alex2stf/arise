package com.arise.astox.rapdroid.progress;

import android.content.res.Configuration;
import android.support.v4.app.FragmentActivity;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by alex on 1/9/2018.
 */

public enum Coordinate {
    OUTER_WEST,
    INNER_NORTH,
    INNER_EAST;

    private float _x;
    private float _y;
    private float _toX;






    public float x(){  return _x; }
    public float y(){  return _y; }



    public int getScreenOrientation(FragmentActivity activity)
    {
        Display getOrient = activity.getWindowManager().getDefaultDisplay();
        int orientation = Configuration.ORIENTATION_UNDEFINED;
        if(getOrient.getWidth()==getOrient.getHeight()){
            orientation = Configuration.ORIENTATION_SQUARE;
        } else{
            if(getOrient.getWidth() < getOrient.getHeight()){
                orientation = Configuration.ORIENTATION_PORTRAIT;
            }else {
                orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        return orientation;
    }

    public void digest(ViewGroup animationLayout, View view, FragmentActivity context) {

       int orientation = getScreenOrientation(context);


        if (this.equals(OUTER_WEST)){
            _x = -view.getWidth();
            _y = animationLayout.getHeight() / 2;
            return;
        }

        if (this.equals(INNER_NORTH)){
            _x = animationLayout.getHeight() - view.getHeight();
            _y = animationLayout.getWidth() / 2;
            return;
        }

        if (this.equals(INNER_EAST)){
            _x = animationLayout.getWidth() - view.getWidth();
            _y = animationLayout.getHeight() - view.getHeight();
            return;
        }
    }


}
