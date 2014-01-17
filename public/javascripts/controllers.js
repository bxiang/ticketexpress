'use strict';

function ListCtrl($scope, $http) {
  var successCallback = function(data, status, headers, config) {
    $scope.tickets = data.tickets;
    $scope.noOfPages = data.totalPages;
    $scope.maxSize = data.totalPages > 5 ? 5 : data.totalPages;
  };

  $http.get('/api/ticket').success(successCallback);

  $scope.currentPage = 1;
  
  $scope.viewPage = function (page) {
    $http.get('/api/ticket?p=' + page).success(successCallback);
  };
}
 
function ViewCtrl($scope, $routeParams, $http, Lookup) {
  $scope.lookup = Lookup;
  $scope.ticket = {};

  $scope.openCommentDialog = function () {
    $scope.commentDialogOpen = true;
  };

  $scope.closeCommentDialog = function () {
    $scope.commentDialogOpen = false;
  };

  $scope.commentDialogOptions = {
    backdropFade: true,
    dialogFade: false
  };

  $scope.addComment = function() {
    $scope.commentDialogOpen = false;
    var log = {
      "log": $scope.comment,
      "user": "Brian Xiang"
    };
    $scope.ticket.histories.push(log);
    $http.put('/api/ticket/' + $routeParams.ticketId, $scope.ticket).
      success(function(data) {
        $scope.comment='';
      });
  };

  $http.get('/api/ticket/' + $routeParams.ticketId).
    success(function(data) {
      $scope.ticket = data;
    });
}

function CreateCtrl($scope, $location, $http, Lookup) {
  $scope.lookup = Lookup;
  $scope.ticket = {
    "status": "Open",
    "ticketType": "Defect",
    "priority": "Normal",
    "severity": "Normal",
    "progress": 0,
    "detail": "",
    "histories": []
  };

  $scope.save = function() {
    $http.post('/api/ticket', $scope.ticket).
      success(function(data) {
        $location.path('/view/' + data.id);
      });
  };

  $scope.cancel = function() {
    $location.path('/');
  };

  $scope.setAssignee = function() {
    for (var i = 0; i < $scope.lookup.assignees.length; i++) {
      if ($scope.ticket.assignee.id === $scope.lookup.assignees[i].id) {
        $scope.ticket.assignee = $scope.lookup.assignees[i];
        break;
      }
    }
    // console.log($scope.ticket.assignee.firstname);
  };
}
  
function EditCtrl($scope, $location, $routeParams, $http, Lookup) {
  $scope.lookup = Lookup;
  $scope.ticket = {};

  $scope.openDeleteDialog = function () {
    $scope.deleteDialogOpen = true;
  };

  $scope.closeDeleteDialog = function () {
    $scope.deleteDialogOpen = false;
  };

  $scope.deleteDialogOptions = {
    backdropFade: true,
    dialogFade: true
  };

  $http.get('/api/ticket/' + $routeParams.ticketId).
    success(function(data) {
      $scope.ticket = data;
    });

  $scope.delete = function() {
    $http.delete('/api/ticket/' + $routeParams.ticketId).
      success(function(data) {
        $location.path('/');
      });
    $scope.deleteDialogOpen = false;
  };
 
  $scope.save = function() {
    $http.put('/api/ticket/' + $routeParams.ticketId, $scope.ticket).
      success(function(data) {
        $location.path('/view/' + data.id);
      });
  };

  $scope.cancel = function() {
    $location.path('/view/' + $routeParams.ticketId);
  };

  $scope.setAssignee = function() {
    for (var i = 0; i < $scope.lookup.assignees.length; i++) {
      if ($scope.ticket.assignee.id === $scope.lookup.assignees[i].id) {
        $scope.ticket.assignee = $scope.lookup.assignees[i];
        break;
      }
    }
    // console.log($scope.ticket.assignee.firstname);
  };
}

function SignupCtrl($scope, $location, $http, Lookup) {
  $scope.lookup = Lookup;
  $scope.user = {
    "activated": false
  };

  $scope.signup = function() {
    $scope.user.username = $scope.user.email;
    $http.post('/api/user', $scope.user).
      success(function(data) {
        $location.path('/');
      });
  };
}
  
function ConfirmCtrl($scope, $location, $routeParams, $http) {
  $http.get('/api/user/' + $routeParams.userId).
    success(function(data) {
      $scope.user = data;
      $scope.user.activated = true;
      $http.put('/api/user/' + $routeParams.userId, $scope.user);
    });
}
