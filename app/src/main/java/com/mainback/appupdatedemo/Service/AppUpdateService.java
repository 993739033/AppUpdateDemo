package com.mainback.appupdatedemo.Service;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.mainback.appupdatedemo.BuildConfig;
import com.mainback.appupdatedemo.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Loki_Zhou
 * @Date 2021/3/30
 **/
public class AppUpdateService extends IntentService {
    private static final String ACTION_UPDATE = BuildConfig.APPLICATION_ID + ".Service.action.update";
    private static final String ACTION_STOP_UPDATE = BuildConfig.APPLICATION_ID + ".Service.action.stopupdate";
    private static final String EXTRA_URL = BuildConfig.APPLICATION_ID + ".Service.extra.url";
    private static final String EXTRA_FILE_NAME = BuildConfig.APPLICATION_ID + ".Service.extra.filename";
    private boolean isRunning = false;
    private NotificationManager updateNotificationManager;
    private Notification updateNotification;
    private PendingIntent updatePendingIntent;
    private static AppUpdateService.OnProgressListener mProgressListener;
    private static final int NOTIFICATION_ID = 1001;
    private static final int NOTIFICATION_ID_2 = 1002;
    private AppUpdateService.DataReceiver dataReceiver = null;
    private static final String tag = "AppUpdateService";

    //存储NOTIFICATION 一些设置常量
    String CHANNEL_ID = "channel_01";
    String CHANNEL_NAME = "channel_01_name";
    String CHANNEL_DESCRIPTION = "channel_01_description";

