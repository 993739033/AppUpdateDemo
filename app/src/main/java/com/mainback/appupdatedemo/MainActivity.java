package com.mainback.appupdatedemo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mainback.appupdatedemo.Service.AppUpdateService;
import com.mainback.appupdatedemo.Service.DialogFactory;
import com.mainback.appupdatedemo.Service.FileUtil;

public class MainActivity extends AppCompatActivity {
    Dialog updateDialog;
    String url = "http://1.down.vr2028.cn:8021/android-soft2/2017/s4/yiwan.com_nova.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onBtnClick(View view) {
        updateDialog = DialogFactory.createUpdateDialog(MainActivity.this);
        updateDialog.findViewById(R.id.tv_in_backstage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateDialog.hide();
            }
        });
        updateDialog.findViewById(R.id.tv_cancel_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateDialog.hide();
            }
        });

        updateDialog.show();
        startDownload();
    }

    private void startDownload() {
        //app安装权限 8.0后所需
        AppUpdateService.startUpdate(this, url,
                FileUtil.getInstance().getUpdateApkPath(this),
                new AppUpdateService.OnProgressListener() {
                    @Override
                    public void onProgress(final int progress) {
                        if (updateDialog != null && updateDialog.isShowing()) {
                            ProgressBar pb_update = updateDialog.findViewById(R.id.pb_update);
                            final TextView tv_update = updateDialog.findViewById(R.id.tv_update);
                            pb_update.setProgress(progress);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv_update.setText(progress + "％");
                                }
                            });
                        }
                    }

                    @Override
                    public void onSuccess(boolean isSuccess) {
                        if (!isSuccess) return;
                        checkAndroidO();
                        updateDialog.hide();
                    }
                });

        if (updateDialog != null && updateDialog.isShowing()) {
            TextView tv_cancel = updateDialog.findViewById(R.id.tv_cancel_update);
            tv_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AppUpdateService.stopUpdate(MainActivity.this);
                    updateDialog.dismiss();
                }
            });
        }
    }


    //检查8.0版本是否开启安装未知应用权限
    public void checkAndroidO() {
        if (Build.VERSION.SDK_INT >= 26) {
            boolean b = getPackageManager().canRequestPackageInstalls();
            if (!b) {
                //请求安装未知应用来源的权限
                DialogFactory.createChooseButtonDialog(this, "无法自动安装更新!\n请设置本app【未知应用安装】权限\n为【允许】", new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                        MainActivity.this.startActivityForResult(intent, 113);
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "无法自动安装请到[" + FileUtil.getInstance().getUpdateApkPath(MainActivity.this) + "]路径下进行手动安装", Toast.LENGTH_SHORT);
                    }
                });
            } else {
                AppUpdateService.installApk(getApplicationContext(), FileUtil.getInstance().getUpdateApkPath(MainActivity.this));
            }
        } else {
            AppUpdateService.installApk(getApplicationContext(), FileUtil.getInstance().getUpdateApkPath(MainActivity.this));
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 113:
                if (resultCode == Activity.RESULT_OK) {
                    AppUpdateService.installApk(getApplicationContext(), FileUtil.getInstance().getUpdateApkPath(MainActivity.this));
                } else {
                    Toast.makeText(MainActivity.this, "无法自动安装请到[" + FileUtil.getInstance().getUpdateApkPath(MainActivity.this) + "]路径下进行手动安装", Toast.LENGTH_SHORT);
                }
                break;
        }

    }
}
