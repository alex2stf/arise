console.log("media");
var mindex = {};
var gData = {};
var gSearch = {};

function fetchMedia(p) {
    startFetch(p);
}

function startFetch(playlist) {
    if(!mindex[playlist]){
        mindex[playlist] = 0;
    }

    if(!gData[playlist]){
        gData[playlist] = {};
    }
    var url = host + "/media/list/" + playlist + "?index=" + mindex[playlist];
    $do_request( url, function( data ) {
        for(var i = 0; i < data.d.length; i++){
            var ob = data.d[i];
            gData[playlist][ob.P] = ob;

            if(!gSearch[playlist]){
                placeThumbnail(ob, playlist);
            } else {
                console.log("skip place for " + playlist + " in search mode")
            }
        }
        // console.log(data, new Date())
        if (data.i > 0){
            mindex[playlist] = data.i;
            setTimeout(function () {
                startFetch(playlist);
            }, 1000);
        }
    });
}



function  getFileNameFromObj(obj) {
    if(obj.F === 'S' && obj.T){
        return decodeString(obj.T);
    }
    return getFileNameFromStr(obj.P);
}


function getFileNameFromStr(x) {
    if(gData){
        for(var p in gData){
            var Z = gData[p];
            for (var k in Z){
                if (k == x){
                    if (Z[k].T){
                        return decodeURIComponent(Z[k].T).replaceAll('+', ' ');
                    }
                }
            }
        }
    }

    var names = decodeURIComponent(x);
    names = names.split("/");
    var n = names[names.length -1 ];
    var x = n.lastIndexOf('.');
    n = n.substr(0, x);
    return decodeString(n);
}




function decodeString(n) {
    return decodeURIComponent(n.replace(/\+/g, ' ').replace(/\%27/g, ' '));
}


function cThCss(o) {
    var p = o.P.split(".");
    var e = p[p.length - 1];
    if ('mp3' === e){
        return 'MPT';
    }
    return o.F;
}

function is_url(x) {
    return x && (x.indexOf('http://') == 0 || x.indexOf('https://') == 0)
}



function placeThumbnail(obj, playlist) {
    var id = (obj.P + '').replace(/\./g, '_')
        .replace(/%/g, '-')
        .replace(/\+/g, 'cP');

    if (document.getElementById(id)){
        return;
    }
    var div = document.createElement('div');
    div.setAttribute('class', 'mb');

    div.setAttribute("onclick", "showOptions('" + obj.P + "')");
    div.id = id;
    var name = getFileNameFromObj(obj);
    var innerHtml = '';

    var id = ("IMGSRC_" + name).replaceAll(" ", "").replaceAll(".", "X").replaceAll(":", "0").replaceAll("/", "A");
    innerHtml += '<img  id ="'+id +'" class="thmb dfth-'+ cThCss(obj) + ' dvth"/>'

    http_request("GET", host + "img-suggestion?q=" + obj.name + "&d=" + obj.Q, {}, function(r, a){
        document.getElementById(id).src = a + "";
    });


    innerHtml += '<div class="mt">';
    if((obj.B && obj.T) && obj.B != '' && obj.T != ''){
        innerHtml += '<div>' + decodeString(obj.B)  +  '</div>';
        innerHtml += '<div style="font-style: italic">' + decodeString(obj.T) + '</div>';
    } else {
        innerHtml += '<div>' + name  + '</div>';
    }
    innerHtml += '</div>';
    div.innerHTML = innerHtml;


    $('#media-content-' + playlist).append(div);

}

