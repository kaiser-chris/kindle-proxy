function calibrate() {
    var tests = document.getElementsByClassName("char-test");
    var params = document.location.search;
    var params = '';
    console.log(params);
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
    document.location.search = params;
}

var calibrationFunction = function executeCalibration() {
    calibrate();
}

window.onload = calibrationFunction;