#pip install PyQtWebEngine
import http.server
import socketserver
import sys
from PyQt5.QtCore import *
from PyQt5.QtCore import QUrl
from PyQt5.QtNetwork import *
from PyQt5.QtWebEngineCore import QWebEngineUrlRequestInterceptor, QWebEngineUrlRequestInfo
from PyQt5.QtWebEngineWidgets import QWebEngineProfile
from PyQt5.QtWebEngineWidgets import QWebEngineSettings as QWebSettings
from PyQt5.QtWebEngineWidgets import QWebEngineView as QWebView
from PyQt5.QtWidgets import *
from PyQt5.QtWidgets import QApplication, QWidget
from http.server import BaseHTTPRequestHandler

window = None



class Browser(QWebView):
    def __init__(self):

        # QWebView
        self.view = QWebView.__init__(self)
        # self.view.setPage(MyBrowser())
        self.setWindowTitle('Loading...')
        self.titleChanged.connect(self.adjustTitle)
        self.loadFinished.connect(self.handleLoadFinished)
        self.load

        # super(Browser, self).__init__()
        # self.page_.mainFrame().evaluateJavaScript("alert('hi')")
        # super(Browser).connect(self.ui.webView,QtCore.SIGNAL("titleChanged (const QString&amp;)"), self.adjustTitle)

    def handleLoadFinished(self, ok):
        if ok:
            print("Page loaded successfully")
            self.page().runJavaScript("alert('hi')");
            # self.get_marker_coordinates()
        else:
            print("Could not load page")

    def load(self, url):
        self.setUrl(QUrl(url))

    def adjustTitle(self):
        self.setWindowTitle(self.title())

    def disableJS(self):
        settings = QWebSettings.globalSettings()
        settings.setAttribute(QWebSettings.JavascriptEnabled, False)


class Page(QWidget):
    browser = None
    def __init__(self, parent=None):             # __init__
        super(Page, self).__init__(parent)       # __init__

        # my_label = QLabel("This is my labet")
        layout   = QVBoxLayout()

        self.browser = Browser()
        self.browser.load("https://www.youtube.com/watch?v=raTMa8MneTY")
        layout.addWidget(self.browser)

        self.setLayout(layout)
        self.setWindowTitle("qt browser")

    def loadUrl(self, arg):
        self.browser.load(arg)


class HttpServerHandler(BaseHTTPRequestHandler):


    def do_GET(self):
        self.send_response(200)
        global window
        window.loadUrl("https://jira.orange.ro/browse/MCC-3353")
        # if '/cc-transactions' == self.path:
        self.send_header("Content-type", "text/html")
        self.end_headers()
        self.wfile.write(bytes("ok", "utf-8"))

def start_http_server(port=8067, host="localhost", serverHandler = http.server.BaseHTTPRequestHandler):
    # webServer = http.server.HTTPServer((host, port), serverHandler)
    webServer = socketserver.TCPServer((host, port), serverHandler)
    print("Server started http://%s:%s" % (host, port))
    try:
        webServer.serve_forever()
    except KeyboardInterrupt:
        pass

    webServer.server_close()
    print("Server stopped.")


class WebEngineUrlRequestInterceptor(QWebEngineUrlRequestInterceptor):

    def interceptRequest(self, info: QWebEngineUrlRequestInfo):
        url = info.requestUrl().url()
        # print(url)
        if "ad" in url:
            print("redirect " + url)
            info.redirect(QUrl('https://www.google.com/'))
        # if url == "https://cdn.sstatic.net/Shared/stacks.css?v=596945d5421b":
        #     self.patch_css(url)
        #     print('Using local file for', url)
            # info.redirect(QtCore.QUrl('http:{}:{}/local_stacks.css'.format(HOST, PORT)))


if __name__ == '__main__':
    QNetworkProxyFactory.setUseSystemConfiguration(True)
    # QWebSettings.globalSettings().setAttribute(QWebSettings.PluginsEnabled, True)
    # QWebSettings.globalSettings().setAttribute(QWebSettings.DnsPrefetchEnabled, True)
    # QWebSettings.globalSettings().setAttribute(QWebSettings.JavascriptEnabled, True)
    # QWebSettings.globalSettings().setAttribute(QWebSettings.OfflineStorageDatabaseEnabled, True)
    # QWebSettings.globalSettings().setAttribute(QWebSettings.AutoLoadImages, True)
    # QWebSettings.globalSettings().setAttribute(QWebSettings.LocalStorageEnabled, True)
    # QWebSettings.globalSettings().setAttribute(QWebSettings.PrivateBrowsingEnabled, True)
    # QWebSettings.globalSettings().setAttribute(QWebSettings.DeveloperExtrasEnabled, True)
    # serverThred = Thread(start_http_server, (8067, 'localhost', HttpServerHandler))


    # serverThred.start()

    app = QApplication(sys.argv)

    interceptor = WebEngineUrlRequestInterceptor()
    QWebEngineProfile.defaultProfile().setRequestInterceptor(interceptor)
    window = Page()
    # window.loadUrl("https://jira.orange.ro/browse/MCC-3353")

    # server_handler = HttpServerHandler
    # server_handler.set_page(window)
    # start_http_server(serverHandler=server_handler)
    # _thread.start_new_thread(start_http_server, (8067, 'localhost', HttpServerHandler))


    window.showMaximized()


    window.loadUrl("https://www.youtube.com/watch?v=raTMa8MneTY")
    # view = Browser()
    # view.show()
    # # view.showMaximized()
    # print('show maximized')
    # view.load("https://pythonspot.com")

    sys.exit(app.exec_())


