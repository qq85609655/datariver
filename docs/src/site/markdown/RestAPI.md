<div id="category"></div>
#异常返回

```
{
    "error": "query cannot be null cannot be empty",
    "stackTrace": 
        "java.lang.IllegalArgumentException: query cannot be null cannot be empty
            at org.apache.atlas.utils.ParamChecker.notEmptyIfNotNull(ParamChecker.java:115)
            at org.apache.atlas.utils.ParamChecker.notEmpty(ParamChecker.java:132)"
}
```

#部分已有接口

##实体管理接口

###通过实体类型或者实体类型及属性和属性值获取实体的定义信息

URL: /api/atlas/entities?type=xxx&property=xxx&value=xxx
参数约束：type不能为空，property/value可同时为空，property不为空时，value也不能为空
METHOD: GET
返回值：

```
{
    "requestId": "qtp1603497926-13 - 339e985d-c88b-407d-a91e-27ebe6882346",
    "typeName": "OdpsResource",
    "results": [
        "be3dcfcf-a79d-4733-b608-6834a022c364",
        "f09dbed3-d5e4-49de-9641-f28c73f67e50",
        "b2b4b2b6-e7da-4c22-a890-b1f09baba14d",
        "a9c5648a-333b-4833-9fa4-174f4ba23f48"
    ],
    "count": 4
}
```

###通过GUID获取实体的定义信息

URL: /api/atlas/entities/{guid}
参数约束：guid不能为空
METHOD: GET
返回值：

```
{
    "requestId": "qtp1603497926-16 - 94264eb4-691d-4cb8-914e-fcfc7d52889d",
    "GUID": "be3dcfcf-a79d-4733-b608-6834a022c364",
    "definition": {
        "jsonClass": "org.apache.atlas.typesystem.json.InstanceSerialization$_Reference",
        "id": {
            "jsonClass": "org.apache.atlas.typesystem.json.InstanceSerialization$_Id",
            "id": "be3dcfcf-a79d-4733-b608-6834a022c364",
            "version": 0,
            "typeName": "OdpsResource"
        },
        "typeName": "OdpsResource",
        "values": {
            "name": "aopalliance-1.0.jar",
            "lastModifiedTime": 1441070922000,
            "project": {
                "jsonClass": "org.apache.atlas.typesystem.json.InstanceSerialization$_Id",
                "id": "865990e3-ca93-4dea-8ae3-8cbc5284fb99",
                "version": 0,
                "typeName": "OdpsProject"
            },
            "createTime": 0,
            "description": null,
            "resourceType": {
                "value": "JAR",
                "ordinal": 2
            },
            "owner": "ALIYUN$xux@dtdream.com"
        },
        "traitNames": [
            "ODPS_OdpsResource"
        ],
        "traits": {
            "ODPS_OdpsResource": {
                "jsonClass": "org.apache.atlas.typesystem.json.InstanceSerialization$_Struct",
                "typeName": "ODPS_OdpsResource",
                "values": {}
            }
        }
    }
}
```

###为实体更新属性

URL: /api/atlas/entities/{guid}?property=xxx&value=xxx
参数约束：都不能为空
METHOD: PUT
返回值：

```
{
    "requestId": "qtp1603497926-13 - 339e985d-c88b-407d-a91e-27ebe6882346",
}
```

###根据实体GUID获取trait列表

URL: /api/atlas/entities/{guid}/traits
参数约束：都不能为空
METHOD: GET
返回值：

```
{
    "requestId": "qtp1603497926-16 - 359f5a71-a82a-4e1c-9f91-341ba89ac1ce",
    "GUID": "be3dcfcf-a79d-4733-b608-6834a022c364",
    "results": [
        "ODPS_OdpsResource"
    ],
    "count": 1
}
```

##类型管理接口

###根据类型名称获取类型定义信息

URL: /api/atlas/types/{typeName}
参数约束：都不能为空
METHOD: GET
返回值：

