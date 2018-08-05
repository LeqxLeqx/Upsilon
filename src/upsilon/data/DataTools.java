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

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.BiPredicate;

class DataTools { private DataTools() {}
  
  static Object reObject(Object object, DataType type) {
    if (object == null)
      return object;

    switch (type) {

      case NONE:
      case OBJECT:
        return object;

      case INTEGER:
        return reObjectInteger(object);
      case REAL:
        return reObjectReal(object);
      case LOGICAL:
        return reObjectLogical(object);

      case STRING:
        return reObjectString(object);
      case DATETIME:
        return reObjectDateTime(object);
      case RAW:
        return object; /* TODO */

      default:
        return object;
    }
  }

  private static Object reObjectInteger(Object object) {

    Class<?> type = object.getClass();

		if (type == Byte.class)
			return (long) ((Byte) object).byteValue();
    else if (type == Short.class)
			return (long) ((Short) object).shortValue();
    else if (type == Integer.class)
			return (long) ((Integer) object).intValue();
    else
      return object;
  }

  private static Object reObjectReal(Object object) {

    Class<?> type = object.getClass();

    if (type == Byte.class)
      return (double) ((Byte) object).byteValue();
    else if (type == Short.class)
      return (double) ((Short) object).shortValue();
    else if (type == Integer.class)
      return (double) ((Integer) object).intValue();
    else if (type == Long.class)
      return (double) ((Long) object).longValue();
    else if (type == Float.class)
      return (double) ((Float) object).floatValue();
    else
      return object;
  }

  private static Object reObjectLogical(Object object) {

    Class<?> type = object.getClass();

		if (type == Byte.class)
			return (Byte) object != 0;
    else if (type == Short.class)
			return (Short) object != 0;
    else if (type == Integer.class)
			return (Integer) object != 0;
    else if (type == Long.class)
      return (Long) object != 0;
    else
      return object;
  }

  private static Object reObjectString(Object object) {
    return object.toString();
  }

  private static Object reObjectDateTime(Object object) {
    
    Class<?> type = object.getClass();

		if (type == String.class) {
      try {
        return LocalDateTime.parse(
            (String) object, 
            DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm:ss")
            );
      } catch (DateTimeParseException e) {
        return object;
      }
    }
    else if (type == Date.class)
      return toLocalDateTime((Date) object);
    else if (type == Timestamp.class)
      return toLocalDateTime((Timestamp) object);
    else
      return object;
  }

  static <T> boolean containsDuplicateReference(List<T> list) {
    for (int k = 0; k < list.size() - 1; k++) {
      for (int i = 1; i < list.size(); i++) {
        if (list.get(k) == list.get(i))
          return true;
      }
    }
    return false;
  }

  static boolean containsDuplicateColumnNames(
      Relation<? extends Row,? extends Column> first,
      Relation<? extends Row,? extends Column> second,
      boolean ignoreCase
      ) {
  
    String c1Name, c2Name;
    BiPredicate<String, String> stringComparator;
    if (ignoreCase)
      stringComparator = String::equalsIgnoreCase;
    else
      stringComparator = String::equals;

    for (Column c1 : first.getColumns()) {
      c1Name = c1.getColumnName();
      if (c1Name == null)
        continue;
      for (Column c2 : second.getColumns()) {
        if (c2 == null)
          continue;
        c2Name = c2.getColumnName();

        if (stringComparator.test(c1Name, c2Name))
          return true;
      }
    }
    
    return false;
  }

  private static LocalDateTime toLocalDateTime(Date date) {
		long millis;
		Instant instant;

		millis = date.getTime();
		instant = Instant.ofEpochMilli(millis);

		return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
  }
	
  private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		long millis;
		Instant instant;

		millis = timestamp.getTime();
		instant = Instant.ofEpochMilli(millis);

		return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
	}
}
