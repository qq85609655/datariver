/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.atlas.dxt.client;

import org.apache.atlas.dxt.util.DxtStoreUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Properties;

/**
 * @author YiSheng 0381
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/4 9:43
 */
public class PropertiesReader {
    /**
     * 根据UTF-8编码的属性配置文件的文件名和文件编码获取配置信息.
     *
     * @param fileName 配置文件的文件名
     * @return 配置信息
     */
    public static Properties getProperties(String fileName) {
        return getProperties(fileName, "utf-8");
    }

    /**
     * 根据属配置文件的文件名和文件编码获取配置信息.
     *
     * @param fileName   配置文件的文件名
     * @param fileEncode 配置文件的编码
     * @return 配置信息
     */
    @SuppressWarnings("ConstantConditions")
    public static Properties getProperties(String fileName, String fileEncode) {
        Properties properties = null;
        try {
            String path = DxtStoreUtil.getConfPath() + fileName;
            path = URLDecoder.decode(path, fileEncode);
            File file = new File(path);
            InputStream inputStream = new FileInputStream(file);
            properties = new Properties();
            properties.load(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return properties;
    }
}
