package com.mainback.appupdatedemo.Service;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static android.os.Environment.MEDIA_MOUNTED;

/**
 * Created by wyw on 2016/11/3.
 */

public class FileUtil {
    private static FileUtil instance;
    private static final String DIR_CACHE = "cache";
    private static final String DIR_DATA = "data";
    private static final String DIR_PATCH = "patch";
    private static final String DIR_BUG = "bug";
    private static final String DIR_DOWNLOAD = "download";
    private static final String DIR_APK = "apk";
    private static final String DIR_EXCEL = "excel";
    private static final String ROOT_NAME = "导出文件";
    private static final String DIR_WORD = "word";
    private static final String FILE_BUG_POSTFIX = ".bug";

    private static int Bug_Cache_Max_Size = 50;//包名/files/bug/  文件夹下最多缓存的文件数

    //文件名字
    public static final String APK_NAME = "APP更新包.apk";
    private Context mContext;

    public FileUtil(Context mContext) {
        this.mContext = mContext;
    }

    public synchronized static FileUtil getInstance() {
        if (instance == null) {
            instance = new FileUtil(MyApplication.getContext());
        }
        return instance;
    }


    //自动生成下载文件夹
    public File getDownLoadDir() {
        return mContext.getExternalFilesDir(DIR_DOWNLOAD);
    }

    public String getDownLoadDirPath() {
        return getDownLoadDir().getAbsolutePath();
    }

    public File getBugDir() {
        return mContext.getExternalFilesDir(DIR_BUG);
    }

    public String getBugDirPath() {
        return getBugDir().getAbsolutePath();
    }


    public File getPatchDir() {
        return mContext.getExternalFilesDir(DIR_PATCH);
    }

    public String getPatchDirPath() {
        return getBugDir().getAbsolutePath();
    }


    //获取图片保存的文件夹 Android/data/com.bbny.hgwlw/files/download
    public String getPicDownLoadDir() {
        String path = getDownLoadDirPath();
        int index = Environment.getExternalStorageDirectory().getAbsolutePath().length();
        String dirPath = path.substring(index + 1, path.length());
        return dirPath;
    }

    //判断是否为图片
    public static boolean isImg(String path) {
        String lowStr = path.toLowerCase();
        if (lowStr.endsWith(".jpg") || lowStr.endsWith(".png") || lowStr.endsWith(".jpeg") || lowStr.endsWith(".gif") || lowStr.endsWith(".bmp")) {
            return true;
        }
        return false;
    }


    public File getCacheDir() {
        return mContext.getExternalFilesDir(DIR_CACHE);
    }

    public String getCacheDirPath() {
        return getCacheDir().getAbsolutePath();
    }

    public File getDataDir() {
        return mContext.getExternalFilesDir(DIR_DATA);
    }

    public String getDataDirPath() {
        return getDataDir().getAbsolutePath();
    }


