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

package org.apache.atlas.dxt.store;

import org.apache.atlas.dxt.examples.DxtDemoInputStepStore;
import org.apache.atlas.dxt.examples.DxtDemoOutputStepStore;

/**
 * 创建不同类型step存储对象的工厂类
 * @author YiSheng 0381
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/4 9:43
 */
public class DxtStepStoreFactory {
    private DxtStepStoreFactory() {

    }

    public static IDxtTransStepStore getStepStore(String stepType) throws Exception {
        IDxtTransStepStore stepStore;

        switch (stepType) {
            case "tableInputs":
                stepStore = new DxtTableInputStepStore();
                break;
            case "tableOutputs":
                stepStore = new DxtTableOutputStepStore();
                break;
            case "odpsInputs":
                stepStore = new DxtOdpsInputStepStore();
                break;
            case "odpsOutputs":
                stepStore = new DxtOdpsOutputStepStore();
                break;
            case "HadoopFileInputPlugin":
                stepStore = new DxtHdfsInputStepStore();
                break;
            case "HadoopFileOutputPlugin":
                stepStore = new DxtHdfsOutputStepStore();
                break;
            case "demoInputs":
                stepStore = new DxtDemoInputStepStore();
                break;
            case "demoOutputs":
                stepStore = new DxtDemoOutputStepStore();
                break;
            default:
                throw new Exception("Unknown step: " + stepType + ".");
        }

        return stepStore;
    }
}
