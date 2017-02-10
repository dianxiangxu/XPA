package org.seal.mcdc;
//package mcdc;
//$Id: CharStack.java,v 1.1 2005/02/12 16:51:22 vickery Exp $
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
 *  $Log: CharStack.java,v $
 *  Revision 1.1  2005/02/12 16:51:22  vickery
 *  Revised to use generics for various Vectors and Enumerations.
 *
 *
 */

//  Class CharStack
//  ------------------------------------------------------------------
/**
  *   Standard stack operations on a stack of chars.
  */
  public class CharStack
  {
    private int     capacity  = 10;
    private int     top       = -1;

    private char[] theStack = new char[10];
    public char pop()
    {
      if ( top < 0 ) throw new RuntimeException( "Pop empty stack." );
      return theStack[top--];
    }
    public void push( char x )
    {
      if ( ++top >= capacity )
      {
        char[] newStack = new char[ capacity + capacity ];
        System.arraycopy( theStack, 0, newStack, 0, capacity );
        theStack = newStack;
        capacity += capacity;
      }
      theStack[top] = x;
    }
    public boolean isEmpty()
    {
      return (top < 0);
    }
    public char peek()
    {
      if ( top < 0 )
        return '\0';
      return theStack[top];
    }

    public String toString()
    {
      StringBuffer sb = new StringBuffer( "[" );
      for ( int i=0; i<=top; i++ ) sb.append( theStack[i] );
      sb.append( "]" );
      return new String( sb );
    }
  }

