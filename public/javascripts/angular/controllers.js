define(['angular'], function (angular) {
    'use strict';
    return angular.module('invoiceApp.controllers', [])
        .controller('InvoiceListCtrl', ['$scope', '$http',
            function ($scope, $http) {
                $http.get('/invoices/list').success(function (data) {
                    $scope.invoices = data;
                    angular.forEach($scope.invoices, function (invoice) {
                        invoice.active = false;
                        invoice.paid = invoice.status === 'Paid';
                    });
                });
                $scope.orderProp = 'invoiceDate';
                $scope.toggleActive = function (invoice) {
                    invoice.active = !invoice.active;
                };
                $scope.deleteInvoice = function (invoice) {
                    $http.delete('/invoices/' + invoice.id).success(function (data) {
                        var i = $scope.invoices.indexOf(invoice);
                        if (i != -1) {
                            $scope.invoices.splice(i, 1);
                        }
                    });
                };
                $scope.updateInvoiceStatus = function (invoice, status) {
                    invoice.status = status
                    invoice.paid = invoice.status === 'Paid';
                	$http.put('/invoices/' + invoice.id, invoice).success(function (data) {});
                };
            }
        ])
});