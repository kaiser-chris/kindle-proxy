function calibrate() {
    var tests = document.getElementsByClassName("char-test");
    var params = document.location.search;
    if (params.indexOf('?') != -1) {
        params += '&';
    } else {
        params += '?';
    }
    params += 'calibrated=true';
    for (var i = 0; i < tests.length; i++) {
        var element = tests.item(i);
        var ratio = Math.round(((element.clientWidth + 1) / (element.clientHeight + 1)) * 100) / 100;
        params += '&' + encodeURIComponent(element.innerHTML) + '=' + encodeURIComponent(ratio);
    }
    var width = window.innerWidth;
    var height = window.innerHeight;
    params += '&width=' + width;
    params += '&height=' + height;
    document.location.search = params;
}

var calibrationFunction = function executeCalibration() {
    calibrate();
}

window.onload = calibrationFunction;