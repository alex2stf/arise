package com.arise.astox.rapdroid.progress;


import android.hardware.SensorEvent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by alex on 1/4/2018.
 */

public abstract class PageFragment extends android.support.v4.app.Fragment {
    public static final int STOPPED = 0;
    public static final int PLAYING = 1;
    public static final int PAUSED = 2;
    public static final int PENDING = 4;
    private static final String TAG = PageFragment.class.getSimpleName().toUpperCase();
    protected int state = STOPPED;
    protected ViewGroup rootView;
    boolean started = false;
    boolean paused = false;
    BookPageAdapter pageAdapter;
//    ViewGroup animationLayout;
    Map<Integer, ImageView> images = new HashMap<>();
    MediaPlayer mediaPlayer;
    private int in;

    protected int playTimes = 0;

    public PageFragment(){

    }

    public int getIndex() {
        return in;
    }

    public void setIndex(int index) {
        this.in = index;
    }

    public final boolean resAvailable(){
        return (getContext() != null || rootView != null);
    }

    public final void start(){
        if (resAvailable() &&  state != PLAYING){
            onAdded();
            state = PLAYING;
        }
    }

    protected abstract void onAdded();




    public final void stop() {
        Log.i(TAG, "STOP view " + getIndex());
        stopSounds();
        onRemoved();
        state = STOPPED;

    }


    protected void stopSounds(){
        if (mediaPlayer != null){

            try {
                mediaPlayer.reset();
            }catch (Exception e){

            }



            try {
                mediaPlayer.stop();
            }catch (Exception e){

            }

            try {
                mediaPlayer.release();
            }catch (Exception e){

            }


            mediaPlayer = null;
        }
    }

    protected void onRemoved(){}








    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        int id = Math.abs(( "PTHOSOPAgwe"+ new Date() + UUID.randomUUID().toString()).hashCode());

        if(container.findViewById(id) != null){
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        rootView = new FrameLayout(getContext());
        rootView.setId(id);



        rootView.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));





        onRootViewCreated();
        return rootView;
    }

    protected void onRootViewCreated() {
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(!isVisibleToUser){
            stop();
            cancelAnimations();
        } else {
            start();
        }
    }

    public void setPageAdapter(BookPageAdapter pageAdapter) {
        this.pageAdapter = pageAdapter;
    }


    protected void cancelAnimations(){
        if (rootView != null){
            rootView.removeAllViews();
        }
        if(images != null){
            images.clear();
        }
    }

    public Animation buildAnimation(View imageView, float fromX, float fromY, float toX, float toY, long duration, final Callback callback){


        imageView.setVisibility(View.VISIBLE);

        TranslateAnimation trans = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_PARENT, fromX,
                TranslateAnimation.RELATIVE_TO_PARENT, toX,
                TranslateAnimation.RELATIVE_TO_PARENT,  fromY,
                TranslateAnimation.RELATIVE_TO_PARENT, toY);
        //


        trans.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                state = PLAYING;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (callback != null){
                    callback.onComplete();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


        trans.setDuration(duration);
        trans.setFillAfter(true);
        return trans;
    }

    public ImageView animateImage(int imageId, float fromX, float fromY, float toX, float toY, long duration, final Callback callback){
        ImageView imageView;
        if (!images.containsKey(imageId)){
            imageView = new ImageView(getContext());
        } else {
            imageView = images.get(imageId);
        }
        imageView.setTranslationX(fromX);
        imageView.setTranslationY(fromY);
        imageView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));

        if (!images.containsKey(imageId)){
            rootView.addView(imageView);
            images.put(imageId, imageView);
        }
        imageView.setImageResource(imageId);
        imageView.setScaleType(ImageView.ScaleType.FIT_START);
        Animation animation = buildAnimation(imageView, fromX, fromY, toX, toY, duration, callback);
        imageView.startAnimation(animation);
        return imageView;
    }

    public void move(int imageId, final Coordinate from, final Coordinate to, final long duration, final  Callback callback){
        final ImageView imageView;
        if (!images.containsKey(imageId)){
            imageView = new ImageView(getContext());
        } else {
            imageView = images.get(imageId);
        }
        imageView.setTranslationX(from.x());
        imageView.setTranslationY(from.y());

        imageView.setVisibility(View.INVISIBLE);
        imageView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));


        imageView.setImageResource(imageId);
        imageView.setScaleType(ImageView.ScaleType.FIT_START);

        if (!images.containsKey(imageId)){
            rootView.addView(imageView);
            images.put(imageId, imageView);
        }

        imageView.post(new Runnable() {
            @Override
            public void run() {
                imageView.setVisibility(View.VISIBLE);
                from.digest(rootView, imageView, getActivity());
                to.digest(rootView, imageView, getActivity());

                TranslateAnimation trans = new TranslateAnimation(
                        TranslateAnimation.ABSOLUTE, from.x(),
                        TranslateAnimation.ABSOLUTE, to.x(),
                        TranslateAnimation.ABSOLUTE,  from.y(),
                        TranslateAnimation.ABSOLUTE, to.y());


                trans.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        state = PLAYING;
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (callback != null){
                            callback.onComplete();
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                trans.setDuration(duration);
                trans.setFillAfter(true);
                imageView.startAnimation(trans);
            }
        });



    }

    public void playSound( int res, final Callback callback) {
        mediaPlayer = MediaPlayer.create(getContext(), res);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        if (callback != null){
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (callback != null){
                        callback.onComplete();
                    }
                }
            });
        }



        mediaPlayer.start();
    }


    public ImageView getImageViewById(int id){
        return images.get(id);
    }

    public void playSoundAndAnimation(int soundId, int imageId, float fromX, float fromY, float toX, float toY, long duration, final Callback callback ){
        final int counts[] = new int[]{0};

        playSound(soundId, new Callback() {
            @Override
            public void onComplete() {

                counts[0]++;
                Log.i(TAG, "complete sound " + counts[0]);
                if (counts[0] == 2 && callback != null){
                    callback.onComplete();
                }
            }
        });

        animateImage(imageId, fromX, fromY, toX, toY, duration, new Callback() {
            @Override
            public void onComplete() {

                counts[0]++;
                Log.i(TAG, "complete animation " + counts[0]);
                if (counts[0] == 2 && callback != null){
                    callback.onComplete();
                }
            }
        });
    }

    public void onSensorChanged(SensorEvent sensorEvent) {
    }
}
