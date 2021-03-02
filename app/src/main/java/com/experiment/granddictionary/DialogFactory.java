package com.experiment.granddictionary;

import androidx.appcompat.app.AlertDialog;;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DialogFactory {
    private static class DialogFactoryHolder {
        public static DialogFactory instance = new DialogFactory();
    }

    private DialogFactory() {}
    public static DialogFactory getInstance() {
        return DialogFactoryHolder.instance;
    }

    public ProgressDialog createDownloadProgressDialog(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMax(1000);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("下载词典");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);

        return progressDialog;
    }

    public AlertDialog createConfirmDeleteAlertDialog(Context context, String word,
                                                DialogInterface.OnClickListener confirmListener) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        AlertDialog alertDialog = dialogBuilder
                .setIcon(R.mipmap.ic_launcher_round)
                .setTitle("删除单词")
                .setMessage("是否删除单词"+word+"?")
                .setNegativeButton("取消",null)
                .setPositiveButton("确定",confirmListener)
                .create();

        return alertDialog;
    }

    public LinearLayout createAddOrModifyWordAlertDialog(Context context, LinearLayout contentView,
                                 String title, DialogInterface.OnClickListener confirmListener) {
        AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setIcon(R.mipmap.ic_launcher)
                .setTitle(title)
                .setView(contentView)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", confirmListener)
                .create()
                .show();
        return contentView;
    }

    public LinearLayout createSearchWordAlertDialog(Context context, LinearLayout contentView,
                                                DialogInterface.OnClickListener confirmListener) {
        AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setIcon(R.mipmap.ic_launcher)
                .setTitle("查询单词")
                .setView(contentView)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", confirmListener)
                .create()
                .show();
        return contentView;
    }
}
