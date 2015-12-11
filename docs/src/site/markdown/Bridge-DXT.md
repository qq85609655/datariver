<div id="category"></div>
# DTX Atlas Bridge

## DTX Model
The default dxt modelling is available in org.apache.atlas.dxt.model.DxtDataModelGenerator. 
It defines the following types:

* TransInstance(ClassType) - super types [ETLInstance] - attributes [lastExecuteTime, status, runTimes, successTimes, failTimes]
* TransStep(ClassType) - super types [ETLStep] - attributes [db]
* GeneralAccInfo(ClassType) - super types [DBAccess] - attributes [host, port, version]

## Importing DTX Metadata
`org.apache.atlas.dxt.bridge.DxtMetaStoreBridge` imports the dxt metadata into Atlas using the model defined in `org.apache.atlas.dxt.model.DxtDataModelGenerator`. 
`import-dxt.sh` command can be used to facilitate this.
Set-up the following configs in `dxt-site.xml` of your dxt set-up and set environment variable `DXT_CONFIG` to the dxt conf directory:
   
   * Atlas endpoint - Add the following property with the Atlas endpoint for your set-up
   
        <property>
          <name>atlas.rest.address</name>
          <value>http://localhost:21000/</value>
        </property>


Usage: `<atlas package>`/bin/import-dxt.sh. The logs are in `<atlas package>`/logs/import-dxt.log


## DTX Hook
DTX supports listeners on dxt command execution using dxt hooks. This is used to `add/update/remove` entities in Atlas using the model defined in `org.apache.atlas.dxt.model.DxtDataModelGenerator`.
The hook submits the request to a thread pool executor to avoid blocking the command execution. The thread submits the entities as message to the notification server and atlas server reads these messages and registers the entities.
Follow these instructions in your dxt set-up to add dxt hook for Atlas:
   * Set-up atlas hook and atlas endpoint in dxt-site.xml:

        <property>
          <name>dxt.exec.post.hooks</name>
          <value>org.apache.atlas.dxt.hook.DxtHook</value>
        </property>
        <property>
          <name>atlas.rest.address</name>
          <value>http://localhost:21000/</value>
        </property>

   * Add 'export DXT_AUX_JARS_PATH=`<atlas package>`/hook/dxt' in dxt-env.sh
   * Copy `<atlas package>`/conf/application.properties to dxt conf directory `<dxt package>`/conf

The following properties in dxt-site.xml control the thread pool and notification details:
   * atlas.hook.dxt.synchronous - boolean, true to run the hook synchronously. default false
   * atlas.hook.dxt.numRetries - number of retries for notification failure. default 3
   * atlas.hook.dxt.minThreads - core number of threads. default 5
   * atlas.hook.dxt.maxThreads - maximum number of threads. default 5
   * atlas.hook.dxt.keepAliveTime - keep alive time in msecs. default 10

Refer [Configuration](Configuration) for notification related configurations


## Limitations
   * Since database name, table name and column names are case insensitive in dxt, the corresponding names in entities are lowercase. So, any search APIs should use lowercase while querying on the entity names
   * Only the following dxt operations are captured by dxt hook currently - create database, create table, create view, CTAS, load, import, export, query, alter table rename and alter view rename