    public AppUpdateService() {
        super("AppUpdateService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_UPDATE.equals(action)) {
                String param1 = intent.getStringExtra(EXTRA_URL);
                String param2 = intent.getStringExtra(EXTRA_FILE_NAME);
                this.startDownloade(param1, param2);
            }
        }
    }

    @Override
    public void onDestroy() {
        if (mProgressListener != null) {
            mProgressListener.onSuccess(false);
            mProgressListener = null;
        }

        this.updateNotificationManager.cancel(1001);
        if (this.dataReceiver != null) {
            this.unregisterReceiver(this.dataReceiver);
            this.dataReceiver = null;
        }
        super.onDestroy();
    }

    //param1 url param2 filepath
    public static void startUpdate(Context context, String param1, String param2, AppUpdateService.OnProgressListener pregressListener) {
        mProgressListener = pregressListener;
        Intent intent = new Intent(context, AppUpdateService.class);
        intent.setAction(ACTION_UPDATE);
        intent.putExtra(EXTRA_URL, param1);
        intent.putExtra(EXTRA_FILE_NAME, param2);
        context.startService(intent);
    }

    public static void stopUpdate(Context context) {
        Intent intent = new Intent(context, AppUpdateService.class);
        context.stopService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.dataReceiver = new AppUpdateService.DataReceiver();
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction(ACTION_STOP_UPDATE);
        this.registerReceiver(this.dataReceiver, intentfilter);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void startDownloade(String url, String fileName) {
        Log.d(tag, "开始升级----" + url + "---" + fileName);
        if (isRunning) {
            return;
        }
        isRunning = true;
        initRemoteView();
        try {
            boolean isSuccess = downloadUpdateFile(url, fileName);
            if (mProgressListener != null) {
                mProgressListener.onSuccess(isSuccess);
            }
            if (isSuccess) {
                Notification notification;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    notification = new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                            .setTicker("更新下载成功")
                            .setContentTitle(getString(R.string.app_name))
                            .setContentText("下载成功")
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .build();
                } else {
                    //下载成功提示
                    notification =
                            new Notification.Builder(this)
                                    .setTicker("更新下载成功")
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setContentTitle(getString(R.string.app_name))
                                    .setContentText("下载成功")
                                    .build();
                }
                updateNotificationManager.notify(NOTIFICATION_ID_2, notification);
            } else {
                Notification notification;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    notificationSetting();
                    notification = new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                            .setTicker("更新下载失败")
                            .setContentTitle(getString(R.string.app_name))
                            .setContentText("下载失败")
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .build();
                } else {
                    //下载失败提示
                    //下载成功提示
                    notification = new Notification.Builder(this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setTicker("更新下载失败")
                            .setContentTitle(getString(R.string.app_name))
                            .setContentText("下载失败")
                            .build();
                }
                updateNotificationManager.notify(NOTIFICATION_ID_2, notification);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void installApk(Context context, String fileName) {
        Intent installIntent = new Intent("android.intent.action.VIEW");
        Uri apkUri;
        if (Build.VERSION.SDK_INT >= 24) {
            apkUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", new File(fileName));
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            apkUri = Uri.fromFile(new File(fileName));
            installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        }

        context.startActivity(installIntent);
    }

    //当安卓版本号8.0时需要设置 notification channel
    @TargetApi(Build.VERSION_CODES.O)
    private void notificationSetting() {
        NotificationManager mNotificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        CharSequence name = CHANNEL_NAME;
        String description = CHANNEL_DESCRIPTION;
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID,
                name, importance);
        mChannel.setDescription(description);
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.RED);
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{
                100, 200, 300, 400, 500, 400, 300, 200, 400
        });
        mNotificationManager.createNotificationChannel(mChannel);
    }

    /**
     * 初始化状态栏进度条
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void initRemoteView() {
        try {
            updateNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            //状态栏提醒内容
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationSetting();
                updateNotification = new Notification.Builder(this, CHANNEL_ID)
                        .setTicker("开始版本更新下载..")
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText("更新下载中")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .build();
            } else {
                updateNotification = new Notification.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setTicker("开始版本更新下载..")
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText("更新下载中")
                        .build();
            }
            //  updateNotification.defaults = Notification.DEFAULT_SOUND;//设置默认声音
            //状态栏提醒内容
            RemoteViews view = new RemoteViews(getApplication().getPackageName(), R.layout.layout_update_app);
            updateNotification.contentView = view;
            updateNotification.contentView.setProgressBar(R.id.progressBar1, 100, 0, false);
            updateNotification.contentView.setTextViewText(R.id.textView1, "0%");

            Intent intent1 = new Intent(ACTION_STOP_UPDATE);
            PendingIntent intent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
            updateNotification.contentView.setOnClickPendingIntent(R.id.tv_cancel, intent);
            // 发出通知
            updateNotificationManager.notify(NOTIFICATION_ID, updateNotification);
            //startForeground(NOTIFICATION_ID, updateNotification);//开启前台服务 更新也调用此方法 id值相同不会重复创建notification
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 下载文件
     * <p>
     * downloadUrl 文件下载地址
     * filepath  文件保存地址
     */
    private boolean downloadUpdateFile(String downloadUrl, String filepath) {
        try {
            int downloadCount = 0;
            int currentSize = 0;
            long totalSize = 0;
            int updateTotalSize = 0;
            boolean result = false;
            HttpURLConnection httpConnection = null;
            InputStream is = null;
            FileOutputStream fos = null;
            File temp = new File(filepath + ".tmp");
            if (temp.getParentFile().isDirectory()) {
                temp.getParentFile().mkdirs();
            }
            //删除原有apk
            File apk = new File(filepath);
            if (apk.exists()) {
                apk.delete();
            }
            try {
                URL url = new URL(downloadUrl);
                httpConnection = (HttpURLConnection) url.openConnection();
                httpConnection.setRequestProperty("User-Agent", "PacificHttpClient");
                if (currentSize > 0) {
                    httpConnection.setRequestProperty("RANGE", "bytes=" + currentSize + "-");//断点续传
                }
                httpConnection.setConnectTimeout(20000);
                httpConnection.setReadTimeout(120000);
                updateTotalSize = httpConnection.getContentLength();
                if (httpConnection.getResponseCode() == 404) {
                    throw new Exception("UpdateApkFail");
                }
                is = httpConnection.getInputStream();
                fos = new FileOutputStream(temp, false);
                //4m
                byte buffer[] = new byte[4096];
                int readsize = 0;
                while ((readsize = is.read(buffer)) > 0 && mProgressListener != null) {
                    fos.write(buffer, 0, readsize);
                    totalSize += readsize;
                    // 为了防止频繁的通知导致应用吃紧，百分比增加1才通知一次
                    if ((downloadCount == 0) || (int) (totalSize * 100 / updateTotalSize) - 1 > downloadCount) {
                        downloadCount += 1;
                        try {
                            updateNotification.contentView.setProgressBar(R.id.progressBar1, 100, ((int) (totalSize * 100 / updateTotalSize)), false);
                            updateNotification.contentView.setTextViewText(R.id.textView1, ((int) (totalSize * 100 / updateTotalSize)) + "%");
                            Intent intent1 = new Intent(ACTION_STOP_UPDATE);
                            PendingIntent intent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
                            updateNotification.contentView.setOnClickPendingIntent(R.id.tv_cancel, intent);
                            updateNotificationManager.notify(NOTIFICATION_ID, updateNotification);
                            if (mProgressListener != null) {
                                mProgressListener.onProgress(((int) (totalSize * 100 / updateTotalSize)));
                                Log.d(tag, "下载进度:" + ((int) (totalSize * 100 / updateTotalSize)));
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                temp.renameTo(new File(filepath));
                temp.delete();
            } finally {
                if (httpConnection != null) {
                    httpConnection.disconnect();
                }
                if (is != null) {
                    is.close();
                }
                if (fos != null) {
                    fos.close();
                }
                result = updateTotalSize > 0 && updateTotalSize == totalSize;
                if (!result) {
                    //下载失败或者为下载完成
                    new File(filepath).delete();
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    private class DataReceiver extends BroadcastReceiver {
        private DataReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_STOP_UPDATE.equals(action)) {
                AppUpdateService.this.stopService(new Intent(context, AppUpdateService.class));
            }
        }
    }

    public interface OnProgressListener {
        void onProgress(int var1);

        void onSuccess(boolean var1);
    }
}
