package cn.wiskind.cordova.plugin.zltdsdkscanner;

import android.content.Context;
import android.content.res.Resources;
import android.media.MediaPlayer;

/**
 * 声音工具
 */
public final class SoundUtils {

    private MediaPlayer successPlayer;

    public SoundUtils(Context context) {
        String packageName = context.getPackageName();
        Resources resources = context.getResources();
        int mSuccessId = resources.getIdentifier("success", "raw", packageName);
        successPlayer = MediaPlayer.create(context, mSuccessId);
        successPlayer.setVolume(1, 1);
    }

    public void success() {
        if (!successPlayer.isPlaying()) {
            successPlayer.start();
        }
    }

    public void release() {
        successPlayer.release();
    }

}
