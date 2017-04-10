package com.example.alex.testnakitel;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Created by alex on 10.04.17.
 */

public class MainPresenter {
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int REPEAT_INTERVAL = 40;
    private static final String TEMP_FILE_PREFIX = "NakitelAudio";
    private static final String TEMP_FILE_AUDIO_SUFFIX = ".3gp";
    private static final String TEMP_FILE_TXT_SUFFIX = ".txt";

    private MainActivity mView;
    private File mAudioFile = null;
    private File mTxtFile = null;
    private boolean mIsRecording = false;
    private MediaRecorder myAudioRecorder;
    private Handler mHandler = new Handler();
    private int mYValue = 0;
    private JSONArray mJsonArray = new JSONArray();

    private Runnable mVisualizer = new Runnable() {
        @Override
        public void run() {
            if (mIsRecording) {
                int xValue = myAudioRecorder.getMaxAmplitude();
                mView.getVisualGraphView().addAmplitude(xValue);
                mView.getVisualGraphView().invalidate();
                mHandler.postDelayed(this, REPEAT_INTERVAL);

                mYValue++;
                saveToJSON(xValue, mYValue);
            }
        }
    };

    public void bindView(MainActivity view) {
        this.mView = view;
    }

    public void onStartBtnClicked() {
        this.mYValue = 0;
        if (ActivityCompat.checkSelfPermission(mView, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(mView, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mView, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            recordAudio();
        }
    }

    public void requestPermissionResult(int requestCode, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                recordAudio();
            }
        }
    }

    public void saveToJSON(int x, int y) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("xValue", x);
            jsonObject.put("yValue", y);
            mJsonArray.put(jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onStopBtnClicked() {
        if (myAudioRecorder != null) {
            myAudioRecorder.stop();
            myAudioRecorder.release();
            myAudioRecorder = null;

            mIsRecording = false;
            mView.getVisualGraphView().clear();
            mHandler.removeCallbacks(mVisualizer);
            saveJSONtoFile();
        }
    }

    private void saveJSONtoFile() {
        try {
            mTxtFile = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_TXT_SUFFIX, Environment.getExternalStorageDirectory());
            Writer output = new BufferedWriter(new FileWriter(mTxtFile));
            output.write(mJsonArray.toString());
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void recordAudio() {
        myAudioRecorder = new MediaRecorder();
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);

        if (haveAudioTempFile()) {
            myAudioRecorder.setOutputFile(mAudioFile.getAbsolutePath());

            try {
                myAudioRecorder.prepare();
                myAudioRecorder.start();
                mIsRecording = true;
                mHandler.post(mVisualizer);
            } catch (IOException | IllegalStateException e) {
                e.printStackTrace();
                mIsRecording = false;
            }
        }
    }

    private boolean haveAudioTempFile() {
        try {
            mAudioFile = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_AUDIO_SUFFIX, Environment.getExternalStorageDirectory());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
