package com.df.app.util;

import java.io.File;
import java.util.List;

/**
 * Created by 岩 on 14-3-27.
 */
public class DeleteFiles {
    private String filePath;
    private List<String> fileNames;

    public DeleteFiles(String filePath, List<String> fileNames) {
        this.filePath = filePath;
        this.fileNames = fileNames;
    }

    public void deleteFiles() {
        for(String fileName : fileNames) {
            File file = new File(filePath + fileName);
            if(file.exists()) {
                file.delete();
            }
        }
    }
}
