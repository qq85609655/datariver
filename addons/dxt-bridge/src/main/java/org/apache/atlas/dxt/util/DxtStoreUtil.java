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

package org.apache.atlas.dxt.util;

import com.aliyun.odps.utils.StringUtils;
import org.apache.atlas.typesystem.Referenceable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DXT保存元数据的工具类
 *
 * @author YiSheng 0381
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/4 9:43
 */
public class DxtStoreUtil {
    public static String getConfPath() throws Exception {
        String confLocation = System.getProperty("atlas.conf");
        if (null == confLocation) {
            throw new Exception("Failed to get conf path.");
        }

        confLocation += "/bridge/";

        return confLocation;
    }

    public static Referenceable createDxtRef(String typeName, String qualifiedName, String... traitNames) {
        Referenceable ref = new Referenceable(typeName, traitNames);
        ref.set("metaSource", "DXT");
        ref.set("qualifiedName", qualifiedName);

        return ref;
    }

    /**
     * 格式化qualifiedName, formatQualifiedName("a", "b")返回"a.b"
     *
     * @param params 需要格式化的字符串
     * @return 格式化后的字符串
     */
    public static String formatQualifiedName(String... params) {
        boolean firstIn = true;
        StringBuilder strBuilder = new StringBuilder();
        for (String p : params) {
            if (StringUtils.isNullOrEmpty(p)) {
                continue;
            }

            if (!firstIn) {
                strBuilder.append(".");
            } else {
                firstIn = false;
            }
            strBuilder.append(p);
        }

        return strBuilder.toString();
    }

    public static Date formatDateFromStr(String dateStr) throws ParseException {
        SimpleDateFormat ex = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return ex.parse(dateStr);
    }
}
