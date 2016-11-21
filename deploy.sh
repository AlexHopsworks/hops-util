#!/bin/bash

set -e
git pull

mvn clean install

VERSION=`grep -o -a -m 1 -h -r "version>.*</version" ./pom.xml | head -1 | sed "s/version//g" | sed "s/>//" | sed "s/<\///g"`

echo ""
echo "Deploying version: $VERSION ... to maven repository"
echo ""
mvn  deploy:deploy-file -Durl=scpexe://kompics.i.sics.se/home/maven/repository \
                      -DrepositoryId=sics-release-repository \
                      -Dfile=./target/hopsworks-util-${VERSION}.jar \
                      -DgroupId=io.hops \
                      -DartifactId=hopsworks-util \
                      -Dversion=${VERSION} \
                      -Dpackaging=jar \
                      -DpomFile=./pom.xml \
-DgeneratePom.description="HopsWorks Services Utility"

echo ""
echo "Deploying hopsworks-util-${VERSION}.jar to snurran.sics.se"
echo ""
scp target/hopworks-util-${VERSION}.jar glassfish@snurran.sics.se:/var/www/hops
