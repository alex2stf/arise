console.log("arise plugin loaded")

var player = document.getElementById('player');
var playerOutro = document.getElementById('player-container-outer');



function continueScan(){
    clickAll('ytp-skip-ad-button');
//    clickAll('yt-spec-touch-feedback-shape yt-spec-touch-feedback-shape--touch-response');//NU
//yt-spec-touch-feedback-shape__fill
//    clickAll('yt-spec-touch-feedback-shape__fill');

//    var accepts = document.querySelectorAll('.yt-spec-touch-feedback-shape.yt-spec-touch-feedback-shape--touch-response-inverse .yt-spec-touch-feedback-shape__fill');
//    for(var i = 0; i < accepts.length; i++) {
//        var x = (accepts[i].innerHTML);
//        if((x + "").toLowerCase().indexOf('accept all') > -1) {
//            console.log(x)
//        }
//    }




//    clickAll('ytp-large-play-button ytp-button');
    clickAll('ytp-ad-skip-button-container ytp-ad-skip-button-container-detached');
}

function clickAll(classes){
    var elems = document.getElementsByClassName(classes);

    for(var i = 0; i < elems.length; i++) {
        try {
            elems[i].click();
            console.log("found  " + classes)
        }catch(ex){

        }

    }
}




setInterval(function(){
    continueScan();
}, 1000 * 2)