console.log("arise plugin loaded")

var player = document.getElementById('player');
var playerOutro = document.getElementById('player-container-outer');



function continueScan(){
    clickAll('ytp-skip-ad-button');
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