#!/bin/bash
if [ ! -d 'src' ] || [ ! -d 'target' ] || [ ! -f 'pom.xml' ]
then
    echo 'We appear to be in the wrong directory, this must be ran from the project root'
    echo $(dir)
    exit -1
fi
mvn package
jar=target/java-asm-obfuscator-*.jar
java -jar ${jar} \
     --verbose \
     --keep com/github/jasmo/Bootstrap --keep com/github/jasmo/util/QueryGenerator \
     --package com/github/jasmo \
     ${jar} \
     target/result.jar