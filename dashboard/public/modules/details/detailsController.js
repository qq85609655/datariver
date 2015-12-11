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

angular.module('dgc.details').controller('DetailsController', ['$window', '$scope', '$state', '$stateParams', 'DetailsResource',
    function ($window, $scope, $state, $stateParams, DetailsResource) {

        $scope.tableName = false;
        $scope.qualifiedName = false;
        $scope.isHiveTable = false;
        $scope.isDataTable = false;
        $scope.isDataField = false;
        $scope.isStep = false;
        $scope.isAction = false;
        $scope.isTask = false;

        DetailsResource.get({
            id: $stateParams.id
        }, function (data) {
            $scope.details = data;
            $scope.schemas = data;
            $scope.tableName = data.values.name;
            $scope.qualifiedName = data.values.qualifiedName;
            $scope.configValue = data.values.config;
            $scope.isHiveTable = isEqualToType(data.typeName, 'table');
            $scope.isStep = isEqualToType(data.typeName, 'transstep') || isEqualToType(data.typeName, 'etlstep');
            $scope.isDataField = isEqualToType(data.typeName, 'datafield');
            $scope.isDataTable = isEqualToType(data.typeName, 'datatable') || isEqualToType(data.typeName, 'odpstable');
            $scope.isDataContainer = isEqualToType(data.typeName, 'datacontainer');
            $scope.isAction = isEqualToType(data.typeName, 'workflowaction');
            $scope.isTask = isEqualToType(data.typeName, 'etltask');
            $scope.isTags = (typeof data.traits !== 'undefined' && typeof data.traits === 'object') ? true : false;

            if ($scope.isHiveTable) {
                $scope.qualifiedName = $scope.tableName;
            }
        });

        $scope.isNumber = angular.isNumber;
        $scope.isObject = angular.isObject;
        $scope.isString = angular.isString;
        $scope.isArray = angular.isArray;

        $scope.goDetails = function(id){
            $state.go("details", {
                id: id
            });
        };

        $scope.isKeyTime = function(name){
            return endWith(name, 'time');
        };

        $scope.isKeyDependency = function(name){
            return isEqualToType(name, 'dependencyinfo');
        };

        //type must be in lower case
        function isEqualToType(name, type) {
            return (typeof name !== 'undefined' && name.toLowerCase() === type);
        }

        //endStr must be in lower case
        function endWith(name, endStr) {
            var lowerName = name.toLowerCase();
            var d = lowerName.length - endStr.length;
            return (d >= 0 && lowerName.lastIndexOf(endStr) === d);
        }

        $scope.goBack = function() {
            $window.history.back();
        };

        $scope.isXmlOrJson = function(xml){
            var sign = 0;
            var xmlSign = 2;
            var jsonSign = 1;
            //var startXmlStartIndex = 0;
            var startXmlEndIndex = xml.indexOf(">");
            if((xml.substr(0,1) === "<") && (startXmlEndIndex>1)){
                var startXmlStr = xml.substr(1,startXmlEndIndex);
                var endXmlStr = "</"+startXmlStr;
                if(xml.substr(xml.length-endXmlStr.length,xml.length)===endXmlStr){
                    sign+=xmlSign;
                    return sign;
                }
            }else if((xml.substr(0,1) === "{") || (xml.substr(xml.length-1,xml.length) === "}")){
                sign+=jsonSign;
                return sign;
            }
            return sign;
        };

        $scope.caculatHeight = function(xml){
            xml = $scope.formatXmlOrJson(xml);
            var b=xml.split("\n");
            var c=1;
            c+=b.length;
            return c;
        };

        $scope.formatJson = function(json, options) {
            var reg = null,
                formatted = '',
                pad = 0,
                PADDING = '   '; // one can also use '\t' or a different number of spaces
            // optional settings
            options = options || {};
            // remove newline where '{' or '[' follows ':'
            options.newlineAfterColonIfBeforeBraceOrBracket = (options.newlineAfterColonIfBeforeBraceOrBracket === true);
            // use a space after a colon
            options.spaceAfterColon = (options.spaceAfterColon !== false);
            // begin formatting...
            // make sure we start with the JSON as a string
            if (typeof json !== 'string') {
                json = JSON.stringify(json);
            }
            // parse and stringify in order to remove extra whitespace
            //json = JSON.parse(json);
            //json = JSON.stringify(json);
            // add newline before and after curly braces
            reg = /([\{\}])/g;
            json = json.replace(reg, '\r\n$1\r\n');
            // add newline before and after square brackets
            reg = /([\[\]])/g;
            json = json.replace(reg, '\r\n$1\r\n');
            // add newline after comma
            reg = /(\,)/g;
            json = json.replace(reg, '$1\r\n');
            // remove multiple newlines
            reg = /(\r\n\r\n)/g;
            json = json.replace(reg, '\r\n');
            // remove newlines before commas
            reg = /\r\n\,/g;
            json = json.replace(reg, ',');
            // optional formatting...
            if (!options.newlineAfterColonIfBeforeBraceOrBracket) {
                reg = /\:\r\n\{/g;
                json = json.replace(reg, ':{');
                reg = /\:\r\n\[/g;
                json = json.replace(reg, ':[');
            }
            if (options.spaceAfterColon) {
                reg = /\:/g;
                json = json.replace(reg, ':');
            }
            var jsonSplitArr = json.split('\r\n');
            for(var index=0;index<jsonSplitArr.length;index++){
                var node = jsonSplitArr[index];
                var i = 0,
                    indent = 0,
                    padding = '';
                if (node.match(/\{$/) || node.match(/\[$/)) {
                    indent = 1;
                } else if (node.match(/\}/) || node.match(/\]/)) {
                    if (pad !== 0) {
                        pad -= 1;
                    }
                } else {
                    indent = 0;
                }
                for (i = 0; i < pad; i++) {
                    padding += PADDING;
                }
                formatted += padding + node + '\r\n';
                pad += indent;
            }
            return formatted.replace(/(\r\n\r\n)/g, '\r\n').replace(/(^\r\n)|(\r\n$)/g,'');
        };

        $scope.formatXmlOrJson = function(inputStr){
            var sign = $scope.isXmlOrJson(inputStr);
            if(sign===1){
                return $scope.formatJson(inputStr);
            }else if(sign===2){
                return inputStr;
            }
            return inputStr;
        };
    }
]);
