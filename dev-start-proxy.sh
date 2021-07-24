git pull

JVEXE=java
JVC=javac


$JVC -d build/classes src/main/java/com/arise/Builder.java
$JVEXE -cp build/classes  com.arise.Builder $1 ./
$JVEXE -cp out/weland-proxy-1.0.jar:libs/* com.arise.weland.ProxyMaster