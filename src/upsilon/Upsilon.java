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

package upsilon;

public /*static*/ class Upsilon { private Upsilon() {}
  
  public static final int
    RELEASE_VERSION = 0,
    MAJOR_VERSION = 3,
    MINOR_VERSION = 3;

  public static String getVersion() {
    return String.format(
        "%d.%d.%d",
        RELEASE_VERSION,
        MAJOR_VERSION,
        MINOR_VERSION
        );
  }
  
}
