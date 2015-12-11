package org.apache.atlas.odps.parser;

import org.apache.atlas.AtlasException;
import org.apache.atlas.odps.model.OdpsDataTypes;
import org.apache.atlas.typesystem.Referenceable;

/**
 * json解析器
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-04 14:13
 */
public interface JsonParser {
    /**
     * 支持的类型
     * @return
     */
    OdpsDataTypes type();

    void parse(ParserContext context,Object... data) throws AtlasException;

}
