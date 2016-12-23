package com.ooolab.whatiswhat;


import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.SynthesizerTool;
import com.baidu.tts.client.TtsMode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity implements
        MainFragment.OnInputBtnClickListner,
        VoiceInputFragment.OnRecogResultListener,
        SpeechSynthesizerListener {

    private static String TAG = "MainActivity";
    private VoiceInputFragment mVoiceInputFragment;
    private EventManager mWakeupManager;
    private WebView mWebView;
    private TextView mInfoText;
    private RequestQueue mVolleyQueue;
    private SpeechSynthesizer mSpeechSynthesizer;

    private static final int MEDIA_FREE = 0;
    private static final int MEDIA_ASRING = 1;
    private static final int MEDIA_TTSING = 2;
    private static final int MEDIA_WATING_WAKEUP = 3;

    private int mMediaStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMediaStatus = MEDIA_FREE;
        mWebView = (WebView) findViewById(R.id.webview);
        mInfoText = (TextView) findViewById(R.id.info_text);

        // 设置 WebView
        WebSettings settings = mWebView.getSettings();
        // 设置启用 JS
        settings.setJavaScriptEnabled(true);
        // 设置缓存模式
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAppCachePath(mWebView.getContext().getCacheDir().getAbsolutePath());
        mWebView.canGoBack();
        mWebView.setWebViewClient(new WebViewClientWrapper());

        // 设置 INFO 块自动语音播报
        mInfoText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getSpeechSynthesizer().speak(s.subSequence(start, start + count).toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        // 默认启动识别
        findViewById(R.id.input_btn).callOnClick();
    }

    @Override
    protected void onPause() {
        // 停止视频播放
        mWebView.onPause();
        // 停止播报
        if (mSpeechSynthesizer != null) {
            mSpeechSynthesizer.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 默认启动识别
        // findViewById(R.id.input_btn).callOnClick();
    }

    @Override
    protected void onStop() {
        if (mSpeechSynthesizer != null) {
            mSpeechSynthesizer.stop();
        }
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        if (mSpeechSynthesizer != null) {
            mSpeechSynthesizer.release();
        }
        super.onDestroy();
    }

    @Override
    public void onInputBtnClick(View v) {
        if (mVoiceInputFragment == null) {
            mVoiceInputFragment = new VoiceInputFragment();
        }
        // 停止唤醒词
        stopWaitingWakeup();
        // 停止 webview 视频播放
        mWebView.onPause();
        // 隐藏报错信息
        hideInfo();
        // 直接使用成员变量，避免初始化开销，待优化s
        if (mSpeechSynthesizer != null) {
            mSpeechSynthesizer.stop();
        }


        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_frame, mVoiceInputFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void onRecogResult(Bundle bundle) {
        getSupportFragmentManager().popBackStack();

        TextView inputText = (TextView) findViewById(R.id.input_text);
        WebView webView = (WebView) findViewById(R.id.webview);

        Log.d("", "onRecogResult: " + bundle.toString());

        Integer error = bundle.getInt("error");
        if (0 == error) {
            ArrayList<String> results = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (results.size() > 0) {
                String query = results.get(0);
                inputText.setText(query);
                String what = QueryParser.getWhat(query);

                if (what.length() != 0) {
                    BaiduBaikeHandler baikeHandler = new BaiduBaikeHandler(this);
                    baikeHandler.search(what);
                } else {
                    showInfo(String.format(getResources().getString(R.string.error_on_input_format), query));
                }
            }
        } else {
            int errStringId = getResources().getIdentifier("vsr_error_" + error.toString(), "string", getPackageName());
            String errMsg = getResources().getString(errStringId);
            String infoMsg = getResources().getString(R.string.error_on_voice_input);
            showInfo(String.format(infoMsg, errMsg));

        }
        // 开始监听唤醒词
        startWaitingWakeup();
        // 继续 WebView 视频播放
        mWebView.onResume();
    }

    public void showInfo(String info) {
        mInfoText.setText(info);
        if (mInfoText.getVisibility() != View.VISIBLE) {
            mInfoText.setVisibility(View.VISIBLE);
        }
    }

    public void showInfo(int id) {
        mInfoText.setText(id);
    }

    public void hideInfo() {
        if (mInfoText.getVisibility() != View.GONE) {
            mInfoText.setVisibility(View.GONE);
        }
    }

    public void startWaitingWakeup() {
        Log.i(TAG, "startWaitingWakeup: ");
        // 通知唤醒管理器, 启动唤醒功能
        HashMap params = new HashMap();
        // 设置唤醒资源, 唤醒资源请到 http://yuyin.baidu.com/wake#m4 来评估和导出
        params.put("kws-file", "assets:///WakeUp.bin");
        getWpEventManager().send("wp.start", new JSONObject(params).toString(), null, 0, 0);
    }

    public void stopWaitingWakeup() {
        Log.i(TAG, "stopWaitingWakeup: ");
        // 停止唤醒监听
        getWpEventManager().send("wp.stop", null, null, 0, 0);
    }

    public RequestQueue getVolleyQueue() {
        if (mVolleyQueue == null) {
            // 使用支持代理和重定向的 HurlStack
            mVolleyQueue = Volley.newRequestQueue(this, new HurlStackWrapper());
        }
        return mVolleyQueue;
    }

    public SpeechSynthesizer getSpeechSynthesizer() {
        Log.i(TAG, "getSpeechSynthesizer:");
        if (mSpeechSynthesizer == null) {
            Log.i(TAG, "getSpeechSynthesizer: Initialize.");
            // 获取语音合成对象实例
            mSpeechSynthesizer = SpeechSynthesizer.getInstance();
            // 设置context
            mSpeechSynthesizer.setContext(this);
            // 设置语音合成状态监听器
            mSpeechSynthesizer.setSpeechSynthesizerListener(this);

            // 设置离线引擎的资源，因为资源文件太大，需要拷贝到SD卡上才能使用
            // 设置语音合成文本模型文件（离线引擎使用）
            File textModelFile = new File(getExternalFilesDir(null), "bd_etts_text.dat");
            Utils.copyAssetsToSdcard(getAssets(), "bd_etts_text.dat", textModelFile);
            mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, textModelFile.getPath());
            // 设置语音合成声学模型文件（离线引擎使用）
            File speechModelFile = new File(getExternalFilesDir(null), "bd_etts_speech_female.dat");
            Utils.copyAssetsToSdcard(getAssets(), "bd_etts_speech_female.dat", speechModelFile);
            mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, speechModelFile.getPath());

            // 拿到 APP 的授权信息，并设置授权
            mSpeechSynthesizer.setApiKey(getResources().getString(R.string.api_key),
                    getResources().getString(R.string.secret_key));
            mSpeechSynthesizer.setAppId(getResources().getString(R.string.app_id));

            // 发音人（在线引擎），可用参数为0,1,2,3。。。（服务器端会动态增加，各值含义参考文档，
            // 以文档说明为准。0--普通女声，1--普通男声，2--特别男声，3--情感男声，4--情感儿童声<度丫丫>
            mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "4");
            mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE,
                    SpeechSynthesizer.MIX_MODE_HIGH_SPEED_NETWORK);
            mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "6");
            mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "6");

            // 初始化语音合成
            mSpeechSynthesizer.initTts(TtsMode.MIX);

            Log.d(TAG, "getSpeechSynthesizer: engineVersion=" + SynthesizerTool.getEngineVersion());
            Log.d(TAG, "getSpeechSynthesizer: engineInfo=" + SynthesizerTool.getEngineInfo());
            Log.d(TAG, "getSpeechSynthesizer: textModelInfo=" + SynthesizerTool.getModelInfo(textModelFile.getPath()));
            Log.d(TAG, "getSpeechSynthesizer: speechModelInfo=" + SynthesizerTool.getModelInfo(speechModelFile.getPath()));
        }
        return mSpeechSynthesizer;
    }

    public EventManager getWpEventManager() {
        if (mWakeupManager == null) {
            // 1) 创建唤醒事件管理器
            mWakeupManager = EventManagerFactory.create(this, "wp");
            // 2) 注册唤醒事件监听器
            mWakeupManager.registerListener(new EventListener() {
                @Override
                public void onEvent(String name, String params, byte[] data, int offset, int length) {
                    Log.i(TAG, "onEvent: name=" + name);
                    try {
                        JSONObject json = new JSONObject(params);
                        if ("wp.data".equals(name)) {
                            // 每次唤醒成功, 将会回调name=wp.data的时间, 被激活的唤醒词在params的word字段
                            String word = json.getString("word"); // 唤醒词
                            Log.i(TAG, "WakeUp by word=" + word);
                            findViewById(R.id.input_btn).callOnClick();
                        } else if ("wp.exit".equals(name)) {
                            // 唤醒已经停止
                        }
                    } catch (JSONException e) {
                        throw new AndroidRuntimeException(e);
                    }
                }
            });
        }
        return mWakeupManager;
    }

    @Override
    public void onError(String arg0, SpeechError arg1) {
        // 监听到出错，在此添加相关操作
        Log.w(TAG, "onError: SpeechSynthesizer error: " + arg1.toString());
    }

    @Override
    public void onSpeechFinish(String arg0) {
        // 监听到播放结束，在此添加相关操作
        Log.d(TAG, "onSpeechFinish: ");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideInfo();
                mWebView.onResume();
            }
        });
    }

    @Override
    public void onSpeechProgressChanged(String arg0, int arg1) {
        // 监听到播放进度有变化，在此添加相关操作
    }

    @Override
    public void onSpeechStart(String arg0) {
        // 监听到合成并播放开始，在此添加相关操作
        Log.d(TAG, "onSpeechStart: ");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWebView.onPause();
            }
        });
    }

    @Override
    public void onSynthesizeDataArrived(String arg0, byte[] arg1, int arg2) {
        // 监听到有合成数据到达，在此添加相关操作
    }

    @Override
    public void onSynthesizeFinish(String arg0) {
        // 监听到合成结束，在此添加相关操作
    }

    @Override
    public void onSynthesizeStart(String arg0) {
        // 监听到合成开始，在此添加相关操作
    }
}
