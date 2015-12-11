/**
 * 描述信息
 *
 * @author XiangShaolong 0095
 * @version 1.0.0
 * @company DtDream
 * @date 2015/11/12 16:11
 */
'use strict';
angular.module('dataRiver.header')
    .factory('headerResource', function($resource) {
        return $resource('modules/dataRiver/views/header/json/header.json', {}, {
            getHeaderData: {
                'method': 'GET',
                'responseType': 'json',
                'transformResponse': function(headerData) {
                    return headerData;
                }
            }
        });
    });
