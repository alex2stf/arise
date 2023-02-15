package com.arise.rapdroid.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import com.arise.rapdroid.components.ui.Layouts;

public class MediaControls extends LinearLayout {
    RoundedLayout central;
    RoundedLayout left;
    RoundedLayout right;




    public MediaControls(Context context) {
        this(context, 300);
    }

    public MediaControls(Context context, int minSize) {
        super(context);
        central = new RoundedLayout(getContext(), minSize, true);
        int mmSize = (int) (minSize / 1.5);
        left = new RoundedLayout(getContext(), mmSize, false);
        right = new RoundedLayout(getContext(), mmSize, false);
        setGravity(Gravity.CENTER);
        setOrientation(HORIZONTAL);

        setButton(android.R.drawable.ic_media_play, central);
        setButton(android.R.drawable.ic_media_next, right);
        setButton(android.R.drawable.ic_media_previous, left);

    }

    public View getCentralButton(){
        return central.nextView;
    }



    public void setCentralButtonImage(int res){
        if (getCentralButton() != null && getCentralButton() instanceof ImageButton){
            ((ImageButton) getCentralButton()).setImageResource(res);
        }
    }

    public void setLeftButtonImage(int res) {
        if (getRightButton() != null && getRightButton() instanceof ImageButton){
            ((ImageButton) getRightButton()).setImageResource(res);
        }
    }

    public void setRightButtonImage(int res) {
        if (getLeftButton() != null && getLeftButton() instanceof ImageButton){
            ((ImageButton) getLeftButton()).setImageResource(res);
        }
    }



    private void setButton(int res, RoundedLayout layout) {
        ImageButton imageButton = new ImageButton(getContext());
        imageButton.setBackgroundColor(Color.TRANSPARENT);
        imageButton.setImageResource(res);
        imageButton.setPadding(0, 0, 0, 0);
        imageButton.setScaleType(ImageView.ScaleType.FIT_XY);
        setButton(imageButton, layout);
    }


    private void setButton(View view, RoundedLayout layout) {
        layout.setFront(view);
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        addView(left, Layouts.Linear.wrapContentWrapContent());
        addView(central, Layouts.Linear.wrapContentWrapContent());
        addView(right, Layouts.Linear.wrapContentWrapContent());
    }

    public void setSeekBarMax(int max) {
        central.background.setMax(max);
    }

    public void setSeekBarProgress(int position) {
        central.background.setProgress(position);
    }

    public View getLeftButton() {
        return left.nextView;
    }

    public View getRightButton(){
        return right.nextView;
    }



    class RoundedLayout extends FrameLayout {

        RoundedView background;
        View nextView;
        public RoundedLayout(@NonNull Context context, int minSize, boolean withScroll) {
            super(context);
            background = new RoundedView(context, minSize, withScroll);
            addView(background, Layouts.Frame.matchParentMatchParent());
        }

        public void setFront(View view){
            if (nextView != null){
                removeView(nextView);
            }
            nextView = view;
            addView(nextView, Layouts.Frame.matchParentMatchParent());
        }
    }

    class RoundedView extends View {

        private final int minSize;
        private final boolean withScroll;

        public RoundedView(Context context, int minSize, boolean withScroll) {
            super(context);
            this.minSize = minSize;
            this.withScroll = withScroll;
            setupDrawing();
        }

        private int STROKE_WIDTH = 10;
        private Paint mBasePaint, mDegreesPaint, mCenterPaint;
        private RectF mRect;
        private int centerX, centerY, radius;




        private void setupDrawing() {


            mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mCenterPaint.setColor(Color.WHITE);
            mCenterPaint.setStyle(Paint.Style.FILL);

            mBasePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mBasePaint.setStyle(Paint.Style.STROKE);
            mBasePaint.setStrokeWidth(STROKE_WIDTH);
            mBasePaint.setColor(Color.parseColor("#2b2273"));

            if (withScroll) {
                mDegreesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                mDegreesPaint.setStyle(Paint.Style.STROKE);
                mDegreesPaint.setStrokeWidth(STROKE_WIDTH);
                mDegreesPaint.setColor(Color.parseColor("#5d53b0"));
            }
        }



        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int width = resolveSize(minSize, widthMeasureSpec);
            int height = resolveSize(minSize, heightMeasureSpec);
            int size = width > height ? height : width;
            setMeasuredDimension(size, size);
        }




        int degree = 0;

        @Override
        protected void onDraw(Canvas canvas) {
            // getHeight() is not reliable, use getMeasuredHeight() on first run:
            // Note: mRect will also be null after a configuration change,
            // so in this case the new measured height and width values will be used:
            centerX = getMeasuredWidth()/ 2;
            centerY = getMeasuredHeight()/ 2;
            radius = Math.min(centerX,centerY);
            int startTop = STROKE_WIDTH / 2;
            int startLeft = startTop;

            int endBottom = 2 * radius - startTop;
            int endRight = endBottom;

            if (mRect == null && withScroll) {

                mRect = new RectF(startTop, startLeft, endRight, endBottom);
            }

//            mCenterPaint.setShader(new LinearGradient(startTop, startLeft, endRight, endBottom,
//                    Color.parseColor("#BE0B0252"), Color.parseColor("#AE2004FF"),
//                    Shader.TileMode.CLAMP));
            mCenterPaint.setColor(Color.parseColor("#6e6b6e"));
            canvas.drawCircle(centerX, centerY, radius - STROKE_WIDTH / 2, mBasePaint);

            if (withScroll){
                // subtract half the stroke width from radius so the blue circle fits inside the View

                // Or draw arc from degree 192 to degree 90 like this ( 258 = (360 - 192) + 90:
                // canvas.drawArc(mRect, 192, 258, false, mBasePaint);

                // draw an arc from 90 degrees to 192 degrees (102 = 192 - 90)
                // Note that these degrees are not like mathematical degrees:
                // they are mirrored along the y-axis and so incremented clockwise (zero degrees is always on the right hand side of the x-axis)
                canvas.drawArc(mRect, 0, degree, false, mDegreesPaint);

            }


            // subtract stroke width from radius so the white circle does not cover the blue circle/ arc
            canvas.drawCircle(centerX, centerY, radius - STROKE_WIDTH, mCenterPaint);
        }


        int max = 360;

        public void setMax(int max) {
            this.max = Math.abs(max);
        }

        public void setProgress(int position) {
            this.degree = (360 * position) / max;
            invalidate();
        }
    }
}
