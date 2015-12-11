/**
 * 描述信息
 *
 * @author XiangShaolong 0095
 * @version 1.0.0
 * @company DtDream
 * @date 2015/11/13 13:56
 */
'use strict';
angular.module('datamap.statistic')
    .factory('statisticResource', function($resource) {
        return $resource('modules/dataRiver/views/datamap/views/statistic/json/statistic.json', {}, {
            getStatisticResData: {
                'method': 'GET',
                'responseType': 'json',
                'transformResponse': function(statisticResData) {
                    return statisticResData;
                }
            }
        });
    });
