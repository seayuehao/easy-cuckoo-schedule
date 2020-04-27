package org.ecs.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class CommonUtil {

    private CommonUtil() {
    }

    private static final String MD5 = "MD5";

    private static final Gson GSON = new GsonBuilder().create();

    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> type) {
        return GSON.fromJson(json, type);
    }


    public static String md5(String content){
        try {
            MessageDigest md = MessageDigest.getInstance(MD5);
            md.update(content.getBytes());
            return new BigInteger(1, md.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            return content;
        }
    }

}
