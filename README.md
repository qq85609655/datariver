# 元数据帮助文档

## Atlas的安装请参考
http://confluence.dtdream.com/pages/viewpage.action?pageId=16943086

## 如何导入Hive的元模型
运行下面命令导入。
 
    ./bin/testhive.sh 

## 如何导入DXT的元模型
运行下面命令导入。
 
    ./bin/testdxt.sh 
## 如何导入WORKFLOW的元模型
运行下面命令导入。
    ./bin/testworkflow.sh
    
## 如何导入COMMON的元模型
运行下面命令导入。
    ./bin/testcommon.sh    
    
## 获取图的所有边

    [root@c6501 atlas]# curl -s http://localhost:21000/api/atlas/graph/edges-all | python -mjson.tool

## 获取图的所有顶点

    [root@c6501 atlas]# curl -s http://localhost:21000/api/atlas/graph/vertices-all | python -mjson.tool

## 如何调试Atlas
http://confluence.dtdream.com/pages/viewpage.action?pageId=16943098
