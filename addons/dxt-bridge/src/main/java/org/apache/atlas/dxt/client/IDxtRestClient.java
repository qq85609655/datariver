package org.apache.atlas.dxt.client;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.mime.TypedString;

/**
 * @author YiSheng 0381
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/4 9:43
 */
public interface IDxtRestClient {
    /**
     * 为用户名为username的用户添加APP中操作Dxt的权限.
     *
     * @param userName 用户名
     * @param authList 权限列表，目前支持的权限有：Dxt-switch
     * @return
     */
    @POST("/api/v1/authorize/{username}")
    DxtRespBean authUser(@Path("username") String userName, TypedString authList);

    /**
     * 获取支持的数据库连接类型.
     *
     * @return dxt respone body
     */
    @GET("/api/v1/db/types")
    String[] getSupportedDBTypes();

    /**
     * 获取支持的hadoop版本.
     *
     * @return dxt respone body
     */
    @GET("/api/v1/hdfs/versions")
    String[] getSupportedHadoopVersions();

    /* 数据库连接 */

    /**
     * 创建一条数据库连接.
     *
     * @param connectionConf 数据库连接配置字符串
     * @return dxt respone body
     */
    @POST("/api/v1/connections")
    DxtRespBean newDBConnection(@Body TypedString connectionConf);

    /**
     * 修改一条数据库连接.
     *
     * @param connectionConf 数据库连接配置字符串
     * @return dxt respone body
     */
    @PUT("/api/v1/connections")
    DxtRespBean updateDBConnection(@Body TypedString connectionConf);

    /**
     * 查询所有数据库连接.
     *
     * @return dxt respone body
     */
    @GET("/api/v1/connections")
    DxtRespBean queryAllDBConnections();

    /**
     * 根据类型查询数据库连接.
     *
     * @return dxt respone body
     */
    @GET("/api/v1/connections/type/{type}")
    DxtRespBean queryDBConnectionsByType(@Path("type") String type);

    /**
     * 查询一条数据库连接.
     *
     * @param uid 数据库连接uid
     * @return dxt respone body
     */
    @GET("/api/v1/connections/{uid}")
    DxtRespBean queryDBConnection(@Path("uid") String uid);

    /**
     * 删除一条数据库连接.
     *
     * @param uid 数据库连接uid
     * @return dxt respone body
     */
    @DELETE("/api/v1/connections/{uid}")
    DxtRespBean deleteDBConnection(@Path("uid") String uid);

    /**
     * 检测一条数据库连接能否连通.
     *
     * @param uid 数据库连接uid
     * @return dxt respone body
     */
    @POST("/api/v1/connections/detect/{uid}")
    DxtRespBean detectDBConnection(@Path("uid") String uid, @Body String body);

    /**
     * 检测一条数据库连接能否连通.
     *
     * @param body 数据库连接配置
     * @return dxt respone body
     */
    @POST("/api/v1/connections/detectWhenModify")
    DxtRespBean detectDBConnectionWhenModify(@Body TypedString body);

    /**
     * 获取一条数据库连接对应的数据库所有表.
     *
     * @param uid 数据库连接uid
     * @return dxt respone body
     */
    @GET("/api/v1/db/table/description/{uid}")
    DxtRespBean getDBTables(@Path("uid") String uid);

    /**
     * 获取一条数据库连接对应的数据库一个表的所有列.
     *
     * @param uid       数据库连接uid
     * @param tableName 数据库表名
     * @return dxt respone body
     */
    @GET("/api/v1/db/table/column/description/{uid}/{tableName}")
    DxtRespBean getDBTableColumns(@Path("uid") String uid, @Path("tableName") String tableName);

    /* HDFS连接 */

    /**
     * 创建一条HDFS连接.
     *
     * @param hdfsConf hdfs连接配置
     * @return dxt respone body
     */
    @POST("/api/v1/hdfs/connections")
    DxtRespBean newHdfsConnection(@Body TypedString hdfsConf);

    /**
     * 修改一条HDFS连接.
     *
     * @param hdfsConf hdfs连接配置
     * @return dxt respone body
     */
    @PUT("/api/v1/hdfs/connections")
    DxtRespBean updateHdfsConnection(@Body TypedString hdfsConf);

    /**
     * 查询所有HDFS连接.
     *
     * @return dxt respone body
     */
    @GET("/api/v1/hdfs/connections")
    DxtRespBean queryAllHdfsConnections();

