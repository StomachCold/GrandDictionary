package com.experiment.granddictionary;

import android.content.Context;
import android.widget.Toast;

public class GlobalUtil {
    public static final String TABLE_NAME = "Dictionary";
    public static final int DOWNLOAD_SUCCESS = 0;
    public static final int DOWNLOAD_FAIL = 1;
    public static final int PROGRESS_UPDATE = 2;
    public static final int PROGRESS_MAX = 3;
    public static final int REFRESH_LIST = 4;
    public static final String DEFAULT_TEXT_COLOR = "#8a000000";
    private static Toast toast;
    public static void showToast(Context context, String content) {
        if (toast == null) {
            toast = Toast.makeText(context, content, Toast.LENGTH_SHORT);
        }
        else {
            toast.setText(content);
        }
        toast.show();
    }
    public static void showLongToast(Context context, String content) {
        if (toast == null) {
            toast = Toast.makeText(context, content, Toast.LENGTH_LONG);
        }
        else {
            toast.setText(content);
        }
        toast.show();
    }
}
