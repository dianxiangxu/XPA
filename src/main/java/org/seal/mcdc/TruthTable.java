package org.seal.mcdc;
//package mcdc;
//$Id: TruthTable.java,v 1.4 2005/02/21 23:32:16 vickery Exp $
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
 *  $Log: TruthTable.java,v $
 *  Revision 1.4  2005/02/21 23:32:16  vickery
 *  Completed GUI.  Don't know how to scroll
 *  within a table cell, and list of covers can
 *  be too long to see sometimes.
 *
 *  Revision 1.3  2005/02/21 04:19:00  vickery
 *  Continuing GUI development.
 *  Fixed TruthTable to give variable names in alphabetic order, and to sort
 *  minterm numbers.
 *
 *  Revision 1.2  2005/02/20 04:24:00  vickery
 *  Started developing GUI.
 *
 *  Revision 1.1  2005/02/12 16:51:22  vickery
 *  Revised to use generics for various Vectors and Enumerations.
 *
 *
 */

import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.table.AbstractTableModel;

//  Class TruthTable
//  -------------------------------------------------------------------
/**
  *   The truth table for a boolean expression.
  *
  *     Version 1.1 adds construction based on array of minterm
  *     numbers.
  *
  *   @version  1.1 - Fall, 2000
  *   @author   C. Vickery
  */
  public class TruthTable extends AbstractTableModel
  {

  static final long serialVersionUID = 4284762403096738378L;

  //  Constants: the operators and constants

  protected static final char LP        = '(';
  protected static final char RP        = ')';
  protected static final char AND       = '&';
  protected static final char OR        = '|';
  protected static final char XOR       = '^';
  protected static final char NOT       = '!';
  protected static final char ZERO      = '0';
  protected static final char ONE       = '1';

  //  Instance variables
  protected int           numVars       = 0;
  protected int           numRows       = 0;
  protected int           numMinterms   = 0;
  protected String[]      variableNames = null;
  protected String[]      namesReversed;
  protected String        normalized    = "Not Given";
  protected boolean[]     theTable      = null;
  protected int           mintermMask;               // (2^numVars) -1
  protected ProductTerm[] minterms      = null;

  //  Accessors
  //  -----------------------------------------------------------------
  public int            getNumVars()      { return numVars;     }
  public int            getNumRows()      { return numRows;     }
  public int            getNumMinterms()  { return numMinterms; }
  public int            getRowCount()     { return numMinterms; }
  public int            getColumnCount()  { return 2;           }
  public Object         getValueAt(int row, int col)
  {
    switch (col)
    {
      case 0:
          return new Integer(minterms[row].getValue());
      case 1:
          return minterms[row].toString();
      default:
        throw new RuntimeException("Program Error: Bad switch");
    }
  }
  public boolean        isCellEditable(int row, int col)
  {
    return false;
  }
  public String         getColumnName(int col)
  {
    switch (col)
    {
      case 0:
          return "Minterm Number";
      case 1:
          return "Product Term";
      default:
        throw new RuntimeException("Program Error: Bad switch");
    }
  }

  public char[]         getVars()
  {
    char[] v = new char[numVars];
    System.arraycopy( variableNames, 0, v, 0, numVars );
    return v;
  }
  public String         expString()       { return normalized;  }
  public boolean[]      getTruthValues()  { return theTable;    }
  public ProductTerm[]  getMinterms()     { return minterms;    }


  //  Constructors
  //  =================================================================

  //  Construct from a String.
  /**
    *   String constructor. The string is a boolean expression using
    *   arbitrary variable names (case sensitive), * or nothing for
    *   AND, + for OR, and ' for not.  AND and OR are infix; NOT is
    *   postfix.  AND takes precedence over OR.
    *   Use parentheses for grouping.  Ignores spaces.
    *
    *   @param  str The boolean expression to be converted into a
    *               truth table.
    */
    public TruthTable( ArrayList<String> expression )
    {
      ArrayList<String> nor=new ArrayList<String>();
      nor.clear();
      for ( int k=0;k<expression.size();k++)
      {
       nor.add(expression.get(k));
      }
      //  Count the number of variables.
      variableNames = new String[2];
      for ( int i=0; i<nor.size(); i++ )
      {
        
        if (!nor.get(i).equals("&")&& !nor.get(i).equals("|")&&!nor.get(i).equals("!")&&!nor.get(i).equals("(")&&!nor.get(i).equals(")") )
        {
          //  See if variable is already in the variableNames list.
          boolean found = false;
          for ( int v=0; v<numVars; v++ )
          {
            if ( nor.get(i).equals(variableNames[ v ]) )
            {
              found = true;
              break;
            }
          }
          if ( !found )
          {
            //  Add this variable name to the variableNames list
            if ( numVars == variableNames.length )
            {
              // Have to make a bigger array
              String[] newVars = new String[ 2 * variableNames.length ];
              System.arraycopy( variableNames, 0, newVars, 0,
              variableNames.length );
              variableNames = newVars;
              newVars = null;
            }
            variableNames[ numVars++ ] = nor.get(i);
          }
        }
      }
      //  Trim the array of variable names to size, sort it, and
      //  create a reversed copy.
      String[] newVars = new String[numVars];
      System.arraycopy( variableNames, 0, newVars, 0, numVars );
      variableNames = newVars;
      
      //System.out.println("var: " +variableNames[0]);
      
      newVars = null;
      Arrays.sort( variableNames, 0, numVars );
      namesReversed = new String[numVars];
      for (int i=0; i<numVars; i++)
        namesReversed[i] = variableNames[numVars-i-1];

      //  Construct the truth table by evaluating the expression for
      //  each combination of variable values.
      if ( numVars > 0 )
        numRows = (int)Math.pow( 2.0, numVars );
      else
        numRows = 1;
      theTable = new boolean[ numRows ];
      numMinterms = 0;
      for ( int i=0; i<numRows; i++ )
      {
        theTable[i] = evaluateBoolean(BitManipulation.reverseBits(i, numVars),variableNames, nor);
        nor.clear();
        for ( int k=0;k<expression.size();k++)
        {
         nor.add(expression.get(k));
        }
        if ( theTable[i] )
        {
          numMinterms++;
        }
      }
      //  Construct array of minterms.
      minterms = new ProductTerm[numMinterms];
      mintermMask = (int) Math.pow(2, numVars) - 1;
      int m = 0;
      for (int i=0; i<numRows; i++)
      {
        if ( theTable[i] )
        {
          minterms[ m++ ] = new ProductTerm(i,
                                        mintermMask, variableNames );
        }
      }
      //  Order the minterms numerically.
      /*  Cannot use Arrays.sort, because ProductTerm implements
       *  a comparison based on the number of variables.  Fix this
       *  (bubble sort) if performance becomes an issue.
       */
        int swapped = -1;
        do
        {
          swapped = 0;
          for (int i=swapped; i<minterms.length-1; i++)
          {
            if (minterms[i].getValue() > minterms[i+1].getValue())
            {
              ProductTerm temp = minterms[i];
              minterms[i] = minterms[i+1];
              minterms[i+1] = temp;
              swapped = i+1;
            }
          }
        } while (swapped != 0);
    }

     protected boolean
    evaluateBoolean( int value, String[] variableNames, ArrayList<String> exp )
    {
      //StringBuffer sb = new StringBuffer( exp );
    	
      for ( int v=0; v<numVars; v++ )
      {
        //  Substitute the proper value (0/1)
        //  for each occurrence of the var.
        char val = ((( 1<<v ) & value ) == 0) ? ZERO :ONE;
        //System.out.println("SIZE:"+exp.size());
        //System.out.println("SIZE:"+exp.get(0));
        //System.out.println("var1:"+variableNames[0]);
        //System.out.println("SIZE:"+variableNames[1]);
        
        
        for ( int i=0; i< exp.size(); i++ )
        {
        	//System.out.println("In si:"+exp.size());
          if ( (exp.get( i )).equals(variableNames[ v ]) )
          {  //System.out.println(val);
        	 String s= new Character(val).toString();
            exp.remove( i);
            exp.add(i,s);
          } 
        }
      }
      //  Evaluate the expression.
      CharStack operator  = new CharStack();
      CharStack operand   = new CharStack();
      for ( int i=0; i<exp.size(); i++ )
      {
        String y = exp.get( i );
        char  x= y.charAt(0);
        switch ( x )
        {
          case LP:
            operator.push( x );
          break;

          case RP:
          {
            char op;
            while ( (op = operator.pop()) != LP )
            {
              eval( operand, op );
            }
          }
          break;

          case NOT:
            //  Postfix NOT -- evaluate immediately.
            if ( operand.isEmpty() )
              throw new RuntimeException(
                        "Syntax Error: Postfix NOT with no operand" );
            char arg = operand.pop();
          operand.push( (arg == ZERO) ? ONE : ZERO );
          break;

          case AND:
            if ( operator.peek() == NOT )
              eval( operand, operator.pop() );
            operator.push( x );
          break;

          case OR:
          case XOR:
            if ( operator.peek() == NOT )
              eval( operand, operator.pop() );
            while ( operator.peek() == AND )
              eval( operand, operator.pop() );
            operator.push( x );
          break;

          case ZERO:
          case ONE:
            operand.push( x );
          break;

          default:
            throw new RuntimeException( "Program Error: BadSwitch" );
        }
      }
      while ( !operator.isEmpty() )
      {
        eval( operand, operator.pop() );
      }
      return operand.pop() == ONE;
    }

  //  Method eval()
  //  ----------------------------------------------------------------
  /**
    *   Apply an operator to the operand stack.
    */
    protected void eval( CharStack operand, char operator )
    {
      switch ( operator )
      {
        case AND:
        {
          if ( operand.isEmpty() )
            throw new RuntimeException( 
                "Syntax Error: AND with no operands." );
          char arg_1 = operand.pop();
          if ( operand.isEmpty() )
            throw new RuntimeException(
                "Syntax Error: AND missing right operand." );
          char arg_2 = operand.pop();
          operand.push(
          ((arg_1 == ONE) && (arg_2 == ONE)) ? ONE : ZERO );
        }
        break;

        case OR:
        {
          if ( operand.isEmpty() )
            throw new RuntimeException(
                "Syntax Error: OR with no operands." );
          char arg_1 = operand.pop();
          if ( operand.isEmpty() )
            throw new RuntimeException(
                "Syntax Error: OR missing right operand." );
          char arg_2 = operand.pop();
          operand.push(
          ((arg_1 == ONE) || (arg_2 == ONE)) ? ONE : ZERO );
        }
        break;

        case XOR:
        {
          if ( operand.isEmpty() )
            throw new RuntimeException(
                "Syntax Error: XOR with no operands." );
          char arg_1 = operand.pop();
          if ( operand.isEmpty() )
            throw new RuntimeException(
                "Syntax Error: XOR missing right operand." );
          char arg_2 = operand.pop();
          operand.push(
            ((arg_1 == ONE ) && (arg_2 == ZERO)) ||
            ((arg_1 == ZERO) && (arg_2 == ONE )) ? ONE : ZERO );
        }
        break;

        default:
          throw new RuntimeException(
              "Program Error: " + operator + " is not an operator");
      }
    }


  //  Method leftBit()
  //  ----------------------------------------------------------------
  /**
    *   Returns the index of the leftmost 1 in an int.  Least
    *   significant bit is at index 0.
    */
    public static int leftBit( int x )
    {
      for (int i = 31; i >=0; i--)
      {
        if ( (x & ( 1<<i )) != 0 ) return i;
      }
      return -1;  // All zeros
    }


  //  Method sopString()
  //  ----------------------------------------------------------------
  /**
    *   Returns printable representation in Sum Of Products form.
    *
    */
    public String sopString()
    {
      //  Generate the printable string.
      StringBuffer sb = new StringBuffer();
      for (int s=0; s<numMinterms; s++)
      {
        sb.append( minterms[s].toString() );
        if ( s < (numMinterms - 1) )
        {
          sb.append( " + " );
        }
      }
      return new String (sb);
    }


  //  Method toString()
  //  ----------------------------------------------------------------
  /**
    *   Returns a printable list of minterm numbers.
    *
    */
    public String toString()
    {
      StringBuffer sb = new StringBuffer( "[" );
      for (int m=0; m<minterms.length; m++)
      {
//        sb.append(BitManipulation.reverseBits(minterms[m].value,
//                                                      numVars) + ",");
        sb.append(minterms[m].value + ",");
      }
      if (minterms.length == 0)
        sb.append( ']' );
      else
        sb.setCharAt( sb.length()-1, ']' );
      return new String( sb );
    }
  }
