/**
 * 描述信息
 *
 * @author XiangShaolong 0095
 * @version 1.0.0
 * @company DtDream
 * @date 2015/11/13 13:55
 */
'use strict';
angular.module('datamap.statistic')
    .controller('statisticController', ['$window', '$scope', 'statisticResource', function($window, $scope, statisticResource) {
        statisticResource.getStatisticResData({}, function(statisticResData) {
            $scope.statisticResData = statisticResData;
        });
    }]);
