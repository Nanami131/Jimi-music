package com.example.jimi.utils;


import java.text.SimpleDateFormat;
import java.util.Date;


public class FileNameUtils {

    //设置文件路径的工具类
    public static String defineNamePath(String originalFilename,String prefix,int id) {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        String uniqueName = timestamp + RandomUtils.code() + id + originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = prefix + uniqueName;
        return fileName;
    }

}
