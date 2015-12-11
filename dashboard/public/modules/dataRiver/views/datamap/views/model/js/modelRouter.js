/**
 * 描述信息
 *
 * @author XiangShaolong 0095
 * @version 1.0.0
 * @company DtDream
 * @date 2015/11/13 16:15
 */
'use strict';
angular.module('datamap.model').config(['$stateProvider',
    function($stateProvider) {
        $stateProvider
            .state('dataRiver.dataMap.model.convertPackage', {
                url: '/convertPackage',
                templateUrl: 'modules/dataRiver/views/datamap/views/model/views/convertPackage.html'
            });
    }
]);
