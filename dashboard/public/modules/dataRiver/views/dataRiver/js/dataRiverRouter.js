/**
 * 描述信息
 *
 * @author XiangShaolong 0095
 * @version 1.0.0
 * @company DtDream
 * @date 2015/11/12 20:24
 **/
"use strict";
angular.module('dataRiver')
    .config(['$stateProvider', function($stateProvider) {
        $stateProvider
            .state('dataRiver', {
                url: '/dataRiver',
                templateUrl: '/modules/dataRiver/dataRiver.html'
            })
            .state('dataRiver.dataManage', {
                url: '/dataManage',
                templateUrl: '/modules/dataRiver/views/dataManage/views/dataManage.html'
            })
            .state('dataRiver.dataMap', {
                url: '/dataMap',
                templateUrl: '/modules/dataRiver/views/datamap/views/dataMap.html'
            })
            .state('dataRiver.dataBusiness', {
                url: '/dataBusiness',
                templateUrl: '/modules/dataRiver/views/dataBusiness/views/dataBusiness.html'
            })
            .state('dataRiver.dataInterFlow', {
                url: '/dataInterFlow',
                templateUrl: '/modules/dataRiver/views/dataInterFlow/views/dataInterFlow.html'
            });
    }]);
