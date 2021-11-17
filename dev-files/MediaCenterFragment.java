package com.arise.rapdroid.media.server.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.arise.core.tools.models.CompleteHandler;
import com.arise.rapdroid.RAPDUtils;
import com.arise.rapdroid.SmartWebView;
import com.arise.rapdroid.media.server.AppUtil;
import com.arise.rapdroid.media.server.MainActivity;
import com.arise.rapdroid.media.server.R;
import com.arise.rapdroid.media.server.appviews.ContentInfoDisplayer;
import com.arise.rapdroid.media.server.Icons;
import com.arise.rapdroid.media.server.appviews.TouchPadView;
import com.arise.rapdroid.media.server.appviews.SettingsView;
import com.arise.rapdroid.media.server.views.MediaDisplayer;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.dto.DeviceStat;
import com.arise.weland.dto.Playlist;
import com.arise.rapdroid.components.ContextFragment;
import com.arise.rapdroid.components.ui.NavView;
import com.arise.weland.utils.AutoplayOptions;

import java.net.URI;
import java.net.URISyntaxException;

import static com.arise.weland.utils.AutoplayOptions.isAutoplayMusic;
import static com.arise.weland.utils.AutoplayOptions.setMusicAutoplay;


public class MediaCenterFragment extends ContextFragment {
    private MediaPlaybackFragment mediaPlaybackFragment;



    private SettingsView settingsView;
    private MainActivity mainActivity;

    public MediaCenterFragment(){

    }

    View root;











    ContentInfoDisplayer localMusic;
    ContentInfoDisplayer localVideos;
    MenuItem autoVideoItem;
    MenuItem autoMusicItem;


    String getAutoplayVideosText(){
        return AutoplayOptions.isAutoplayVideos() ? "Autoplay off" : "Autoplay on";
    }

    String getAutoplayMusicText(){
        return "TODO";
    }

    //TODO meke this once:
    static String[] playlists = Playlist.displayableNames();
    static String[] names = new String[playlists.length + 1];
    static {
        for(int i = 0; i < playlists.length; i++){
            names[i] = playlists[i];
        }

        names[names.length - 1] = "CONTROL";
    }

    private void updateMenuItems(){
        if (autoVideoItem != null){
            autoVideoItem.setTitle(getAutoplayVideosText());
        }
        if (autoMusicItem != null){
            autoMusicItem.setTitle(getAutoplayMusicText());
        }
    }

    MediaDisplayer.Option optionOpen = new MediaDisplayer.Option() {
        @Override
        public String getTitle(ContentInfo info) {
            return "Open";
        }

        @Override
        public void onClick(ContentInfo info, MediaDisplayer displayer) {
            WelandClient.openFile(info.getPath(), displayer.getWorker(), new CompleteHandler() {
                @Override
                public void onComplete(Object data) {
                    checkControlRequirements(displayer);
                }
            });
        }
    };

