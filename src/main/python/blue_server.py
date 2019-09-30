from bluetooth import *

'''


server_socket=BluetoothSocket( RFCOMM )

server_socket.bind(("", 3 ))
server_socket.listen(1)

client_socket, address = server_socket.accept()

data = client_socket.recv(1024)

print "received [%s]" % data

client_socket.close()
server_socket.close()





name = "BluetoothChat"
uuid = "fa87c0d0-afac-11de-8a39-0800200c9a66"

server_sock = bluetooth.BluetoothSocket( bluetooth.RFCOMM )
server_sock.bind(("", bluetooth.PORT_ANY))
server_sock.listen(1)
port = server_sock.getsockname()[1]

bluetooth.advertise_service( server_sock, name, uuid )

print "Waiting for connection on RFCOMM channel %d" % port

class echoThread(threading.Thread):
  def __init__ (self,sock,client_info):
    threading.Thread.__init__(self)
    self.sock = sock
    self.client_info = client_info
  def run(self):
    try:
      while True:
        data = self.sock.recv(1024)
        if len(data) == 0: break
        print self.client_info, ": received [%s]" % data
        self.sock.send(data)
        print self.client_info, ": sent [%s]" % data
    except IOError:
      pass
    self.sock.close()
    print self.client_info, ": disconnected"

while True:
  client_sock, client_info = server_sock.accept()
  print client_info, ": connection accepted"
  echo = echoThread(client_sock, client_info)
  echo.setDaemon(True)
  echo.start()

server_sock.close()
print "all done"


'''

'''
sudo vim /lib/systemd/system/bluetooth.service

ExecStart=/usr/lib/bluez5/bluetooth/bluetoothd -E -C

sudo sdptool add SP

sudo systemctl daemon-reload

sudo systemctl restart bluetooth

'''

from bluetooth import *

server_sock=BluetoothSocket( RFCOMM )
server_sock.bind(("",PORT_ANY))
server_sock.listen(1)

port = server_sock.getsockname()[1]

# uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"
uuid = "fa87c0d0-afac-11de-8a39-0800200c9a66"

advertise_service( server_sock, "SampleServer",
                   service_id = uuid,
                   service_classes = [ uuid, SERIAL_PORT_CLASS ],
                   profiles = [ SERIAL_PORT_PROFILE ])

print("Waiting for connection on RFCOMM channel %d" % port)

client_sock, client_info = server_sock.accept()

print("Accepted connection from ", client_info)

#this part will try to get something form the client
# you are missing this part - please see it's an endlees loop!!
try:
  while True:
    data = client_sock.recv(1024)
    if len(data) == 0: break
    print("received [%s]" % data)

# raise an exception if there was any error
except IOError:
  pass

print("disconnected")

client_sock.close()
server_sock.close()