/************************************************************************
 *
 * $Id: URI.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 *
 * Copyright (c) 2001 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *       Sun Microsystems, Inc. for Project JXTA."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA"
 *    must not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact Project JXTA at http://www.jxta.org.
 *
 * 5. Products derived from this software may not be called "JXTA",
 *    nor may "JXTA" appear in their name, without prior written
 *    permission of Sun.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL SUN MICROSYSTEMS OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Project JXTA.  For more
 * information on Project JXTA, please see
 * <http://www.jxta.org/>.
 *
 * This license is based on the BSD license adopted by the Apache
 * Foundation.
 **********************************************************************/

package ch.ethz.jadabs.jxme;


/**
 * Abstract class to build URI. Instead of giving a general URI
 * implementation, it just give a set of tools to build specifique
 * URI.
 */
abstract public class URI {


    /**
     * returns true if c is an element of JXTA ID ABNF <upper>
     * @param c the tested character
     * @return c >= 'A' && c <= 'Z'
     */ 
    static private boolean isUpper(char c) {
	return c >= 'A' && c <= 'Z' ;
    }


    /**
     * returns true if c is an element of JXTA ID ABNF <lower>
     * @param c the tested character
     * @return c >= 'a' && c <= 'z'
     */ 
    static private boolean isLower(char c) {
	return c >= 'a' && c <= 'z' ;
    }


    /**
     * returns true if c is an element of JXTA ID ABNF <hex>
     *
     *<hex> ::= <number> | "A" | "B" | "C" | "D" | "E" | "F" | "a" | "b" | "c" | "d" | "e" | "f"
     *
     * @param c the tested character
     * @return 
     */ 
    static private boolean isHex(char c) {
	return isNumber(c) || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f') ;
    }


    /**
     * returns true if c is an element of JXTA ID ABNF <number>
     * @param c the tested character
     * @return c >= '0' && c <= '9'
     */ 
    static private boolean isNumber(char c) {
	return c >= '0' && c <= '9' ;
    }


    /**
     * returns true if c is an element of JXTA ID ABNF <other>
     *
     * <other> ::= "(" | ")" | "+" | "," | "-" | "." | ":" | "=" | "@" | ";" | "$" | "_" | "!" | "*" | "'"
     *
     * @param c the tested character
     * @return 
     */ 
    static private boolean isOther(char c) {
	return c=='(' || c==')' || c=='+' || c==',' || c=='-' || c=='.' || c==':' ||
	    c=='=' || c=='@' || c==';' || c=='$' || c=='_' || c=='!' || c=='*' || c=='\'';
    }

    /**
     * return true is c is an element of JXTA ID ABNF <reserved>
     *
     * <reserved> ::= "%" | "/" | "?" | "#"
     *
     * @param c the tested character
     * @return 
     */ 
    static private boolean isReserved(char c) {
	return c=='%' || c=='/' || c=='?' || c=='#' ;
    }
    

    /**
     * Encodes str in the uri format. All the characters which is not in <upper> | <lower>
     * | <number> | <other> will be encoded in the %<hex><hex> format.
     *
     * example: encode("Hello World!") => "Hello%20World!"
     * @param str the String to encode.
     * @exception IllegalArgumentException if a character can not be encoded
     * @return the encoded String.
     */
    static public String encode(String str) throws IllegalArgumentException {
	char[] in = str.toCharArray();
	StringBuffer out = new StringBuffer();
	for(int i = 0 ; i < in.length ; i++) {
	    char c = in[i] ;
	    if(isLower(c)||isUpper(c)||isNumber(c)||isOther(c)){
		out.append(c) ;
	    }else{
		String cstr = Integer.toHexString((int)c) ;
		if(cstr.length() == 1)
		    cstr = "0" + cstr;
		else if(cstr.length() != 2)
		    throw new IllegalArgumentException ("Can not encode the chararcter '" + c + "' of the string \"" + str + "\"");
		out.append('%').append(cstr);
	    }
	}
	return out.toString();
    }
    

    /**
     * Unencodes str from the uri format. All the %<hex><hex> element
     * will be remplaced by the matching character.
     *
     * example: unencode("Hello%20World!") => "Hello World!"
     * @param str the String to unencode.
     * @exception IllegalArgumentException if a '%' character is not
     * followed by two <hex>.
     * @return the unencoded String.  */
    static public String unencode(String str) throws IllegalArgumentException {
	char[] in = str.toCharArray();
	StringBuffer out = new StringBuffer();
	for(int i = 0 ; i < in.length ; i++) {
	    char c = in[i] ;
	    if(c == '%'){
		if(in.length<i+2||!isHex(in[i+1])||!isHex(in[i+2]))
		    throw new IllegalArgumentException ("Can not unencode the string \"" + str + "\"");
		c = (char)Integer.parseInt(str.substring(i+1, i+3) , 16) ;
		i+=2;
	    }
	    out.append(c) ;
	}
	return out.toString();
    }

    /**
     * returns a string with the description of the list of uris. The
     * string have the folowing format: [uri1 uri2 ... urin] */
    static public String toString(URI[] uris) {
	StringBuffer str = new StringBuffer("[") ;
	if (uris.length > 0) {
	    str.append(uris[0].toString()) ;
	}
	for (int i = 1 ; i < uris.length ; i++) {
	    str.append(' ').append(uris[i].toString()) ;
	}
	str.append(']') ;
	return str.toString() ;
    }

    /**
     * returns a string with the description of the list of list of uris. The
     * string have the folowing format: [list1 list2 ... listn] */
    static public String toString(URI[][] luris) {
	StringBuffer str = new StringBuffer("[") ;
	if (luris.length > 0) {
	    str.append(toString(luris[0])) ;
	}
	for (int i = 1 ; i < luris.length ; i++) {
	    str.append(' ').append(toString(luris[i])) ;
	}
	str.append(']') ;
	return str.toString() ;
    }

}
