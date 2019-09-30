

var jentilAng = angular.module("ng-jentil", []);

jentilAng.run(function ($http, $rootScope) {
     $http({
          method: 'GET',
          url: '/lang/en.json'
       }).then(function (success){

       },function (error){

       });

//    $http.get('/lang/en/list')
//        .success(function(data, status, headers, config) {
//            $rootScope.lang = data;
//            JentilApp.lang = data;
//            $rootScope.$broadcast('lang-loaded');
//        })
//        .error(function(data, status, headers, config) {
//            alert('error');
//        });
})


var services = (function(){


    function doPost(url, data, handler) {
        console.log(data);

        var encoded = quixot.URL.querify(data);
        if (encoded) {
            url+="?"+encoded;
        }
        
        jQuery.ajax({
           method: 'POST',
            headers: {'Accept':'application/json', 'jnt-tkn-sig': JentilApp.env._signup_token,
                    'Content-Type': 'application/x-www-form-urlencoded' },

            url: url,
            success: function(data){
                if(handler){
                    handler(data)
                }
            },
            error: function (err) {
                console.log(err);
                bootbox.alert({
                    message: "An error occured!",
                    backdrop: true,
                    size: 'small'
                });
            }
        });
    }


   function login(data, handler){
       doPost('/login', data, handler);
   }


    function signup(data, handler){

        try {
            if(data.birthDate) {
                data.bd = data.birthDate.getTime();
            }
        }catch (e) {
            console.log(e);
        }
        doPost('/signup', data, handler);
    }


   return {
        login: login,
        signup: signup,
        doPost: doPost
   }


})();


window.alert = bootbox.alert;



jentilAng.controller('loginCtrl', function($scope, $http) {
    $scope.login = function() {
        console.log($scope.logind);
        services.login($scope.logind, function(response){
        console.log(response)
            if (response.success) {
                alert("login success")
            } else {
                alert("login failed")
            }
        })
    }
});


jentilAng.controller('signupCtrl', function($scope, $rootScope) {
    console.log($rootScope)
    $scope.signup = function() {
        if($scope.signd) {
            //add uid:

            if(quixot.Cache.getSafe('localUid', false)){
                alert($rootScope.lang['servlet.signup.browser.used']);
                return;
            }

            $scope.signd.u = md5(quixot.Fingerprint.identifier());



            services.signup($scope.signd, function(response){
                console.log(response);
                if(response.success){
                    quixot.Cache.put('localUid', $scope.signd.u);
                }
                var text = '';
                for(var i = 0; i <  response.messageList.length; i++){
                    var obj = response.messageList[i];
                    if ($rootScope.lang[obj]){
                        text+= $rootScope.lang[obj];
                    }
                }
                if(text) {
                    alert(text);
                }
            })
        } else {
            alert($rootScope.lang['servlet.signup.invalid.data'])
        }
    }
});
