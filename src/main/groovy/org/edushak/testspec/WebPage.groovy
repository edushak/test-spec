package org.edushak.testspec

import geb.Page

class WebPage extends Page {
    WebPage(String url) {
        super()
        Page.url = url
    }
}
