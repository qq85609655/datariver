/**
 *
 * @author XiangShaolong 0095
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/4 9:43
 */

'use strict';
//Setting up route
angular.module('dgc.metamodel').config(['$stateProvider',
    function($stateProvider) {
        $stateProvider.state('metamodel', {
            url: '/metamodel',
            templateUrl: '/modules/metamodel/views/metamodel.html',
            controller: 'MetamodelController'
        });
    }
]);
