#!/bin/bash
# This test is to bulk test the obfuscator
# Because the output is randomly generated it may take multiple tests to find errors
#
# Run this once to make 'target/result.jar'
./src/test/obfuscate-self.bash
#
# Swaps two files
function swap { # (FileA, FileB)
    a=${1}
    b=${2}
    mv "${a}" "${b}.temp"; mv "${b}" "${a}"; mv "${b}.temp" "${b}"
}
#
while true; do
    ./src/test/obfuscate-self-with-obfuscated.bash
    if [ ! "$?" = "0" ]; then exit 1; fi
    # Faster than running obfuscate-self.bash before each attempt
    if [ -f 'target/result.jar' ] && [ -f 'target/result2.jar' ]; then
        swap 'target/result.jar' 'target/result2.jar'
    fi
done