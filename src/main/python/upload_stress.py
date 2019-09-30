#!/usr/bin/env python

import socket
import ssl
from threading import Thread


input = "POST /api/dh/api/documents/4120/upload HTTP/1.1\r\n" \
        "Origin:chrome-extension://fhbjgbiflinjbdggehcddcbncdddomop\r\n" \
        "Authorization:Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbl8xIiwiYXV0aCI6IlJPTEVfQURNSU4iLCJleHRUb2tlbiI6ImFkbWluXzEiLCJpbnRlcm5hbFVzZXIiOnRydWUsImV4cCI6MTU1MDU3NDM0OCwibG9naW5UeXBlIjoiQURNSU4iLCJsb2FucyI6W119.PdXs9rMPHe0peKvsa6r7q30cfknK5YuakjtfUJLn1fcvBYCeL48yY2RbWl4s1QhGkMToLTUMNfOw7y0MF6WyjA\r\n" \
        "Cache-Control:no-cache\r\n" \
        "Accept:*/*\r\n" \
        "Connection:keep-alive\r\n" \
        "User-Agent:Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/71.0.3578.98 Chrome/71.0.3578.98 Safari/537.36\r\n" \
        "Host:gateway.bcr-dev.qualitance.com\r\n" \
        "Postman-Token:8165fcc-0f63-5f05-2ff5-4d72a208640a\r\n" \
        "Accept-Encoding:gzip, deflate, br\r\n" \
        "Accept-Language:en-US,en;q=0.9\r\n" \
        "Content-Length:191\r\n" \
        "Content-Type:multipart/form-data; boundary=----WebKitFormBoundaryITN2oJdQ21C8zKdp\r\n" \
        "\r\n" \
        "------WebKitFormBoundaryITN2oJdQ21C8zKdp\r\n" \
        "Content-Disposition: form-data; name=\"file\"; filename=\"test.pdf\"\r\n" \
        "Content-Type: application/pdf\r\n" \
        "\r\n" \
        "xxx" \
        "\r\n" \
        "\r\n" \
        "------WebKitFormBoundaryITN2oJdQ21C8zKdp--\r\n"

TCP_IP = 'gateway.bcr-dev.qualitance.com'
TCP_PORT = 443

BUFFER_SIZE = 1024

def connect(x):
        print 'sending ' + str(x)
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        context = ssl.create_default_context(ssl.Purpose.SERVER_AUTH)
        context.options |= ssl.OP_NO_TLSv1 | ssl.OP_NO_TLSv1_1  # optional
        conn = context.wrap_socket(sock, server_hostname=TCP_IP)

        conn.connect((TCP_IP, TCP_PORT))
        conn.send(input)
        data = conn.recv(BUFFER_SIZE)
        conn.close()
        print str(x) + ") received data:", data


for x in range(222):
        thread1 = Thread(target = connect, args = (x, ))
        thread1.start()




'''

input = "GET /api/dh/api/loans/333 HTTP/1.1\r\n" \
        "Host: gateway.bcr-dev.qualitance.com\r\n" \
        "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbl8xIiwiYXV0aCI6IlJPTEVfQURNSU4iLCJleHRUb2tlbiI6ImFkbWluXzEiLCJpbnRlcm5hbFVzZXIiOnRydWUsImV4cCI6MTU1MDMxMTI2MiwibG9naW5UeXBlIjoiQURNSU4iLCJsb2FucyI6W119.UhsaG4gz-JXrSkLW28RP2CPMLSe6n6W8-1g41a_MYZ2l1WN7LiAowM0f12uTuDZbQKJGqwDMXQ74bTWGsjagtQ\n" \
        "Cache-Control: no-cache\r\n" \
        "Postman-Token: 7489dd1c-a6f2-6425-1a28-72308617acef\n\n"

TCP_IP = '172.17.0.1'
TCP_PORT = 8080

BUFFER_SIZE = 2024


s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect((TCP_IP, TCP_PORT))
s.send(input)
data = s.recv(BUFFER_SIZE)
s.close()

print "received data:", data

'''