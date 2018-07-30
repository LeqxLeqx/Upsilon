/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
 *                                                                         *
 *  Upsilon: A general utilities library for java                          *
 *  Copyright (C) 2018  LeqxLeqx                                           *
 *                                                                         *
 *  This program is free software: you can redistribute it and/or modify   *
 *  it under the terms of the GNU General Public License as published by   *
 *  the Free Software Foundation, either version 3 of the License, or      *
 *  (at your option) any later version.                                    *
 *                                                                         *
 *  This program is distributed in the hope that it will be useful,        *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of         *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          *
 *  GNU General Public License for more details.                           *
 *                                                                         *
 *  You should have received a copy of the GNU General Public License      *
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.  *
 *                                                                         *
\* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package upsilon.tools;

public class WrapperTools {  private WrapperTools() {}
	
	public static Long defaultToZero(Long l) {
		if (l == null)
			return 0l;
		else
			return l;
	}
	public static Integer defaultToZero(Integer i) {
		if (i == null)
			return 0;
		else
			return i;
	}
	public static Short defaultToZero(Short s) {
		if (s == null)
			return (short) 0;
		else
			return s;
	}
	public static Byte defaultToZero(Byte b) {
		if (b == null)
			return 0;
		else
			return b;
	}
	public static Boolean defaultToFalse(Boolean b) {
		if (b == null)
			return false;
		else
			return b;
	}
	public static Double defaultToZero(Double d) {
		if (d == null)
			return 0d;
		else
			return d;
	}
	public static Float defaultToZero(Float f) {
		if (f == null)
			return 0f;
		else
			return f;
	}
	public static Character defaultToZero(Character c) {
		if (c == null)
			return '\0';
		else
			return c;
	}

	
}
