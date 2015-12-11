/**
 * 描述信息
 *
 * @author XiangShaolong 0095
 * @version 1.0.0
 * @company DtDream
 * @date 2015/11/13 16:15
 */
'use strict';
angular.module('datamap.model')
    .controller('modelController', ['$scope', 'modelResource', function($scope, modelResource) {
        modelResource.getModelData({}, function(modelData) {
            $scope.modelData = modelData;
        });
    }]);
