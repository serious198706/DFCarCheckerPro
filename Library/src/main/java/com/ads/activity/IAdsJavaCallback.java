package com.ads.activity;

public interface IAdsJavaCallback {
    
    void sendWinMessageFromNative(int what, int wparam, int lparam);
    
    int getUserSelection();
    
    int getUserSelectionTimed(int timeout);
    
    void showDialog(byte[] info, boolean question);
    
    void dismissDialog();
    
    void showStatusDialog(byte[] info);
    
    void showStatusDialog(String title, String content);
    
    void setStatusDialogProgress(int progress);
    
    void dismissStatusDialog();
    
    void showInputDialog(String title, byte[] info);
    
    void setWindowTitle(String title);
    
    void switchWindowView(int type);
    
    void showStatText(String s);
    
    void clearStatText();
    
    void invalidateListView();
    
    int getListViewTopItemPos();
    
    int getListViewBottomItemPos();
    
    //void addMenuItem(String item);
    void addMenuItem(String item, int id);
    
    void clearMenuItem();
    
    void showPopMenu(String title);
    
    void addPopMenuItem(String item);
    
    void dismissPopMenu();
    
    void addDtcItem(byte buff[]);
    
    void clearDtcItem();
    
    void clearCdsItem();
    
    void addCdsItem(int id, String txt, String max, String min, String unit, String format);
    
    int getCdsItemId(int index);
    
    void setCdsItemValue(int id, float fValue, String strValue);
    
    void clearCustView();
    
    void custShowText(String text, int pos, int color);
    
    void showFunctionButton(int index, String text);
}
