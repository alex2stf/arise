




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
        console.log('get_device_stat() => ', d);
        update_ui_with_device_stat(d);
    })
}

var latest_data = {},
    last_ui_update_date = new Date()
    _max_update_interval = 1000 * 60 * 30;

function update_ui_with_device_stat(d) {
    update_ui_with_device_stat_into_root('sting', 'stream-controls', d);
    last_ui_update_date = new Date();
}



function update_ui_loop(){
    setTimeout(function(){
        var now = new Date(), diff = now.getTime() - last_ui_update_date.getTime();
        console.log("update_ui_loop(" + diff + "/" + _max_update_interval + ")")
        if (diff > _max_update_interval){
            last_ui_update_date = new Date();
            get_device_stat();
        }
        update_ui_loop();
    }, _max_update_interval);
}




get_device_stat();
update_ui_loop();






function update_ui_with_device_info(nf){
    // update_ui_with_device_info_into_root
}


