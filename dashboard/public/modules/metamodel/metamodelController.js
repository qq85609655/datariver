/**
*
* @author XiangShaolong 0095
* @version 1.0.0
* @company DTDream
* @date 2015/11/4 9:43
*/

'use strict';

angular.module('dgc.metamodel').controller('MetamodelController', ['$window', '$scope', '$http', 'EdgesResource', 'VerticesResource', 'd3',
    function($window, $scope, $http, EdgesResource, VerticesResource, d3) {
        /* 获取所有边数据*/
        function getEdgesAndVertices(){
            EdgesResource.getedges({}, function (edges) {
                combineLinks(edges);
            });
        }
        /* 获取所有节点数据 并换算出绘图所需要的Links以及Nodes数据*/
        function combineLinks(edges){
            VerticesResource.getvertices({}, function (vertices) {
                var links = [];
                var nodes = {};
                for (var i=0; i < edges.count; i++) {
                    for(var j=0; j < vertices.count; j++){
                        if(edges.results[i]._inV === vertices.results[j]._id){
                            links.push({source: "", target: "", edgeId: "",
                                sourceId: "", targetId: "", sourceType: "", targetType: "", label: "",
                                type: "licensing"});
                            links[i].label = edges.results[i]._label;
                            links[i].source = vertices.results[j]['__type.name'] || vertices.results[j].__typeName;
                            links[i].edgeId = edges.results[i]._id;
                            links[i].sourceId = vertices.results[j]._id;
                            links[i].sourceType = vertices.results[j]._type;
                            for(var k=0; k < vertices.count; k++){
                                if(edges.results[i]._outV === vertices.results[k]._id){
                                    links[i].target = vertices.results[k]['__type.name'] || vertices.results[k].__typeName;
                                    links[i].targetId = vertices.results[k]._id;
                                    links[i].targetType = vertices.results[k]._type;
                                }
                            }
                        }
                    }
                }

                links.forEach(function(link) {
                    link.source = nodes[link.source] ||
                        (nodes[link.source] = {name: link.source, nodeId: link.sourceId, type: link.sourceType});
                    link.target = nodes[link.target] ||
                        (nodes[link.target] = {name: link.target, nodeId: link.targetId, type: link.targetType});
                });
                drawFDG(links, nodes);
            });
        }

        /**
         * 绘制图形方法
         * */
        function drawFDG(links, nodes){
            /* 定义SVG画布*/
            var svgWidth = 1140;
            var svgHeight = 680;
            var svg = d3.select("#drawFDG").append("svg")
                .attr("width", svgWidth)
                .attr("height", svgHeight);

            /* 使用椭圆弧路径段双向编码。*/
            function tick() {
                /* 打点path格式是：Msource.x,source.yArr00,1target.x,target.y*/
                edgesPath.attr("d", function(d) {
                    var dx = d.target.x - d.source.x,//增量
                        dy = d.target.y - d.source.y,
                        dr = Math.sqrt(dx * dx + dy * dy);

                    return "M" + d.source.x + "," + d.source.y + "A" + dr + "," + dr + " 0 0,1 " + d.target.x + "," + d.target.y;
                });
                /* 实时刷新节点位置*/
                nodesCircle.attr("transform", function(d) {
                    return "translate(" + d.x + "," + d.y + ")";
                });
                /* 实时刷新节点文字*/
                nodesText.attr("transform", function(d) {
                    return "translate(" + d.x + "," + d.y + ")";
                });
            }

            /* 力导向图布局*/
            var forceLayout = d3.layout.force()
                .nodes(d3.values(nodes))        /* 设定力导向图布局的节点数组*/
                .links(links)                   /* 设定力导向图布局的连线数组*/
                .size([svgWidth, svgHeight])    /* 设定力导向图的作用范围*/
                .linkDistance(80)              /* 设定连线的距离*/
                //.linkStrength(0.5)
                .theta(0.1)
                //.alpha(0.2)
                .charge(-200)                   /* 设定力导向图的电荷数，值为负，互相排斥*/
                //.friction(0.5)                  /* 设定力导向图的摩擦系数*/
                .on("tick", tick)
                .start();

            /* 创建连线的箭头标记*/
            svg.append("defs").selectAll("marker")
                .data(["suit", "licensing", "resolved"])    /* 可以有多种样式*/
                .enter()
                .append("marker")
                .attr("id", String)                 /* 会把“data”中定义的样式应用到“id”*/
                .attr("markerUnits", "userSpaceOnUse")
                .attr("viewBox", "0 0 12 12")
                .attr("refX", 16)
                .attr("refY", 6)
                .attr("markerWidth", 18)
                .attr("markerHeight", 18)
                .attr("orient", "auto")
                .append("path")                 /* 利用path创建箭头标记*/
                .attr("d", "M2,2 L10,6 L2,10 L5,6 L2,2");
                //.style("fill", "blue");

            /* 根据连线类型引用上面创建的标记*/
            var edgesPath = svg.append("g").selectAll("path")
                .data(forceLayout.links())
                .enter()
                .append("path")
                .attr("id", function(d){
                    return d.edgeId;
                })
                .attr("class", "edgePath") //function(d) { return "link " + d.type; })
                .attr("marker-end", function(d) { return "url(#" + d.type + ")"; });

            /* 添加边的提示文字*/
            var edgesText = svg.append("g").selectAll(".lineText")
                .data(forceLayout.links())
                .enter()
                .append("text")
                .attr("x", 20)
                .attr("class","lineText")
                .append('textPath').attr({
                    'xlink:href': function(d){
                        return "#" + d.edgeId;
                    }
                })
                .attr("class", "lineTextClass")
                .text(function (d) {
                    return d.label;
                });

            /* 定义节点颜色*/

            /* 绘制节点*/
            var nodesCircle = svg.append("g").selectAll(".nodesCircle")
                .data(forceLayout.nodes())
                .enter()
                .append("g")
                .attr("class", "nodesCircle")
                /* 定义鼠标移动到节点上时的动作*/
                .on("mouseover", function(d){
                    /* 显示连接线上的文字*/
                    edgesText.style("fill-opacity",function(edge){
                        if( edge.source === d || edge.target === d ){
                            return 1.0;
                        }
                    });

                    /* 选定的节点放大*/
                    d3.select(this).select("circle").transition()
                        .duration(350)
                        .attr("r", function(){  //设置圆点半径
                            return 15;
                        });
                })
                /* 定义鼠标移出节点时的动作*/
                .on("mouseout",function(d){
                    /* 隐去连接线上的文字*/
                    edgesText.style("fill-opacity",function(edge){
                        if( edge.source === d || edge.target === d ){
                            return 0.0;
                        }
                    });
                    /* 选定的节点还原*/
                    d3.select(this).select("circle").transition()
                        .duration(350)
                        .attr("r", function(){  //设置圆点半径
                            return 8;
                        }) ;
                })
                /* 用圆形作为节点*/
                .append("circle")
                .attr("r", 8)
                .call(forceLayout.drag);

            /* 双击解除节点移动后的锁定*/
            nodesCircle.on("dblclick", function(d){
                d3.select(this).classed("fixed", d.fixed = false);
            });

            /* 鼠标移动到节点上时显示节点的详细信息*/
            nodesCircle.append("title")
                .text(function(d) {
                    return ("node-name: " + d.name + "\nnode-id: " + d.nodeId + "\ntype: " + d.type);
                });

            /* 拖拽开始后设定被拖拽对象为固定*/
            forceLayout.drag()
                .on("dragstart", function(d){
                    d.fixed = true;
                });

            /* 添加节点文字*/
            var nodesText = svg.append("g").selectAll(".nodesText")
                .data(forceLayout.nodes())
                .enter()
                .append("text")
                .attr("class", "nodesText")
                .attr("x", 18)
                .attr("y", "0.31em")
                .text(function(d) { return d.name; });


        }

        getEdgesAndVertices();

        $scope.goBack = function () {
            $window.history.back();
        };
    }
]);