```
{
    "typeName": "OdpsResource",
    "definition": {
        "enumTypes": [],
        "structTypes": [],
        "traitTypes": [],
        "classTypes": [
            {
                "superTypes": [
                    "Referenceable"
                ],
                "hierarchicalMetaTypeName": "org.apache.atlas.typesystem.types.ClassType",
                "typeName": "OdpsResource",
                "attributeDefinitions": [
                    {
                        "name": "name",
                        "dataTypeName": "string",
                        "multiplicity": "required",
                        "isComposite": false,
                        "isUnique": false,
                        "isIndexable": true,
                        "reverseAttributeName": null
                    },
                    {
                        "name": "project",
                        "dataTypeName": "OdpsProject",
                        "multiplicity": "required",
                        "isComposite": false,
                        "isUnique": false,
                        "isIndexable": true,
                        "reverseAttributeName": null
                    },
                    {
                        "name": "owner",
                        "dataTypeName": "string",
                        "multiplicity": "optional",
                        "isComposite": false,
                        "isUnique": false,
                        "isIndexable": true,
                        "reverseAttributeName": null
                    },
                    {
                        "name": "createTime",
                        "dataTypeName": "long",
                        "multiplicity": "optional",
                        "isComposite": false,
                        "isUnique": false,
                        "isIndexable": true,
                        "reverseAttributeName": null
                    },
                    {
                        "name": "lastModifiedTime",
                        "dataTypeName": "long",
                        "multiplicity": "optional",
                        "isComposite": false,
                        "isUnique": false,
                        "isIndexable": true,
                        "reverseAttributeName": null
                    },
                    {
                        "name": "resourceType",
                        "dataTypeName": "OdpsResourceType",
                        "multiplicity": "optional",
                        "isComposite": false,
                        "isUnique": false,
                        "isIndexable": true,
                        "reverseAttributeName": null
                    },
                    {
                        "name": "description",
                        "dataTypeName": "string",
                        "multiplicity": "optional",
                        "isComposite": false,
                        "isUnique": false,
                        "isIndexable": true,
                        "reverseAttributeName": null
                    }
                ]
            }
        ]
    },
    "requestId": "qtp1603497926-17 - 415e0cd6-8a7a-4aa9-aa8b-c974cec2b6b1"
}
```

##搜索服务接口

###DSL查询接口

URL: /api/atlas/discovery/search/dsl?query=xxx
参数约束：都不能为空
METHOD: GET
返回值：

```
{
    "requestId": "qtp1603497926-15 - ce680f1e-912e-4f88-8881-9cfe25ca3149",
    "query": "OdpsResource",
    "queryType": "dsl",
    "count": 4,
    "results": [
        {
            "$typeName$": "OdpsResource",
            "$id$": {
                "id": "be3dcfcf-a79d-4733-b608-6834a022c364",
                "$typeName$": "OdpsResource",
                "version": 0
            },
            "lastModifiedTime": 1441070922000,
            "owner": "ALIYUN$xux@dtdream.com",
            "description": null,
            "resourceType": "JAR",
            "name": "aopalliance-1.0.jar",
            "createTime": 0,
            "project": {
                "id": "865990e3-ca93-4dea-8ae3-8cbc5284fb99",
                "$typeName$": "OdpsProject",
                "version": 0
            },
            "$traits$": {
                "ODPS_OdpsResource": {
                    "$typeName$": "ODPS_OdpsResource"
                }
            }
        }
    ],
    "dataType": {
        "superTypes": [
            "Referenceable"
        ],
        "hierarchicalMetaTypeName": "org.apache.atlas.typesystem.types.ClassType",
        "typeName": "OdpsResource",
        "attributeDefinitions": [
            {
                "name": "name",
                "dataTypeName": "string",
                "multiplicity": {
                    "lower": 1,
                    "upper": 1,
                    "isUnique": false
                },
                "isComposite": false,
                "isUnique": false,
                "isIndexable": true,
                "reverseAttributeName": null
            },
            {
                "name": "project",
                "dataTypeName": "OdpsProject",
                "multiplicity": {
                    "lower": 1,
                    "upper": 1,
                    "isUnique": false
                },
                "isComposite": false,
                "isUnique": false,
                "isIndexable": true,
                "reverseAttributeName": null
            },
            {
                "name": "owner",
                "dataTypeName": "string",
                "multiplicity": {
                    "lower": 0,
                    "upper": 1,
                    "isUnique": false
                },
                "isComposite": false,
                "isUnique": false,
                "isIndexable": true,
                "reverseAttributeName": null
            },
            {
                "name": "createTime",
                "dataTypeName": "long",
                "multiplicity": {
                    "lower": 0,
                    "upper": 1,
                    "isUnique": false
                },
                "isComposite": false,
                "isUnique": false,
                "isIndexable": true,
                "reverseAttributeName": null
            },
            {
                "name": "lastModifiedTime",
                "dataTypeName": "long",
                "multiplicity": {
                    "lower": 0,
                    "upper": 1,
                    "isUnique": false
                },
                "isComposite": false,
                "isUnique": false,
                "isIndexable": true,
                "reverseAttributeName": null
            },
            {
                "name": "resourceType",
                "dataTypeName": "OdpsResourceType",
                "multiplicity": {
                    "lower": 0,
                    "upper": 1,
                    "isUnique": false
                },
                "isComposite": false,
                "isUnique": false,
                "isIndexable": true,
                "reverseAttributeName": null
            },
            {
                "name": "description",
                "dataTypeName": "string",
                "multiplicity": {
                    "lower": 0,
                    "upper": 1,
                    "isUnique": false
                },
                "isComposite": false,
                "isUnique": false,
                "isIndexable": true,
                "reverseAttributeName": null
            }
        ]
    }
}
```

