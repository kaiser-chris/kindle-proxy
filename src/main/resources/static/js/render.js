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