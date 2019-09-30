window.alert = bootbox.alert;

var i18n = {}, app = angular.module("webapp", ["ngRoute"]);


app.config(function($routeProvider) {
    $routeProvider
    .when('/user_signup', {
        templateUrl : '/html/user_signup.htm',
        controller: 'userSignupCtrl'
    })
    .when("/user_login", {
        templateUrl : "/html/user_login.htm"
    })
})
.run(function ($http, $rootScope) {
       $http({
          method: 'GET',
          url: '/lang/en.json'
       }).then(function (r){
            i18n = r.data;
       },function (error){
            alert('failed to load lang');
       });
});




app.controller('userSignupCtrl', ['$scope', function($scope) {

        $scope.saveData = function() {

            if(!$scope.usgd){
                alert('data required');
            }


          console.log($scope.usgd);
        };
}])
