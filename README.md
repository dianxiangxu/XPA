# XPA
A toolkit for analyzing XACML3.0 policies. The main features are:  
(1) Editing GUI;   
(2) Coverage-based test generation (rule coverage, decision coverage, MC/DC coverage, rule pair coverage);   
(3) Generation of mutants;   
(4) Execution of a test suite for a policy and its mutants.  
  
## building Z3-str  
Source code of Z3-str and Z3 are included in this repository. To get executable file, you would need to compile it on your computer. For sake of portability, please do not commit executable files and temporary file lile .o .so .a files into repository.  
  
Before compiling:  
You might need to install some tools on your computer to compile the code. Run the following commands to install them, if necessary.  
  
sudo apt-get install autoconf  
sudo apt-get install tofrodos  
sudo ln -s /usr/bin/fromdos /usr/bin/dos2unix   
sudo apt-get install g++  
  
How to compile Z3-str:  
from top level folder of XPA project  
cd Z3-str/z3/  
autoconf  
./configure  #if the file 'configure' do not have permission to be executed, run the commmand 'sudo chmod +x configure'  
make	#this may take about 15 minutes  
make a  
cd ..  
make  
  
test whether Z3-str works:  
./Z3-str.py -f tests/concat-002	#if the files Z3-str.py and str do not have executable permission, change permission use the commmand 'sudo chmod +x Z3-str.py str'  
