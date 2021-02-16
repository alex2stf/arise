function showSettingsView() {
    _h();
    $('#sting').show();
}

function gsfh(x, h){
    $.get(x + '/device/stat', function (data) {
        h(data);
    });
}


function getDeviceStat(){
    gsfh(host, function (d) {
        updateUi(d);
    })
}

function updateUi(x) {

    //read from properties:
    if (x.pP){
        updateProps(x.pP);
    }

    if (x.D) {
        document.getElementById('d-name').innerHTML = x.D;
    }

    if (x.B1){
        document.getElementById('d-bscale').innerHTML = x.B1;
    }

    if (x.B2){
        document.getElementById('d-blevel').innerHTML = x.B2;
    }

    if (x.pP){
        readSensors(x.pP);
    }

    if (x.I4){
        $('#sips').html(x.I4.join(', '));
    }

}



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


