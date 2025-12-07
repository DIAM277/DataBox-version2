package com.databox.utils;

import com.databox.entity.enums.ResponseCodeEnum;
import com.databox.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OKHttpUtils {
    private static final int TIME_OUT_SECOND = 8;

    private static OkHttpClient.Builder getClientBuilder(){
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder().followSslRedirects(false).retryOnConnectionFailure(false);
        clientBuilder.connectTimeout(TIME_OUT_SECOND, TimeUnit.SECONDS).readTimeout(TIME_OUT_SECOND, TimeUnit.SECONDS);
        return clientBuilder;
    }

    private static Request.Builder getRequestBuilder(Map<String, String> header){
        Request.Builder requestBuilder = new Request.Builder();
        if(null != header){
            for(Map.Entry<String, String> map : header.entrySet()){
                String key = map.getKey();
                String value;
                if(map.getValue() == null){
                    value = "";
                } else {
                    value = map.getValue();
                }
                requestBuilder.addHeader(key, value);
            }
        }
        return requestBuilder;
    }

    public static String getRequest(String url){
        ResponseBody  responseBody = null;
        try{
            OkHttpClient.Builder clientBuilder = getClientBuilder();
            Request.Builder requestBuilder = getRequestBuilder(null);
            OkHttpClient client = clientBuilder.build();
            Request request = requestBuilder.url(url).get().build();
            Response response = client.newCall(request).execute();
            responseBody = response.body();
            String responseStr = responseBody.string();
            log.info("postRequest请求地址{}, 返回信息{}", url, responseStr);
            return responseStr;
        } catch (SocketTimeoutException | ConnectException e) {
            log.error("Okhttp POST 请求超时, url:{}", url, e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        } catch (Exception e){
            log.error("Okhttp GET 请求异常", e);
            return null;
        }finally {
            if(responseBody != null){
                responseBody.close();
            }
        }
    }
}
