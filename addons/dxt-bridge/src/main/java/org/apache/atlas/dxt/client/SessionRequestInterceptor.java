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

import retrofit.RequestInterceptor;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author YiSheng 0381
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/4 9:43
 */
public class SessionRequestInterceptor implements RequestInterceptor {
    private Map<String, String> httpHeader;

    public SessionRequestInterceptor(Map<String, String> httpHeader) {
        this.httpHeader = httpHeader;
    }

    @Override
    public void intercept(RequestFacade request) {
        Set<Map.Entry<String, String>> entries = httpHeader.entrySet();
        Iterator<Map.Entry<String, String>> iterator = entries.iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            request.addHeader(entry.getKey(), entry.getValue());
        }

        return;
    }
}
