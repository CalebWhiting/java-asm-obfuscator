<b><big>java-asm-obfuscator (JASMO)</big></b>
=
<div>
    Obfuscates compiled java code to make it harder to reverse engineer.
</div>

Current Obfuscators:
-
<div>
    <u>Inline Accessors</u>
    <div>
        Removes get/set methods, replacing them with direct field access
    </div>
</div>
<br>
<div>
    <u>Remove Debug Information</u>
    <div>
        Removes debugging information left by the compiler such as variable names and line numbers
    </div>
</div>
<br>
<div>
    <u>Scramble Classes</u>
    <div>
        Renames classes, optionally skipping classes provided by the --keep option, all classes are moved to the package provided by the --package option (otherwise to the base package)
    </div>
</div>
<br>
<div>
    <u>Scramble Fields</u>
    <div>
        Renames fields
    </div>
</div>
<br>
<div>
    <u>Scramble Methods</u>
    <div>
        Renames methods, skipping those that are required not to be (such as main), native or are overrides of external libraries (Libraries should be included in java class path).
    </div>
</div>
<br>
<div>
    <u>Scramble Strings</u>
    <div>
        Replaces string constants with an "unscramble" method
    </div>
</div>