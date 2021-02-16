var WSKontrol = (function(){
    var ips = [{{ipv4Addrs}}],
    port = {{port}},
    protocol = "{{protocol}}",
        id = "{{id}}",
        opened = false,
        ws = null,
        call = null,
    buffer = [], tested = {};



    function  tryConnect(index) {
        if (index > ips.length - 1){
            return;
        }



        var url = protocol + "://" + ips[index] + ":" + port + "/kontrol";
        if (tested[url]){
            tryConnect(index + 1);
            return;
        }

        try {
            ws = new WebSocket(url)
        }catch (e) {
            ws = null;
        }

        if (ws != null){
            console.log("ws connection found at " + url);

            ws.onopen = function() {
                opened = true;
                for(var i = 0; i < buffer.length; i++){
                    ws.send(id + "|" + buffer[i]);
                }
                buffer = [];
            };

            ws.onmessage = function (evt) {
                var msg = evt.data;
                console.log("RECEIVED " + msg)
                if (call != null){
                    var pts = msg.split("|");
                    call(pts[0], +pts[1])
                }
            };

            ws.onerror = function(e){
                ws = null;
                opened = false;
                tryConnect( index + 1);
            }

            ws.onclose = function() {
                console.log("CLOSING " + url);
                opened = false;
            };
        } else {
            tryConnect( index + 1);
        }
    }

    function connect(){
        tryConnect(0);
    }






    function  sendTxt(t) {
        if (!opened || ws == null){
            buffer.push(t);
        }
        else {
            ws.send(id + "|" + t);
        }
    }


    return {
        connect: connect,
        send: sendTxt,
        onReceive: function(c){
            console.log("adding callback" + c)
            call = c;
        },

        close: function () {
            ws.close();
            ws = null;
        }
    }

})();

