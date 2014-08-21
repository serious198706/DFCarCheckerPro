package com.df.library.service.SpeechRecognize;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.df.library.R;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;

/**
 * Created by 岩 on 2014/7/30.
 */
public class SpeechDialog extends Dialog {
    private Context context;
    private ImageView[] imageViews;

    public interface OnResult {
        public void onResult(String result);
    }
    private OnResult mCallback;
    private SpeechRecognizer mIat;

    //听写监听器
    private RecognizerListener mRecoListener = new RecognizerListener(){
        //听写结果回调接口(返回Json格式结果，用户可参见附录)；
        //一般情况下会通过onResults接口多次返回结果，完整的识别内容是多次结果的累加；
        //关于解析Json的代码可参见MscDemo中JsonParser类；
        //isLast等于true时会话结束。
        public void onResult(RecognizerResult results, boolean isLast) {
            String result = SpeechJsonParser.parseIatResult(results.getResultString());
            mCallback.onResult(result);
            findViewById(R.id.tip).setVisibility(View.INVISIBLE);
        }
        //会话发生错误回调接口
        public void onError(SpeechError error) {
            error.getPlainDescription(true); //获取错误码描述
            Toast.makeText(context, error.getErrorDescription(), Toast.LENGTH_LONG).show();
        }
        //开始录音
        public void onBeginOfSpeech() {}
        //音量值0~30
        public void onVolumeChanged(int volume){
            if(volume == 30)
                volume = 29;

            showVolume(volume / 3);
        }
        //结束录音
        public void onEndOfSpeech() {
            ((Button) findViewById(R.id.start)).setText("开始");
            findViewById(R.id.cancel).setEnabled(false);
            showVolume(0);
        }
        //扩展用接口
        public void onEvent(int eventType,int arg1,int arg2,String msg) {}
    };

    public SpeechDialog(Context context, OnResult listener) {
        super(context);

        this.context = context;
        this.mCallback = listener;

        init();
    }

    @Override
    protected void onStop() {
        mIat.cancel();
    }

    private void init() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.speech_dialog);
       // setCanceledOnTouchOutside(false);

        imageViews = new ImageView[9];

        for(int i = 1; i <= 9; i++) {
            int id = this.getWindow().getDecorView().getResources().getIdentifier("lv" + i, "id", context.getPackageName());

            ImageView imageView = (ImageView)findViewById(id);
            imageViews[i - 1] = imageView;
        }

        //1.创建SpeechRecognizer对象，第二个参数：本地听写时传InitListener
        mIat = SpeechRecognizer.createRecognizer(context, null);
        //2.设置听写参数，详见《科大讯飞MSC API手册(Android)》SpeechConstant类
        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin");
        mIat.setParameter(SpeechConstant.ASR_PTT, "0");

        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 当按钮是“开始”时，就开始听写，并把按钮改为“说完了”
                if(((Button)view).getText().equals("开始")) {
                    mIat.startListening(mRecoListener);
                    ((Button) view).setText("说完了");
                    findViewById(R.id.tip).setVisibility(View.VISIBLE);
                    ((TextView)findViewById(R.id.tip)).setText("请讲话");
                    findViewById(R.id.cancel).setEnabled(true);
                }
                // 当按钮是“说完了”时，就停止听写，并把按钮改为“开始”
                else {
                    mIat.stopListening();
                    ((Button) view).setText("开始");
                    ((TextView)findViewById(R.id.tip)).setText("正在解析...");
                    findViewById(R.id.cancel).setEnabled(false);
                    showVolume(0);
                }
            }
        });

        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIat.cancel();

                showVolume(0);
                findViewById(R.id.tip).setVisibility(View.INVISIBLE);
                ((Button)findViewById(R.id.start)).setText("开始");
            }
        });

        setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if(((Button) findViewById(R.id.start)).getText().equals("开始")) {
                    cancel();
                } else {
                    return;
                }
            }
        });

        Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.BOTTOM);
        dialogWindow.setBackgroundDrawableResource(android.R.color.transparent);

        lp.x = 0; // 新位置X坐标
        lp.y = 0; // 新位置Y坐标
        lp.dimAmount = 0f; // 背景不变暗

        dialogWindow.setAttributes(lp);

        //3.开始听写
        mIat.startListening(mRecoListener);
    }

    private void showVolume(int level) {
        for(int i = 0; i < 9; i++) {
            imageViews[i].setVisibility(View.INVISIBLE);
        }

        for(int i = 0; i < level; i++) {
            imageViews[i].setVisibility(View.VISIBLE);
        }
    }
}
