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
    $.get( host + "/media/list/" + playlist + "?index=" + mindex[playlist], function( data ) {
        for(var i = 0; i < data.d.length; i++){
            var ob = data.d[i];
            gData[playlist][ob.P] = ob;

            if(!gSearch[playlist]){
                placeThumbnail(ob, playlist);
            } else {
                console.log("skip place for " + playlist + " in search mode")
            }
        }

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
    //= '<span class="media-title">'+name+'</span><button class="media-btn" onclick="showOptions(\'' + obj.P + '\')">';
    if(obj.Q){
        innerHtml += '<img src="' + host +  '/thumbnail?id=' + obj.Q +'" class="thmb"/>'
    } else {
        //TODO sensitive icon
        innerHtml += '<div class="thmb dfth-'+ cThCss(obj) + ' dvth"></div>';
    }

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

    $('#air-play').attr('onclick', 'airPlayShowOptions(\''+path+'\')');
    $('#air-play').show();

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
                        '<img src="data:image/png;base64,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">' +
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
    place_friend_ips(_g('modal-status'), function (ip) {
        return '<span class="host">' + ip + '</span><button class="host-btn" onclick="open_to_remote(\''+encodeURI(ip)+'\', \''+encodeURI(url)+'\')">' +
            '<img src="data:image/png;base64,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"/>' +
            '</button>'
    })
}

function airPlayShowOptions(path) {
    setTimeout(function () {
        $('#modal-status').show();
        place_friend_ips(_g('modal-status'),
  function (ip) {
            return '<span class="host">' + ip + '</span><button class="host-btn" onclick="air_play(\''+encodeURI(ip)+'\', \''+path+'\')">' +
                '<img src="data:image/png;base64,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"/>' +
                '</button>'
        },
        function (ips) {
           return  '<button onclick="air_play_distribute(\''+encodeURI(path)+'\')">' +
               '<img src="data:image/png;base64,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"/>' +
               '</button>';
        });


    }, 1000)
}


function air_play_distribute(rPath) {
    console.log('rPath=', rPath);
    AppSettings.getFriendIps(function (x) {
        console.log(x.length);
        for(var t = 0; t < x.length; t++){

            air_play(x[t], rPath);
        }
    })
}


function open_to_remote(rHost, rPath) {
    var url = rHost + '/files/open?path=' + encodeURI(rPath);
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
        _g('modal-status').innerHTML += '<div style="color: red">'+rHost+': upload init, expect '+f.len+'</div>'
        _g('modal-status').setAttribute('onclick', '_mh()');
        air_play_check(rHost, f.len, f.name, function (p) {
            open_to_remote(rHost, p.path);
        });
    })
}

function air_play_check(rHost, len, name, ch) {
    $do_request(rHost + '/upload/stat?name=' + name, function (d) {
        console.log('upload stat = ', d);

        if(!d.exists){
            _g('modal-status').innerHTML += '<div style="color: red">'+rHost+'waiting upload...</div>'
            setTimeout(function () {
                air_play_check(rHost, len, name, ch);
            }, 1000)
            return;
        }

        if(d.len < len){
            _g('modal-status').innerHTML += '<div style="color: red">'+rHost+'uploaded '+d.len + ' from ' + len +'</div>'
            setTimeout(function () {
                air_play_check(rHost, len, name, ch);
            }, 1000)
            return;
        }
        _g('modal-status').innerHTML += '<div style="color: red">upload complete</div>'
        ch(d);
    })
}



function upload_to_friend_ips(remHost, path, upc) {
    var exts = path.split('.');
    var ext = exts[exts.length -1];
    var url = host + '/upload?file=' + path + '&name=sync-media.'+ext+'&destination=' + remHost ;
    $do_request(url, function (d) {
        upc(d);
    });
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