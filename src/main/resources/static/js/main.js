function toggleMenu() {
    var menu = document.getElementById("menu");
    var top = document.getElementById("top");
    var bottom = document.getElementById("bottom");
    if (menu.style.visibility === "hidden") {
        menu.style.visibility = "visible";
        top.style.visibility = "hidden";
        bottom.style.visibility = "visible";
    } else {
        menu.style.visibility = "hidden";
        top.style.visibility = "visible";
        bottom.style.visibility = "hidden";
    }
}

function nextPage(maxPage) {
    changePage(1, maxPage);
}

function prevPage(maxPage) {
    changePage(-1, maxPage);
}

function getUrlParam(name){
    var results = new RegExp('[\?&]' + name + '=([^&#]*)').exec(window.location.href);
    if (results == null){
        return null;
    }
    else {
        return decodeURI(results[1]) || 0;
    }
}

function changePage(modPage, maxPage) {
    var page = getUrlParam('page');
    if (page == null) {
        page = 1;
    }
    var currentPage = parseInt(page);
    var newPageNumber = currentPage + modPage;
    var url = updateURLParameter(window.location.href, 'page', newPageNumber);
    // Transition Page handling
    if (newPageNumber > maxPage || newPageNumber < 1 || currentPage == 0 || currentPage > maxPage) {
        window.location.href = url;
    } else {
        window.history.pushState({},'', url);
        activatePage(newPageNumber, maxPage)
    }
}

function activatePage(pageNumber, maxPage) {
    var pages = document.getElementsByClassName("page-content");

    for (var i = 0; i < pages.length; i++) {
        if ((i + 1) == pageNumber) {
            pages.item(i).setAttribute('class', 'page-content active');
        } else {
            pages.item(i).setAttribute('class', 'page-content hidden');
        }
    }

    var footerPageCounter = document.getElementById("footer-right");
    if (footerPageCounter != null) {
        footerPageCounter.textContent = 'Page ' + pageNumber + ' of ' + maxPage;
    }
}

function updateURLParameter(url, param, paramVal) {
    var TheAnchor = null;
    var newAdditionalURL = "";
    var tempArray = url.split("?");
    var baseURL = tempArray[0];
    var additionalURL = tempArray[1];
    var temp = "";

    if (additionalURL) {
        var tmpAnchor = additionalURL.split("#");
        var TheParams = tmpAnchor[0];
        TheAnchor = tmpAnchor[1];
        if(TheAnchor)
            additionalURL = TheParams;

        tempArray = additionalURL.split("&");

        for (var i=0; i<tempArray.length; i++) {
            if (tempArray[i].split('=')[0] != param) {
                newAdditionalURL += temp + tempArray[i];
                temp = "&";
            }
        }
    } else {
        var tmpAnchor = baseURL.split("#");
        var TheParams = tmpAnchor[0];
        TheAnchor = tmpAnchor[1];

        if (TheParams) {
            baseURL = TheParams;
        }
    }

    if (TheAnchor) {
        paramVal += "#" + TheAnchor;
    }

    var rows_txt = temp + "" + param + "=" + paramVal;
    return baseURL + "?" + newAdditionalURL + rows_txt;
}
