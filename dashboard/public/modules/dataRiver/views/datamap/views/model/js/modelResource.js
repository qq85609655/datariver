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
    .factory('modelResource', function($resource) {
        return $resource('modules/dataRiver/views/datamap/views/model/json/model.json', {}, {
            getModelData: {
                'method': 'GET',
                'responseType': 'json',
                'transformResponse': function(modelData) {
                    return modelData;
                }
            }
        });
    });