    private void checkControlRequirements(MediaDisplayer displayer) {

        Playlist playlist = Playlist.find(displayer.getPlaylistId());
        if (Playlist.GAMES.equals(playlist) || Playlist.PRESENTATIONS.equals(playlist)){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addRemoteControlTab(displayer.getRemoteConnection());
                    touchPadView.showControlView();
                }
            });
        }
    }

    MediaDisplayer.Option playNativeOptions = new MediaDisplayer.Option() {
        @Override
        public String getTitle(ContentInfo info) {
            return "Play native";
        }

        @Override
        public void onClick(ContentInfo info, MediaDisplayer displayer) {
            WelandClient.playNative(info.getPath(), displayer.getWorker(), new CompleteHandler() {
                @Override
                public void onComplete(Object data) {
                    checkControlRequirements(displayer);
                }
            });
        }
    };


    MediaDisplayer.Option playEmbeddedOption = new MediaDisplayer.Option() {
        @Override
        public String getTitle(ContentInfo info) {
            return "Play embedded";
        }

        @Override
        public void onClick(ContentInfo info, MediaDisplayer displayer) {
            WelandClient.playEmbedded(info.getPath(), displayer.getWorker(), new CompleteHandler() {
                @Override
                public void onComplete(Object data) {
                    checkControlRequirements(displayer);
                }
            });
        }
    };

    MediaDisplayer.Option stopOption = new MediaDisplayer.Option() {
        @Override
        public String getTitle(ContentInfo info) {
            return "Stop";
        }

        @Override
        public void onClick(ContentInfo info, MediaDisplayer displayer) {
            WelandClient.stop(info, displayer.getWorker());
        }
    };

    DialogInterface.OnClickListener cancelDialog = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {

        }
    };


    MediaDisplayer.Option addToQueue = new MediaDisplayer.Option() {
        @Override
        public String getTitle(ContentInfo info) {
            return "Play later (native)";
        }

        @Override
        public void onClick(ContentInfo info, MediaDisplayer displayer) {

//            if (info.getDuration() > 10) {
//                WelandClient.addToQueue(info, "native", displayer.getWorker());
//                return;
//            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Estimated duration (seconds)");
            EditText editText = new EditText(getContext());
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setText(info.getDuration() + "");
            builder.setView(editText);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    RAPDUtils.hideKeyboard(editText);
                    info.setDuration(Integer.valueOf(editText.getText().toString()));
                    WelandClient.addToQueue(info, "native", displayer.getWorker());
                }
            });

            builder.setNegativeButton("Cancel", cancelDialog);
            builder.show();
        }
    };




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (root == null) {
            root = new SmartWebView(getContext(), SmartWebView.DEFAULT);
//            root = new NavView(getContext())
//                .setSelectedColor(Icons.tab1Foreground)
//                .setReleasedColor(Icons.tab1Background);

//            URI uri = null;
//            try {
//                uri = new URI("http://localhost:8221/");
//            } catch (URISyntaxException e) {
//                e.printStackTrace();
//            }
////            RemoteConnection localConnection = new RemoteConnection(uri, DeviceStat.getInstance());


//            localMusic = new ContentInfoDisplayer(getContext(), R.drawable.ic_unknown_file, localConnection, "music", "Music");
//
//            localVideos = new ContentInfoDisplayer(getContext(), R.drawable.ic_unknown_file, localConnection, "videos", "Videos");
//
//
//            localMusic.enableMenu(R.drawable.ic_menu_light)
//                    .addMenu(getAutoplayMusicText(), new MediaDisplayer.OnMenuClickListener() {
//                        @Override
//                        public void onClick(MenuItem menuItem) {
//                            autoMusicItem = menuItem;
//                            if (isAutoplayMusic()){
//                                setMusicAutoplay(false);
//                            }
//                            else {
//                                setMusicAutoplay(true);
//                                mediaPlaybackFragment.startMusicAutoplay();
//                            }
//                            updateMenuItems();
//
//                        }
//                    });
//
//            localVideos.enableMenu(R.drawable.ic_menu_light)
//                    .addMenu(getAutoplayVideosText(), new MediaDisplayer.OnMenuClickListener() {
//                        @Override
//                        public void onClick(MenuItem menuItem) {
//                            autoVideoItem = menuItem;
//                            if (AutoplayOptions.isAutoplayVideos()){
//                                AutoplayOptions.setVideosAutoplay(false);
//                            }
//                            else {
//                                AutoplayOptions.setVideosAutoplay(true);
//                                mediaPlaybackFragment.startVideosAutoplay();
//                            }
//                            updateMenuItems();
//
//                        }
//                    });
//
//
//
//           ContentInfoDisplayer localStreams = new ContentInfoDisplayer(getContext(), R.drawable.ic_unknown_file, localConnection, Playlist.STREAMS.name(), "Streams");
//
//           localStreams.addOption(optionOpen);
//           localMusic.addOption(optionOpen);
//           localVideos.addOption(optionOpen);





//            root.addMenu(icon(Playlist.MUSIC)[0], icon(Playlist.MUSIC)[1], "Music", localMusic);
//            root.addMenu(icon(Playlist.VIDEOS)[0], icon(Playlist.VIDEOS)[1], "Videos", localVideos);
//            root.addMenu(icon(Playlist.STREAMS)[0], icon(Playlist.STREAMS)[1], "Streams", localStreams);



//            root.addButton(R.drawable.ic_media_add, new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                   AppUtil.showConnectOptions(getContext(), settingsView, new CompleteHandler<RemoteConnection>() {
//                       @Override
//                       public void onComplete(RemoteConnection data) {
//                           if (data == null){
//                               return;
//                           }
//
//                           runOnUiThread(new Runnable() {
//                               @Override
//                               public void run() {
//                                   AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                                   builder.setTitle("Select list");
//
//
//                                   builder.setItems(names, new DialogInterface.OnClickListener() {
//                                       @Override
//                                       public void onClick(DialogInterface dialogInterface, int i) {
//                                           if (data.getDeviceStat() == null){
//                                               System.out.println("WTF???");
//                                           }
//                                           if ("CONTROL".equals(names[i])){
//                                                addRemoteControlTab(data);
//                                           }
//                                           else {
//                                               addRemoteTab(data, names[i]);
//                                           }
//                                       }
//                                   });
//                                   builder.create().show();
//                               }
//                           });
//
//                       }
//                   });
//                }
//            });
//        }

        return root;
    }


    TouchPadView touchPadView;
    int touchPadIndex = 0;