###gremlin查询接口
暂未成功执行


#数据地图

##获取所有组织及组织内包含的数据库名称、数据表数目
URL: /api/atlas/organizations/statistics
METHOD: GET
返回值：

```
{
    "requestId": "qtp1603497926-14 - d6af4252-05c4-4f37-b345-eea0f7c08ae5",
    "results": [
        {
            "name": "会计处",
            "GUID": "0c1e740e-2759-4b2b-bcac-6e0d83d8ca10",
            "tableCount": 20,
            "databases": [
                {
                    "name": "人力系统库",
                    "GUID": "0c1e740e-2759-4b2b-bcac-6e0d83d8ca10"
                },
                {
                    "name": "财务系统库",
                    "GUID": "0c1e740e-2759-4b2b-bcac-6e0d83d8ca10"
                }
            ]
        },
        {
            "name": "社会保障处",
            "GUID": "0c1e740e-2759-4b2b-bcac-6e0d83d8ca10",
            "tableCount": 20,
            "databases": [
                {
                    "name": "法人系统库",
                    "GUID": "0c1e740e-2759-4b2b-bcac-6e0d83d8ca10"
                },
                {
                    "name": "纳税系统库",
                    "GUID": "0c1e740e-2759-4b2b-bcac-6e0d83d8ca10"
                }
            ]
        }
    ]
}
```

##获取数据仓库信息,包含各类型数据库及具体数据库实例信息

URL: /api/atlas/datamap/warehouse
METHOD: GET
返回值：

```
{
    "requestId": "qtp1603497926-14 - d6af4252-05c4-4f37-b345-eea0f7c08ae5",
    "results": [
        {
            "name": "ODPS",
            "dbCount": 20,
            "databases": [
                {
                    "name": "人力系统库",
                    "GUID": "0c1e740e-2759-4b2b-bcac-6e0d83d8ca10"
                },
                {
                    "name": "财务系统库",
                    "GUID": "0c1e740e-2759-4b2b-bcac-6e0d83d8ca10"
                }
            ]
        },
        {
            "name": "RDS",
            "dbCount": 20,
            "databases": [
                {
                    "name": "法人系统库",
                    "GUID": "0c1e740e-2759-4b2b-bcac-6e0d83d8ca10"
                },
                {
                    "name": "纳税系统库",
                    "GUID": "0c1e740e-2759-4b2b-bcac-6e0d83d8ca10"
                }
            ]
        }
    ]
}
```


##查询指定时间的统计信息

URL: /api/atlas/datamap/statistics?startTime=2015-11-11&endTime=2015-11-17&time=week/month
参数约束：含有time参数时以time为准且只能取值week/month.endTime应>=startTime
METHOD: GET
返回值：

```
{
    "requestId": "qtp1603497926-14 - d6af4252-05c4-4f37-b345-eea0f7c08ae5",
    "database": {
        "outerCount": 20,
        "innerCount": 20
    },
    "table": {
        "innerCount": 10,
        "outerCount": 20
    },
    "field": {
        "innerCount": 10,
        "outerCount": 20
    },
    "organization": {
        "count": 10,
    },
    "job": {
        "templateCount": 10,
        "jobCount": 20
    },
    "resource": {
        "count": 20
    }
}
```