function showOptions(path) {
    var name = getFileNameFromStr(path);
    console.log(path, 'name', name);
    $('#media-title').html(name);
    $('#play-btn').attr('onclick', 'openFile(\''+path+'\')');
    $('#play-btn').show();

    $('#air-play').attr('onclick', 'start_distribute_path(\''+path+'\')');
    $('#air-play').show();

    $('#send-to-device').attr('onclick', 'sendToDevice(\''+path+'\')');
    $('#send-to-device').show();

    $('#stop-btn').attr('onclick', 'closeFile(\''+path+'\')');
    $('#stop-btn').show();
    $('#pause-btn').attr('onclick', 'pauseFile(\''+path+'\')')
    $('#pause-btn').show();
    $('#download-btn').attr('href', host + '/download?file='+path);
    $('#download-btn').attr('target', '_blank');
    $('#download-btn').show();
    $('#add-btn').attr('onclick', 'playlistView(\''+path+'\')');
    $('#add-btn').show();
    $('#modal-area').show();
}

function playlistView(p) {
    getPlaylists(function (data) {
        $('#playlist-view').show();
        document.getElementById('playlist-view').innerHTML = '';

        var inner = '';
        inner += '<div class="pli">'
        inner += '<button class="bcrpl" onclick="createPlaylist()"> NEW PLAYLIST </button>'
        inner += '</div>'

        for(var i = 0; i < data.length; i++){


                inner += '<div class="pli">'

                if(p){
                    inner += '<div class="plb pln">'+data[i]+'</div>'
                    inner += '<button class="btn plb" onclick="pushToPlaylist(\'' + p +'\', \'' + data[i] + '\')">' +
                        '<img src="data:image/png;base64,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">' +
                        '</button>'
                } else {
                    inner += '<div class="plb pln" style="width: 40%">'+data[i]+'</div>'
                    inner += '<button class="btn plb" onclick="playPlaylist(\''+data[i]+'\')">'
                        + '<img src="data:image/png;base64,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"/>'
                        + '</button>'
                    inner +='<button class="btn plb"  onclick="showDetails(\''+data[i]+'\')">' +
                        '<img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAACXBIWXMAAAsTAAALEwEAmpwYAAAKT2lDQ1BQaG90b3Nob3AgSUNDIHByb2ZpbGUAAHjanVNnVFPpFj333vRCS4iAlEtvUhUIIFJCi4AUkSYqIQkQSoghodkVUcERRUUEG8igiAOOjoCMFVEsDIoK2AfkIaKOg6OIisr74Xuja9a89+bN/rXXPues852zzwfACAyWSDNRNYAMqUIeEeCDx8TG4eQuQIEKJHAAEAizZCFz/SMBAPh+PDwrIsAHvgABeNMLCADATZvAMByH/w/qQplcAYCEAcB0kThLCIAUAEB6jkKmAEBGAYCdmCZTAKAEAGDLY2LjAFAtAGAnf+bTAICd+Jl7AQBblCEVAaCRACATZYhEAGg7AKzPVopFAFgwABRmS8Q5ANgtADBJV2ZIALC3AMDOEAuyAAgMADBRiIUpAAR7AGDIIyN4AISZABRG8lc88SuuEOcqAAB4mbI8uSQ5RYFbCC1xB1dXLh4ozkkXKxQ2YQJhmkAuwnmZGTKBNA/g88wAAKCRFRHgg/P9eM4Ors7ONo62Dl8t6r8G/yJiYuP+5c+rcEAAAOF0ftH+LC+zGoA7BoBt/qIl7gRoXgugdfeLZrIPQLUAoOnaV/Nw+H48PEWhkLnZ2eXk5NhKxEJbYcpXff5nwl/AV/1s+X48/Pf14L7iJIEyXYFHBPjgwsz0TKUcz5IJhGLc5o9H/LcL//wd0yLESWK5WCoU41EScY5EmozzMqUiiUKSKcUl0v9k4t8s+wM+3zUAsGo+AXuRLahdYwP2SycQWHTA4vcAAPK7b8HUKAgDgGiD4c93/+8//UegJQCAZkmScQAAXkQkLlTKsz/HCAAARKCBKrBBG/TBGCzABhzBBdzBC/xgNoRCJMTCQhBCCmSAHHJgKayCQiiGzbAdKmAv1EAdNMBRaIaTcA4uwlW4Dj1wD/phCJ7BKLyBCQRByAgTYSHaiAFiilgjjggXmYX4IcFIBBKLJCDJiBRRIkuRNUgxUopUIFVIHfI9cgI5h1xGupE7yAAygvyGvEcxlIGyUT3UDLVDuag3GoRGogvQZHQxmo8WoJvQcrQaPYw2oefQq2gP2o8+Q8cwwOgYBzPEbDAuxsNCsTgsCZNjy7EirAyrxhqwVqwDu4n1Y8+xdwQSgUXACTYEd0IgYR5BSFhMWE7YSKggHCQ0EdoJNwkDhFHCJyKTqEu0JroR+cQYYjIxh1hILCPWEo8TLxB7iEPENyQSiUMyJ7mQAkmxpFTSEtJG0m5SI+ksqZs0SBojk8naZGuyBzmULCAryIXkneTD5DPkG+Qh8lsKnWJAcaT4U+IoUspqShnlEOU05QZlmDJBVaOaUt2ooVQRNY9aQq2htlKvUYeoEzR1mjnNgxZJS6WtopXTGmgXaPdpr+h0uhHdlR5Ol9BX0svpR+iX6AP0dwwNhhWDx4hnKBmbGAcYZxl3GK+YTKYZ04sZx1QwNzHrmOeZD5lvVVgqtip8FZHKCpVKlSaVGyovVKmqpqreqgtV81XLVI+pXlN9rkZVM1PjqQnUlqtVqp1Q61MbU2epO6iHqmeob1Q/pH5Z/YkGWcNMw09DpFGgsV/jvMYgC2MZs3gsIWsNq4Z1gTXEJrHN2Xx2KruY/R27iz2qqaE5QzNKM1ezUvOUZj8H45hx+Jx0TgnnKKeX836K3hTvKeIpG6Y0TLkxZVxrqpaXllirSKtRq0frvTau7aedpr1Fu1n7gQ5Bx0onXCdHZ4/OBZ3nU9lT3acKpxZNPTr1ri6qa6UbobtEd79up+6Ynr5egJ5Mb6feeb3n+hx9L/1U/W36p/VHDFgGswwkBtsMzhg8xTVxbzwdL8fb8VFDXcNAQ6VhlWGX4YSRudE8o9VGjUYPjGnGXOMk423GbcajJgYmISZLTepN7ppSTbmmKaY7TDtMx83MzaLN1pk1mz0x1zLnm+eb15vft2BaeFostqi2uGVJsuRaplnutrxuhVo5WaVYVVpds0atna0l1rutu6cRp7lOk06rntZnw7Dxtsm2qbcZsOXYBtuutm22fWFnYhdnt8Wuw+6TvZN9un2N/T0HDYfZDqsdWh1+c7RyFDpWOt6azpzuP33F9JbpL2dYzxDP2DPjthPLKcRpnVOb00dnF2e5c4PziIuJS4LLLpc+Lpsbxt3IveRKdPVxXeF60vWdm7Obwu2o26/uNu5p7ofcn8w0nymeWTNz0MPIQ+BR5dE/C5+VMGvfrH5PQ0+BZ7XnIy9jL5FXrdewt6V3qvdh7xc+9j5yn+M+4zw33jLeWV/MN8C3yLfLT8Nvnl+F30N/I/9k/3r/0QCngCUBZwOJgUGBWwL7+Hp8Ib+OPzrbZfay2e1BjKC5QRVBj4KtguXBrSFoyOyQrSH355jOkc5pDoVQfujW0Adh5mGLw34MJ4WHhVeGP45wiFga0TGXNXfR3ENz30T6RJZE3ptnMU85ry1KNSo+qi5qPNo3ujS6P8YuZlnM1VidWElsSxw5LiquNm5svt/87fOH4p3iC+N7F5gvyF1weaHOwvSFpxapLhIsOpZATIhOOJTwQRAqqBaMJfITdyWOCnnCHcJnIi/RNtGI2ENcKh5O8kgqTXqS7JG8NXkkxTOlLOW5hCepkLxMDUzdmzqeFpp2IG0yPTq9MYOSkZBxQqohTZO2Z+pn5mZ2y6xlhbL+xW6Lty8elQfJa7OQrAVZLQq2QqboVFoo1yoHsmdlV2a/zYnKOZarnivN7cyzytuQN5zvn//tEsIS4ZK2pYZLVy0dWOa9rGo5sjxxedsK4xUFK4ZWBqw8uIq2Km3VT6vtV5eufr0mek1rgV7ByoLBtQFr6wtVCuWFfevc1+1dT1gvWd+1YfqGnRs+FYmKrhTbF5cVf9go3HjlG4dvyr+Z3JS0qavEuWTPZtJm6ebeLZ5bDpaql+aXDm4N2dq0Dd9WtO319kXbL5fNKNu7g7ZDuaO/PLi8ZafJzs07P1SkVPRU+lQ27tLdtWHX+G7R7ht7vPY07NXbW7z3/T7JvttVAVVN1WbVZftJ+7P3P66Jqun4lvttXa1ObXHtxwPSA/0HIw6217nU1R3SPVRSj9Yr60cOxx++/p3vdy0NNg1VjZzG4iNwRHnk6fcJ3/ceDTradox7rOEH0x92HWcdL2pCmvKaRptTmvtbYlu6T8w+0dbq3nr8R9sfD5w0PFl5SvNUyWna6YLTk2fyz4ydlZ19fi753GDborZ752PO32oPb++6EHTh0kX/i+c7vDvOXPK4dPKy2+UTV7hXmq86X23qdOo8/pPTT8e7nLuarrlca7nuer21e2b36RueN87d9L158Rb/1tWeOT3dvfN6b/fF9/XfFt1+cif9zsu72Xcn7q28T7xf9EDtQdlD3YfVP1v+3Njv3H9qwHeg89HcR/cGhYPP/pH1jw9DBY+Zj8uGDYbrnjg+OTniP3L96fynQ89kzyaeF/6i/suuFxYvfvjV69fO0ZjRoZfyl5O/bXyl/erA6xmv28bCxh6+yXgzMV70VvvtwXfcdx3vo98PT+R8IH8o/2j5sfVT0Kf7kxmTk/8EA5jz/GMzLdsAAAAgY0hSTQAAeiUAAICDAAD5/wAAgOkAAHUwAADqYAAAOpgAABdvkl/FRgAAAvdJREFUeNrs202oVVUYBuDnHLc386IZ+JNCliYoSE0SB1oDSVCJZoJRg0gFFRypKEqJEAgOFARRJzpo0rRwYKQDJYjLjVRuBWnhVVC8agX+gZa35WDvCzs5P3cN9zr7Ha2911qD7z3nW9/PencjhPAZPtca32A9HkoUjRDCHczosGY5fig9z8SrFbf7bwxDhv4ui18ojV/HSayoOAFXsQlnmwhdFpfnDyRgPMzHEUxvRmyagfcTcv+FeDuGgJfQl9gZ2B9DwAjuJmT8Y4zEEPCw8JuQCAHfYjBDNxIapfHBwg02Pve+ajiP7XjaCCEM4c0Of5Ol+DnVRCjDDmxpM/8dfpEwGiEEvYymHkdNQE1ATUBvI8PLRXXUCrdxI/UweBKftpm/iNW4kzIBjzC5w5oVOFd6XiLvElUZV3B6zAW65fTl+eX4soPLVAUPsBPHm/ivy+JQOjD3J2A8TME+zIuJArOLwigVzMLiGAImVrwEboUJMQTckndTU8E9DDcjDsEn+KLYWHU8xQkMZZgQsfErjGJdxbPIARwaywPOYGWbhSNF6LsqUTRCCAuLbK8VLsn7Z0mnwnU1WBNQE1ATUBPQq8iwCKt6OQyexXtt5m9jWcqJUIZ3upSMrz1HwEdYW/HKcFB+0ftPVuT248XHOIqpFf/hP5ArXrbFaIT6sDsB4xUF4Aa8FRMF5mBBQu4/VWRL7F/pqEPGMBqrEfoxIeNH8GvT+CUyo/JW8u8JGH9f3hUezsQJJQfk3aB3Kx4GL8tFUhohhBNyQXQrXMAaiV+NTcMbHfzkZuqpcF0N1gTUBNQE1AT0KjJ5N2hzm/kzOJZgDfC/MNjzYulOJe4kTC8992EvPql4Kvw9tuFWZvwSGUUxtEf1hRIf4kWsjTkE++WfmqWiElmFpbEaoVkJuf8kvBJDwH15VyglPIoh4C5OJWT8b/gpRiMUsKvIDaqOP7AVf2boJpV9Uhpfl98NzK04AX/h2lgecLiI7a3wNYZauEIyH1A+GwB27LDDPN6zzwAAAABJRU5ErkJggg==">' +
                        '</button>'
                }

                inner += '<hr>'
                inner += '</div>'


        }
        document.getElementById('playlist-view').innerHTML = inner;

        console.log(data);
    });
}





