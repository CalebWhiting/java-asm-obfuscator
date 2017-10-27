<b><big>java-asm-obfuscator (JASMO)</big></b>
=
<div>
 & Obfuscates compiled java code to make it harder to reverse engineer.
</div>

Current Obfuscators:
-

<div>
 <u>Inline Accessors</u>
 
 Removes get/set methods, replacing them with direct field access
</div>
&
<div>
 <u>Remove Debug Information</u>
 
 Removes debugging information left by the compiler such as variable names and line numbers
</div>
&
<div>
<u>Scramble Classes</u>
  
 Renames classes, optionally skipping classes provided by the --keep option, all classes are moved to the package provided by the --package option (otherwise to the base package)
</div>
&
<div>
<u>Scramble Fields</u>
  
 Renames fields
</div>
&
<div>
<u>Scramble Methods</u>
  
 Renames methods, skipping those that are required not to be (such as main), native or are overrides of external libraries (Libraries should be included in java class path).
</div>
&
<div>
<u>Scramble Strings</u>
  
 Replaces string constants with an "unscramble" method
</div>