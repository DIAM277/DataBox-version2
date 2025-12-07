package com.databox.utils;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class JsonUtils {

    private static String convertObj2Json(Object obj){
        return JSONObject.toJSONString(obj);
    }

    public static <T> T convertJson2Obj(String json, Class<T> clazz){
        return JSONObject.parseObject(json, clazz);
    }

    public static <T> List<T> convertJsonArray2List(String json, Class<T> clazz){
        return JSONObject.parseArray(json, clazz);
    }

}
