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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import upsilon.data.DataColumn;
import upsilon.data.DataRelation;
import upsilon.data.DataRow;
import upsilon.data.PrimaryKey;
import upsilon.types.Tuple2;

public class DefaultDataReader extends DataReader {
  
  @Override
	public void fillDataRelation(
      DataRelation relation, 
      ResultSet resultSet
      ) throws SQLException {

		DataRow row;
		boolean originalAutoNullToDefault;

		if (relation == null)
			throw new IllegalArgumentException("table cannot be null");
		if (resultSet == null)
			throw new IllegalArgumentException("result set cannot be null");

		originalAutoNullToDefault = relation.getRules().getAutoNullToDefault();
		relation.getRules().setAutoNullToDefault(true);

		while (resultSet.next()) {
			row = relation.createRow();
			for (int k = 0; k < relation.getColumnCount(); k++) {
				row.set(k, resultSet.getObject(k + 1));
			}
      relation.addRow(row);
		}

		relation.getRules().setAutoNullToDefault(originalAutoNullToDefault);
	}

  @Override
	public DataRelation parseDataRelation(
      ResultSet resultSet, 
      DatabaseMetaData dbMetaData
      ) throws SQLException {
		
		DataRelation relation;

		if (resultSet == null)
			throw new IllegalArgumentException("result set cannot be null");

		relation = parseDataRelationSchema(resultSet, dbMetaData);
		fillDataRelation(relation, resultSet);

		return relation;
	}

  @Override
	public DataRelation parseDataRelationSchema(
      ResultSet resultSet,
      DatabaseMetaData dbMetaData
      ) throws SQLException {

		int columnCount;
		DataRelation relation;
		DataColumn column;
		ResultSetMetaData metaData;

		metaData = resultSet.getMetaData();
		columnCount = metaData.getColumnCount();
		relation = new DataRelation();
    relation.setRelationName(metaData.getTableName(1));

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
    
    if (dbMetaData != null)
      relation.setPrimaryKey(parsePrimaryKeyFromMetaData(relation, dbMetaData));

		return relation;
	}
  @Override
  protected PrimaryKey parsePrimaryKeyFromMetaData(
      DataRelation relation,
      DatabaseMetaData metaData
      ) throws SQLException {

    ResultSet keyTable;
    List<Tuple2<Short, DataColumn>> columnTuples = new LinkedList<>();
    final List<DataColumn> columns = new LinkedList<>();
    String columnName;
    short keySeq;

    keyTable = metaData.getPrimaryKeys(null, null, relation.getRelationName());
    while (keyTable.next()) {
      columnName = keyTable.getString("COLUMN_NAME");
      keySeq = keyTable.getShort("KEY_SEQ");

      columnTuples.add(new Tuple2(keySeq, relation.getColumn(columnName)));
    }

    columnTuples.sort((t0, t1) -> t0.value0 - t1.value0);
    columnTuples.forEach(t -> columns.add(t.value1));
    
    if (columns.isEmpty())
      return null;
    else
      return new PrimaryKey(columns);
  }
	
	
}
