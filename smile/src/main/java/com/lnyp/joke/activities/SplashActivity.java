package com.lnyp.joke.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.lnyp.joke.R;
import com.lnyp.joke.bean.UpgradeBean;
import com.lnyp.joke.util.AppUtils;
import com.lnyp.joke.widget.DialogUpdateInfo;

import im.fir.sdk.FIR;
import im.fir.sdk.VersionCheckCallback;

public class SplashActivity extends AppCompatActivity {

    /**
     * 最短启动时间
     */
    private static final int SHOW_TIME_MIN = 2500;

    private long startTime;

    UpgradeBean respUpdate;

    DialogUpdateInfo dialogUpdateInfo;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {

                case R.id.btnSubmit:
                    Uri uri = Uri.parse(respUpdate.getInstall_url());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    SplashActivity.this.startActivity(intent);
                    break;
                case R.id.btnCancle:
                    jumpToMain();
                    break;
            }
            dialogUpdateInfo.dismiss();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!this.isTaskRoot()) {
            //判断该Activity是不是任务空间的源Activity，“非”也就是说是被系统重新实例化出来
            //如果你就放在launcher Activity中话，这里可以直接return了
            Intent mainIntent = getIntent();
            String action = mainIntent.getAction();
            if (mainIntent.hasCategory(Intent.CATEGORY_LAUNCHER) && action.equals(Intent.ACTION_MAIN)) {
                finish();
                return;//finish()之后该活动会继续执行后面的代码，你可以logCat验证，加return避免可能的exception
            }
        }

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);

        checkUpdate();

    }

    private void checkUpdate() {

        FIR.checkForUpdateInFIR("c4eba07f521cf456edd68b9517c24df3", new VersionCheckCallback() {
                    @Override
                    public void onSuccess(String versionJson) {
                        Log.i("fir", "onSuccess " + "\n" + versionJson);
                        Gson gson = new Gson();
                        respUpdate = gson.fromJson(versionJson, UpgradeBean.class);
                    }

                    @Override
                    public void onFail(Exception exception) {
                        Log.i("fir", "onFail" + "\n" + exception.getMessage());
                    }

                    @Override
                    public void onStart() {
                        Log.i("fir", "onStart ");
                        startTime = System.currentTimeMillis();
                    }

                    @Override
                    public void onFinish() {
                        Log.i("fir", "onFinish");

                        long loadingTime = System.currentTimeMillis() - startTime;

                        if (loadingTime < SHOW_TIME_MIN) {
                            try {
                                Thread.sleep(SHOW_TIME_MIN - loadingTime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        try {

                            // 判断是否需要更新
                            if (respUpdate.getVersion() > AppUtils.getVersionCode(SplashActivity.this)) {
                                String update_desc = respUpdate.getChangelog();

                                dialogUpdateInfo = new DialogUpdateInfo(SplashActivity.this, R.style.dialog_update_app, update_desc, mOnClickListener);
                                dialogUpdateInfo.show();

                            } else {
                                jumpToMain();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            jumpToMain();
                        }
                    }
                }
        );
    }

    private void jumpToMain() {
        Intent intent = new Intent();

        intent.setClass(SplashActivity.this, MainActivity.class);

        startActivity(intent);
        SplashActivity.this.finish();
    }
}
