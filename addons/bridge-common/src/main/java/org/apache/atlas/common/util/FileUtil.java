package org.apache.atlas.common.util;

import org.apache.atlas.common.exception.BridgeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * 公共的读取文件的接口
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-04 14:13
 */

public abstract class FileUtil {
    private static final Logger LOG = LoggerFactory.getLogger(FileUtil.class);

    public static String loadResourceFile(Object bean, String filename) {
        return loadResourceFile(bean.getClass().getResource("/").getPath() + "/" + filename);
    }

    public static String loadResourceFile(String fileFullName) {
        LOG.debug("Load resource file {}", fileFullName);
        try (BufferedReader br =
                 new BufferedReader(new InputStreamReader(new FileInputStream(fileFullName), "utf-8"))) {
            //try (BufferedReader br =
            // new BufferedReader(new FileReader(this.getClass().getResource("/").getPath()+"/"+filename))) {
            String data = br.readLine();//一次读入一行，直到读入null为文件结束
            StringBuilder sb = new StringBuilder();
            while (data != null) {
                sb.append(data);
                data = br.readLine(); //接着读下一行
            }
            return sb.toString();
        } catch (Exception e) {
            throw new BridgeException("加载资源异常", e);
        }
    }
}
