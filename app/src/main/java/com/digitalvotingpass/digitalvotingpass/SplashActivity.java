package com.digitalvotingpass.digitalvotingpass;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.digitalvotingpass.blockchain.BlockChain;
import com.digitalvotingpass.blockchain.BlockchainCallBackListener;
import com.digitalvotingpass.camera.Camera2BasicFragment;
import com.digitalvotingpass.electionchoice.ElectionChoiceActivity;

import java.text.DecimalFormat;
import java.util.Date;

public class SplashActivity extends Activity implements BlockchainCallBackListener {
    public static final int REQUEST_CODE_STORAGE = 15;
    public static final int REQUEST_CODE_INTERNET = 5;

    // Duration of splash screen in millis
    private final int SPLASH_DISPLAY_LENGTH = 1000;
    private String TAG = getClass().getSimpleName();

    private TextView downloadPogressText;
    private TextView currentTask;
    private ProgressBar downloadProgressBar;
    private BlockchainCallBackListener thisActivity;
    private Handler handler;
    private Handler initTextHandler;

    DecimalFormat percentFormatter = new DecimalFormat("###.0");

    Runnable startBlockChain = new Runnable(){
        @Override
        public void run() {
            if (ContextCompat.checkSelfPermission((Activity)thisActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestStoragePermissions();
                return;
            }
            BlockChain.getInstance().receiveBlockChain(thisActivity);
        }
    };

    Runnable initUpdate = new Runnable() {
        int i = 0;
        @Override
        public void run() {
            String[] s = ((Activity)thisActivity).getResources().getStringArray(R.array.init_array);
            currentTask.setText(s[i % s.length]);
            i++;
            initTextHandler.postDelayed(this, 500);
        }
    };

    /**
     * Creates a splash screen
     * @param bundle
     */
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_splash_screen);

        downloadPogressText = (TextView) findViewById(R.id.download_progress_text);
        currentTask = (TextView) findViewById(R.id.progress_current_task);
        downloadProgressBar = (ProgressBar) findViewById(R.id.download_progress_bar);

        thisActivity = this;
        handler = new Handler();
        initTextHandler = new Handler();
        initTextHandler.post(initUpdate);
        handler.post(startBlockChain);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
       if (requestCode == REQUEST_CODE_STORAGE) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            } else {
                handler.post(startBlockChain);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void requestStoragePermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Camera2BasicFragment.ErrorDialog.newInstance(getString(R.string.ocr_storage_permission_explanation))
                    .show(getFragmentManager(), "");
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE);
        }
    }

    @Override
    public void onInitComplete() {
        initTextHandler.removeCallbacks(initUpdate);
    }

    @Override
    public void onDownloadComplete() {
        Intent mainIntent = new Intent(SplashActivity.this, ElectionChoiceActivity.class);
        SplashActivity.this.startActivity(mainIntent);
        SplashActivity.this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("","");
        initTextHandler.removeCallbacks(initUpdate);
    }

    @Override
    public void onDownloadProgress(double pct, int blocksSoFar, Date date) {
        onInitComplete();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                currentTask.setText(R.string.downloading_text);
                downloadPogressText.setText(percentFormatter.format(pct) + "%");
                downloadProgressBar.setProgress((int)pct);
            }
        });
    }
}
