/**
 * OWASP Enterprise Security API (ESAPI)
 * 
 * This file is part of the Open Web Application Security Project (OWASP) Enterprise Security API
 * (ESAPI) project. For details, please see <a
 * href="http://www.owasp.org/index.php/ESAPI">http://www.owasp.org/index.php/ESAPI</a>.
 * 
 * Copyright (c) 2007 - The OWASP Foundation
 * 
 * The ESAPI is published by OWASP under the BSD license. You should read and accept the LICENSE
 * before you use, modify, and/or redistribute this software.
 * 
 * @author Jeff Williams <a href="http://www.aspectsecurity.com">Aspect Security</a>
 * @created 2007
 */
package org.owasp.esapi.codecs;

/**
 * Implementation of the Codec interface for backslash encoding used in CSS.
 * 
 * @author Jeff Williams (jeff.williams .at. aspectsecurity.com) <a
 *         href="http://www.aspectsecurity.com">Aspect Security</a>
 * @since June 1, 2007
 * @see org.owasp.esapi.Encoder
 */
public class CSSCodec extends Codec {


    /**
	 * {@inheritDoc}
	 *
     * Returns backslash encoded character.
     *
     * @param immune
     */
    public String encodeCharacter(char[] immune, Character c) {
		char ch = c.charValue();
		
		// check for immune characters
		if ( containsCharacter( ch, immune ) ) {
			return ""+ch;
		}
		
		// check for alphanumeric characters
		String hex = Codec.getHexForNonAlphanumeric( ch );
		if ( hex == null ) {
			return ""+ch;
		}
		
		// return the hex and end in whitespace to terminate
        return "\\" + hex + " ";
    }

    
    /**
	 * {@inheritDoc}
	 * 
	 * Returns the decoded version of the character starting at index, or null if no decoding is
     * possible.  This implementation does not support \\### octal encoding.
     * 
     * Formats all are legal both upper/lower case: \\x - special characters \\HHHH
	 */
    public Character decodeCharacter(PushbackString input) {
        input.mark();
        Character first = input.next();
        if (first == null) {
            input.reset();
            return null;
        }

        // if this is not an encoded character, return null
        if (first.charValue() != '\\') {
            input.reset();
            return null;
        }

        Character second = input.next();
        if (second == null) {
            input.reset();
            return null;
        }

        // look for \HHH format
        if (input.isHexDigit(second)) {
            // Search for up to 6 hex digits following until a space
        	StringBuilder sb = new StringBuilder();
            sb.append(second);
            for (int i = 0; i < 5; i++) {
                Character c = input.next();
                if (c == null || c.charValue() == 0x20)
                    break;
                if (input.isHexDigit(c)) {
                    sb.append(c);
                } else {
                    input.pushback(c);
                    break;
                }
            }
            try {
                // parse the hex digit and create a character
                int i = Integer.parseInt(sb.toString(), 16);
                // TODO: in Java 1.5 you can test whether this is a valid code point
                // with Character.isValidCodePoint() et al.
                return Character.valueOf((char) i);
            } catch (NumberFormatException e) {
                // throw an exception for malformed entity?
                // just continue which will reset and return null
            }
        }

        return second;
    }

}