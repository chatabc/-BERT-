package com.llw.xfasrdemo;//package com.llw.xfasrdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.llw.xfasrdemo.databinding.ActivityMainBinding;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private SpeechRecognizer mIat; // 语音听写对象
    private TextView tvResult; // 识别结果显示
    private TextView tvPrediction; // 诈骗判断结果显示
    private Button btnStart; // 开始/结束识别按钮

    private HashMap<String, String> mIatResults = new LinkedHashMap<>();
    private SharedPreferences mSharedPreferences; // 缓存
    private String mEngineType = SpeechConstant.TYPE_CLOUD; // 引擎类型
    private String language = "zh_cn"; // 识别语言
    private String resultType = "json"; // 结果内容数据格式

    private boolean isRecognizing = false; // 是否正在识别

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tvResult = findViewById(R.id.tv_result);
        tvPrediction = findViewById(R.id.tv_prediction);
        btnStart = findViewById(R.id.btn_start);

        btnStart.setOnClickListener(v -> toggleRecognition());

        initPermission(); // 权限请求

        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(MainActivity.this, mInitListener);
        mSharedPreferences = getSharedPreferences("ASR", Activity.MODE_PRIVATE);
    }

    private void toggleRecognition() {
        if (isRecognizing) {
            // 停止识别
            if (mIat != null) {
                mIat.stopListening();
                mIat.cancel();
            }
            btnStart.setText("开始识别");
            isRecognizing = false;
        } else {
            // 开始识别
            if (null == mIat) {
                showMsg("创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化");
                return;
            }

            mIatResults.clear(); // 清除数据
            setParam(); // 设置参数
            mIat.startListening(mRecognizerListener); // 开始持续识别
            btnStart.setText("结束识别");
            isRecognizing = true;
        }
    }

    private final InitListener mInitListener = code -> {
        Log.d(TAG, "SpeechRecognizer init() code = " + code);
        if (code != ErrorCode.SUCCESS) {
            showMsg("初始化失败，错误码：" + code + ", 请访问 https://www.xfyun.cn/document/error-code 查询解决方案");
        }
    };

    private final RecognizerListener mRecognizerListener = new RecognizerListener() {
        public void onBeginOfSpeech() {
            // 识别开始
            Log.d(TAG, "识别开始");
        }

        public void onEndOfSpeech() {
            // 识别结束，自动开始下一句识别
            Log.d(TAG, "识别结束，自动开始下一句识别");
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (mIat != null && isRecognizing) {
                    mIat.startListening(mRecognizerListener);
                }
            }, 0); // 延迟500毫秒
        }

        public void onResult(RecognizerResult results, boolean isLast) {
            String text = JsonParser.parseIatResult(results.getResultString());
//            if (! (isLast || text=="." || text=="。" || text=="!" || text=="！" || text=="?" || text=="？")) {
            tvResult.append("\n"+text); // 更新识别结果显示
            sendToFlask(text); // 发送句子到Flask后端
//            }
        }

        public void onError(SpeechError error) {
            showMsg(error.getPlainDescription(true));
        }

        public void onVolumeChanged(int volume, byte[] data) {
            // 音量变化
            Log.d(TAG, "音量变化: " + volume);
        }

        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 事件处理
            Log.d(TAG, "事件处理: " + eventType);
        }
    };

    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

        tvResult.setText(resultBuffer.toString()); // 听写结果显示
    }

    private void setParam() {
        mIat.setParameter(SpeechConstant.PARAMS, null);
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        mIat.setParameter(SpeechConstant.RESULT_TYPE, resultType);

        if (language.equals("zh_cn")) {
            String lag = mSharedPreferences.getString("iat_language_preference", "mandarin");
            Log.e(TAG, "language:" + language);
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            mIat.setParameter(SpeechConstant.ACCENT, lag);
        } else {
            mIat.setParameter(SpeechConstant.LANGUAGE, language);
        }
        Log.e(TAG, "last language:" + mIat.getParameter(SpeechConstant.LANGUAGE));

        mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));
        mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));
        mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));

        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }

    private void showMsg(String msg) {
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mIat) {
            mIat.cancel();
            mIat.destroy();
        }
    }

    private void initPermission() {
        String[] permissions = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        ArrayList<String> toApplyList = new ArrayList<>();
        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void sendToFlask(String text) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.31.221:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        PredictionRequest request = new PredictionRequest(text);
        Call<PredictionResponse> call = apiService.predict(request);
        call.enqueue(new Callback<PredictionResponse>() {
            @Override
            public void onResponse(Call<PredictionResponse> call, Response<PredictionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 更新UI显示预测结果
                    tvPrediction.post(() -> tvPrediction.append("\n" + response.body().getPrediction()));
                } else {
                    try {
                        String error = response.errorBody().string();
//                        tvPrediction.post(() -> tvPrediction.append("预测失败：\n" + error + "\n"));
                        Log.e("Retrofit", "预测失败：\n" + error);
                    } catch (IOException e) {
//                        tvPrediction.post(() -> tvPrediction.append("预测失败：\n" + e.getMessage() + "\n"));
                        Log.e("Retrofit", "预测失败：\n" + e.getMessage(), e);
                    }
                }
            }

            @Override
            public void onFailure(Call<PredictionResponse> call, Throwable t) {
                if (t instanceof IOException) {
//                    tvPrediction.post(() -> tvPrediction.append("网络请求失败：\n" + t.getMessage() + "\n"));
                    Log.e("Retrofit", "网络请求失败：\n" + t.getMessage(), t);
                } else {
//                    tvPrediction.post(() -> tvPrediction.append("请求失败：\n" + t.getMessage() + "\n"));
                    Log.e("Retrofit", "请求失败：\n" + t.getMessage(), t);
                }
            }
        });
    }
}