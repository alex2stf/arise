var uniqeId = 0;

function place_friend_ips(div, extra, extra2){
    uniqeId++;


    AppSettings.getFriendIps(function (ips) {
        var id = 'hpanel' +uniqeId;
        var t = '<div class="host_area"><div id="'+id+'" class="hpanel-cnt">';
        for(var i = 0; i < ips.length; i++ ){
            if (extra){
                t+=extra(ips[i]);
            }
        }

        if(extra2){
            t+= extra2(ips);
        }
        t+='</div>' +
            '<button class="host-btn" onclick="add_friend_host(\''+id+'\')">' +
            '<img src="data:image/png;base64,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"/>' +
            '</button>'

        t+='</div>';
        div.innerHTML = t;
    })
}

place_friend_ips(_g('ips-container'), function (ip) {
    return '<span class="host">' + ip + '</span><button class="host-btn" ' +
        'onclick="test_host(\''+encodeURI(ip)+'\')">' +
        '<img src="data:image/png;base64,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"/>' +
        '</button>'
});


function distribute_to_all() {
    AppSettings.getFriendIps(function (ips) {

    })
}

function test_host(h) {
    console.log("test host", h);
    $.ajax({
        type: 'GET',
        url: h + '/device/stat',
        success: function (d) {
            alert('Host '+h+' is still running')

        },
        error: function (xhr, ajaxOptions, thrownError) {
          alert('error TODO' + h)
        }
    })
}








// function updateUi(x) {
//     console.log('device stat = ', x);
//
//     //read from properties:
//     if (x.pP){
//         updateProps(x.pP);
//     }
//
//     if (x.D) {
//         document.getElementById('d-name').innerHTML = x.D;
//     }
//
//     if (x.B1){
//         document.getElementById('d-bscale').innerHTML = x.B1;
//     }
//
//     if (x.B2){
//         document.getElementById('d-blevel').innerHTML = x.B2;
//     }
//
//     if (x.pP){
//         readSensors(x.pP);
//     }
//
//     if (x.I4){
//         $('#sips').html(x.I4.join(', '));
//     }
//
// }



var sensors = {};

function readSensors(obj){
    for(var prop in obj){
        if(prop.indexOf("s.") == 0){
            var keys = prop.split(".");
            if (keys.length == 3){

                var key = keys[1];
                if (!sensors[key]){
                    sensors[key] = {};
                }



                sensors[key].name = obj['s.'+ key + ".N"];
                sensors[key].power = obj['s.'+ key + ".P"];
                sensors[key].accuracy = obj['s.'+ key + ".A"];
                sensors[key].version = obj['s.'+ key + ".V"];
                sensors[key].vendor = obj['s.'+ key + ".vd"];
                sensors[key].values = obj['s.'+ key + ".L"];
                if (sensors[key].values){
                    sensors[key].values = sensors[key].values.split(",");
                }



            }
        }
    }


    for(var key in sensors){
        if (sensors[key].values && 0 < sensors[key].values.length ){
            placeSensorInDOM(sensors[key], key)
        }
    }
}


function placeSensorInDOM(sensor, id){
    var div;
    id =  "s_dinfo_" + id;

    if (null == document.getElementById(id) || !document.getElementById(id)){
        div = document.createElement('tr');
        div.setAttribute('class', 'd-info sensor-data');
        div.id = id;
        $('#sensors-data').append(div);
        var innerHTML = ' <td class="dev-lbl sensor-name"> ' + sensor.name + ' ' + sensor.version +  ' </td>';
        innerHTML += ' <td class="dev-lbl sensor-info"> ' + sensor.vendor + ' P'  + sensor.power + ' A' + sensor.accuracy +   ' </td>';
        if (sensor.values){
            innerHTML +='<td id="s_' + id + '_values" class="sensor-values"> ' + sensor.values + '</td>';
        }
        div.innerHTML = innerHTML;
    } else {
        div = document.getElementById(id);
        console.log("place " + id + "REUSE");
    }
}



function updateProps(props){
    if ("vlc" === props.cbin){
        $('#nwi-btn').show();
        document.getElementById('nwif').src = 'http://' + props['vlc-host'];
    }
    else {
        $('#nwi-btn').hide();
    }


    if (true == props.ks || 'true' == props.ks){
        _h();
        $('#controls').show();
    }
}


