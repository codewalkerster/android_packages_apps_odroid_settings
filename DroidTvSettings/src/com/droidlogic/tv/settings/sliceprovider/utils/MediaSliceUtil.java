package com.droidlogic.tv.settings.sliceprovider.utils;

import android.content.Context;
import android.os.SystemProperties;
import android.net.Uri;
import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.sliceprovider.MediaSliceConstants;

public class MediaSliceUtil {
    public static String getFirstSegment(Uri uri) {
        if (uri.getPathSegments().size() > 0) {
            return uri.getPathSegments().get(0);
        }
        return null;
    }

    public static String generateTargetSliceUri(String path) {
        switch (path) {
            case MediaSliceConstants.MATCH_CONTENT_PATH:
            case MediaSliceConstants.RESOLUTION_PATH:
            case MediaSliceConstants.HDR_FORMAT_PREFERENCE_PATH:
            case MediaSliceConstants.COLOR_ATTRIBUTE_PATH:
            case MediaSliceConstants.DOLBY_VISION_MODE_PATH:
                return "content://" + MediaSliceConstants.HDR_AUTHORITY + "/" + path;
            case MediaSliceConstants.ADVANCED_SOUND_SETTINGS_PATH:
            case MediaSliceConstants.ADVANCED_SOUND_SETTINGS_FORMAT_SELECTION_PATH:
                return "content://" + MediaSliceConstants.ADVANCED_SOUND_AUTHORITY + "/" + path;
            default:
                throw new IllegalStateException("Unexpected value: " + path);
        }
    }

    public static String generateKeyFromSurroundSoundFormatId(Context context, int formatId) {
        return context.getString(R.string.surround_sound_format_key_prefix) + formatId;
    }

    public static int fetchSurroundSoundFormatIdFromKey(Context context, String key) {
        return Integer.parseInt(
                key.replace(context.getString(R.string.surround_sound_format_key_prefix), ""));
    }

}
