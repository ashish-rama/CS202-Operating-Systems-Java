Ashish Ramachandran
ar3986
Lab #1: Linker Lab (Java)

To compile: javac Lab1_ar3986.java
To run: java Lab1_ar3986 [text document input name with ".txt" extention]

The way I went about solving the lab was to read the input file and store everything in a 3d Object array. There is a specific array for each module (based on input file) each with its own symbol definition, symbol use list, and program text arrays. 

I created a class called "Symbol" to help keep information stored for each defined symbol in the input file including which module it belongs to and how many times it was used. 

I then went about creating the symbol table by calculating absolute addresses and then printed the memory map using the guidelines set for us online.