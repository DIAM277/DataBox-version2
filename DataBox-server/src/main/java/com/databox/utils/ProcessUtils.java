package com.databox.utils;

import com.databox.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class ProcessUtils {

    public static String executeCommand(String cmd, Boolean outPrintLog) throws BusinessException {
        if(StringTools.isEmpty(cmd)){
            log.error("------------ 执行命令为空! ------------");
            return null;
        }
        Runtime runtime = Runtime.getRuntime();
        Process process = null;
        try{
            process = Runtime.getRuntime().exec(cmd);
            // 执行ffmpeg命令 取出输出流和错误流的信息
            // 必须要取出ffmpeg在执行命令过程中产生的输出信息， 否则当输出流信息填满jvm存储输出流信息的缓冲区时，线程会被阻塞
            PrintStream errorStream = new PrintStream(process.getErrorStream());
            PrintStream inputStream = new PrintStream(process.getInputStream());
            errorStream.start();
            inputStream.start();
            // 等待命令执行完毕
            process.waitFor();
            // 获取执行结果
            String result = errorStream.stringBuffer.append(inputStream.stringBuffer + "\n").toString();
            // 输出执行的命令
            if(outPrintLog){
                log.info("执行命令:{}, 已执行完毕，执行结果:{}", cmd, result);
            } else {
                log.info("执行命令:{}, 已执行完毕", cmd);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("视频转换失败!");
        } finally {
            if(null != process){
                ProcessKiller ffmpegKiller = new ProcessKiller(process);
                runtime.addShutdownHook(ffmpegKiller);
            }
        }
    }

    private static class ProcessKiller extends Thread {
        private Process process;
        public ProcessKiller(Process process) {
            this.process = process;
        }

        @Override
        public void run() {
            process.destroy();
        }
    }

    static class PrintStream extends Thread {
        InputStream inputStream = null;
        BufferedReader  bufferedReader = null;
        StringBuffer stringBuffer = new StringBuffer();
        public PrintStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            try {
                if(null == inputStream){
                    return;
                }
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line);
                }
            } catch (IOException e) {
                log.error("读取输入流出错，错误信息" + e);
            }finally {
                try {
                    if(null != bufferedReader){
                        bufferedReader.close();
                    }
                    if(null != inputStream){
                        inputStream.close();
                    }
                } catch (IOException e) {
                    log.error("关闭输入流出错，错误信息" + e);
                }
            }
        }
    }
}
