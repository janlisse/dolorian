'use strict';

// use German version
define('angular', ['webjars!angular-locale_de-de.js'], function () {
    return angular;
});

require(['angular', './controllers'],
    function (angular, controllers) {
        angular.module('invoiceApp', ['invoiceApp.controllers']);
        angular.bootstrap(document, ['invoiceApp']);
    });