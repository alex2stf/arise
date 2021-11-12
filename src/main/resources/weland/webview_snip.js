console.log('js snip loaded at ' + new Date());

/*unmute*/
function unmute(){

    var yum = document.getElementsByClassName('ytp-unmute');
    if(yum && yum[0]){
        yum[0].click();
    }

    console.log('unmute executed');
};

unmute();

setTimeout(function (){
    unmute();
}, 1000 * 3);


function auto_click_elements(items){
        if(items && items.length){
            for(var i = 0; i < items.length; i++){
                if(items[i].innerHTML.indexOf('YES') > -1 ||
                   items[i].innerHTML.indexOf('Yes') > -1 ||
                   items[i].innerHTML.indexOf('Da') > -1 ||
                   items[i].innerHTML.indexOf('DA') > -1 ||
                   items[i].innerHTML.indexOf('da') > -1 ||
                   items[i].innerHTML.indexOf('i agree') > -1 ||
                   items[i].innerHTML.indexOf('I agree') > -1 ||
                   items[i].innerHTML.indexOf('yes') > -1){
                           items[i].click();
                }
            }
        }
}

var nextClickIn = 10;

function auto_click_on(){
    try {
        auto_click_elements(
            document.getElementsByClassName('style-scope yt-button-renderer style-blue-text size-default')
        );
    } catch (e){

    }

    try {
        auto_click_elements(
            document.getElementsByClassName('c3-material-button-button')
        );
    } catch(e) {

    }

    if(nextClickIn < 80){
        nextClickIn += 10;
    }


    console.log('auto_click_v2_on executed,  nextClickIn = ' + nextClickIn);
    setTimeout(function(){
        auto_click_on();
    }, 1000 * nextClickIn)
};

auto_click_on();






