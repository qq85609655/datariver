/**
 * 描述信息
 *
 * @author XiangShaolong 0095
 * @version 1.0.0
 * @company DtDream
 * @date 2015/11/12 21:12
 */
'use strict';
angular.module('dataRiver.datamap')
    .factory('dataMapResource', function($resource) {
        return $resource('modules/dataRiver/views/datamap/json/dataMap.json', {}, {
            getDataMapData: {
                'method': 'GET',
                'responseType': 'json',
                'transformResponse': function(dataMapData) {
                    return dataMapData;
                }
            }
        });
    });
