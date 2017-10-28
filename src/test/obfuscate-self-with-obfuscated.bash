#!/bin/bash
# make sure files have permission to run
src/test/obfuscate-self.bash
jar=target/result.jar
if [ "${OSTYPE}" == linux* ] || [ "${OSTYPE}" == darwin* ] || [ "${OSTYPE}" == freebsd* ]; then
    chmod 777 ${jar}
fi
java -jar ${jar} \
     --verbose \
     --cfn 3 \
     --keep com/github/jasmo/Bootstrap --keep com/github/jasmo/util/QueryGenerator \
     --package com/github/jasmo \
     target/java-asm-obfuscator-*.jar \
     target/result2.jar