package org.apache.atlas.common.util;

import org.apache.atlas.typesystem.Referenceable;

import java.util.List;

/**
 * @author YiSheng 0381
 * @version 1.0.0
 * @company DTDream
 * @date 2015/12/3 14:19
 */
public interface LineageHandler {
    void addToLineage(List<Referenceable> inputTables, List<Referenceable> outputTables,
                      List<Referenceable> inputDbs, List<Referenceable> outputDbs) throws Exception;
}
