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

import com.alibaba.fastjson.JSON;
import com.squareup.okhttp.OkHttpClient;
import org.apache.atlas.dxt.util.DxtStoreUtil;
import org.apache.atlas.odps.client.DDPClient;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.mime.TypedString;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLDecoder;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

/**
 * @author YiSheng 0381
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/4 9:43
 */
public class DxtClient {
    private Properties properties;
    private IDxtRestClient idxtRestClient;

    private OkHttpClient getUnsafeOkHttpClient() {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            String path = DxtStoreUtil.getConfPath() + "DXTServerKeyStore";
            path = URLDecoder.decode(path, "utf-8");
            File file = new File(path);
            keyStore.load(new FileInputStream(file), "DXTServer".toCharArray());
            @SuppressWarnings("deprecation")
            SSLContext ssl = SSLContexts.custom().loadTrustMaterial(keyStore, new TrustSelfSignedStrategy()).build();

            SSLSocketFactory sslSocketFactory = ssl.getSocketFactory();

            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.setSslSocketFactory(sslSocketFactory);
            okHttpClient.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            Long connectTimeout = Long.parseLong(properties.getProperty("dxt.connectTimeout"));
            okHttpClient.setConnectTimeout(connectTimeout, TimeUnit.MILLISECONDS);

            Long readTimeout = Long.parseLong(properties.getProperty("dxt.readTimeout"));
            okHttpClient.setReadTimeout(readTimeout, TimeUnit.MILLISECONDS);

            Long writeTimeout = Long.parseLong(properties.getProperty("dxt.writeTimeout"));
            okHttpClient.setWriteTimeout(writeTimeout, TimeUnit.MILLISECONDS);

            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public DxtClient(String workspaceName) {
        properties = PropertiesReader.getProperties("dxt.properties");
        String endPoint = properties.getProperty("dxt.endpoint");
        String accountStr = DDPClient.getInstance().getDXTAccount(workspaceName);
        String authToken = JSON.parseObject(accountStr).getString("account");

        Map<String, String> httpHeader = new HashMap<>();
        httpHeader.put("X-Auth-token", authToken);
        httpHeader.put("Content-Type", "Application/json");
        httpHeader.put("Accept-Encoding", "Application/json");
        SessionRequestInterceptor requestInterceptor = new SessionRequestInterceptor(httpHeader);

        OkClient client = new OkClient(getUnsafeOkHttpClient());

        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(endPoint)
            .setRequestInterceptor(requestInterceptor).setClient(client).build();
        if (null == restAdapter) {
            throw new NullPointerException("restAdapter == null");
        }

        idxtRestClient = restAdapter.create(IDxtRestClient.class);
    }

    public DxtRespBean queryDBConnection(String connId) {
        return idxtRestClient.queryDBConnection(connId);
    }

    public DxtRespBean getDBTables(String connId) {
        return idxtRestClient.getDBTables(connId);
    }

    public DxtRespBean getDBTableColumns(String connId, String tableName) {
        return idxtRestClient.getDBTableColumns(connId, tableName);
    }

    public DxtRespBean getHDFSFields(String uid, String body) {
        TypedString typedString = new TypedString(body);
        return idxtRestClient.getHdfsFields(uid, typedString);
    }

    public DxtRespBean getTransHisLogs(String transId) {
        return idxtRestClient.queryHisTransStatistics(transId);
    }

    public DxtRespBean getTrans(String transId) {
        return idxtRestClient.queryTrans(transId);
    }
}
