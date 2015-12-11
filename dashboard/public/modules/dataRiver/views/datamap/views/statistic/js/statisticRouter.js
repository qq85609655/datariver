/**
 * 描述信息
 *
 * @author XiangShaolong 0095
 * @version 1.0.0
 * @company DtDream
 * @date 2015/11/13 13:55
 */
'use strict';
angular.module('datamap.statistic').config(['$stateProvider',
    function($stateProvider) {
        $stateProvider
        /* 统计--数据库*/
            .state('dataRiver.dataMap.statistic.database', {
                url: '/database',
                templateUrl: 'modules/dataRiver/views/datamap/views/statistic/views/database.html'
            })
            /* 统计--数据库人员信息*/
            .state('dataRiver.dataMap.statistic.database.pepInfo', {
                url: '/pepInfo',
                templateUrl: 'modules/dataRiver/views/datamap/views/statistic/views/pepInfo/pepInfo.html'
            })
            /* 人员信息库下子菜单*/
            .state('dataRiver.dataMap.statistic.database.pepInfo.linkInfo', {
                url: '/linkInfo',
                templateUrl: 'modules/dataRiver/views/datamap/views/statistic/views/pepInfo/linkInfo.html'
            })
            .state('dataRiver.dataMap.statistic.database.pepInfo.dataTable', {
                url: '/dataTable',
                templateUrl: 'modules/dataRiver/views/datamap/views/statistic/views/pepInfo/dataTable.html'
            })
            .state('dataRiver.dataMap.statistic.database.pepInfo.lineageInfo', {
                url: '/lineageInfo',
                templateUrl: 'modules/dataRiver/views/datamap/views/statistic/views/pepInfo/lineageInfo.html'
            })
            .state('dataRiver.dataMap.statistic.database.pepInfo.resourceCollect', {
                url: '/resourceCollect',
                templateUrl: 'modules/dataRiver/views/datamap/views/statistic/views/pepInfo/resourceCollect.html'
            })
            .state('dataRiver.dataMap.statistic.database.pepInfo.statisticInfo', {
                url: '/statisticInfo',
                templateUrl: 'modules/dataRiver/views/datamap/views/statistic/views/pepInfo/statisticInfo.html'
            })
            .state('dataRiver.dataMap.statistic.database.pepInfo.taskInfo', {
                url: '/taskInfo',
                templateUrl: 'modules/dataRiver/views/datamap/views/statistic/views/pepInfo/taskInfo.html'
            })
            /* 数据表*/
            .state('dataRiver.dataMap.statistic.dataTable', {
                url: '/dataTable',
                templateUrl: 'modules/dataRiver/views/datamap/views/statistic/views/dataTable.html'
            })
            /* 字段*/
            .state('dataRiver.dataMap.statistic.wordSeg', {
                url: '/wordSeg',
                templateUrl: 'modules/dataRiver/views/datamap/views/statistic/views/wordSeg.html'
            })
            /* 组织*/
            .state('dataRiver.dataMap.statistic.organization', {
                url: '/organization',
                templateUrl: 'modules/dataRiver/views/datamap/views/statistic/views/organization.html'
            })
            /* 任务*/
            .state('dataRiver.dataMap.statistic.task', {
                url: '/task',
                templateUrl: 'modules/dataRiver/views/datamap/views/statistic/views/task.html'
            })
            /* 资源*/
            .state('dataRiver.dataMap.statistic.resource', {
                url: '/resource',
                templateUrl: 'modules/dataRiver/views/datamap/views/statistic/views/resource.html'
            });
    }
]);
