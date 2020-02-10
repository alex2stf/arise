
function playYoutubeVideo(){
	var playBtn = document.getElementsByClassName('ytp-play-button ytp-button');
	if(playBtn){
		playBtn = playBtn[0];
	}
	if(playBtn && playBtn.title){
		var playTxt = playBtn.title.toLowerCase();
		if(playTxt.indexOf('play') > -1){
			playBtn.click();
		}
	}
}

function skipAd(){
	var adArea = document.getElementById('player-container-outer');
	if(adArea){
		var skipBtn = document.getElementsByClassName('ytp-ad-skip-button-slot');
		if(skipBtn){
			skipBtn = skipBtn[0];
		}
		if(skipBtn){
			console.log("skip youtube AD");
			skipBtn.click();
		}
	}
	else {
		console.log("no youtube AD to skip");
	}
	return false;
}

setTimeout(function(){
	skipAd();
}, 10 * 1000);

setTimeout(function(){
	playYoutubeVideo();
}, 20 * 1000);