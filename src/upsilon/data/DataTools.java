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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class DataTools { private DataTools() {}

	public static DataRelation parseDataRelation(ResultSet resultSet) 
					throws SQLException {
		
		DataRelation relation;

		if (resultSet == null)
			throw new IllegalArgumentException("result set cannot be null");

		relation = parseDataRelationSchema(resultSet);
		fillDataTable(relation, resultSet);

		return relation;
	}

	public static DataRelation parseDataRelationSchema(ResultSet resultSet) 
					throws SQLException {

		int columnCount;
		DataRelation relation;
		DataColumn column;
		ResultSetMetaData metaData;

		metaData = resultSet.getMetaData();
		columnCount = metaData.getColumnCount();
		relation = new DataRelation();

		for (int k = 1; k < columnCount + 1; k++) {
			column = new DataColumn();
			
			column.setColumnName(metaData.getColumnName(k));
			column.setColumnDataType(
					sqlTypeIntToDataType(metaData.getColumnType(k))
					);
			column.setIsNullable(
				  metaData.isNullable(k) != ResultSetMetaData.columnNoNulls
					);
			
			relation.addColumn(column);
		}

		return relation;
	}

	public static void fillDataTable(DataRelation table, ResultSet resultSet) 
					throws SQLException {

		DataRow row;
		boolean originalAutoNullToDefault;

		if (table == null)
			throw new IllegalArgumentException("table cannot be null");
		if (resultSet == null)
			throw new IllegalArgumentException("result set cannot be null");

		originalAutoNullToDefault = table.getRules().getAutoNullToDefault();
		table.getRules().setAutoNullToDefault(true);

		while (resultSet.next()) {
			row = table.createRow();
			for (int k = 0; k < table.getColumnCount(); k++) {
				row.set(k, upsizeObject(resultSet.getObject(k + 1)));
			}
		}

		table.getRules().setAutoNullToDefault(originalAutoNullToDefault);
	}


	private static Object upsizeObject(Object object) {

		Class type;

		if (object == null)
			return null;
		type = object.getClass();

		if (type == Byte.class)
			return (long) ((Byte) object).byteValue();
		if (type == Short.class)
			return (long) ((Short) object).shortValue();
		if (type == Integer.class)
			return (long) ((Integer) object).intValue();
		
		if (type == Float.class)
			return (double) ((Float) object).floatValue();
		if (type == Timestamp.class)
			return toLocalDateTime((Timestamp) object);

		return object;
	}

	private static DataType sqlTypeIntToDataType(int type) {
		
		DataType ret;

		switch (type) {

			case Types.BINARY:
			case Types.VARBINARY:
			case Types.LONGVARBINARY:
			case Types.BLOB:
				ret = DataType.RAW;
				break;
			case Types.BOOLEAN:
			case Types.BIT:
				ret = DataType.LOGICAL;
				break;
			case Types.CHAR:
			case Types.VARCHAR:
			case Types.NCHAR:
			case Types.NVARCHAR:
			case Types.LONGVARCHAR:
			case Types.LONGNVARCHAR:
				ret = DataType.STRING;
				break;
			case Types.INTEGER:
			case Types.TINYINT:
			case Types.ROWID:
				ret = DataType.INTEGER;
				break;
			case Types.DATE:
			case Types.TIMESTAMP:
				ret = DataType.DATETIME;
				break;
			case Types.FLOAT:
			case Types.DOUBLE:
			case Types.REAL:
			case Types.DECIMAL:
				ret = DataType.REAL;
				break;

			default:
				ret = DataType.OBJECT;
				break;

		}

		return ret;

	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		long millis;
		Instant instant;

		millis = timestamp.getTime();
		instant = Instant.ofEpochMilli(millis);

		return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
	}
	
}