//    public void addRemoteControlTab(RemoteConnection connection){
//
//        if (this.touchPadView != null){
//            this.touchPadView.setConnection(connection);
//            lockTabLayout();
//            touchPadView.unLockedIcon();
//            root.show(touchPadIndex);
//            return;
//        }
//        touchPadView = new TouchPadView(getContext());
//        touchPadView.setConnection(connection);
//        lockTabLayout();
//        touchPadView.unLockedIcon();
//        touchPadView.onButtonClick(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (mainActivity.isViewPagerLocked()) {
//                    mainActivity.unlockViewPager();
//                    ((ImageButton)view).setImageResource(R.drawable.ic_lock);
//                }
//                else {
//                    mainActivity.lockViewPager();
//                    ((ImageButton)view).setImageResource(R.drawable.ic_unlock);
//                }
//            }
//        });
//        touchPadIndex = root.addMenu(R.drawable.ic_touchpad, R.drawable.ic_touchpad_disabled, "Remote Ctrl", touchPadView);
//        root.show(touchPadIndex);
//    }
//
//    void lockTabLayout(){
//        if (mainActivity != null){
//            mainActivity.lockViewPager();
//        }
//    }
//
//
//    static final int [] musicIcons = new int[]{R.drawable.ic_local_music, R.drawable.ic_local_music_disabled};
//    static final int [] videoIcons = new int[]{R.drawable.ic_local_video, R.drawable.ic_local_video_disabled};
//    static final int [] streamIcons = new int[]{R.drawable.ic_local_stream, R.drawable.ic_local_stream_disabled};
//
//    int[] icon(Playlist playlist){
//        switch (playlist){
//            case MUSIC:
//                return musicIcons;
//            case VIDEOS:
//                return videoIcons;
//            case STREAMS:
//                return streamIcons;
//        }
//        return musicIcons;
//    }


//    public void addRemoteTab(RemoteConnection data, String playlistName){
//
//        Playlist playlist = Playlist.find(playlistName);
//
//        ContentInfoDisplayer displayer =
//                new ContentInfoDisplayer(getContext(), R.drawable.ic_unknown_file, data, playlistName, data.getName() + " " + playlistName);
//        displayer.enableMenu(R.drawable.ic_menu_light);
//        displayer.addMenu("Remove", new MediaDisplayer.OnMenuClickListener() {
//            @Override
//            public void onClick(MenuItem menuItem) {
//                root.removeTab(displayer);
//            }
//        });
//        displayer.addOption(optionOpen);
//        displayer.addOption(playNativeOptions);
//        displayer.addOption(playEmbeddedOption);
//        displayer.addOption(stopOption);
//        displayer.addOption(addToQueue);
////        displayer.addOption(addToQueueEmbedded);
//        root.addMenu(icon(playlist)[0], icon(playlist)[1], data.getDeviceStat().getDisplayName(), displayer);
//    }


//    public void setMediaPlaybackFragment(MediaPlaybackFragment mediaPlaybackFragment) {
//        this.mediaPlaybackFragment = mediaPlaybackFragment;
//    }
//
//    public MediaCenterFragment setNeworkRefreshView(SettingsView settingsView) {
//        this.settingsView = settingsView;
//        return this;
//    }
//
//    public MediaCenterFragment setMainActivity(MainActivity mainActivity) {
//        this.mainActivity = mainActivity;
//        return this;
//    }
}
