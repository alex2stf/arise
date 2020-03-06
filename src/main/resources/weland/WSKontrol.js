var WSKontrol = (function(){
    var ips = [{{ipv4Addrs}}],
    port = {{port}},
    protocol = "{{protocol}}",
        id = "{{id}}",
        opened = false,
        ws = null,
        call = null,
    buffer = [];

    function connect(){
        for (var i = 0; i < ips.length; i++){
            var url = protocol + "://" + ips[i] + ":" + port + "/kontrol";
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
                    console.log("RECEIVED " + msg, "call= " + call)
                    if (call != null){
                        var pts = msg.split("|");
                        call(pts[0], +pts[1])
                    }
                };

                ws.onclose = function() {
                    console.log("CLOSING")
                };

                break;
            }
        }
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

