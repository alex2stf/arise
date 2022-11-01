package com.arise.droid;


import static com.arise.core.tools.AppCache.putString;

import com.arise.core.tools.AppCache;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Mole;
import com.arise.droid.impl.AndroidContentDecoder;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.impl.ContentInfoProvider;
import com.arise.weland.impl.RadioPlayer;

public class AppUtil {


    public static final String APP_STATE = "app-state-1275";
    public static final AndroidContentDecoder DECODER = new AndroidContentDecoder();

    public static RadioPlayer rPlayer;

    public static final ContentInfoProvider contentInfoProvider
            = new ContentInfoProvider(DECODER)
            .addRoot(FileUtil.findMusicDir())
            .addRoot(FileUtil.getUploadDir())
            .addRoot(FileUtil.findMoviesDir())
            .addFromLocalResource("weland/config/commons/content-infos.json");
//            .get()
    ;
    public static final Mole log = Mole.getInstance(AppUtil.class);
    public static final String ON_START = "onStart";
    public static final String OPEN_PATH = "openPath";
    public static final String PATH = "path";

    public static MediaState saveState(ContentInfo contentInfo, State state) {
        if (contentInfo == null) {
            contentInfoProvider.getDecoder().clearState();
        } else {
            contentInfoProvider.getDecoder().saveState(contentInfo);
        }

        putString(APP_STATE, state.name());
        return new MediaState(contentInfo, state);
    }

    public static MediaState getState() {
        ContentInfo contentInfo = contentInfoProvider.getDecoder().getSavedState();
        String stateStr = AppCache.getString(APP_STATE, State.STOPPED.name());
        return new MediaState(contentInfo, State.valueOf(stateStr));
    }

    public static void clearState() {
        contentInfoProvider.getDecoder().clearState();
        putString(APP_STATE, State.STOPPED.name());
    }


    public enum State {
        PLAYING, STOPPED;
    }


    public static class MediaState {
        private final ContentInfo info;
        private final State state;

        MediaState(ContentInfo info, State state) {

            this.info = info;
            this.state = state;
        }

        public ContentInfo getInfo() {
            return info;
        }

        public State getState() {
            return state;
        }

        public boolean isPlaying() {
            return State.PLAYING.equals(state);
        }

    }

}

