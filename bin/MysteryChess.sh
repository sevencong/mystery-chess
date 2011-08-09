#!/bin/ksh
curDir=`pwd`
cd `dirname $0`
appDir=`pwd`
cd $curDir
java -cp $appDir/MysteryChess-2.0.jar mysterychess.Main