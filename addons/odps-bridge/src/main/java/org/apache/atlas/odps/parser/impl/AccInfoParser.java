package org.apache.atlas.odps.parser.impl;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.odps.utils.StringUtils;
import org.apache.atlas.AtlasException;
import org.apache.atlas.odps.model.OdpsDataTypes;
import org.apache.atlas.odps.parser.BaseJsonParser;
import org.apache.atlas.odps.parser.ParserContext;
import org.apache.atlas.typesystem.Referenceable;

/**
 * project访问信息解析器
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-04 14:13
 */
public class AccInfoParser extends BaseJsonParser {
    @Override
    public void doParse(ParserContext context, Referenceable currentEntity, Object... data) throws AtlasException {
        JSONObject accJson = (JSONObject) data[0];
        Referenceable project = (Referenceable) context.getValue(OdpsDataTypes.ODPS_PROJECT);
        currentEntity.set(QUALIFIED_NAME,
            formatQualifiedName((String) project.get(QUALIFIED_NAME), accJson.getString("name")));
        setStringWithSameKey(currentEntity, accJson, "name");
        setStringWithSameKey(currentEntity, accJson, "endpointURL");
    }

    @Override
    public OdpsDataTypes type() {
        return OdpsDataTypes.ODPS_ACCINFO;
    }
}
