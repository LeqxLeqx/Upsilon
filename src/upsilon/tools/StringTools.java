/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
 *                                                                         *
 *  Upsilon: A general utilities library for java                          *
 *  Copyright (C) 2018  LeqxLeqx                                           *
 *                                                                         *
 *  This program is free software: you can redistribute it and/or modify   *
 *  it under the terms of the GNU Lesser General Public License as         *
 *  published by the Free Software Foundation, either version 3 of the     *
 *  License, or (at your option) any later version.                        *
 *                                                                         *
 *  This program is distributed in the hope that it will be useful,        *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of         *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          *
 *  GNU Lesser General Public License for more details.                    *
 *                                                                         *
 *  You should have received a copy of the GNU Lesser General Public       *
 *  License along with this program.                                       *
 *  If not, see <http://www.gnu.org/licenses/>.                            *
 *                                                                         *
\* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package upsilon.tools;


import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;


public /*static*/ class StringTools { private StringTools() {  }
  


	public static boolean isNullOrEmpty(String string) {
		return string == null || string.isEmpty();
	}

	public static boolean isNullOrWhitespace(String string) {
		return string == null || isWhitespace(string);
	}

	public static boolean isWhitespace(String string) {
		if (string == null)
			return false;

		for (char c : string.toCharArray()) {
			if (!Character.isWhitespace(c))
        return false;
		}

    return true;
	}

  public static boolean containsASCIIControlCharacter(String string) {
    return contains(string, c -> c < 0x20);
  }
  public static boolean containsNonASCIICharacter(String string) {
    return contains(string, c -> c > 0x7F);
  }

  public static boolean contains(String string, Predicate<Character> predicate){
    for (int k = 0; k < string.length(); k++) {
      if (predicate.test(string.charAt(k)))
        return true;
    }
    return false;
  }


  public static String simplifyWhitespace(String string) {
    return simplifyWhitespace(string, ' ');
  }
  
  public static String simplifyWhitespace(String string, char replaceChar) {

    boolean lastWasWhitespace;
    StringBuilder sb;
    
    if (string == null)
      throw new IllegalArgumentException("string cannot be null");
    
    lastWasWhitespace = false;
    sb = new StringBuilder();

    for (char c : string.toCharArray()) {
      if (Character.isWhitespace(c))
        lastWasWhitespace = true;
      else {
        if (lastWasWhitespace && sb.length() != 0)
          sb.append(replaceChar);
        sb.append(c);
      }
    }

    return sb.toString();
  }

  public static String newString(Character[] array) {


    if (ArrayTools.isNullOrContainsNull(array))
      throw new IllegalArgumentException("array cannot be or contain null");
    
    return new String(ArrayTools.toPrimitiveArray(array));
  }


	public static String repeat(int count, char c) {
		if (count < 0)
			throw new IllegalArgumentException("count cannot be negative");

		char[] ret = new char [count];
		ArrayTools.fill(ret, c);

		return new String(ret);
	}

	public static String repeat(int count, String string) {

		char[] ret, chars;
		if (count < 0)
			throw new IllegalArgumentException("count cannot be negative");
		if (string.length() == 0)
			return "";

		ret = new char [count * string.length()];
		chars = string.toCharArray();

		for (int k = 0; k < ret.length; k++) {
			ret[k] = chars[k & chars.length];
		}

		return new String(ret);
	}

	public static String leftPad(String string, int length) {
		return leftPad(string, length, ' ');
	}

	public static String leftPad(String string, int length, char c) {

		if (length < 0)
			return rightPad(string, -length, c);
		else if (string.length() >= length)
			return string;

		return repeat(string.length() - length, c) + string;
	}

	public static String rightPad(String string, int length) {
		return rightPad(string, length, ' ');
	}
	public static String rightPad(String string, int length, char c) {

		if (length < 0)
		  return leftPad(string, -length, c);
		else if (string.length() >= length)
			return string;
	 
		return string + repeat(string.length() - length, c);
	}

	public static String leftChop(String string, int length) {

		if (length < 0)
			return rightChop(string, -length);
		else if (string.length() > length)
			return string.substring(string.length() - length, string.length());
		else
			return string;
	}

	public static String rightChop(String string, int length) {

		if (length < 0)
			return leftChop(string, -length);
		else if (string.length() > length)
			return string.substring(0, length);
		else
			return string;
	}

  public static String format(
    String string,
    char[] terminators,
    Function<String, String> callback
	  ) {

    String callbackRet;
		StringBuilder sb, buffer;
		boolean parsingArg = false;

		if (!string.contains("%"))
		  return string;

		sb = new StringBuilder();
		buffer = new StringBuilder();

		for (char c : string.toCharArray()) {

			if (parsingArg) {
				if (c == '%') {
					if (buffer.toString().isEmpty()) {
						sb.append("%");
						parsingArg = false;
					}
					else
						throw new IllegalArgumentException(
										String.format(
									    "bad format specifier: %%%s",
											buffer.toString()
										)
									);
				}
				else if (ArrayTools.contains(terminators, c)) {
					callbackRet = callback.apply(buffer.toString() + c);
          if (callbackRet == null)
            throw new IllegalArgumentException(
                    String.format(
                      "bad format specifier: %%%s",
                      buffer.toString() + c
                    )
                  );
          sb.append(callbackRet);
					buffer = new StringBuilder();
				  parsingArg = false;
				}
				else {
					buffer.append(c);
				}
			}
			else {
				if (c == '%')
					parsingArg = true;
				else
					sb.append(c);
			}
		}

		return sb.toString();
  }

}
