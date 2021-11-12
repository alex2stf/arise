function is_empty(d) {
    var c = 0;
    for(var i in d){
        c++;
    }
    return c == 0;
}


function http_request(request_method, request_url, request_data, success_callback, failure_callback, request_headers) {
    var x;
    if(typeof window != 'undefined' && window.XMLHttpRequest) {
        x = new XMLHttpRequest();
    } else if (window.ActiveXObject) {
        try {
            x = new ActiveXObject('Msxml2.XMLHTTP');
        } catch (err) {
            try {
                x = new ActiveXObject('Microsoft.XMLHTTP');
            } catch (err) {
                x = false;
                console.log('xmlhttp failed to init');
                if (failure_callback) {
                    failure_callback(err);
                }
                return {};
            }
        }
    }

    var ferr = false;
    var fsucc = false;
    var data = {
        url: request_url,
        responses: []
    }
    var expire = setTimeout(function(){
        if(!fsucc && !ferr && data.length > 0){
            console.log('enter expire request for ' + request_url)
            clearTimeout(expire);
            ferr = true;
            if(failure_callback){
                failure_callback(data);
            }
        }

    }, 10 * 1000);

    x.onreadystatechange = function() {
        if (x.readyState == 4 && x.status == 200) {
            fsucc = true;
            clearTimeout(expire);
            if (success_callback){
                success_callback(x, (x.responseText || x.response));
            }
        }
        else {
            data.responses.push({
                state: x.readyState,
                status: x.status
            });
        }
    };

    x.onerror = function(e) {
        ferr = true;
        clearTimeout(expire);
        if (failure_callback) {
            failure_callback(e);
        }
    };


    try {
        x.open(request_method, request_url, true);
        for(var i in request_headers){
            x.setRequestHeader(i, request_headers[i]);
        }

        if (request_data) {
            x.send(request_data);
        } else {
            x.send();
        }

    } catch (e){
        ferr = true;
        if (failure_callback) {
            failure_callback(e);
        }
    }
}



function $do_request(url, c, f) {



    http_request("GET", url, {}, function (a1, a2) {
            var data = a2;
            try {
                data = JSON.parse(a2);
            }catch (e) {
                // console.log("Invalid json response [" + a2 + ']' + ' from [' + url + ']', e);
                data = a2;
            }

            if(c){
                c(data);
            }
    }, function (e) {
            if(f){
                f(e);
            } else {
                console.log('[' + url + '] ajax err  ', e);
            }
    }, {});
    //
    // $.ajax({
    //     type: 'GET',
    //     url: url,
    //     success: function (d) {
    //         console.log(d);
    //         if(c){
    //             c(d);
    //         }
    //         else {
    //             console.log("No handler found for " + url, d)
    //         }
    //     },
    //     error: function (xhr, ajaxOptions, thrownError) {
    //         if (e){
    //             e({
    //                 xhr: xhr,
    //                 options: ajaxOptions,
    //                 error: thrownError,
    //                 url: url
    //             });
    //         } else  {
    //             console.log(url + ' error\n', xhr, ajaxOptions, thrownError);
    //         }
    //     }
    // })


}


function $get(u, c) {
    console.log("request ", host + u)
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




var latest_data = {};



function update_ui_with_device_stat(d) {
    latest_data = d;
    _g('curl').innerHTML = window ? window.location : 'xxx';

    if(!d){
        return;
    }
    if (d.I4 && d.I4.length > 0){
        _g('sips').innerHTML = d.I4.join(' | ');
    }

    _g('d-name').innerHTML = d.N;

    if (+d.B1 && +d.B2){
        _g('d-battery').innerHTML = Math.round(d.B2 * 100 / d.B1) + '%';
        $('#c-battery').show();
    } else {
        $('#c-battery').hide();
    }

    if(!d.pP){
        return;
    }
    var musicVolume = d.pP['audio.music.volume'];
    if (+musicVolume){
        _g('volume').value = +musicVolume;
    }

    var flash_modes = d.pP["flash.modes.v1"];
    var cam_ids = d.pP['cams.v1'];
    var txt = ''; var selected = '';

    if(flash_modes && flash_modes.length > 0){
        _g('lightMode').innerHTML = '';
        selected = d.pP['flash.modes.active'];
        txt = '';
        for(var i = 0; i < flash_modes.length; i++){
            if (flash_modes[i].k === selected){
                txt += '<option selected value="'+flash_modes[i].k+'">'+flash_modes[i].v+'</option>'
            } else {
                txt += '<option value="'+flash_modes[i].k+'">'+flash_modes[i].v+'</option>'
            }
        }
        _g('lightMode').innerHTML = txt;
    }

    if(cam_ids && cam_ids.length > 0){
        selected = d.pP['cams.active.id'];

        _g('cameraIndex').innerHTML = '';
        txt = '';
        for(var i = 0; i < cam_ids.length; i++){
            if (cam_ids[i].k === selected){
                txt += '<option selected value="'+cam_ids[i].k+'">'+cam_ids[i].v+'</option>'
            } else {
                txt += '<option value="' + cam_ids[i].k + '">' + cam_ids[i].v + '</option>'
            }
        }
        _g('cameraIndex').innerHTML = txt;
    }

    var camera_enabled = 'true' == d.pP['cams.active.run'];
    if (camera_enabled){
        showCamStopBtn();
        showStream();
    } else {
        showCamPlayBtn();
    }


    update_sensor_data(d.pP);
}


function update_sensor_data(props) {

}


get_device_stat();