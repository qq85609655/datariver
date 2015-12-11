package org.apache.atlas.odps.parser;

import org.apache.atlas.odps.model.OdpsDataTypes;
import org.apache.atlas.typesystem.Referenceable;

import java.util.*;

/**
 * 解析上下文
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-04 14:13
 */
public class ParserContext {

    private Map<OdpsDataTypes,Object> values = new HashMap<>();
    public Set<Referenceable> inputTables = new HashSet<>();
    public Set<Referenceable> outputTables = new HashSet<>();
    public Set<Referenceable> inputDbs = new HashSet<>();
    public Set<Referenceable> outputDbs = new HashSet<>();

    public void put(OdpsDataTypes type,Object value) {
        values.put(type, value);
    }

    public Map<OdpsDataTypes,Object> getValues() {
        return values;
    }

    public Object getValue(OdpsDataTypes type) {
        return values.get(type);
    }

}