    /**
     * 查询一条HDFS连接.
     *
     * @param uid HDFS连接uid
     * @return dxt respone body
     */
    @GET("/api/v1/hdfs/connections/{uid}")
    DxtRespBean queryHdfsConnection(@Path("uid") String uid);

    /**
     * 删除一条HDFS连接.
     *
     * @param uid HDFS连接uid
     * @return dxt respone body
     */
    @DELETE("/api/v1/hdfs/connections/{uid}")
    DxtRespBean deleteHdfsConnection(@Path("uid") String uid);

    /**
     * 检测一条HDFS连接能否连通.
     *
     * @param uid HDFS连接uid
     * @return dxt respone body
     */
    @POST("/api/v1/hdfs/connections/detect/{uid}")
    DxtRespBean detectHdfsConnection(@Path("uid") String uid, @Body String body);

    /**
     * 在未保存的情况下检测一条HDFS连接能否连通.
     *
     * @param hdfsConf hdfs连接配置
     * @return dxt respone body
     */
    @POST("/api/v1/hdfs/connections/detectWhenModify")
    DxtRespBean detectHdfsConnection(@Body TypedString hdfsConf);

    /**
     * 获取一条HDFS连接对应的文件的所有字段.
     *
     * @param uid HDFS连接uid
     * @return dxt respone body
     */
    @POST("/api/v1/hdfs/fields/{uid}")
    DxtRespBean getHdfsFields(@Path("uid") String uid, @Body TypedString body);

    /* 转换任务 */

    /**
     * 创建一个转换任务.
     *
     * @param transConf 转换任务配置
     * @return dxt respone body
     */
    @POST("/api/v1/trans")
    DxtRespBean createTrans(@Body TypedString transConf);

    /**
     * 修改一个转换任务.
     *
     * @param transConf 转换任务配置
     * @return dxt respone body
     */
    @PUT("/api/v1/trans")
    DxtRespBean updateTrans(@Body TypedString transConf);

    /**
     * 删除一个转换任务.
     *
     * @param uid 转换任务ID
     * @return dxt respone body
     */
    @DELETE("/api/v1/trans/{uid}")
    DxtRespBean deleteTrans(@Path("uid") String uid);

    /**
     * 获取一个转换任务信息.
     *
     * @param uid 转换任务ID
     * @return dxt respone body
     */
    @GET("/api/v1/trans/{uid}")
    DxtRespBean queryTrans(@Path("uid") String uid);

    /**
     * 获取所有的转换任务信息.
     *
     * @return dxt respone body
     */
    @GET("/api/v1/trans")
    DxtRespBean queryAllTrans();

    /**
     * 执行一个转换任务.
     *
     * @param uid 转换任务ID
     * @return dxt respone body
     */
    @POST("/api/v1/trans/run/{uid}")
    DxtRespBean executeTrans(@Path("uid") String uid, @Body String body);

    /**
     * 终止一个转换任务.
     *
     * @param uid 转换任务ID
     * @return dxt respone body
     */
    @POST("/api/v1/trans/stop/{uid}")
    DxtRespBean terminateTrans(@Path("uid") String uid, @Body String body);

    /**
     * 获取正在运行的转换任务的统计信息.
     *
     * @param uid 转换任务ID
     * @return dxt respone body
     */
    @GET("/api/v1/trans/statistics/running/{uid}")
    DxtRespBean queryRunningTransStatistics(@Path("uid") String uid);

    /**
     * 获取一个转换任务的历史统计信息.
     *
     * @param uid 转换任务ID
     * @return dxt respone body
     */
    @GET("/api/v1/trans/statistics/{uid}")
    DxtRespBean queryHisTransStatistics(@Path("uid") String uid);

    /**
     * 获取一个转换任务的历史统计信息，带实例号查询.
     *
     * @param uid        转换任务ID
     * @param instanceId 实例ID
     * @return dxt respone body
     */
    @GET("/api/v1/trans/statistics/{uid}/{instanceid}")
    DxtRespBean queryHisTransStatistics(@Path("uid") String uid, @Path("instanceid") String instanceId);

    /**
     * 获取转换任务实例运行状态.
     *
     * @param uid
     * @param instanceId
     * @return
     */
    @GET("/api/v1/trans/status/{uid}/{instanceid}")
    DxtRespBean getTransStatus(@Path("uid") String uid, @Path("instanceid") String instanceId);
}
