package com.mainback.appupdatedemo.Service;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.mainback.appupdatedemo.R;


/**
 * 作者：gjt66
 * 时间：2018/4/4 10:48
 * 介绍：
 */
public class DialogFactory {
    //创建更新弹窗
    public static Dialog createUpdateDialog(Context context) {
        Dialog dialog = new Dialog(context, R.style.MyDialogStyle);
        dialog.setContentView(R.layout.dialog_update);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    //创建两种状态回调的选择弹窗
    public static Dialog createChooseButtonDialog(Context context, String content,
                                                  final View.OnClickListener enterListener,
                                                  View.OnClickListener cancelListener) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context,
                AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        TextView title = new TextView(context);
        title.setText(content);
        title.setPadding(10, 15, 10, 15);
        title.setGravity(Gravity.CENTER);
        title.setTextSize(20);
        builder.setCustomTitle(title);
        builder.setNegativeButton("取消", null);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                enterListener.onClick(null);
            }
        });
        AlertDialog mAlertDialog = builder.create();
        mAlertDialog.show();
        return mAlertDialog;
    }
}
