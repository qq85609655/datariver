package org.apache.atlas.odps.parser;

import org.apache.atlas.common.exception.BridgeException;
import org.apache.atlas.odps.parser.impl.*;

/**
 * 解析工场
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-04 14:13
 */
public abstract class ParserFactory {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ParserFactory.class);

    /**
     * 获取处理器
     *
     * @return
     */
    private static JsonParser getParser(Class<? extends BaseJsonParser> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            LOGGER.error("创建解析器失败", e);
        }
        throw new BridgeException("创建解析器失败," + clazz.getName());
    }

    /**
     * @return
     */
    public static JsonParser getAccInfoParser() {
        return getParser(AccInfoParser.class);
    }

    /**
     * @return
     */
    public static JsonParser getInstanceParser() {
        return getParser(InstanceParser.class);
    }

    /**
     * @return
     */
    public static JsonParser getColumnParser() {
        return getParser(ColumnParser.class);
    }

    /**
     * @return
     */
    public static JsonParser getPartitionParser() {
        return getParser(PartitionParser.class);
    }

    /**
     * @return
     */
    public static JsonParser getProjectParser() {
        return getParser(ProjectParser.class);
    }

    /**
     * @return
     */
    public static JsonParser getResourceParser() {
        return getParser(ResourceParser.class);
    }

    /**
     * @return
     */
    public static JsonParser getTableParser() {
        return getParser(TableParser.class);
    }

    /**
     * @return
     */
    public static JsonParser getTaskParser() {
        return getParser(TaskParser.class);
    }

    /**
     * @return
     */
    public static JsonParser getPackageParser() {
        return getParser(PackageParser.class);
    }

    /**
     * @return
     */
    public static JsonParser getTablesParser() {
        return getParser(TablesParser.class);
    }

    /**
     * @return
     */
    public static JsonParser getSimpleTableParser() {
        return getParser(SimpleTableParser.class);
    }
}
