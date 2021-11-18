Raspberry Pi 4

QT (optional)
sudo apt-get install qt5-default qtcreator qtdeclarative5-dev libqt5webenginewidgets5




cd /etc/systemd/system


touch weland-app.service


create file with the following content:

[Unit]
Description=Weland-App service
After=multi-user.target

[Service]
Type=idle
ExecStart=/usr/bin/java -Dconfig.location=/home/pi/Desktop/arise/application.properties -cp /home/pi/Desktop/arise/out/weland-1.0.jar:/home/pi/Desktop/arise/libs/* com.arise.weland.Main

[Install]
WantedBy=multi-user.target
Alias=weland-app.service


sudo chmod 644 weland-app.service


sudo systemctl daemon-reload
sudo systemctl enable weland-app.service




install fswebcam