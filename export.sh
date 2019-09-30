#!/usr/bin/env bash


JAVA_BIN=java
JAVA_CMP=javac
INIT_DIR=$(pwd)

WORK_DIR=~/arise-local-workdir
EXPORT_DIR=~/arise-local/export


export_full(){
    echo "init project export in $EXPORT_DIR"

    FULL_PACK="$EXPORT_DIR/full"


    echo "export full at[ $FULL_PACK ]"
    mkdir -p "$FULL_PACK/com"

    cp -R $INIT_DIR/src/main/java/com/arise "$FULL_PACK/com"


    cd $FULL_PACK

    #remove android package and tests package
    rm -rf com/arise/rapdroid
    rm -rf com/arise/tests

    mkdir -p "$FULL_PACK/classes"
    "$JAVA_CMP" -d $FULL_PACK/classes $FULL_PACK/com/arise/**/*.java

    echo "-----------------------------------"
    echo "--- PROJECT IS COMPILABLE CLEAN ---"
    echo "-----------------------------------"
}

export_cargo(){
    CARGO_PACK="$EXPORT_DIR/cargo"

    echo "export and compile cargo at[ $CARGO_PACK ]"

    mkdir -p "$CARGO_PACK/src/main/java/com/arise"
    mkdir -p "$CARGO_PACK/src/test/java/com/arise"
    mkdir -p "$CARGO_PACK/src/main/resources"
    mkdir -p "$CARGO_PACK/src/test/resources"

    #copy sources, reminiscence included
    cp -R $INIT_DIR/src/main/java/com/arise/cargo "$CARGO_PACK/src/main/java/com/arise"
    cp -R $INIT_DIR/src/main/java/com/arise/core "$CARGO_PACK/src/main/java/com/arise"
    cp -R $INIT_DIR/src/main/java/com/arise/reminiscence "$CARGO_PACK/src/main/java/com/arise"

    #copy src res:
    cp -R $INIT_DIR/src/main/resources/_cargo_ "$CARGO_PACK/src/main/resources"
    cp -R $INIT_DIR/src/test/resources/_cargo_ "$CARGO_PACK/src/test/resources"

    cp -R $INIT_DIR/src/test/java/com/arise/cargo "$CARGO_PACK/src/test/java/com/arise"

    mv "$CARGO_PACK/src/main/java/com/arise/cargo/pom.xml" "$CARGO_PACK"

    cd "$CARGO_PACK"
    mvn clean package
    mvn package



    echo "-----------------------------------"
    echo "------- CARGO TOOLS EXPORTED ------"
    echo "-----------------------------------"
}


export_blue_svr(){
  BLUESVR_PACK="$EXPORT_DIR/bluesrv"
  echo "export and compile bluesvr at[ $BLUESVR_PACK ]"

  mkdir -p "$BLUESVR_PACK/src/com/arise"

  cp -R $INIT_DIR/src/main/java/com/arise/net "$BLUESVR_PACK/src/com/arise"
  cp -R $INIT_DIR/src/main/java/com/arise/core "$BLUESVR_PACK/src/com/arise"

  mkdir -p "$BLUESVR_PACK/classes"

  "$JAVA_CMP" -d "$BLUESVR_PACK/classes" $BLUESVR_PACK/src/com/arise/core/**/*.java
}





to_do(){
NET_PACK="$EXPORT_DIR/net"
echo "export and compile net at[ $NET_PACK ]"

QUIXOT_PACK="$EXPORT_DIR/quixot"
echo "export and compile quixot at[ $QUIXOT_PACK ]"

#last is android with SDK
DROID_PACK="$EXPORT_DIR/rapdroid"
echo "export and compile rapdroid at[ $DROID_PACK ]"
}


export_cargo