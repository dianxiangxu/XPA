# XPA
A toolkit for analyzing XACML3.0 policies. The main features are:  
(1) Editing GUI;   
(2) Coverage-based test generation (rule coverage, decision coverage, MC/DC coverage, rule pair coverage);   
(3) Generation of mutants;   
(4) Execution of a test suite for a policy and its mutants.  
  
  
## building Z3-str  
Go to the root folder of XPA
clone the Z3 repository from https://github.com/z3prover/z3 as z3
cd z3
python scripts/mk_make.py
cd build ; make


## test whether Z3-str works:  
if you are still inside build folder from z3, run ./z3 --version or if you are in XPA root, run ./z3/build/z3 --version


## Using Eclipse for the XPA 
install AJDT(ApspectJ) plugin
install m2e(Maven) plugin


## Using maven from command line to build the project
If eclipse is used, the maven plugin for eclipse will automatically build the project 
To use maven from commandline instead use, 
i) install maven from https://maven.apache.org/install.html#
ii) go to the root of the XPA
iii) run 'mvn install'


## Run XPA
Run the mail class file org.seal.xacml.xpa.XPA


## running legacy code with aspectJ capability
The aspectJ file in legacy code uses the same point cuts as in the new code, causing conflicts when aspectJ capacity is needed. Hence the file name extension of org.seal.coverage.PolicyTracer.aj is changed to aj_ so that it will not be recognized by aspectJ compiler. When aspectJ capability is needed for running the legacy code, e.g. running tests in GUI, change the suffix name back to aj, and change the file name extension of org.seal.semanticCoverage.SematicPolicyTracer.aj to avoid conflicts.
