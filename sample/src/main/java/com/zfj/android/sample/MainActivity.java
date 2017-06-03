package com.zfj.android.sample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    OkHttpClient okHttpClient = new OkHttpClient();
    private TextView tv;
    private ImageView im;
    private String mBaseUrl = "http://219.218.147.137:8080/Okhttp/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.tv_result);
        im = (ImageView) findViewById(R.id.im_result);
    }

    /**
     * 1.拿到okHttpClient对象
     * 2.构造Request
     * 3.将Request封装为Call
     * 4.执行Call
     */
    public void doGet(View view) throws IOException {
        //构造OkHttpClient
        /*OkHttpClient okHttpClient = new OkHttpClient();*/
        //构造Request
        Request request = new Request.Builder()
                .get()
                .url(mBaseUrl + "login?username=get&password=12223")
                .build();
        //执行Call
        executeRequest(request);
    }

    /**
     * Post与Get不同，构造Request的同时，也要构造RequestBody
     */
    public void doPost(View view) {

        FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
        RequestBody requestBody = formEncodingBuilder
                .add("username", "zfj")
                .add("password", "123")
                .build();
        Request request = new Request.Builder()
                .url(mBaseUrl + "login")
                .post(requestBody)
                .build();

        executeRequest(request);
    }

    public void doPostString(View view) {
        // RequestBody.create(MediaType.parse("text/plain"));
        RequestBody requestBody = RequestBody
                .create(MediaType.parse("text/plain;chaset=utf-8"), "{username:zfj,password=123}");
        Request request = new Request.Builder()
                .url(mBaseUrl + "postString")
                .post(requestBody)
                .build();
        executeRequest(request);

    }

    public void doPostFile(View view) {
        //得到外存图片文件
        File file = new File(Environment.getExternalStorageDirectory(), "shaozuo.jpg");
        if (!file.exists()) {
            L.e(file.getAbsolutePath() + "not exist! ");
            return;
        }
        L.e("find pic");
        //构造RequstBody
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"),
                file);
        //构造Request
        Request request = new Request.Builder()
                .url(mBaseUrl + "postFile")
                .post(requestBody)
                .build();
        executeRequest(request);
    }

    /**
     * 将外存图片上传到服务器端
     */
    public void doUpload(View view) {
        File file = new File(Environment.getExternalStorageDirectory(), "shaozuo.jpg");
        if (!file.exists()) {
            L.e(file.getAbsolutePath() + "not exist! ");
            return;
        }
        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("username", "zfj")
                .addFormDataPart("password", "123")
                .addFormDataPart("mPhoto", "zfj.jpg",
                        RequestBody.create(MediaType.parse("image/jpeg"), file))
                .build();
        //CountingRequstBody为RequestBody子类
        CountingRequestBody countingRequestBody = new CountingRequestBody(requestBody,
                new CountingRequestBody.Listener() {
                    @Override
                    public void onRequestProgress(final long byteWrited, final long contentLength) {
                        L.e(byteWrited + "/" + contentLength);
                    }
                });
        Request request = new Request.Builder()
                .url(mBaseUrl + "uploadInfo")
                .post(countingRequestBody)
                .build();
        executeRequest(request);

    }
    /**
     * 将服务器图片下载到外存
     */
    public void doDownload(View view) {
        Request request = new Request.Builder()
                .url(mBaseUrl + "files/shaozuo.jpg")
                .build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {

            @Override
            public void onFailure(Request request, IOException e) {
                L.e("onFailure:" + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                L.e("onResponse");
                downloadPicToStorage(response);
                L.e("Download Success");
            }
        });
    }

    /**
     * 将服务器图片加载到ImageView中显示
     */
    public void doDownloadImg(View view) {
        Request request = new Request.Builder()
                .url(mBaseUrl + "files/shaozuo.jpg")
                .build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                L.e("onFailure:" + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                L.e("onResponse");
                InputStream is = response.body().byteStream();

                final Bitmap bitmap = BitmapFactory.decodeStream(is);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        im.setImageBitmap(bitmap);
                    }
                });
                //应该对图像压缩
                L.e("Download Success");
            }


        });
    }

    private void downloadPicToStorage(Response response) throws IOException {
        final Long total = response.body().contentLength();
        long sum = 0L;

        InputStream is = response.body().byteStream();
        File file = new File(Environment.getExternalStorageDirectory(), "shaozuo11.jpg");
        FileOutputStream fos = new FileOutputStream(file);
        int len = 0;
        byte[] buf = new byte[128];
        while ((len = is.read(buf)) != -1) {
            fos.write(buf, 0, len);
            sum += len;
            L.e(sum + "/" + total);
            final long finalSum = sum;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv.setText(finalSum + "/" + total);
                }
            });
        }
        fos.flush();
        fos.close();
    }

    private void executeRequest(Request request) {
        Call call = okHttpClient.newCall(request);
        //Response execute = call.execute();
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                L.e("onFailure:" + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                L.e("onResponse");
                final String res = response.body().string();
                L.e(res);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv.setText(res);
                    }
                });
            }

        });
    }
}
