#!/bin/sh

#this config file needs defects4j 1.1.0
CDIR=$(pwd)
DIR=$1
PROJECT=$2
BUGID=$3
RESULTPATH=$4
PROP="confix.properties"
echo "Create confix.properties for $1"
cp /root/LetsPatch/checkout/$PROP.sample $1/$PROP
cd $1

#Export properties
value=$(/root/defects4j/framework/bin/defects4j export -p dir.src.classes)
echo "\nsrc.dir=$value" >> $PROP
value=$(/root/defects4j/framework/bin/defects4j export -p dir.bin.classes)
echo "target.dir=$value" >> $PROP
value=$(/root/defects4j/framework/bin/defects4j export -p dir.src.tests)
echo "test.dir=$value" >> $PROP
value=$(/root/defects4j/framework/bin/defects4j export -p cp.compile)
echo "cp.compile=$value" >> $PROP
value=$(/root/defects4j/framework/bin/defects4j export -p cp.test)
echo "cp.test=$value" >> $PROP
echo "project=$PROJECT" >> $PROP
echo "bugid=$BUGID" >> $PROP
echo "result.path=$RESULTPATH" >> $PROP

#Create test lists
/root/defects4j/framework/bin/defects4j export -p tests.all > tests.all
/root/defects4j/framework/bin/defects4j export -p tests.relevant > tests.relevant
/root/defects4j/framework/bin/defects4j export -p tests.trigger > tests.trigger

cd $CDIR
