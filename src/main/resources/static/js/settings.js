function adjustTextSize(increase) {
    var sizeElement = document.getElementById("settings-font-size-value");
    var sizeFormElement = document.getElementById("textSize");
    var currentSize = sizeElement.textContent;
    var index = scale.indexOf(+currentSize);

    if (increase == true) {
        if (index >= (scale.length - 1)) {
            return;
        }
        index++;
    } else {
        if (index <= 0) {
            return;
        }
        index--;
    }
    sizeElement.textContent = scale[index];
    sizeFormElement.setAttribute('value', scale[index]);
    renderTextSize();
}

function renderTextSize() {
    var sizeElement = document.getElementById("settings-font-size-value");
    var currentSize = sizeElement.textContent;
    var index = scale.indexOf(+currentSize);
    var boxes = document.getElementsByClassName("size-box");

    for (var i = 0; i < boxes.length; i++) {
        if (i <= index) {
            boxes.item(i).setAttribute('class', 'size-box active');
        } else {
            boxes.item(i).setAttribute('class', 'size-box');
        }
    }
}