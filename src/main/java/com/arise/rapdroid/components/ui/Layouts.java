package com.arise.rapdroid.components.ui;

import android.view.ViewGroup;
import android.widget.LinearLayout;

public class Layouts {

    public static final LinearLayout.LayoutParams matchParentMatchParent(){
        return MATCH_PARENT_MATCH_PARENT;
    }

    public static final LinearLayout.LayoutParams matchParentMatchParent02f(){
        return MATCH_PARENT_MATCH_PARENT_02F;
    }

    public static final LinearLayout.LayoutParams matchParentMatchParent08f(){
        return MATCH_PARENT_MATCH_PARENT_08F;
    }

    public static final LinearLayout.LayoutParams matchParentMatchParent088f(){
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.88f);
    }

    public static final LinearLayout.LayoutParams matchParentMatchParent012f(){
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.12f);
    }

    public static final LinearLayout.LayoutParams matchParentMatchParent09f(){
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.9f);
    }

    public static final LinearLayout.LayoutParams matchParentMatchParent01f(){
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.1f);
    }

    public static final LinearLayout.LayoutParams matchParentMatchParent05f(){
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.49f);
    }



    public static final LinearLayout.LayoutParams wrapContentWrapContent(){
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public static final LinearLayout.LayoutParams wrapContentMatchParent(){
        return WRAP_CONTENT_MATCH_PARENT;
    }

    public static final LinearLayout.LayoutParams matchParentWrapContent(){
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public static final LinearLayout.LayoutParams MATCH_PARENT_MATCH_PARENT =
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

    public static final LinearLayout.LayoutParams MATCH_PARENT_MATCH_PARENT_05F =
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.5f);

    public static final LinearLayout.LayoutParams MATCH_PARENT_MATCH_PARENT_03F =
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.3f);

    private static final LinearLayout.LayoutParams MATCH_PARENT_MATCH_PARENT_02F =
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.2f);

    private static final LinearLayout.LayoutParams MATCH_PARENT_MATCH_PARENT_08F =
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.8f);



    private static final LinearLayout.LayoutParams WRAP_CONTENT_MATCH_PARENT =
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
}
