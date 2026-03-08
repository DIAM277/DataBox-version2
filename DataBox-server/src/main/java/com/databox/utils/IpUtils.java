package com.databox.utils;

import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;

public class IpUtils {

    // 缓存 Searcher 对象
    private static Searcher searcher = null;

    static {
        try {
            // 项目启动时，将 resources 下的 xdb 文件加载到内存中
            ClassPathResource resource = new ClassPathResource("ip2region_v4.xdb");
            InputStream inputStream = resource.getInputStream();
            byte[] cBuff = FileCopyUtils.copyToByteArray(inputStream);
            searcher = Searcher.newWithBuffer(cBuff);
        } catch (Exception e) {
            System.err.println("加载 ip2region_4.xdb 失败，请检查文件是否存在！");
            e.printStackTrace();
        }
    }

    /**
     * 获取客户端真实IP
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 对于通过多个代理的情况，第一个IP为客户端真实IP
        if (ip != null && ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }

    /**
     * 获取IP归属地
     */
    public static String getIpLocation(String ip) {
        if (ip == null || ip.trim().isEmpty() || "127.0.0.1".equals(ip) || ip.startsWith("192.168") || ip.startsWith("10.")) {
            return "局域网/本机";
        }
        try {
            // 返回格式：国家|区域|省份|城市|ISP
            String region = searcher.search(ip);
            // 将没用的 0 替换掉，如：中国|0|广东|深圳|电信 -> 中国-广东-深圳-电信
            return region.replace("|0", "").replace("|", "-");
        } catch (Exception e) {
            return "未知";
        }
    }
}