package com.ads.model;

import com.df.library.util.MyApplication;

public class UserSelection {
    private int code = MyApplication.WM_KEY_NONE;
    
    public void setKeyCode(int c) {
        code = c;
    }
    
    public int consumeKeyCode() {
        int ret = code;
        code = MyApplication.WM_KEY_NONE;
        return ret;
    };
}
