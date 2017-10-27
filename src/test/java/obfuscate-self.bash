#!/bin/bash
if [ ! -d 'src' ] || [ ! -d 'target' ] || [ ! -f 'pom.xml' ]
then
    echo 'We appear to be in the wrong directory, this must be ran from the project root'
    echo $(dir)
    exit -1
fi
mvn package
java -jar target/java-asm-obfuscator-*.jar \
     -v \
     -k com/github/jasmo/Bootstrap \
     -p com/github/jasmo \
     target/classes \
     target/result.jar