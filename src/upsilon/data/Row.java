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
import upsilon.sanity.Sane;
import upsilon.tools.WrapperTools;

public interface Row<OwningType> extends Ownable<OwningType>, Sane {

  RowType getRowType();

  Object get(Column<OwningType> column);
  Object get(int index);
  Object get(String columnName);

  Row<OwningType> set(Column<OwningType> column, Object object);
  Row<OwningType> set(int index, Object object);
  Row<OwningType> set(String columnName, Object object);
  
  DataType getDataType(String columnName);
  DataType getDataType(int index);
  
  default boolean isNull(Column<OwningType> column) {
    return get(column) == null;
	}
  default boolean isNull(int index) {
    return get(index) == null;
  }
  default boolean isNull(String columnName) {
    return get(columnName) == null;
  }

  default <T> T get(Column<OwningType> column, Class<T> realType) {
    Object ret;
    DataType dataType;
    
    ret = get(column);
    dataType = column.getColumnDataType();

    return dataType.cast(ret, realType);
  }
  default <T> T get(int index, Class<T> realType) {
    Object ret;
    DataType dataType;
    
    ret = get(index);
    dataType = getDataType(index);

    return dataType.cast(ret, realType);
  }
  default <T> T get(String columnName, Class<T> realType) {
    Object ret;
    DataType dataType;
    
    ret = get(columnName);
    dataType = getDataType(columnName);

    return dataType.cast(ret, realType);
  }

  /* TYPED GETTERS */

  default long getLong(Column<OwningType> column) {
		return WrapperTools.defaultToZero(get(column, Long.class));
  }
  default long getLong(int index) {
    return WrapperTools.defaultToZero(get(index, Long.class));
  }
  default long getLong(String columnName) {
    return WrapperTools.defaultToZero(get(columnName, Long.class));
  }
  
  default int getInt(Column<OwningType> column) {
    return (int) getLong(column);
  }
  default int getInt(int index) {
    return (int) getLong(index);
  }
  default int getInt(String columnName) {
    return (int) getLong(columnName);
  }

  default short getShort(Column<OwningType> column) {
    return (short) getLong(column);
  }
  default short getShort(int index) {
    return (short) getLong(index);
  }
  default short getShort(String columnName) {
    return (short) getLong(columnName);
  }

  default byte getByte(Column<OwningType> column) {
    return (byte) getLong(column);
  }
  default byte getByte(int index) {
    return (byte) getLong(index);
  }
  default byte getByte(String columnName) {
    return (byte) getLong(columnName);
  }

  default double getDouble(Column<OwningType> column) {
    return WrapperTools.defaultToZero(get(column, Double.class));
  }
  default double getDouble(int index) {
    return WrapperTools.defaultToZero(get(index, Double.class));
  }
  default double getDouble(String columnName) {
    return WrapperTools.defaultToZero(get(columnName, Double.class));
  }

  default float getFloat(Column<OwningType> column) {
    return (float) getDouble(column);
  }
  default float getFloat(int index) {
    return (float) getDouble(index);
  }
  default float getFloat(String columnName) {
    return (float) getDouble(columnName);
  }

  default boolean getBoolean(Column<OwningType> column) {
    return WrapperTools.defaultToFalse(get(column, Boolean.class));
  }
  default boolean getBoolean(int index) {
    return WrapperTools.defaultToFalse(get(index, Boolean.class));
  }
  default boolean getBoolean(String columnName) {
    return WrapperTools.defaultToFalse(get(columnName, Boolean.class));
  }

  default String getString(Column<OwningType> column) {
    return get(column, String.class);
  }
  default String getString(int index) {
    return get(index, String.class);
  }
  default String getString(String columnName) {
    return get(columnName, String.class);
  }

  default LocalDateTime getDateTime(Column<OwningType> column) {
    return get(column, LocalDateTime.class);
  }
  default LocalDateTime getDateTime(int index) {
    return get(index, LocalDateTime.class);
  }
  default LocalDateTime getDateTime(String columnName) {
    return get(columnName, LocalDateTime.class);
  }

  default byte[] getRaw(Column<OwningType> column) {
    return get(column, byte[].class);
  }
  default byte[] getRaw(int index) {
    return get(index, byte[].class);
  }
  default byte[] getRaw(String columnName) {
    return get(columnName, byte[].class);
  }

  /* TYPED SETTERS */

  default Row<OwningType> setLong(Column<OwningType> column, long value) {
    return set(column, value);
  }
  default Row<OwningType> setLong(int index, long value) {
    return set(index, value);
  }
  default Row<OwningType> setLong(String columnName, long value) {
    return set(columnName, value);
  }
  
  default Row<OwningType> setInt(Column<OwningType> column, int value) {
    return setLong(column, value);
  }
  default Row<OwningType> setInt(int index, int value) {
    return setLong(index, value);
  }
  default Row<OwningType> setInt(String columnName, int value) {
    return setLong(columnName, value);
  }
  
  default Row<OwningType> setShort(Column<OwningType> column, short value) {
    return setLong(column, value);
  }
  default Row<OwningType> setShort(int index, short value) {
    return setLong(index, value);
  }
  default Row<OwningType> setShort(String columnName, short value) {
    return setLong(columnName, value);
  }
  
  default Row<OwningType> setByte(Column<OwningType> column, byte value) {
    return setLong(column, value);
  }
  default Row<OwningType> setByte(int index, byte value) {
    return setLong(index, value);
  }
  default Row<OwningType> setByte(String columnName, byte value) {
    return setLong(columnName, value);
  }

  default Row<OwningType> setDouble(Column<OwningType> column, double value) {
    return set(column, value);
  }
  default Row<OwningType> setDouble(int index, double value) {
    return set(index, value);
  }
  default Row<OwningType> setDouble(String columnName, double value) {
    return set(columnName, value);
  }

  default Row<OwningType> setFloat(Column<OwningType> column, float value) {
    return setDouble(column, value);
  }
  default Row<OwningType> setFloat(int index, float value) {
    return setDouble(index, value);
  }
  default Row<OwningType> setFloat(String columnName, float value) {
    return setDouble(columnName, value);
  }

  default Row<OwningType> setBoolean(Column<OwningType> column, boolean value) {
    return set(column, value);
  }
  default Row<OwningType> setBoolean(int index, boolean value) {
    return set(index, value);
  }
  default Row<OwningType> setBoolean(String columnName, boolean value) {
    return set(columnName, value);
  }

  default Row<OwningType> setString(Column<OwningType> column, String value) {
    return set(column, value);
  }
  default Row<OwningType> setString(int index, String value) {
    return set(index, value);
  }
  default Row<OwningType> setString(String columnName, String value) {
    return set(columnName, value);
  }

  default Row<OwningType> setDateTime(Column<OwningType> column, LocalDateTime value) {
    return set(column, value);
  }
  default Row<OwningType> setDateTime(int index, LocalDateTime value) {
    return set(index, value);
  }
  default Row<OwningType> setDateTime(String columnName, LocalDateTime value) {
    return set(columnName, value);
  }

  default Row<OwningType> setRaw(Column<OwningType> column, byte[] value) {
    return set(column, value); /* TODO: consider cloning */
  }
  default Row<OwningType> setRaw(int index, byte[] value) {
    return set(index, value);
  }
  default Row<OwningType> setRaw(String columnName, byte[] value) {
    return set(columnName, value);
  }

}