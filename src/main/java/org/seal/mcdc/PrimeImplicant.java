package org.seal.mcdc;
//package mcdc;
//$Id: PrimeImplicant.java,v 1.2 2005/02/21 23:32:15 vickery Exp $
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
 *  $Log: PrimeImplicant.java,v $
 *  Revision 1.2  2005/02/21 23:32:15  vickery
 *  Completed GUI.  Don't know how to scroll
 *  within a table cell, and list of covers can
 *  be too long to see sometimes.
 *
 *  Revision 1.1  2005/02/12 16:51:22  vickery
 *  Revised to use generics for various Vectors and Enumerations.
 *
 *
 */

import java.util.Vector;

//  Class PrimeImplicant
//  ------------------------------------------------------------------
/**
  *   Product term with list of minterms covered by it.  Sortable wrt
  *   size of product term.
  *
  *   @version  1.0 - Fall 2000
  *   @author   C. Vickery
  */
  public class PrimeImplicant extends ProductTerm
                              implements Comparable
  {

    protected Vector<ProductTerm>   covers = new Vector<ProductTerm>();

  //  Constructor
  //  ----------------------------------------------------------------
  /**
    *
    *   Creates a prime implicant out of a product term by adding a
    *   list of the minterms covered by this product term.  It's an
    *   error if this product term doesn't cover any minterms.
    */
    public PrimeImplicant( ProductTerm pt, ProductTerm[] minterms )
    {
      super( pt.getValue(), pt.getMask(), pt.getVariableNames() );
      boolean found = false;
      for (int m=0; m<minterms.length; m++)
      {
        if ( covers( minterms[m] ) )
        {
          found = true;
          covers.add( minterms[m] );
        }
      }
      if ( ! found )
        throw new RuntimeException( "Attempt to create a prime " +
        "implicant that covers no minterms." );
    }


  //  addCover()
  //  ----------------------------------------------------------------
  /**
    *   Adds a minterm to the list of minterms covered by this
    *   prime implicant.
    */
    public void addCover( ProductTerm pt )
    {
      if ( ! pt.isMinterm() )
      {
        throw new RuntimeException( "Attempt to add " + pt.toString()
        + " to the list of minterms covered by prime implicant " +
        super.toString() + ", but " + pt.toString() +
                                               " is not a minterm." );
      }
      if ( ! covers( pt ) )
      {
        throw new RuntimeException( "Attempt to add " + pt +
        " to the list of minterms covered by prime implicant " +
        super.toString() + ", but " + pt +  " is not covered by " +
        super.toString() );
      }
      covers.add( pt );

    }

  //  removeCover()
  //  ----------------------------------------------------------------
  /**
    *   Removes an item from the list of covered minterms.  No error
    *   if operation fails.
    */
    public void removeCover( ProductTerm pt )
    {
      covers.removeElement( pt );
    }

  //  getCovers()
  //  -----------------------------------------------------------------
  /**
    *   Returns Vector of covers.
    */
    public Vector<ProductTerm> getCovers() { return covers; }


  //  getCoverCount()
  //  -----------------------------------------------------------------
  /**
    *   Returns how many minterms are covered by this prime implicant.
    */
    public int getCoverCount() { return covers.size(); }


  //  getImplicant()
  //  ----------------------------------------------------------------
  /**
   *  Returns the string representation of the implicant.
   */
  public String getImplicantString()
  {
    return super.toString();
  }

  
  //  toString()
  //  ----------------------------------------------------------------
  /**
    *   Returns the string representation of the implicant and the
    *   product terms it covers.
    */
    public String toString()
    {
      StringBuffer sb = new StringBuffer( "[ " + super.toString() +
                                                               " => ");
      for (int i=0; i<covers.size(); i++)
      {
        sb.append( covers.elementAt(i) );
        if ( i < (covers.size()-1) )
          sb.append( ", " );
      }
      sb.append( " ]" );
      return new String( sb );
    }

  }
