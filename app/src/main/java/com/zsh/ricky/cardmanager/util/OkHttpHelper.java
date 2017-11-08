package com.zsh.ricky.cardmanager.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
/**
 * Created by Ricky on 2017/10/16.
 */

public class OkHttpHelper {
    private OkHttpClient client;
    private Call call;
    private final String BASE_URL = "http://192.168.1.102:8081/CGService/";
    private final String BASE_IMAGE_URL = "http://192.168.1.102:8080/image/";
    private static final String BITMAP_SAVE_FOLDER="/cgame/image/";
    public static final String BITMAP_SAVE_PATH = "sdcard"
            + BITMAP_SAVE_FOLDER;
    /**
     * send get request
     * @param url 地址
     * @return the call object
     */
    public Call getRequest(final String url){

        client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(BASE_URL + url)
                .build();

        this.call = client.newCall(request);

        return this.call;
    }

    /**
     * send post request to service
     * @param url 地址
     * @param rawsParam 参数列表
     * @return the ok call object
     */
    public Call postRequest(final String url, final Map<String, String> rawsParam)
    {
        client = new OkHttpClient();

        FormBody.Builder builder = new FormBody.Builder();

        for (String key : rawsParam.keySet())
        {
            builder.add(key, rawsParam.get(key));
        }

        RequestBody requestBody = builder.build();

        Request requestPost = new Request.Builder()
                .url(BASE_URL + url)
                .post(requestBody)
                .build();

        this.call = client.newCall(requestPost);

        return this.call;
    }

    /**
     * 同步的向服务器发送请求，获取回复的字符串
     * @param url 服务器url
     * @return 服务器回复的字符串
     */
    public String get(final String url) {
        Call call = getRequest(url);
        String result = null;

        try {
            Response response = call.execute();

            result = response.body().string();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public Bitmap getPic(final String url) {
        //获取okHttp对象get请求
        try {
            OkHttpClient client = new OkHttpClient();
            //获取请求对象
            Request request = new Request.Builder().url(BASE_IMAGE_URL + url).build();
            //获取响应体
            ResponseBody body = client.newCall(request).execute().body();
            //获取流
            InputStream in = body.byteStream();
            //转化为bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(in);

            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void onSaveBitmap(final Bitmap mBitmap, final Context context,String fileName) {
        // 第一步：首先保存图片
        //将Bitmap保存图片到指定的路径"/sdcard/"+"BITMAP_SAVE_DIR"+"/"下
        File appDir = new File(Environment.getExternalStorageDirectory(), BITMAP_SAVE_FOLDER);

        if (!appDir.exists()) {
            appDir.mkdirs();
        }

        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 第二步：其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // 第三步：最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file)));
        //context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
    }

    public Call imageUpLoad(final String url,String localPath) {
        MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
        OkHttpClient client = new OkHttpClient();


        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        File f = new File(localPath);
        builder.addFormDataPart("file", f.getName(), RequestBody.create(MEDIA_TYPE_PNG, f));

        final MultipartBody requestBody = builder.build();
        //构建请求
        final Request request = new Request.Builder()
                .url(BASE_URL + url)
                .post(requestBody)
                .build();

        this.call = client.newCall(request);
        return this.call;
    }
}
