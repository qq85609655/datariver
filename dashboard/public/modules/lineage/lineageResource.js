/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

angular.module('dgc.lineage').factory('LineageResource', ['$resource', function ($resource) {
    return $resource('', {}, {
        queryHiveIO: {
            method: 'GET',
            url: '/api/atlas/lineage/hive/table/:qualifiedName/:type/graph',
            responseType: 'json'
        },
        queryDataTableIO: {
            method: 'GET',
            /* queryType支持step和task，表示的是查询的是task级别的还是step级别的血缘，下同 */
            url: '/api/atlas/lineage/:queryType/table/:qualifiedName/:type/graph',
            responseType: 'json'
        },
        queryDataContainerIO: {
            method: 'GET',
            url: '/api/atlas/lineage/:queryType/db/:qualifiedName/:type/graph',
            responseType: 'json'
        },
        queryDataFieldST: {
            method: 'GET',
            url: '/api/atlas/lineage/:queryType/field/:qualifiedName/:type/graph',
            responseType: 'json'
        },
        queryStepPS: {
            method: 'GET',
            /* queryType支持step、action和task，和上述三类不同，表示的就是查询的血缘的对象类型 */
            url: '/api/atlas/lineage/:queryType/:qualifiedName/:type/graph',
            responseType: 'json'
        }
    });
}]);
