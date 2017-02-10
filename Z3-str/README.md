Z3-str2 is a string theory plug-in built on the powerful Z3 SMT solver.

Z3-str2 treats strings as a primitive type, thus avoiding the inherent limitations 
observed in many existing solvers that encode strings in terms of other primitives.


# Documentations:

Please refer to [https://sites.google.com/site/z3strsolver/](https://sites.google.com/site/z3strsolver/)

install following tools if necessary
sudo apt-get install autoconf
sudo apt-get install tofrodos
sudo ln -s /usr/bin/fromdos /usr/bin/dos2unix 
sudo apt-get install g++

# Install Z3-str2

1. Check out the latest version of Z3-str2 from the git repo.


2. Download [the source code of Z3 4.1.1](http://z3.codeplex.com/releases/view/95640)
   
  MD5 SUM: ```c0c2367e4de05614a80b3f62480c23db  z3-src-4.1.1.zip```


3. Unzip "z3-src-4.1.1.zip" and patch the original Z3. Z3-str2 will work with 
   the modified Z3 core.
   * How to patch Z3 core. Suppose the folder name after unzipping is "z3".

     *  $ cp z3.patch z3/
     *  $ cd z3
     *  $ patch -p0 < z3.patch
   
   
4. In the top level folder of Z3 Build the modifed version of Z3
   * $ autoconf
   
   * $ ./configure
   
   * $ make
   
   * $ make a
   
   
5. Build  Z3-str2
   * Modify variable "Z3_path" in the Z3-str2 Makefile to indicate the patched Z3 location.

     * $ make

       
6. Setup Z3-str2 driver script
   * In "Z3-str2.py", change the value of the variable "solver" to point to the 
     Z3-str2 binary "str" just built
 
 
7. Run Z3-str2
   *  ```Z3-str2.py -f <inputFile>```, e.g 
   
     *  $./Z3-str.py -f tests/concat-002
chmod +x str Z3-str.py (if necessary)
