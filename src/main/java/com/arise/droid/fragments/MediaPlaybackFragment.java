package com.arise.droid.fragments;

import static com.arise.droid.AppUtil.State.PLAYING;
import static com.arise.droid.AppUtil.contentInfoProvider;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.arise.core.tools.AppCache;
import com.arise.core.tools.Mole;
import com.arise.core.tools.ThreadUtil;
import com.arise.droid.AppUtil;
import com.arise.droid.AppUtil.MediaState;
import com.arise.droid.MainActivity;
import com.arise.droid.tools.ContextFragment;

import com.arise.droid.views.MediaControls;
import com.arise.droid.views.MyVideoView;
import com.arise.droid.views.ThumbnailView;
import com.arise.weland.PlaylistWorker;
import com.arise.weland.dto.ContentInfo;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class MediaPlaybackFragment extends ContextFragment {





    private static final Mole log = Mole.getInstance(MediaPlaybackFragment.class);





//    ImageView musicImage;
    ThumbnailView thumbnailView;
    MyVideoView videoView;
    RelativeLayout root;
    ThreadUtil.TimerResult updateSeekbarTimer;

    ContentInfo currentInfo;

    MediaControls mediaControls;
    Map<String, Drawable> arts = new HashMap<>();
    int controlsHideCnt = 0;



    View.OnClickListener showControls = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            showMediaControls();
        }
    };
    private int numErrors =  0;





    private RelativeLayout.LayoutParams getParams(){
        RelativeLayout.LayoutParams params;
        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 1);
        }
        //portrait
        else {
            params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 1);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
        }
        params.addRule(RelativeLayout.CENTER_IN_PARENT, 1);
        params.addRule(RelativeLayout.FOCUSABLE, 0);
        params.addRule(RelativeLayout.FOCUSABLES_TOUCH_MODE, 0);
        return params;

    }

    public void showMediaControls(){
        if (mediaControls != null){
            mediaControls.setVisibility(View.VISIBLE);
        }
        controlsHideCnt = 0;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       if (root == null){
           root = new RelativeLayout(getContext());


           root.setBackgroundColor(Color.BLACK);
           root.setOnClickListener(showControls);
           videoView = new MyVideoView(getContext());
           videoView.setOnClickListener(showControls);
           thumbnailView = new ThumbnailView(getContext());

           root.addView(videoView);
           videoView.setLayoutParams(getParams());
           root.addView(thumbnailView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


           mediaControls = new MediaControls(getContext(), 150);




           RelativeLayout.LayoutParams params =
                   new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                           ViewGroup.LayoutParams.WRAP_CONTENT);
           params.addRule(RelativeLayout.ALIGN_BOTTOM, 1);
           params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1);
           params.addRule(RelativeLayout.ALIGN_TOP, 0);
           root.addView(mediaControls, params);
           mediaControls.setBackgroundColor(Color.TRANSPARENT);


           mediaControls.setCentralButtonImage(android.R.drawable.ic_media_play);
           mediaControls.setLeftButtonImage(android.R.drawable.ic_media_next);
           mediaControls.setRightButtonImage(android.R.drawable.ic_media_previous);


           mediaControls.getCentralButton().setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {

                   MediaState mediaState = AppUtil.getState();
                   if (mediaState.isPlaying()){
                       saveState();
                       stopPlayer();
                       AppUtil.clearState();
                   }
                   else {
                       stopPlayer();
                       AppUtil.clearState();
                       play(AppUtil.getState().getInfo());
                   }
                   showMediaControls();
               }
           });

           mediaControls.getLeftButton().setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   showMediaControls();
               }
           });

           mediaControls.getRightButton().setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   playNextFromPlaylist();
                   showMediaControls();
               }
           });

           //this is only internal
           restoreMediaPlaybackFragment();
       }
       return root;
    }

    private void restoreMediaPlaybackFragment(){
        MediaState mediaState;
        try {

            mediaState = AppUtil.getState();
        }catch (Exception e){
            mediaState = null;
        }
        if (mediaState != null && mediaState.getInfo() != null  && mediaState.isPlaying() ){
            play(mediaState.getInfo());
            showPauseButton();
            updateSeekBar();
//            focusCurrentTab();
        } else {
            stopPlayer();
        }
    }



    private void stopPlayer(){
        currentInfo = null;
        videoView.setOnCompletionListener(null);
        videoView.setOnPreparedListener(null);
        if (videoView != null && videoView.isPlaying()){
            videoView.stopPlayback();
            videoView.seekTo(0);
        }

        ThreadUtil.closeTimer(updateSeekbarTimer);
        showPlayButton();
        AppUtil.clearState();
    }

    private void showPauseButton(){
        if (mediaControls != null){
            mediaControls.setCentralButtonImage(android.R.drawable.ic_media_pause);
            mediaControls.getCentralButton().setEnabled(true);
        }
        updateSeekBar();
    }

    private void showPlayButton(){
        if(mediaControls != null) {
            mediaControls.setCentralButtonImage(android.R.drawable.ic_media_play);
            mediaControls.getCentralButton().setEnabled(true);
        }
        updateSeekBar();
    }



    public void play(final ContentInfo info) {
        if (info == null){
            return;
        }
        File f = new File(info.getPath());
        if (!f.exists()){
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                currentInfo = null;
                videoView.setVisibility(View.VISIBLE);
                videoView.setLayoutParams(getParams());
                int width = info.getWidth();
                int height = info.getHeight();

                log.info("play media size " + width + " " + height);
                if (width > 0 || height > 0){
                    videoView.setVideoSize(width, height);
                }

                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        info.setDuration(mediaPlayer.getDuration());
                        int w = mediaPlayer.getVideoWidth();
                        int h = mediaPlayer.getVideoHeight();
                        if (w > 0 || h > 0){
                            videoView.setVideoSize(w, h);
                        }
                        currentInfo = AppUtil.saveState(info, PLAYING).getInfo();
                        videoView.seekTo(info.getPosition());
                        videoView.start();


                        setupThumbnailViewIfRequired(info);
                        showPauseButton();
                        saveState();
                        startUpdateThread();
//                        focusCurrentTab();
                    }
                });
                videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                        log.error("ERROR DETECTED");
                        numErrors++;
                        if (numErrors < 10){
//                            clearStateAndPlayNextIfRequired();
                        } else {
                            stopPlayer();
                            AppUtil.clearState();
                            numErrors = 0;
                        }
                        return true;
                    }
                });
                videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        stopPlayer();
                        updateSeekBar();
                        clearState();
                        showMediaControls();
                        numErrors = 0;
                        playNextFromPlaylist();
                    }
                });
                videoView.setVideoPath(info.getPath());
                updateSeekBar();
            } //exit main thread runnable
        });

    }



    private void setupThumbnailViewIfRequired(ContentInfo info) {
        if (thumbnailView != null){
            try {
                thumbnailView.regenerate();
                thumbnailView.setDisplayText(info.getName());
            } catch (Exception ex){

            }
        }
    }

    private void startUpdateThread(){
        updateSeekbarTimer = ThreadUtil.repeatedTask(new Runnable() {
            @Override
            public void run() {
                updateSeekBar();
                saveState();
            }
        }, 500);
    }





    private void updateSeekBar(){
        if (mediaControls == null){
            return;
        }
        if (currentInfo != null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (thumbnailView != null){
                       try {
                           thumbnailView.invalidate();
                       }catch (Throwable t){

                       }
                    }
                    if (currentInfo != null && currentInfo.getPosition() > 0){
                        try {
                            if (controlsHideCnt > 10 && currentInfo.isVideo()){
                                mediaControls.setVisibility(View.INVISIBLE);
                            }
                            else {
                                mediaControls.setSeekBarMax(videoView.getDuration());
                                mediaControls.setSeekBarProgress(videoView.getCurrentPosition());
                            }
                            controlsHideCnt++;
                        } catch (Throwable t){
                            //mightthrow java.lang.IllegalStateException
                        }
                    }
                }
            });

        }
    }





    public void saveState() {
        if (videoView != null && currentInfo != null){
            try {

               int newPosition = videoView.getCurrentPosition();

               if (newPosition > currentInfo.getPosition()){
                   AppUtil.saveState(currentInfo, PLAYING);
               }
                currentInfo.setPosition(newPosition);
            } catch (Throwable t){
                //may throw  java.lang.IllegalStateException
            }
        }
    }

    private void clearState() {
//        contentInfoProvider.clearState();
    }


    @Override
    public void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    public void onStop() {
        super.onStop();
        clearState();
    }

    public void stop() {
        stopPlayer();
        AppUtil.clearState();
    }

    public void stopAndPlayPlaylist() {
        stopPlayer();
        playNextFromPlaylist();
    }

    private void playNextFromPlaylist() {
        String path = PlaylistWorker.getNextFromCurrentPlaylist();
        if (path == null){
            return;
        }
        ContentInfo contentInfo = contentInfoProvider.findByPath(path);
        if (contentInfo == null){
            contentInfo = contentInfoProvider.findByPath(ContentInfo.decodePath(path));
            if(contentInfo != null){
                play(contentInfo);
            }
        } else {
            play(contentInfo);
        }
    }


}
