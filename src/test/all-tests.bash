#!/bin/bash
echo 'Running Test [1]' && src/test/obfuscate-self.bash && \
echo 'Running Test [2]' && src/test/obfuscate-self-with-obfuscated.bash
