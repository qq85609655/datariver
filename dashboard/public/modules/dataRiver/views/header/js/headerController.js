/**
 * 描述信息
 *
 * @author XiangShaolong 0095
 * @version 1.0.0
 * @company DtDream
 * @date 2015/11/12 16:12
 */
'use strict';
angular.module('dataRiver.header')
    .controller('headerController', ['$scope', 'headerResource', function($scope, headerResource) {
        headerResource.getHeaderData({}, function(headerData) {
            $scope.headerData = headerData;
        });
    }]);
