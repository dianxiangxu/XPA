package org.seal.mcdc;
//package mcdc;
//$Id: ProductTerm.java,v 1.2 2005/02/21 04:19:00 vickery Exp $
/*
 *  Author:     C. Vickery
 *
 *  Copyright (c) 2000-2005, Queens College of the City University
 *  of New York.  All rights reserved.
 * 
 *  Redistribution and use in source and binary forms, with or
 *  without modification, are permitted provided that the
 *  following conditions are met:
 *
 *      * Redistributions of source code must retain the above
 *        copyright notice, this list of conditions and the
 *        following disclaimer.
 * 
 *      * Redistributions in binary form must reproduce the
 *        above copyright notice, this list of conditions and
 *        the following disclaimer in the documentation and/or
 *        other materials provided with the distribution.  
 * 
 *      * Neither the name of Queens College of CUNY
 *        nor the names of its contributors may be used to
 *        endorse or promote products derived from this
 *        software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 *  CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 *  GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 *  BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 *
 *  $Log: ProductTerm.java,v $
 *  Revision 1.2  2005/02/21 04:19:00  vickery
 *  Continuing GUI development.
 *  Fixed TruthTable to give variable names in alphabetic order, and to sort
 *  minterm numbers.
 *
 *  Revision 1.1  2005/02/12 16:51:22  vickery
 *  Revised to use generics for various Vectors and Enumerations.
 *
 *
 */

//  Class ProductTerm
//  ------------------------------------------------------------------
/**
  *   Represents a product term in a boolean expression.
  *
  *   @version  1.0 - Fall, 2000
  *   @author   C. Vickery
  */
  public class ProductTerm implements Comparable
  {
    public final static ProductTerm identity =
                                  new ProductTerm( 0, 0, new String[0] );
    int     value;
    int     mask;
    int     numLiterals;
    String[]  variableNames;
    int     numVars;
    int     coverCount;   //  If this is a minterm, how many
                          //  prime implicants cover it.


  //  Constructor
  //  ----------------------------------------------------------------
  /**
    *   Initialized a product term object with the bitmap
    *   representation the values of the variables and a bitmask
    *   indicating which variables are used in the term.  Needs a
    *   reference to the array of variable names so toString() can
    *   return a properly formatted representation of the term.
    */
    public ProductTerm( int value, int mask, String[] variableNames )
    {
      this.value = value;
      this.mask  = mask;
      this.numLiterals = BitManipulation.countBits( mask );
      this.variableNames = variableNames;
      this.numVars = variableNames.length;
      this.coverCount = 0;
    }

    public int
    getValue()          { return value;                   }

    public int
    getMask()           { return mask;                    }

    public String[]
    getVariableNames()  { return variableNames;           }

    public int
    getNumLiterals()    { return numLiterals;             }

    public boolean
    isMinterm()         { return numLiterals == numVars;  }

    public void clearCoverCount()
    {
      coverCount = 0;
    }

    public void incrementCoverCount()
    {
      coverCount++;
    }

    public int getCoverCount() { return coverCount; }


  //  Method compareTo()
  //  --------------------------------------------------------------
  /**
    *   The size of a product term is defined here as the number of
    *   literals it contains.
    *   This implementation of the Comparable interface is NOT
    *   consistent with equals().
    */
    public int compareTo( Object x )
    {
      return ((ProductTerm)x).numLiterals - numLiterals;
    }

    //  Method reduces()
    //  --------------------------------------------------------------
    /**
      *   Returns true if this and target can be reduced by eliminating
      *   one variable.
      *
      *   @param target   The ProductTerm to be combined with this one.
      *   @param reduced  The potentially reduced product term, which
      *                   will have its value and mask set if reduction
      *                   can be done.
      *   @return true if the two terms can be reduced.
      */
      public ProductTerm reduces( ProductTerm target )
      {
        if ( target.mask == mask ) // same variables?
        {
          if ( mask == 0 )
            return identity;

          int difference = (target.value & mask) ^ (value & mask);
          if ( 1 == BitManipulation.countBits( difference ) )
          {
          ProductTerm reduced = new ProductTerm( ~difference & value,
                                                 ~difference & mask,
                                                 variableNames );
          return reduced;
          }
        }
        return null;
      }


    //  Method equals()
    //  --------------------------------------------------------------
    /**
      *   Does this term equal the target?  That is, does this term
      *   have any variables in common with the target, and if so do
      *   they have the same values?
      *
      *   @param target The product term to be tested for equality.
      *   @return       True if this term implies the target.
      */
    public boolean equals( ProductTerm target )
    {

      return (value == target.getValue()) &&
             (mask == target.getMask());
    }


    //  Method covers()
    //  --------------------------------------------------------------
    /**
      *   Does this term cover the target?  That is, will the target
      *   be true whenever this is true?
      *
      *   @param target The product term to be tested for coverage.
      *   @return       True if this term implies the target.
      */
    public boolean covers( ProductTerm target )
    {
      return (value & mask) == (target.getValue() & mask);
    }


  //  Method ptString()
  //  ----------------------------------------------------------------
  /**
    *   Don't ask ...
    */
    public String ptString()
    {
      if ( mask == 0 ) return "1";
      StringBuffer sb = new StringBuffer();
      for (int i=numVars-1; i>=0; i--)
      {
        if ( (mask & (1<<i)) != 0 )
        {
        	//System.out.println("var:"+variableNames[(numVars-1)-i]);
          sb.append( variableNames[(numVars-1)-i]+',');
          if ( (value & (1<<i)) == 0)
            sb.append( '!' );  // Negate
          //sb.append(',');
        }
      }
      return new String( sb );
    }


  //  Method toString()
  //  ----------------------------------------------------------------
  /**
    *   Returns the printable representation of the product term.
    */
    public String toString()
    {
      return ptString();
    }

  }
