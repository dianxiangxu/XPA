package org.seal.mcdc;
//package mcdc;
//$Id: BitManipulation.java,v 1.1 2005/02/12 16:51:22 vickery Exp $
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
 *  $Log: BitManipulation.java,v $
 *  Revision 1.1  2005/02/12 16:51:22  vickery
 *  Revised to use generics for various Vectors and Enumerations.
 *
 *
 */

//  Class BitManipulation
//  ------------------------------------------------------------------
/**
  *   Static methods for manipulating bits in an integer.
  *
  *   @version  1.0   Fall, 2000
  *   @author   C. Vickery
  *
  */
  public class BitManipulation
  {

  //  Method reverseBits()
  //  ----------------------------------------------------------------
  /**
    *   Returns an int with the bit pattern of the rightmost numBits
    *   bits in source reversed from left to right.
    */
    public static int reverseBits( int source, int numBits )
    {
      int sourcePosition = 1 << (numBits - 1);
      int destPosition   = 1;
      int result = source & ~((int)Math.pow( 2, numBits ) -1);
      for(int i=0; i<numBits; i++)
      {
        if ( (source & sourcePosition) != 0 )
          result |= destPosition;
        sourcePosition >>= 1;
        destPosition <<= 1;
      }
      return result;
    }


  //  Method countBits()
  //  ----------------------------------------------------------------
  /**
    *   Returns the number of 1's in an int.
    *
    */
    public static int countBits( int x )
    {
      int n = 0;
      for (int i=0; i<32; i++)
         if ( 0 != (x & (1<<i)) ) n++;
      return n;
    }

  }

