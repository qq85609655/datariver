/**
 * 描述信息
 *
 * @author XiangShaolong 0095
 * @version 1.0.0
 * @company DtDream
 * @date 2015/11/12 20:55
 */
'use strict';
angular.module('dataRiver.datamap').config(['$stateProvider',
    function($stateProvider) {
        $stateProvider
            .state('dataRiver.dataMap.finance', {
                url: '/finance',
                templateUrl: 'modules/dataRiver/views/datamap/views/finance.html'
            })
            .state('dataRiver.dataMap.dynamic', {
                url: '/dynamic',
                templateUrl: 'modules/dataRiver/views/datamap/views/dynamic/views/dynamic.html'
            })
            .state('dataRiver.dataMap.statistic', {
                url: '/statistic',
                templateUrl: 'modules/dataRiver/views/datamap/views/statistic/views/statistic.html'
            })
            .state('dataRiver.dataMap.search', {
                url: '/search',
                templateUrl: 'modules/dataRiver/views/datamap/views/search/views/search.html'
            })
            .state('dataRiver.dataMap.latest', {
                url: '/latest',
                templateUrl: 'modules/dataRiver/views/datamap/views/latest/views/latest.html'
            })
            .state('dataRiver.dataMap.model', {
                url: '/model',
                templateUrl: 'modules/dataRiver/views/datamap/views/model/views/model.html'
            });
    }
]);