##根据guid列表批量获取实体信息

URL: /api/atlas/entities/definitions?guids=xxx,xxx
参数约束：guids不能为空，多个guid用逗号隔开
METHOD: GET
返回值：

```
{
    "requestId": "qtp1603497926-16 - 94264eb4-691d-4cb8-914e-fcfc7d52889d",
    "definitions": [{
        "jsonClass": "org.apache.atlas.typesystem.json.InstanceSerialization$_Reference",
        "id": {
            "jsonClass": "org.apache.atlas.typesystem.json.InstanceSerialization$_Id",
            "id": "be3dcfcf-a79d-4733-b608-6834a022c364",
            "version": 0,
            "typeName": "OdpsResource"
        },
        "typeName": "OdpsResource",
        "values": {
            "name": "aopalliance-1.0.jar",
            "lastModifiedTime": 1441070922000,
            "project": {
                "jsonClass": "org.apache.atlas.typesystem.json.InstanceSerialization$_Id",
                "id": "865990e3-ca93-4dea-8ae3-8cbc5284fb99",
                "version": 0,
                "typeName": "OdpsProject"
            },
            "createTime": 0,
            "description": null,
            "resourceType": {
                "value": "JAR",
                "ordinal": 2
            },
            "owner": "ALIYUN$xux@dtdream.com"
        },
        "traitNames": [
            "ODPS_OdpsResource"
        ],
        "traits": {
            "ODPS_OdpsResource": {
                "jsonClass": "org.apache.atlas.typesystem.json.InstanceSerialization$_Struct",
                "typeName": "ODPS_OdpsResource",
                "values": {}
            }
        }
    }]
}
```

##查询具有相同的property的实体

URL: /api/atlas/datamap/samePropertyEntities?typeName=xxx&propertyName=xxx&guid=xxx
参数约束：均不能为空
参考场景：查询某odpsproject所有的表，参数取：typeName=OdpsTable&propertyName=database&guid=odpsproject的guid
METHOD: GET
返回值：

```
{
    "requestId": "qtp1603497926-16 - 94264eb4-691d-4cb8-914e-fcfc7d52889d",
    "results": [{
        "jsonClass": "org.apache.atlas.typesystem.json.InstanceSerialization$_Reference",
        "id": {
            "jsonClass": "org.apache.atlas.typesystem.json.InstanceSerialization$_Id",
            "id": "be3dcfcf-a79d-4733-b608-6834a022c364",
            "version": 0,
            "typeName": "OdpsResource"
        },
        "typeName": "OdpsResource",
        "values": {
            "name": "aopalliance-1.0.jar",
            "lastModifiedTime": 1441070922000,
            "project": {
                "jsonClass": "org.apache.atlas.typesystem.json.InstanceSerialization$_Id",
                "id": "865990e3-ca93-4dea-8ae3-8cbc5284fb99",
                "version": 0,
                "typeName": "OdpsProject"
            },
            "createTime": 0,
            "description": null,
            "resourceType": {
                "value": "JAR",
                "ordinal": 2
            },
            "owner": "ALIYUN$xux@dtdream.com"
        },
        "traitNames": [
            "ODPS_OdpsResource"
        ],
        "traits": {
            "ODPS_OdpsResource": {
                "jsonClass": "org.apache.atlas.typesystem.json.InstanceSerialization$_Struct",
                "typeName": "ODPS_OdpsResource",
                "values": {}
            }
        }
    }]
}
```

##根据表/容器（库）的guid查询使用该表/容器的所有ETLTask

URL: /api/atlas/datamap/tasks?type=(table/container)&guid=xxx
参数约束：均不能为空
METHOD: GET
返回值：

