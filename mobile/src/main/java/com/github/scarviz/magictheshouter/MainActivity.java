package com.github.scarviz.magictheshouter;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE = 0;

    private static final String URL_READY = "http://magictheshouter.appspot.com/ready";
    private static final String URL_CANCEL = "http://magictheshouter.appspot.com/cancel";
    private static final String URL_REGIST = "http://magictheshouter.appspot.com/regist";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((Button) findViewById(R.id.btnShout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMagicReady();

                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.shout));

                try {
                    startActivityForResult(intent, REQUEST_CODE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    cancelMagic();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String resultsString = "";

                // 結果文字列リスト
                ArrayList<String> results = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);

                for (int i = 0; i < results.size(); i++) {
                    // ここでは、文字列が複数あった場合に結合しています
                    resultsString += results.get(i);
                }

                requestPostVoiceData(resultsString);
                // トーストを使って結果を表示
                Toast.makeText(this, resultsString, Toast.LENGTH_LONG).show();
            } else {
                cancelMagic();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 魔法準備
     */
    private void setMagicReady() {
        requestSetReady(URL_READY);
    }

    /**
     * 魔法キャンセル
     */
    private void cancelMagic() {
        requestSetReady(URL_CANCEL);
    }

    /**
     * 魔法準備状態の設定リクエスト
     */
    private void requestSetReady(final String url) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();

                OkHttpClient client = new OkHttpClient();

                String result = null;
                try {
                    Response response = client.newCall(request).execute();
                    result = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                Log.d(TAG, result);
            }
        }.execute();
    }

    /**
     * 音声データのPost
     */
    private void requestPostVoiceData(final String voiceData) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                RequestBody requestBody = RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8")
                        , "{\"data\":\""+ voiceData +"\"}"
                );
                Request request = new Request.Builder()
                        .url(URL_REGIST)
                        .post(requestBody)
                        .build();

                OkHttpClient client = new OkHttpClient();

                String result = null;
                try {
                    Response response = client.newCall(request).execute();
                    result = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                Log.d(TAG, result);
            }
        }.execute();
    }
}
