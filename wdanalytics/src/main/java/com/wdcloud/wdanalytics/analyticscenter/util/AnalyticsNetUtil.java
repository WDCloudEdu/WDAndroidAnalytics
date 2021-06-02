package com.wdcloud.wdanalytics.analyticscenter.util;

import android.os.Handler;
import android.os.Looper;
import android.telecom.Call;
import android.util.Log;


import com.google.gson.Gson;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Info:统计上传网络框架
 * Created by Yanxin.
 * CreateTime: 2019/11/29 18:20
 */
public class AnalyticsNetUtil {
    private static String BSSE_ANALYTIC="http://xztjfx.xzeduc.cn/client-capture/";
    private static String url;
    //    private static String BSSE_ANALYTIC="http://192.168.5.66/";
    /**
     * post  以json数据格式为请求参数
     *
     * @param path
     * @param paramObj
     * @param netCallBack
     */
    public static void postJson(final String path, final Object paramObj, final AnalyticNetCallBack netCallBack) {
        Log.e("伟东统计开始上传" ,"=======================================");
        com.wdcloud.wdanalytics.util.AnalyticsSharedPreference instance = com.wdcloud.wdanalytics.util.AnalyticsSharedPreference.getInstance();
        String accessKey = (String) instance.getData("access-key", "602e1a7278e9d960063a9e097c580b11");
        String baseUrl = (String)instance.getData("BaseUrl", "");
        String paramJson = new Gson().toJson(paramObj);
        if(!baseUrl.equals(""))
        {
            url = path.startsWith("http") ? path : baseUrl + path;
        }
        else
        {
            url = path.startsWith("http") ? path : BSSE_ANALYTIC + path;
        }
        Request request = new Request.Builder()
                .url(url)
                .addHeader("access-key",accessKey)
                .addHeader("Content-Type", "application/json-patch+json")
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), paramJson))
                .build();
        OkHttpClient build = new OkHttpClient.Builder()
                .followRedirects(false)  // 关闭重定向
                .followSslRedirects(false)
                .build();
        okhttp3.Call call = build.newCall(request);
        execute(call, netCallBack);
    }

    private static void execute(final okhttp3.Call call, final AnalyticNetCallBack netCallback) {
        Log.e("伟东统计===", "上传请求 Url =" + call.request().url().toString());
        Handler handler = new Handler(Looper.getMainLooper());
        final Callback callback = new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        String errorMessage = "";
                        if (e instanceof UnknownHostException) {
                            errorMessage = "网络连接异常";
                        } else if (e instanceof SocketTimeoutException) {
                            errorMessage = "网络超时";
                        } else {
                            errorMessage = "网络异常";
                        }
                        if (netCallback != null) netCallback.failed(null, errorMessage);

                    }
                });
                e.printStackTrace();
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                try {
                    if (call.isCanceled()) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (netCallback != null) netCallback.failed(response, "Canceled!");
                            }
                        });
                        return;
                    }
                    if (!response.isSuccessful()) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (netCallback != null)
                                    netCallback.failed(response, response.toString());
                            }
                        });
                    }
                    final String data = response.body().string();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            netCallback.successed(response, data);
                        }
                    });

                } catch (final Exception e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (netCallback != null) netCallback.failed(null, e.getMessage());
                        }
                    });
                    e.printStackTrace();
                } finally {
                    if (response.body() != null)
                        response.body().close();
                }
            }
        };
        call.enqueue(callback);
    }

    /**
     * 网络回调中
     */
    public interface AnalyticNetCallBack {
        void failed(Response response, String errorMsg);

        void successed(Response response, String data);
    }
}
