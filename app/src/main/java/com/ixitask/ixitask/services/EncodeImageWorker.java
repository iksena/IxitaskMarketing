package com.ixitask.ixitask.services;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;

import com.ixitask.ixitask.utils.Constants;
import com.ixitask.ixitask.utils.FileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class EncodeImageWorker extends Worker {

    public EncodeImageWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String uriString = getInputData().getString(Constants.ARG_HP_IMAGE_URI);
        if (uriString==null) return Result.failure();
        File file = new File(FileUtils.getPath(getApplicationContext(), Uri.parse(uriString)));
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            // TO DO Auto-generated catch block
            e.printStackTrace();
            return Result.failure();
        } catch (IOException e) {
            // TO DO Auto-generated catch block
            e.printStackTrace();
            return Result.failure();
        }
        String imageStr = Base64.encodeToString(bytes, Base64.DEFAULT);
        Data output = new Data.Builder()
                .putString(Constants.ARG_HP_IMAGE, imageStr)
                .build();
        return Result.success(output);
    }
}
