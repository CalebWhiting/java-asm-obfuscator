# java-asm-obfuscator (jasmo)
Obfuscates compiled java code to make it harder to reverse engineer.

```text
usage: java -jar jasmo.jar <src> <dest> [-h] [-k <arg>] [-p <arg>] [-v]
 -h,--help            Print help message
 -k,--keep <arg>      Don't rename this class
 -p,--package <arg>   Move obfuscated classes to this package
 -v,--verbose         Increase verbosity
 ```
 
Current Obfuscators:
-
<dl>
    
    <dt>Inline Accessors<br></dt>
    <dd>Removes get/set methods, replacing them with direct field access</dd>
    
    <dt>Remove Debug Information</dt>
    <dd>Removes debugging information left by the compiler such as variable names and line numbers</dd>
    
    <dt>Scramble Classes</dt>
    <dd>Renames classes, optionally skipping classes provided by the --keep option, all classes are moved to the package provided by the --package option (otherwise to the base package)</dd>
    
    <dt>Scramble Fields</dt>
    <dd>Renames fields</dd>
    
    <dt>Scramble Methods</dt>
    <dd>Renames methods, skipping those that are required not to be (such as main), native or are overrides of external libraries (Libraries should be included in java class path).</dd>
    
    <dt>Scramble Strings</dt>
    <dd>Replaces string constants with an "unscramble" method</dd>

</dl>
-
