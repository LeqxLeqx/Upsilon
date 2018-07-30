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

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public enum DataType {

	NONE (null, null),
	OBJECT (Object.class, new Object()),

	INTEGER (Long.class, 0L),
	REAL (Double.class, 0D) ,
	LOGICAL (Boolean.class, false),

	STRING (String.class, ""),
	DATETIME (
		LocalDateTime.class, 
		LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC)
		),
	RAW (byte[].class, new byte[0]),

	;

  public final Class storingClass;
	public Object nonNullDefault;

  DataType(Class storingClass, Object nonNullDefault) {
    this.storingClass = storingClass;
		this.nonNullDefault = nonNullDefault;
  }
	

  public <T> T cast(Object object, Class<T> realType) {

    if (storingClass != realType)
      throw new DataTypeException(this, realType);

    try {
      return realType.cast(object);
    } catch (ClassCastException e) {
      throw new DataTypeException(this, realType, e);
    }
  }

  public boolean accepts(Class realType) {
    return this.storingClass.isAssignableFrom(realType);
  }

	
}
