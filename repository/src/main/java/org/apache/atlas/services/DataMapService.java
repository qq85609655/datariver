package org.apache.atlas.services;

import org.apache.atlas.AtlasException;
import org.apache.atlas.typesystem.ITypedReferenceableInstance;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 数据地图服务接口
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-20 9:07
 */
public interface DataMapService {
    /**
     *
     * @param startTime
     * @param endTime
     * @return
     * @throws AtlasException
     */
    String selectEntityDefinitions(Map<String, Object> properties, String startTime, String endTime) throws AtlasException;

    /**
     * @param typeName
     * @param startTime
     * @param endTime
     * @return
     * @throws AtlasException
     */
    List<ITypedReferenceableInstance> getTypedEntities(String typeName, String startTime, String endTime) throws AtlasException;

    /**
     * @param typeName
     * @param guid
     * @return
     * @throws AtlasException
     */
    List<ITypedReferenceableInstance> getEntitesWithSamePropertyGuid(String typeName, String guid) throws AtlasException;

    /**
     * @param tableOrContainer
     * @param guid
     * @return
     * @throws AtlasException
     */
    Set<String> getTaskGuidsByTableOrContainerGuid(String tableOrContainer, String guid) throws AtlasException;

    /**
     * @param properties
     * @param startTime
     * @param endTime
     * @return
     * @throws AtlasException
     */
    List<ITypedReferenceableInstance> selectEntities(Map<String, Object> properties, long startTime, long endTime) throws AtlasException;

    /**
     * @param gremlinQuery
     * @return
     * @throws AtlasException
     */
    Set<String> getEntityGuidsByGremlinQuery(String gremlinQuery)throws AtlasException;
}
