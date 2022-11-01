package com.arise.droid.views;

import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class Layouts {



    public static final LinearLayout.LayoutParams matchParentMatchParent02f(){
        return MATCH_PARENT_MATCH_PARENT_02F;
    }

    public static final LinearLayout.LayoutParams matchParentMatchParent08f(){
        return MATCH_PARENT_MATCH_PARENT_08F;
    }



    public static final LinearLayout.LayoutParams matchParentMatchParent09f(){
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.9f);
    }


    public static final LinearLayout.LayoutParams matchParentMatchParent05f(){
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.49f);
    }




    public static final LinearLayout.LayoutParams wrapContentMatchParent(){
        return WRAP_CONTENT_MATCH_PARENT;
    }

    public static final LinearLayout.LayoutParams matchParentWrapContent(){
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, WRAP_CONTENT);
    }

    public static final LinearLayout.LayoutParams MATCH_PARENT_MATCH_PARENT =
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


    private static final LinearLayout.LayoutParams MATCH_PARENT_MATCH_PARENT_02F =
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.2f);

    private static final LinearLayout.LayoutParams MATCH_PARENT_MATCH_PARENT_08F =
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.8f);


    private static final LinearLayout.LayoutParams WRAP_CONTENT_MATCH_PARENT =
            new LinearLayout.LayoutParams(WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);

    public static final LinearLayout.LayoutParams MATCH_PARENT_MATCH_PARENT_03F =
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.3f);


    public static final class Linear {
        public static final LinearLayout.LayoutParams matchParentMatchParent(){
            return MATCH_PARENT_MATCH_PARENT;
        }

        public static final LinearLayout.LayoutParams matchParentMatchParent03f(){
            return MATCH_PARENT_MATCH_PARENT_03F;
        }

        public static final LinearLayout.LayoutParams wrapContentWrapContent(){
            return new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        }

        public static final LinearLayout.LayoutParams wrapContentWrapContentWithMargin(int margin){
            LinearLayout.LayoutParams r =  new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            r.setMargins(margin, margin, margin, margin);
            return r;
        }

        public static final LinearLayout.LayoutParams matchParentWrapContentWithMargin(int margin){
            LinearLayout.LayoutParams r =  new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            r.setMargins(margin, margin, margin, margin);
            return r;
        }

    }

    public static final class Frame {
        public static FrameLayout.LayoutParams wrapContentWrapContent(){
            return new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        }

        public static FrameLayout.LayoutParams matchParentMatchParent(){
            return new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        }
    }
}