```
{
    "requestId": "qtp1603497926-16 - 94264eb4-691d-4cb8-914e-fcfc7d52889d",
    "results": [{
        "jsonClass": "org.apache.atlas.typesystem.json.InstanceSerialization$_Reference",
        "id": {
            "jsonClass": "org.apache.atlas.typesystem.json.InstanceSerialization$_Id",
            "id": "be3dcfcf-a79d-4733-b608-6834a022c364",
            "version": 0,
            "typeName": "OdpsResource"
        },
        "typeName": "OdpsResource",
        "values": {
            "name": "aopalliance-1.0.jar",
            "lastModifiedTime": 1441070922000,
            "project": {
                "jsonClass": "org.apache.atlas.typesystem.json.InstanceSerialization$_Id",
                "id": "865990e3-ca93-4dea-8ae3-8cbc5284fb99",
                "version": 0,
                "typeName": "OdpsProject"
            },
            "createTime": 0,
            "description": null,
            "resourceType": {
                "value": "JAR",
                "ordinal": 2
            },
            "owner": "ALIYUN$xux@dtdream.com"
        },
        "traitNames": [
            "ODPS_OdpsResource"
        ],
        "traits": {
            "ODPS_OdpsResource": {
                "jsonClass": "org.apache.atlas.typesystem.json.InstanceSerialization$_Struct",
                "typeName": "ODPS_OdpsResource",
                "values": {}
            }
        }
    }]
}
```

##全文查询实体信息

URL: /api/atlas/datamap/fullTextSearch?types=xxx&query=xxx&startTime=2015-11-11&endTime=2015-11-12
参数约束：均不能为空,types为任意已有的实体所属的class类型，多个类型用逗号","隔开，支持父类型
时间为实体创建时间，结束时间应在创建时间之前
METHOD: GET
返回值：

```
{
    "requestId": "qtp1603497926-16 - 94264eb4-691d-4cb8-914e-fcfc7d52889d",
    "results": [{
        "jsonClass": "org.apache.atlas.typesystem.json.InstanceSerialization$_Reference",
        "id": {
            "jsonClass": "org.apache.atlas.typesystem.json.InstanceSerialization$_Id",
            "id": "be3dcfcf-a79d-4733-b608-6834a022c364",
            "version": 0,
            "typeName": "OdpsResource"
        },
        "typeName": "OdpsResource",
        "values": {
            "name": "aopalliance-1.0.jar",
            "lastModifiedTime": 1441070922000,
            "project": {
                "jsonClass": "org.apache.atlas.typesystem.json.InstanceSerialization$_Id",
                "id": "865990e3-ca93-4dea-8ae3-8cbc5284fb99",
                "version": 0,
                "typeName": "OdpsProject"
            },
            "createTime": 0,
            "description": null,
            "resourceType": {
                "value": "JAR",
                "ordinal": 2
            },
            "owner": "ALIYUN$xux@dtdream.com"
        },
        "traitNames": [
            "ODPS_OdpsResource"
        ],
        "traits": {
            "ODPS_OdpsResource": {
                "jsonClass": "org.apache.atlas.typesystem.json.InstanceSerialization$_Struct",
                "typeName": "ODPS_OdpsResource",
                "values": {}
            }
        }
    }]
}
```


##查询最近某段时间创建的实体信息
URL: /api/atlas/datamap/recently?name=xxx&&startTime=2015-11-11&endTime=2015-11-12
参数约束：startTime/endTime不能为空,name可为空
 时间为实体创建时间，结束时间应在创建时间之前
METHOD: GET
返回值：

```
{
    "requestId": "qtp1603497926-16 - 94264eb4-691d-4cb8-914e-fcfc7d52889d",
    "results": [{
        "jsonClass": "org.apache.atlas.typesystem.json.InstanceSerialization$_Reference",
        "id": {
            "jsonClass": "org.apache.atlas.typesystem.json.InstanceSerialization$_Id",
            "id": "be3dcfcf-a79d-4733-b608-6834a022c364",
            "version": 0,
            "typeName": "OdpsResource"
        },
        "typeName": "OdpsResource",
        "values": {
            "name": "aopalliance-1.0.jar",
            "lastModifiedTime": 1441070922000,
            "project": {
                "jsonClass": "org.apache.atlas.typesystem.json.InstanceSerialization$_Id",
                "id": "865990e3-ca93-4dea-8ae3-8cbc5284fb99",
                "version": 0,
                "typeName": "OdpsProject"
            },
            "createTime": 0,
            "description": null,
            "resourceType": {
                "value": "JAR",
                "ordinal": 2
            },
            "owner": "ALIYUN$xux@dtdream.com"
        },
        "traitNames": [
            "ODPS_OdpsResource"
        ],
        "traits": {
            "ODPS_OdpsResource": {
                "jsonClass": "org.apache.atlas.typesystem.json.InstanceSerialization$_Struct",
                "typeName": "ODPS_OdpsResource",
                "values": {}
            }
        }
    }]
}
```



