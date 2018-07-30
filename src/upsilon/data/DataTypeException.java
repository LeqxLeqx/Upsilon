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
package upsilon.data;

public class DataTypeException extends RuntimeException {

  public final DataType dataType;
  public final Class realType;

  DataTypeException(DataType dataType, Class realType) {
    super(String.format(
        "java class type %s is not compatable with data type %s",
        realType.getName(),
        dataType.toString()
        ));
    this.dataType = dataType;
    this.realType = realType;
  }

  DataTypeException(DataType dataType, Class realType, ClassCastException e) {
    super(String.format(
        "failed to cast object of stated type %s to java class type %s",
        dataType.toString(),
        realType.getName()
        ),
        e);
    this.dataType = dataType;
    this.realType = realType;
  }
  
}
