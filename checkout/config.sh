#!/bin/sh

CDIR=$(pwd)
DIR=$1
PROJECT=$2
BUGID=$3
RESULTPATH=$4
PROP="confix.properties"
echo "Create confix.properties for $1"
cp /home/hjs/dldoldam/checkout/$PROP.sample $1/$PROP
cd $1


#Export properties
value=$(defects4j export -p dir.src.classes)
echo "\nsrc.dir=$value" >> $PROP
value=$(defects4j export -p dir.bin.classes)
echo "target.dir=$value" >> $PROP
value=$(defects4j export -p dir.src.tests)
echo "test.dir=$value" >> $PROP
value=$(defects4j export -p cp.compile)
echo "cp.compile=$value" >> $PROP
value=$(defects4j export -p cp.test)
echo "cp.test=$value" >> $PROP
echo "project=$PROJECT" >> $PROP
echo "bugid=$BUGID" >> $PROP
echo "result.path=$RESULTPATH" >> $PROP

#Create test lists
defects4j export -p tests.all > tests.all
defects4j export -p tests.relevant > tests.relevant
defects4j export -p tests.trigger > tests.trigger

cd $CDIR
