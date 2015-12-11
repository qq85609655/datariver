package org.apache.atlas.dataStudio;

import org.apache.atlas.dataStudio.hook.DataStudioHook;

/**
 * 描述信息
 *
 * @author FanZeng 0189
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/26 13:34
 */
public class StartDataStudioBridge {

    public static void main(String[] args) {
        DataStudioHook.getInstance();
        while (true) {
        }
    }
}
