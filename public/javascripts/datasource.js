'use strict';

angular.module('myApp.datasource', ['ngResource']).
    factory('Ticket', function($resource) {
      var Ticket = $resource('https://api.mongolab.com/api/1/databases' +
          '/housedepot/collections/tickets/:id',
          { apiKey: 'uutoSVz--P6D2HHgcwz81R9TqVDssh8K' }, 
          {
            update: { method: 'PUT' }
          }
      );
 
      Ticket.prototype.create = function(cb) {
        return Ticket.update({id: this._id.$oid},
            angular.extend({}, this, {_id:undefined}), cb);
      };
 
      Ticket.prototype.update = function(cb) {
        return Ticket.update({id: this._id.$oid},
            angular.extend({}, this, {_id:undefined}), cb);
      };
 
      Ticket.prototype.delete = function(cb) {
        return Ticket.remove({id: this._id.$oid}, cb);
      };
 
      return Ticket;
    }).
    factory('Lookup', function($http) {
      var Lookup = {
        "statues": ["Open", "In progress", "Completed", "Tested", "Reopen", "Won't fix", "Canceled", "Closed"],
        "types": ["Defect", "Change Request"],
        "roles": ["Developer", "QA", "User"],
        "severities": ["Minor", "Normal", "Critical"],
        "priorities": ["Low", "Normal", "High", "Urgent"]};

      $http.get('/api/user').
        success(function(data, status, headers, config) {
          Lookup.assignees = data;
        });

      return Lookup;
    });