function pauseFile(x) {
    $.get( host + "/media/pause?path=" + x, function( data ) {
        console.log("PAUSE PLAY " + data);
    });
}

function openFile(x) {
    $get( "/files/open?path=" + x, function( data ) {
        update_ui_with_device_stat(data);
    });
}


function show_send_options(url) {
    _g('modal-status').innerHTML = '';
    $('#modal-status').show();

    if(is_empty(g_friends) && window.location && window.location.origin){
        g_friends[window.location.origin] = {
            name: 'current device'
        }
    }

    place_friend_ips_for_air_play('modal-status', url, 'air-url-open');
}

var dist_hosts = {};
function start_distribute_path(path) {
    dist_hosts = {};
    if(is_empty(g_friends)){
        alert('No friend ips detected');
        return;
    }
    _g('modal-status').innerHTML = '';
    $('#modal-status').show();
    _g('modal-status').innerHTML += '<button onclick="distributed_play()"> DISTRIBUTE PLAY </button>';

    for(var i in g_friends){
        console.log('upload try to ' + i);
        upload_to_remote_and_check(i, path);
    }

    setTimeout(function () {
        $('#modal-status').show();
    }, 1000)

}

function upload_to_remote_and_check(i, path) {
    upload_to_friend_ips(i, path, function (f) {
        var rhost = f.host;
        var elemId = extract_id(rhost) + '-upl-stat';
        var g_element = g_friends[rhost];
        var g_name = g_friends[rhost].name != 'undefined' ?  g_friends[rhost].name : rhost;

        if (!_g(elemId)) {
            _g('modal-status').innerHTML += '<div id="' + elemId + '" style="color:' + g_element.color + ';"> <div>' + g_name + '</div></div>';
        }

        air_play_check(f.host, f.len, f.name, function (p) {
            dist_hosts[f.host] = f;
            dist_hosts[f.host].path = p.path;
        },  elemId, g_name);

    }, function (err) {
        console.log('failed to upload to ' + i);
    })
}