    public static void deleteCacheFile(File dir) {
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                deleteCacheFile(file);
            }
        } else {
            dir.delete();
        }
    }

    /**
     * apk文件位置
     *
     * @return
     */
    public File getApkFile() {
        return new File(Environment.getExternalStorageDirectory(), APK_NAME);
    }

    //获取更新apk文件保存路径
    public String getUpdateApkPath(Context context) {
        String apkPath = "";
        apkPath = getFilePath(context, DIR_APK) + "/" + APK_NAME;
        return apkPath;
    }

    //获取excel文档保存路径
    public String getExcelSavePath(Context context, String excelName) {
        String excelPath = "";
        excelPath = getRootPath(context, ROOT_NAME + File.separator + DIR_EXCEL) + "/" + excelName;
        checkEmptyAndNewDir(getRootPath(context, ROOT_NAME));
        checkEmptyAndNewDir(getRootPath(context, ROOT_NAME + File.separator + DIR_EXCEL));
        checkEmptyAndNewDir(excelPath);
        return excelPath;
    }

    //检查是否为空  为空则创建文件夹  不为空则是否为文件夹 不为文件夹则删除重新创建
    public static void checkEmptyAndNewDir(String filePath) {
        if (TextUtils.isEmpty(filePath)) return;
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdir();
        } else {
            if (!file.isDirectory()) {
                file.delete();
                file.mkdir();
            }
        }
    }

    //获取Word文档保存路径
    public String getWordSavePath(Context context, String wordName) {
        String wordPath = "";
        wordPath = getRootPath(context, ROOT_NAME + File.separator + DIR_WORD) + "/" + wordName;
        checkEmptyAndNewDir(getRootPath(context, ROOT_NAME));
        checkEmptyAndNewDir(getRootPath(context, ROOT_NAME + File.separator + DIR_WORD));
        checkEmptyAndNewDir(wordPath);
        return wordPath;
    }

    //获取文档保存根路径路径
    public String getDocumentSavePath(Context context) {
        String rootPath = "";
        rootPath = getRootPath(context, ROOT_NAME);
        return rootPath;
    }

    /**
     * 获取海康威视log日志保存文件夹
     *
     * @return
     */
    public File getHKLogsDir(Context context) {
        if (context.getExternalFilesDir("sdklog") == null) {
            return new File(context.getFilesDir().getAbsolutePath());//使用内部存储
        }
        return new File(context.getExternalFilesDir("sdklog").getAbsolutePath());
    }


    //获取外部app包的文件夹路径 没有外部存储是使用内部存储  /data 内部存储 /storage 外部存储（4.4以上）
    //dir 文件夹名称
    public static String getFilePath(Context context, String dir) {
        String directoryPath = "";
        if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {//判断外部存储是否可用
            // 获取外部存储
            directoryPath = context.getExternalFilesDir(dir).getAbsolutePath() ;
        } else {//没外部存储就使用内部存储
            // 获取内部存储
            directoryPath = context.getFilesDir() + File.separator + dir;
        }
        File file = new File(directoryPath);
        if (!file.exists()) {//判断文件目录是否存在
            file.mkdirs();
        }
        return directoryPath;
    }

    //获取根目录下的文件夹位置
    public static String getRootPath(Context context, String dir) {
        String directoryPath = "";
        if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {//判断外部存储是否可用
            // 获取外部存储
            directoryPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + dir;
        } else {//没外部存储就使用内部存储
            // 获取内部存储
            directoryPath = context.getFilesDir() + File.separator + dir;
        }
        File file = new File(directoryPath);
        if (!file.exists()) {//判断文件目录是否存在
            file.mkdirs();
        }
        return directoryPath;
    }

    /**
     * 拷贝到apk安装路径的数据库目录下
     *
     * @return 拷贝是否成功
     */
    public boolean copyToInterPath(String srcFile, String destFile) {
        boolean b = false;
        //判断文件夹是否存在
        String dirPath = destFile.substring(0, destFile.lastIndexOf(File.separator) + 1);
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File dest = new File(destFile);
        if (!(b = isFileExists(dest, true))) {
            InputStream is = null;
            OutputStream os = null;
            try {
                is = new FileInputStream(new File(srcFile));
                os = new FileOutputStream(dest);
                int length;
                byte[] buf = new byte[1024 * 2];
                while ((length = is.read(buf)) != -1) {
                    os.write(buf, 0, length);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (os != null) {
                        os.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return !b;
    }

    private static boolean isFileExists(File f, boolean refresh) {
        boolean b = false;
        if (f.exists()) {
            if (refresh) {
                f.delete();
                b = false;
            } else {
                b = true;
            }
        }
        return b;
    }

    public static void clearBugFilesWhileFull() {
        File file = new File(FileUtil.getInstance().getBugDirPath());
        if (file.listFiles().length > Bug_Cache_Max_Size) {
            try {
                clearBugCacheFiles(file);
            } catch (Exception e) {
                Log.d("FileUtil", "删除数据失败");
                e.printStackTrace();
            }
        }
    }

    //清楚所有bug的缓存文件
    private static void clearBugCacheFiles(File files) {
        for (File file : files.listFiles()) {
            file.delete();
        }
    }


    //判断该文件是否存在下载文件夹中
    public boolean isInDownLoadDir(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return false;
        }
        String path = getDownLoadDirPath() + File.separator + fileName;
        File file = new File(path);
        if (file.exists()) {
            return true;
        }
        return false;
    }

    //获取文件的下载路径
    public String getDownLoadFilePath(String fileName) {
        return getDownLoadDirPath() + File.separator + fileName;
    }

}
