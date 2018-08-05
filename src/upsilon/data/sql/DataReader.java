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
package upsilon.data.sql;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import upsilon.data.DataRelation;
import upsilon.data.DataType;
import upsilon.data.PrimaryKey;

public abstract class DataReader {

  
  public static DataReader getDefault() {
    return new DefaultDataReader();
  }

	

  public abstract void fillDataRelation(
      DataRelation relation, 
      ResultSet resultSet
      ) throws SQLException;
  public abstract DataRelation parseDataRelation(
      ResultSet resultSet, 
      DatabaseMetaData dbMetaData
      ) throws SQLException;
	public abstract DataRelation parseDataRelationSchema(
      ResultSet resultSet,
      DatabaseMetaData dbMetaData
      ) throws SQLException;

  
  protected abstract PrimaryKey parsePrimaryKeyFromMetaData(
      DataRelation relation,
      DatabaseMetaData metaData
      ) throws SQLException;
  
	protected static DataType sqlTypeIntToDataType(int type) {
		
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

}
