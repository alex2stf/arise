function showLinkage() {
    _h();
    $('#lnk').show();
}

function testConnection(){
    var h =_g('ipin').value;
    if(!h.indexOf('http') == 0){
        h = 'http://' + h;
    }
    gsfh(h, function (r) {
        connectionAdd(r.N, h);
    })
}

function getConnectionAndUpdateUi() {
    $.get(host + '/connections', function (x) {
        updateConnectionsUi(x);
    });
}

function connectionAdd(n, h) {
    $.get(host + '/connections?name='+ n + "&host=" + h, function (x) {
        updateConnectionsUi(x);
    });
}


function updateConnectionsUi(CNX) {
    _g('cnxlst').innerHTML = '';
    var txt = '';
    for (var p in CNX){
        var dm = '<div class="item">\n' +
            '            <span> ' + p + '</span>\n' +
            '            <button onclick="dvput(\'' +  CNX[p] + '\')">connect</button>\n' +
            '            <button onclick="dvrem(\'' + p + '\')">remove</button>\n' +
            '        </div>';
        txt += dm;
    }

    _g('cnxlst').innerHTML = txt;
}


function dvput(x){
    window.location.href = x + '/app';
}

$(document).ready(function(){
    console.log('doc ready', showSettingsView());
    getDeviceStat();
    showLinkage();
    getConnectionAndUpdateUi();
})