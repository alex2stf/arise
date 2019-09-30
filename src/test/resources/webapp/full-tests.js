

//
var servers = {
    'nano-public': 'http://localhost:6003'
};




var serverName = 'jersey-public', host = 'http://localhost:6002';

//testSuite(serverName, host);
testSuite('nano-public', 'http://localhost:6003');

var exampleSocket = new WebSocket("ws://127.0.0.1:8009/user", 'echo');

exampleSocket.onopen = function (event) {
    console.log('connection opened');
    exampleSocket.send("Here's some text that the server is urgently awaiting!");
};

exampleSocket.onmessage = function (event) {
  console.log('received ', event);
}
//exampleSocket.close();

function testSuite(serverName, host){

    var assert = quixot.Sancho.getInstance(),
    service = quixot.Service(serverName).setRoot(host);
//    console.log(service);



//    service.HelloCompat.testGet({name: 'nume-get', auth:'auth', arg: 2}, function(response){
//        assert.equals(response, 'name:nume-get;arg:2;auth:auth')
//    });
//
//    service.HelloCompat.testPost({name: 'nume-post', auth:'auth', arg: 2, id: 5}, function(response){
//         console.log(response)
//         assert.equals(response, 'name:nume-post;arg:2;auth:auth;id=5')
//    });
//
//    service.HelloCompat.testPut({name: 'nume-put', auth:'xxx', arg: 89, id: 52}, function(response){
//         assert.equals(response, 'name:nume-put;arg:89;auth:xxx;id=52')
//    });
//
//    service.HelloCompat.testDelete({name: 'nume-delete', auth:'zzz', arg: 32, id: 5}, function(response){
//         assert.equals(response, 'name:nume-delete;arg:32;auth:zzz;id=5')
//    });


    var userName = "test-user" + serverName + quixot.Util.incr(),
    userMail = userName + "@mail.com",
    pass = "passwd" + userName;

    console.log('signup user ' + userName);
    service.UserActions.signup({
        name: userName,
        'From': userMail,
        'Password': pass,
        'Referrer-Id': 'some-referrer'
    }, function(response){
         console.log("SIGNUP DONE" + response);

         console.log('login user ' + userName);
         service.UserActions.login({
           name: userName,
           'Password': pass
         }, function(r){
                console.log(r);

                service.UserActions.profile({'Authorization': r.authToken}, function(x){
                    console.log("VIEW PROFILE DONE" + x);
                    console.log(x)
                })


         });

    });

    console.log('run suite for ' + serverName + ' at ' + host);
}