function _g(x) {
    return document.getElementById(x);
}

function __get_child(rootId, clzChild, c) {
    var root = document.getElementById(rootId);
    if (root){
        var childs = root.getElementsByClassName(clzChild);
        if (childs){
            for(var i = 0; i < childs.length; i++){
                c(childs[i])
            }
        }
    }
}


function __set_text(rootId, clzChild, html_text) {
    __get_child(rootId, clzChild, function (i) {
        i.innerHTML = html_text;
    });
}

function _show_block_and_set_text(rootId, clzChild, html_text) {
    __get_child(rootId, clzChild, function (i) {
        i.innerHTML = html_text;
        i.style.display = 'block'
    });
}

function _hide_block(rootId, clzChild) {
    __get_child(rootId, clzChild, function (i) {
        i.style.display = 'none'
    });
}


function __set_value(rootId, clzChild, txt) {
    __get_child(rootId, clzChild, function (i) {
        i.value = txt;
    });
}






function extract_id(ip) {
    return ip.replace(/\./g, '')
        .replace(/\+/g, '_')
        .replace(/\:/g, '')
        .replace(/\//g, '')
        .replace(/http/g, 'x');
}


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
            console.log("Invalid json response [" + a2 + ']' + ' from [' + url + ']', e);
            data = a2;
        }

        if( typeof c != 'undefined'){
            c(data);
        }
    }, function (e) {
        if(f){
            f(e);
        } else {
            console.log('[' + url + '] ajax err  ', e);
        }
    }, {});
}

function $get(u, c) {
    $do_request(host + u, c);
}

function check_local_network(rootIp, c){


    var parts = rootIp.split(".");


    for( var i = 0; i < 30; i++){
        var th =  parts[0] + '.'  + parts[1] + '.' + parts[2] + '.' + i;
        console.log('check ' + th);
        $get('/proxy/exec?host=' + th + "&port=8221&protocol=http&path=/device/stat", function (x) {
            if(x.err && x.test){
                console.log('nothing for ' +  x.test);
            } else if(c){
                c(x);
            }
        })
    }
}

function check_test() {
    check_local_network("192.168.1.0");
}

var drUid = Math.round(Math.random() * 10);
function request_uid() {
    drUid++;
    return drUid + '-' +  Math.round(Math.random() * 10);
}


function getStorage(){
    return localStorage || sessionStorage;
}

var AppSettings = {


    get: function(prop, def){
        try {
            var x = getStorage().getItem(prop);
            if(x){
                return x;
            }
        }catch  (e) {
            console.log("failed to set prop")
        }
        return def;
    },

    set: function(prop, value){
        getStorage().setItem(prop, value);
    },

    addFriendIp : function (ip) {
        $get("/props/get?key=friend-ips", function (arr) {
            var actarr = [];
            if(!arr || !arr.length){
                console.log('nothing saved in props');
                actarr = [];
            }else {
                if(arr.indexOf(ip) > -1){
                    console.log('ip ' + ip + 'already saved');
                    return;
                }
                for(var i = 0; i < arr.length; i++){
                    actarr[i] = arr[i];
                }
            }
            actarr.push(ip);
            $get("/props/set?key=friend-ips&value=" + JSON.stringify(actarr), function (x) {
                // console.log(x)
            })


        });
    }
}



function update_ui_with_device_stat_into_root(root, secondRoot, d) {
    latest_data = d;

    __set_text(root, 'window-location', window ? window.location : 'xxx')

    if(!d){
        return;
    }

    console.log("received update device state with ", d)
    if (d.I4 && d.I4.length > 0){
        __set_text(root, 'sips', d.I4.join(' | ') );
    }

    __set_text(root,'d-name', d.N);

    if (+d.B1 && +d.B2){
        __set_text(root, 'd-battery', Math.round(d.B2 * 100 / d.B1) + '%' );
        $('#c-battery').show();
    } else {
        $('#c-battery').hide();
    }


    if(!d.pP){
        return;
    }
    var musicVolume = d.pP['audio.music.volume'];
    if (+musicVolume && _g('volume')){
        __set_value(root, 'music-volume',  +musicVolume);
    }

    var flash_modes = d.pP["flash.modes.v1"];
    var cam_ids = d.pP['cams.v1'];
    var txt = '';
    var selected = '';

    if(flash_modes && flash_modes.length > 0){
        selected = d.pP['flash.modes.active'];
        txt = '';
        for(var i = 0; i < flash_modes.length; i++){
            if (flash_modes[i].k === selected){
                txt += '<option selected value="'+flash_modes[i].k+'">'+flash_modes[i].v+'</option>'
            } else {
                txt += '<option value="'+flash_modes[i].k+'">'+flash_modes[i].v+'</option>'
            }
        }
        __set_text(secondRoot, 'light-mode', txt);
    }

    if(cam_ids && cam_ids.length > 0){
        selected = d.pP['cams.active.id'];

        txt = '';
        for(var i = 0; i < cam_ids.length; i++){
            if (cam_ids[i].k === selected){
                txt += '<option selected value="'+cam_ids[i].k+'">'+cam_ids[i].v+'</option>'
            } else {
                txt += '<option value="' + cam_ids[i].k + '">' + cam_ids[i].v + '</option>'
            }
        }
        __set_text(secondRoot, 'camera-index', txt);
    }

    var camera_enabled = 'true' == d.pP['cams.active.run'];
    if (camera_enabled && typeof showCamStopBtn != 'undefined'){
        showCamStopBtn(d);

        if (typeof showStream != 'undefined'){
            showStream();
        }
    } else if(typeof showCamPlayBtn != 'undefined') {
        showCamPlayBtn(d);
    }

    var ron = d.pP['rplayer.play'];
    if ('true' == ron){
        _show_block_and_set_text(root, 'radio-stat', '<button id="rpl24" class="btn-txt" onclick="close_radio()">Close radio</button>')
    } else if('false' == ron) {
        _show_block_and_set_text(root, 'radio-stat', '<button id="rpl24" class="btn-txt" onclick="start_radio()">Start radio</button>')
    }

    var sh = d.pP['rplayer.show.name'];
    if ('true' == ron){
        _show_block_and_set_text(root, 'radio-show', '<span>'+sh+'</span>');
    } else {
        _hide_block(root, 'radio-show');
    }

    var sys = {};
    for(var lk in d.pP){
        if((lk+'').indexOf('sys[') == 0){
            var kn = lk.substring(4, lk.length - 3);
            var pr = lk.substring(lk.length - 1);
            if(!sys[kn]){
                sys[kn] = {};
            }
            sys[kn][pr] = d.pP[lk];
        }
    }

    var txt = '<tr><td>Name</td><td>Description</td><td>Value</td></tr>';
    for (var p in sys){
        txt +='<tr id="'+p.replace(/./g, '_').replace(/\\/g, 'x')+'">' +
            '<td>'+sys[p].n +'</td>' +
            '<td>'+_fixQP(sys[p].i) +'</td>' +
            '<td>'+sys[p].v +'</td>' +
            '</tr>'
    }
    _g('device-data').innerHTML = txt;

    console.log(sys)


    update_sensor_data(d.pP);
}

function _fixQP(qp){
    if (qp.indexOf('&') > -1){
        return qp.replace(/&/g, '</br>');
    }
}


function update_sensor_data(props) {

}