function sendToDevice(path) {
    _g('modal-status').innerHTML = '';
    $('#modal-status').show();
}


function  distributed_play() {
    for (var i in dist_hosts){
        open_to_remote(dist_hosts[i].host, decodeURIComponent(dist_hosts[i].path));
    }
}


function open_to_remote(rHost, rPath) {
    var url = rHost + '/files/open?path=' + encodeURIComponent(rPath);
    console.log(url);
    $do_request(url, function () {
        _mh();
    });
}

function air_play(rHost, path) {
    _g('modal-status').innerHTML = '';
    $('#modal-status').show();
    _g('modal-status').removeAttribute('onclick');
    upload_to_friend_ips(rHost, path, function (f) {
        $('#modal-status').show();
        console.log('upload to friend: ', f);
        _g('modal-status').innerHTML += '<div style="color: red">' + f.host + ': upload init, expect '+f.len+'</div>'
        _g('modal-status').setAttribute('onclick', '_mh()');
        air_play_check(f.host, f.len, f.name, function (p) {
            open_to_remote(p.host, p.path);
        },  _g('modal-status'));
    })
}

var cnt = 0;

function air_play_check(rHost, len, name, ch, divId, device_name) {
    var div = _g(divId);
    var url = rHost + '/upload/stat?name=' + name;
    div.innerHTML = '<div>' + cnt + ') ' + device_name + ' check init at ' + url + '</div>';

    cnt++;
    $do_request(url, function (d) {



        if(d.exists == false){
            console.log("I ", url, d, len)
            div.innerHTML = '<div>' + device_name + 'waiting upload...</div>'
            setTimeout(function () {
                air_play_check(rHost, len, name, ch, divId, device_name);
            }, 1000)
            return;
        }

        if(d.len < len){
            console.log("II ", url, d, len)
            div.innerHTML = '<div>' + device_name + ': uploaded ' + d.len + ' from ' + len +'</div>'
            setTimeout(function () {
                air_play_check(rHost, len, name, ch, divId, device_name);
            }, 1000)
            return;
        }

        console.log("III ", url, d, len, device_name, rHost, div)
        div.innerHTML = '<div>' + device_name + ': upload complete</div>';
        d.host = rHost;
        d.el = div;
        ch(d);

    }, function (err) {

        div.innerHTML = '<div>' + device_name + ': check request failed </div>';
    })
}



