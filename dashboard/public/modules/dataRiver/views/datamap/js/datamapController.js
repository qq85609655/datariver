/**
 * 描述信息
 *
 * @author XiangShaolong 0095
 * @version 1.0.0
 * @company DtDream
 * @date 2015/11/12 19:33
 */
'use strict';
angular.module('dataRiver.datamap')
    .controller('datamapController', ['$scope', 'dataMapResource', function($scope, dataMapResource) {
        $scope.checkboxModel = {
            upOrDown: "下钻"
        };
        dataMapResource.getDataMapData({}, function(dataMapData) {
            $scope.dataMapData = dataMapData;
        });
    }]);
