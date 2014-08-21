package com.df.library.util;

import java.io.File;
import java.util.List;

/**
 * Created by 岩 on 14-3-27.
 */
public class DeleteFiles {
    public static void deleteFiles(String filePath, List<String> fileNames) {
        for(String fileName : fileNames) {
            File file = new File(filePath + fileName);
            if(file.exists()) {
                file.delete();
            }
        }
    }

    public static void deleteFiles(String path) {
        File dir = new File(path);

        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                new File(dir, children[i]).delete();
            }
        }
    }

    public static boolean deleteFile(String path) {
        File file = new File(path);
        return file.delete();
    }
}
