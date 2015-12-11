/**
 *
 * @author XiangShaolong 0095
 * @version 1.0.0
 * @company DTDream
 * @date 2015/11/4 9:43
 */

'use strict';

angular.module('dgc.metamodel')
    .factory('EdgesResource', ['$resource', function($resource) {
        return $resource('/api/atlas/graph/edges-all', {}, {
            getedges: {
                'method': 'GET',
                'responseType': 'json',
                'transformResponse': function(edges) {
                    return edges;
                }
            }
        });
    }])

    .factory('VerticesResource', ['$resource', function($resource) {
        return $resource('/api/atlas/graph/vertices-all', {}, {
            getvertices: {
                'method': 'GET',
                'responseType': 'json',
                'transformResponse': function(vertices) {
                    return vertices;
                }
            }
        });
    }]);
