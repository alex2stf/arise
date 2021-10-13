
function $do_request(url, c, e) {

    console.log('DO REQUEST', url)

    $.ajax({
        type: 'GET',
        url: url,
        success: function (d) {
            console.log(d);
            if(c){
                c(d);
            }
            else {
                console.log("No handler found for " + url, d)
            }
        },
        error: function (xhr, ajaxOptions, thrownError) {
            if (e){
                e({
                    xhr: xhr,
                    options: ajaxOptions,
                    error: thrownError,
                    url: url
                });
            } else  {
                console.log(url + ' error\n', xhr, ajaxOptions, thrownError);
            }
        }
    })


}


function $get(u, c) {
    $do_request(host + u, c);
}


function send_device_update(p, c) {
    var a = 0, t = ''
    for(var i in p){
        if (a > 0){
            t+='&';
        }
        t += i + '=' + p[i];
        a++;
    }

    $get('/device/update?' + t, function (d) {
        update_ui_with_device_stat(d);
        if(c){
            c(d);
        }
    });
}

function get_device_stat(){
    $get('/device/stat', function (d) {
        console.log('device stat = ', d);
        update_ui_with_device_stat(d);
    })
}

function add_friend_host(){
    var txt =  prompt("Enter host name");
    var hh = 'http://' + txt + ':8221';
    var th = hh + '/device/stat';
    console.log('add_friend_host' + th);
    $do_request(th, function (d) {
        if (confirm('Add ' + hh + ' as friend?')){
            AppSettings.addFriendIp(hh);
        }
    });

}

// function check_friend_apps(ips) {
//     if (checking_hosts){
//         return;
//     }
//     var hosts = [];
//     for(var i = 0; i < ips.length; i++){
//         var ip = ips[0];
//         console.log(ip);
//         var parts = ip.split('.');
//         // console.log("parts", parts);
//
//         for (var j = 0; j < 256; j++){
//             for (var k = 0; k < 256; k++){
//                 var remIp = parts[0] + '.' + parts[1] + '.' + j + '.' + k;
//                 hosts.push(remIp)
//             }
//         }
//     }
//
//     checking_hosts = true;
//     start_domain_check(hosts, 0);
// }


// function start_domain_check(hosts, index){
//     if (index > hosts.length){
//         checking_hosts = false;
//     }
//     var rh = hosts[index];
//     var th = 'http://' + rh + ':8221/device/stat';
//     //console.log(th);
//
//     $.ajax({
//         type: 'GET',
//         url: th,
//         success: function (d) {
//             console.log('remote ' + rh + ' = ' + d);
//         },
//         error: function (xhr, ajaxOptions, thrownError) {
//             console.log(thrownError);
//         }
//     })
//
//     setTimeout(function () {
//         start_domain_check(hosts, index + 1);
//     }, 1000);
// }


function update_ui_with_device_stat(d) {
    if(!d){
        return;
    }
    if (d.I4 && d.I4.length > 0){
        _g('sips').innerHTML = d.I4.join(' | ');
    }

    var camIds = d.pP["CV1"];
    var flashIds = d.pP["FMV1"];
    var cC = d.pP['ECV1'];

    _g('cameraIndex').innerHTML = '';
    var txt = '';
    for(var i = 0; i < camIds.length; i++){
        txt += '<option value="'+camIds[i].i+'">'+camIds[i].n+'</option>'
    }
    _g('cameraIndex').innerHTML = txt;

    _g('lightMode').innerHTML = '';
    var txt = '';
    for(var i = 0; i < flashIds.length; i++){
        txt += '<option value="'+flashIds[i].i+'">'+flashIds[i].n+'</option>'
    }
    _g('lightMode').innerHTML = txt;


    if (cC){
        _g('cameraIndex').value = cC.i;

        var cameraEnabled = ("true" == cC.n);
        if (cameraEnabled){
            showCamStopBtn();
            showStream();
        } else {
            showCamPlayBtn();
        }
    }
}


get_device_stat();
