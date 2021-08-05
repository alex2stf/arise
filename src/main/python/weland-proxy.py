import socket, sys, os, threading



serverSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
serverSocket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
serverSocket.bind( ('', 80))
serverSocket.listen(10) # become a server socket
# put the socket into listening mode
# s.listen(5)
print ("socket is listening")


host = '192.168.1.6'
port = 8221


def proxy_thread(client_socket, client_address):
    request = client_socket.recv(81267)
    url = (request.split(b' ')[1]).decode('utf-8')

    if(url.find('/proxy') > -1 and url.find('?') > -1):
        global host, port
        args = url[url.find('?') + 1:len(url)]
        params = args.split('&')
        for param in params:
            keyval = param.split('=');
            if(keyval[0] == 'host'):
                host = keyval[1]
            if(keyval[0] == 'port'):
                port = int(keyval[1])


        print("defined host = " + host + " port=" + str(port))

        body_raw = '<iframe src="/app" style="display: block; width: 100%; height: 100%"></iframe>'
        client_socket.send("HTTP/1.0 200 OK".encode())
        client_socket.send("\nContent-Type:text/html".encode())
        client_socket.send("\nContent-Length:" + str(len(body_raw)).encode() )
        client_socket.send('\n\n'.encode())  # to separate headers from body
        client_socket.send(body_raw.encode())  # to separate headers from body
        client_socket.close()
        return

    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.settimeout(1000)
    s.connect((host, port))
    s.sendall(request)

    while 1:
        # receive data from web server
        data = s.recv(999999)

        if (len(data) > 0):
            client_socket.send(data)  # send to browser/client
        else:
            break


while True:
    (clientSocket, client_address) = serverSocket.accept()
    d = threading.Thread(name=client_address, target=proxy_thread, args=(clientSocket, client_address))
    d.setDaemon(True)
    d.start()


