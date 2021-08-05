git pull

JVEXE=java
JVC=javac


$JVC -d build/classes src/main/java/com/arise/Builder.java
$JVEXE -cp build/classes  com.arise.Builder $1 ./
sudo $JVEXE -cp out/weland-1.0.jar:libs/* -Dweland.proxy.port=80 com.arise.weland.ProxyMaster