function upload_to_friend_ips(W, path, call, err) {
    var exts = path.split('.');
    var ext = exts[exts.length -1];
    var url = host + '/upload?file=' + path + '&name=sync-media.'+ext+'&destination=' + W ;
    $do_request(url, function (d) {
        d.host = W;
        call(d);
    }, err);
}

function closeFile(x) {
    $get("/files/close?path=" + x, function( data ) {
        console.log("after close ", data);
        update_ui_with_device_stat(data);
    });
}




function isMatch(e, txt) {
    console.log('is match in ', e)
    for(var i in e){
       var val = e[i] + '';
       console.log('key (' + i + ") = " + val);
        if(val.indexOf(txt) > -1){
            console.log(e[i], txt);
            return true;
        }
    }
    return false;
}

function placeAll(d, p) {
    document.getElementById('media-content-' + p).innerHTML = '';
    for(var k in d){
        placeThumbnail(d[k], p);
    }
}

function mediaSearch(p) {
    var val = $('#mdsrc-' + p).val();
    var lData = {}
    console.log(gData[p]);
    if (val == ''){
        gSearch[p] = false;
        placeAll(gData[p], p);
    }
    else {
        gSearch[p] = true;
        var d = gData[p];
        for(var k in d){
            if(isMatch(d[k], val)){
                lData[d[k].P] = d[k];
            }
        }
        placeAll(lData, p);
    }

}


function getPlaylists(c) {
    $.get(host + "/playlist", function (data) {
        c(data);
    });
}

function pushToPlaylist(x, y) {
    console.log("x = ", x, "y = ", y);
    $.get(host + "/playlist?action=add&name=" + y + "&path=" + x, function (data) {

    });
}


function createPlaylist() {
    var name = prompt("enter playlist name");


    if(confirm('Create playlist [' + name + '] ?')){
        $.get(host + "/playlist?action=create&name="+ name, function (data) {
            console.log("device control succeeded", data);
        });
    }
}

function playPlaylist(name) {
    $.get(host + "/playlist?action=play&name="+ name, function (data) {
        console.log("device control succeeded", data);
    });
}