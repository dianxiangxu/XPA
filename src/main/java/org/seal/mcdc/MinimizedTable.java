package org.seal.mcdc;
//package mcdc;
//$Id: MinimizedTable.java,v 1.3 2005/02/21 23:32:15 vickery Exp $
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
 *  $Log: MinimizedTable.java,v $
 *  Revision 1.3  2005/02/21 23:32:15  vickery
 *  Completed GUI.  Don't know how to scroll
 *  within a table cell, and list of covers can
 *  be too long to see sometimes.
 *
 *  Revision 1.2  2005/02/21 04:19:00  vickery
 *  Continuing GUI development.
 *  Fixed TruthTable to give variable names in alphabetic order, and to sort
 *  minterm numbers.
 *
 *  Revision 1.1  2005/02/20 04:24:00  vickery
 *  Started developing GUI.
 *
 *  Revision 1.1  2005/02/12 16:51:22  vickery
 *  Revised to use generics for various Vectors and Enumerations.
 *
 *
 */

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

//  Class MinimizedTable
//  -------------------------------------------------------------------
/**
  *   Generates minimized sum of products representation of a truth
  *   table.
  *
  *   @version  1.1 - Fall, 2000
  *   @author   C. Vickery
  */
  public class MinimizedTable extends TruthTable
  {
    static final long serialVersionUID = 6365229939599366603L;
    
    PrintStream out = System.out;
/** Maximum number of passes needed to find prime implicants.   */
    protected final int     numLevels       = numVars + 1;

    /** Housekeeping array of vectors for partially-reduced product
        terms                                                       */
    protected Vector<ProductTerm>[] levelTerms
                                            = new Vector[ numLevels ];

    /** The prime implicants for this truth table.                  */
    protected Vector<PrimeImplicant>  primeImplicants;

    Vector<ProductTerm> minimum; // Only one minimization for now.

  //  Accessors
  //  ----------------------------------------------------------------
  /*
   *  These are to support the Table Model interface.  I've repeated
   *  some that would be inherited from TruthTable, for clarity.
   */
    public int      getRowCount() { return primeImplicants.size(); }
    public int      getColumnCount()  { return 2; }
    public boolean  isCellEditable(int row, int col) { return false; }
    public String   getColumnName(int col)
    {
      switch (col)
      {
        case 0:
            return "Prime Implicant";
        case 1:
            return "Implied Terms";
        default:
          throw new RuntimeException("Program Error: Bad switch");
      }
    }
    public Object   getValueAt(int row, int col)
    {
      switch (col)
      {
        case 0:
            return primeImplicants.elementAt(row).getImplicantString();
        case 1:
            return primeImplicants.elementAt(row).getCovers();
        default:
          throw new RuntimeException("Program Error: Bad switch");
      }
    }


  //  Constructors
  //  ================================================================
  //  ----------------------------------------------------------------
  /**
    *   Creates a minimized sum of products function equivalent to the
    *   list of minterms provided by class TruthTable.
    *
    *   Algorithm:
    *   1.  Initialize the truth table, and create a list of all prime
    *       implicants for the truth table.
    *   2.  Select minimal sets of prime implicants that will cover
    *       all minterms.
    *
    *   @param  s   A text string representing the boolean function to
    *               be minimized.
    */
    public MinimizedTable( ArrayList<String> s )
    {
      super( s );
      minimizeIt();
    }
    
  //  Method minimizeIt()
  //  ----------------------------------------------------------------
  /**
    *   Minimizes a truth table by first determining all prime
    *   implicants and then selecting minimal sets of prime implicants
    *   that cover all minterms using the Quine-McCluskey "chart"
    *   method.
    */
    private void minimizeIt()
    {
      /*  Determine prime implicants.
       *    Terms are coalesced using the principle of
       *    complementation, which says that A(x + x') == A.  All
       *    minterms are put into a "Level zero" vector, then as many
       *    as numVars "passes" are made applying the rule of
       *    complementation to all pairs of product terms, passing
       *    newly-reduced and irreducible terms from one level to the
       *    next until all remaining product terms are irreducible.
       *    (Prime implicants, by definition, are product terms that
       *    cannot be reduced any further and still cover only
       *    minterms of the truth table.)
       */

      //  Level zero has all minterms.
      levelTerms[0] = new Vector<ProductTerm>();
      for (int m = 0; m < numMinterms; m++)
      {
        levelTerms[0].add( minterms[m] );
      }

      //out.println("SIMPLIFY PRODUCT TERMS:");
      ProductTerm targetTerm = null;
      ProductTerm candidateTerm = null;
      ProductTerm reducedTerm = null;
      int level = 0;
      if ( numVars > 0 )  // Allow for initially constant expressions.
      {
        //  Reduction loop continues until all terms are prime
        //  implicants or expression reduces to a constant.
levelLoop:
        for ( level = 1; level<numLevels; level++)
        {
          int numReduced = 0;
          levelTerms[level] = new Vector<ProductTerm>();
          Enumeration<ProductTerm> termsToReduce
                                     = levelTerms[level -1].elements();
          while ( termsToReduce.hasMoreElements() )
          {
            targetTerm = termsToReduce.nextElement();
            boolean isReduced = false;
            Enumeration<ProductTerm> candidateTerms 
                                   = levelTerms[level - 1].elements();
            while ( candidateTerms.hasMoreElements() )
            {
              candidateTerm = candidateTerms.nextElement();
              reducedTerm = candidateTerm.reduces( targetTerm );
              if ( reducedTerm != null )
              {
                numReduced++;
                //  Add this reduced term only if it is not
                //  already included.
                boolean found = false;
                Enumeration e = levelTerms[level].elements();
                while ( e.hasMoreElements() )
                {
                  if ( reducedTerm.equals(
                                      (ProductTerm)e.nextElement() ) )
                  {
                    //out.println( "Already included" );
                    found = true;
                    break;
                  }
                }
                if ( !found )
                {
                  //out.println( "Done" );
                  levelTerms[level].add( reducedTerm );
                  if ( ProductTerm.identity.equals( reducedTerm ) )
                  {
                    //out.println( "  Expression reduces to identity." );
                    break levelLoop;
                  }
                }
                isReduced = true;
              }
            }
            if ( ! isReduced )
            {
              levelTerms[level].add( targetTerm );
              
            }
          }
        if ( numReduced == 0 )
          break;
        }
      }

      //  levelTerms[level] now has product terms that are prime
      //  implicants of the function.  Create a new vector of prime
      //  implicant objects from this vector.
      primeImplicants = new Vector<PrimeImplicant>();
      Enumeration<ProductTerm> piEnum = levelTerms[level].elements();
      while ( piEnum.hasMoreElements() )
      {
        PrimeImplicant pi = new PrimeImplicant( piEnum.nextElement(),
                                                          minterms );
        primeImplicants.add( pi );
      }
      
      //out.println("\nDETERMINE ESSENTIAL PRIME IMPLICANTS:");
      //  Sort the prime implicants by number of literals.
      Collections.sort( primeImplicants );

      /*  Determine minimal sets of prime implicants.
       *    1.  If any minterms are covered by just one p.i., that
       *        p.i. must be used.
       *    2.  Remove dominated p.i.s and dominating minterms.
       *    3.  Use smallest p.i. if there is one; else branch.
       */
      //  Make a working copies of the minterms and prime implicants.
      Vector<ProductTerm> uncoveredMinterms
                          = new Vector<ProductTerm>( minterms.length );
      Vector<PrimeImplicant> unusedPrimeImplicants
                       = new Vector<PrimeImplicant>( primeImplicants );

      //  Add all minterms to working list, and find how many PIs
      //  cover each one.
      for (int m=0; m< numMinterms; m++)
      {
        ProductTerm mt = minterms[m];
        uncoveredMinterms.add(mt);
        int numCovers = countCovers( mt, unusedPrimeImplicants );
        
      }

      //  Select the prime implicants to use.
      
      minimum = new Vector<ProductTerm>();
      while ( uncoveredMinterms.size() > 0 )
      {
        //  Determine any and all (relatively) essential prime
        //  implicants.
        while ( addEssentialPrimeImplicants( minimum,
                          uncoveredMinterms, unusedPrimeImplicants ) )
          ; // repeat until method returns false

        //  Remove dominated prime implicants and dominating minterms.
        doDomination( unusedPrimeImplicants, uncoveredMinterms );
        if ( uncoveredMinterms.size() == 0 )
          break;

        //  Pick a prime implicant to cover the least-covered minterm.
        ProductTerm least =
                       (ProductTerm) uncoveredMinterms.elementAt( 0 );
        int min = countCovers( least, unusedPrimeImplicants );
        for (int m=1; m<uncoveredMinterms.size(); m++)
        {
          ProductTerm minterm =
                       (ProductTerm) uncoveredMinterms.elementAt( m );
          int numCovers =
                        countCovers( minterm, unusedPrimeImplicants );
          if ( numCovers < min )
          {
            least = minterm;
            min = numCovers;
          }
        }
        for (int p=0; p<unusedPrimeImplicants.size(); p++)
        {
          PrimeImplicant pi =
                (PrimeImplicant) unusedPrimeImplicants.elementAt( p );
          if (pi.covers( least ) )
          {
            //out.println("  " +  pi + " covers " + least );
            minimum.add( pi );
            unusedPrimeImplicants.remove( pi );
            //  Remove all minterms covered by this prime implicant
            Enumeration e = pi.getCovers().elements();
            while ( e.hasMoreElements() )
            {
              ProductTerm pt = (ProductTerm) e.nextElement();
              boolean removed = uncoveredMinterms.remove( pt );
              if ( removed ){}
                //out.println("  " + uncoveredMinterms.size() +
                 //  " minterm" + (uncoveredMinterms.size()!=1?"s":"") +
                  //                                        " remain" );
            }
          }
        }
      }
    }


  //  Method addEssentialPrimeImplicants()
  //  ----------------------------------------------------------------
  /**
    *   An essential prime implicant is one which is the only one to
    *   cover a minterm.
    *
    *   @return true if an essential prime implicant was found.
    */
    private boolean addEssentialPrimeImplicants(
        Vector<ProductTerm> minimum,
        Vector<ProductTerm> minterms,
        Vector<PrimeImplicant> primeImplicants )
    {
      for (int m=0; m<minterms.size(); m++)
      {
        ProductTerm minterm = (ProductTerm) minterms.elementAt( m );
        int numCovers = countCovers( minterm, primeImplicants );
        if ( numCovers == 1 )
        {
          for (int p=0; p<primeImplicants.size(); p++)
          {
            PrimeImplicant pi =
                      (PrimeImplicant) primeImplicants.elementAt( p );
            if (pi.covers( minterm ) )
            {
              
              minimum.add( pi );
              primeImplicants.remove( pi );
              Enumeration e = pi.getCovers().elements();
              while ( e.hasMoreElements() )
              {
                boolean removed = minterms.remove( e.nextElement() );
                if ( removed ){}
                  //out.println( "  " + minterms.size() + " minterm" +
                  //          (minterms.size()!=1?"s":"") + " to go." );
              }
              return true;
            }
          }
        }
      }
      return false;
    }


  //  Method doDomination()
  //  -----------------------------------------------------------------
  /**
    *   Removes dominated prime implicants and dominating minterms
    *   from their respective vectors.
    */
    private void doDomination( Vector<PrimeImplicant> primeImplicants,
                               Vector<ProductTerm> minterms )
    {
      //  One prime implicant dominates another if it is no more
      //  complicated and implies a superset of the minterms implied
      //  by the dominatee, which must be removed from consideration.
      boolean piDominated = true;
piDomination:
      while ( piDominated )
      {
        piDominated = false;
        int numPI = primeImplicants.size();
        Vector<ProductTerm>[] implies = new Vector[ numPI ];
        boolean[] dominated = new boolean[ numPI ];
        Arrays.fill( dominated, false);
        for (int pix=0; pix<numPI; pix++ )
        {
          PrimeImplicant pi = primeImplicants.elementAt( pix );
          implies[pix] = new Vector<ProductTerm>();
          for (int m=0; m<minterms.size(); m++)
          {
            if ( pi.covers( (ProductTerm)minterms.elementAt( m ) ) )
              implies[pix].add( minterms.elementAt( m ) );
          }
        }
        //  Check each pair of prime implicants for domination.
        for (int i=0; i<numPI; i++)
        {
          for (int j=0; j<numPI; j++)
          {
            if ( i == j ) continue;
            if (  implies[i].containsAll( implies[j] ) &&
                  implies[i].size() > implies[j].size() )
            {
out.println("Error: " + primeImplicants.elementAt( i ) + " dominates " +
primeImplicants.elementAt( j ) );
              primeImplicants.removeElementAt( j );
              piDominated = true;
              break piDomination;
            }
          }
        }
      }
    }


  //  Method countCovers()
  //  ----------------------------------------------------------------
  /**
    *   Sets the cover count for a minterm, given a list of prime
    *   implicants.
    */
    private int countCovers(  ProductTerm minterm,
                              Vector<PrimeImplicant> primeImplicants )
    {
      minterm.clearCoverCount();
      Enumeration<PrimeImplicant> e = primeImplicants.elements();
      while ( e.hasMoreElements() )
      {
        PrimeImplicant pi = e.nextElement();
        if ( pi.covers( minterm ) )
        {
          minterm.incrementCoverCount();
        }
      }
      int numCovers = minterm.getCoverCount();
      if ( numCovers == 0 )
        throw new RuntimeException( "Minterm " +
               BitManipulation.reverseBits( minterm.value, numVars ) +
                         " is not covered by any prime implicants." );
      return numCovers;
    }



  //  Method priString()
  //  -----------------------------------------------------------------
  /**
    *   Returns printable representation of prime implicants.
    *
    */
    public String priString()
    {

      if ( 0 == primeImplicants.size() )
        return "none";

      StringBuffer sb = new StringBuffer();
      for (int i=0; i<primeImplicants.size(); i++)
      {
    	 
        sb.append( primeImplicants.elementAt(i).toString() );
        if ( i < (primeImplicants.size() - 1) )
          sb.append( ", "  );
      }
      System.out.println("sb:"+sb);
      return new String( sb );
    }


  //  Method toString()
  //  -----------------------------------------------------------------
  /**
    *   Returns a printable representation of the minimized function.
    */
    public String toString()
    {

      if ( 0 == minimum.size() )
        return "0";

      StringBuffer sb = new StringBuffer( );
      for (int i=0; i<minimum.size(); i++)
      {
    	//System.out.println("i:"+i);  
        ProductTerm pt = (ProductTerm)minimum.elementAt( i );
        //System.out.println("str1:"+pt.ptString());
        sb.append( pt.ptString() );
        if ( i < minimum.size()-1 )
        {
          sb.append( "|" );
          //System.out.println("+");
        }
       // System.out.println("sb:"+sb);
      }
      return new String( sb );
    }

  }
