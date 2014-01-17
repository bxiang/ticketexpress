'use strict';

// Declare app level module which depends on filters, and services
angular.module('myApp', ['myApp.filters', 'myApp.services', 'myApp.datasource', 'myApp.directives', 'ui.bootstrap']).
  config(['$routeProvider', function($routeProvider) {
    $routeProvider.
      when('/', {
      	templateUrl: '/assets/partials/list.html',
      	controller: ListCtrl 
      }).
      when('/view/:ticketId', {
        templateUrl: '/assets/partials/view.html',
        controller: ViewCtrl 
      }).
      when('/edit/:ticketId', {
      	templateUrl: '/assets/partials/detail.html',
      	controller: EditCtrl 
      }).
      when('/new', {
        templateUrl: '/assets/partials/detail.html',
        controller: CreateCtrl 
      }).
      when('/signup', {
        templateUrl: '/assets/partials/signup.html',
        controller: SignupCtrl 
      }).
      when('/confirm/:userId', {
        templateUrl: '/assets/partials/confirmed.html',
        controller: ConfirmCtrl 
      }).
      otherwise({
      	redirectTo: '/'
      });
  }]);
