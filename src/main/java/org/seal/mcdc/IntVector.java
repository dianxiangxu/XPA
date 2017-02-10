package org.seal.mcdc;
//package mcdc;
//$Id: IntVector.java,v 1.1 2005/02/12 16:51:22 vickery Exp $
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
 *  $Log: IntVector.java,v $
 *  Revision 1.1  2005/02/12 16:51:22  vickery
 *  Revised to use generics for various Vectors and Enumerations.
 *
 *
 */

//  Class IntVector
//  -------------------------------------------------------------------
/**
  *   Just like a Vector (grows automatically), but for int primitives.
  *
  *   @author   C. Vickery
  *   @version  1.0 - Fall 2000
  */
  public class IntVector
  {
    protected int[] theVector = new int[1];
    protected int   capacity  = 1;
    protected int   increment = 10;
    protected int   size      = 0;

    //  Accessors
    //  ---------------------------------------------------------------
    public int getCapacity()  { return capacity; }
    public int getIncrement() { return increment; }
    public int getSize()      { return size; }

    //  Method append()
    //  ---------------------------------------------------------------
    public void append( int val )
    {
      if ( (size + 1) >= capacity )
      {
        int[] temp = new int[ capacity + increment ];
        System.arraycopy( theVector, 0, temp, 0, size );
        theVector = temp;
      }
      theVector[ size++ ] = val;
    }

    //  Method toArray()
    //  ---------------------------------------------------------------
    /**
      *   Clones current vector into an int[].
      */
      public int[] toArray()
      {
        int[] temp = new int[ size ];
        System.arraycopy( theVector, 0, temp, 0, size );
        return temp;
      }


    //  Method toString()
    //  ---------------------------------------------------------------
    public String toString()
    {
      StringBuffer sb = new StringBuffer( "[" );
      for ( int i=0; i< size; i++ )
      {
        sb.append( theVector[i] );
        if ( i < size-1 ) sb.append( ", " );
      }
      sb.append( "]" );
      return new String( sb );
    }

  }
