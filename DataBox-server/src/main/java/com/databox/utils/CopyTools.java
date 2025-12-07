package com.databox.utils;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

public class CopyTools {
    public static <T, S> List<T> copyList(List<S> sList, Class<T> classz){
        List<T> List = new ArrayList<>();
        for(S s : sList){
            T t = null;
            try{
                t = classz.newInstance();
            } catch (Exception e){
                e.printStackTrace();
            }
            BeanUtils.copyProperties(s, t);
            List.add(t);
        }
        return List;
    }

    public static <T, S> T copy(S s, Class<T> classz){
        T t = null;
        try{
            t = classz.newInstance();
        } catch (Exception e){
            e.printStackTrace();
        }
        BeanUtils.copyProperties(s, t);
        return t;
    }
}
