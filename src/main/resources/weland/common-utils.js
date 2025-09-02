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



function $do_get(url, c, f) {
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
    $do_get(host + u, c);
}






