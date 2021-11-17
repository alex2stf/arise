




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
    update_ui_with_device_stat_into_root('sting', 'stream-controls', d);
}




get_device_stat();
