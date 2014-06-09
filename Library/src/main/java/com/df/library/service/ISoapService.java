package com.df.library.service;

/**
 * Created by 岩 on 13-10-9.
 *
 * soapService接口
 */

import android.graphics.Bitmap;

public interface ISoapService {
    boolean communicateWithServer();
    boolean communicateWithServer(String jsonString);
    boolean uploadPicture(Bitmap bitmap, String jsonString);
    boolean uploadPicture(String jsonString);
}
