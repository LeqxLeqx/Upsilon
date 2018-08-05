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

import java.sql.SQLException;
import java.sql.Statement;
import upsilon.data.DataRow;
import upsilon.data.PrimaryKey;
import upsilon.types.Ptr;

public class DefaultDataWriter extends DataWriter {

  protected DefaultDataWriter() {
    super();
  }
  
  @Override
  protected boolean addInsertToBatch(
      Statement statement, 
      DataRow row
      ) throws SQLException {
    final StringBuilder sb;
    Ptr<Boolean> first;

    sb = new StringBuilder();
    first = new Ptr<>();

    sb.append(String.format(
        "INSERT INTO [%s] (",
        row.getOwner().getRelationName()
        ));

    first.value = true;
    row.getOwner().forEachColumn(column -> {
      if (!first.value)
        sb.append(',');
      
      sb.append(String.format("[%s]", column.getColumnName()));
      first.value = false;
    });

    sb.append(") VALUES (");
    
    first.value = true;
    row.getOwner().forEachColumn(column -> {
      if (!first.value)
        sb.append(',');

      sb.append(objectToString(row.get(column), column.getColumnDataType()));
      first.value = false;
    });

    sb.append(")");
    
    statement.addBatch(sb.toString());

    return true;
  }

  @Override
  protected boolean addUpdateToBatch(
      Statement statement, 
      DataRow row
      ) throws SQLException {

    final StringBuilder sb;
    final PrimaryKey key;
    Ptr<Boolean> first;
    
    sb = new StringBuilder();
    key = row.getOwner().getPrimaryKey();
    first = new Ptr<>();

    sb.append(String.format("UPDATE [%s] ", row.getOwner().getRelationName()));
    sb.append("SET ");

    first.value = true;
    row.getOwner().forEachColumn(column -> {
      if (key.hasColumn(column))
        return;

      if (!first.value)
        sb.append(",");
      sb.append(String.format(
          "[%s]=%s", 
          column.getColumnName(),
          objectToString(row.get(column), column.getColumnDataType())
          )); first.value = false;
    });

    sb.append(" WHERE ");

    first.value = true;
    row.getOwner().forEachColumn(column -> {
      if (!key.hasColumn(column))
        return;

      if (!first.value)
        sb.append(" AND ");
      sb.append(String.format(
          "[%s]=%s",
          column.getColumnName(),
          objectToString(row.get(column), column.getColumnDataType())
          ));
      first.value = false;
    });

    statement.addBatch(sb.toString());

    return true;
  }

  @Override
  protected boolean addDeleteToBatch(
      Statement statement, 
      DataRow row
      ) throws SQLException {
    
    final StringBuilder sb;
    final PrimaryKey key;
    Ptr<Boolean> first;
    
    sb = new StringBuilder();
    key = row.getOwner().getPrimaryKey();
    first = new Ptr<>();

    sb.append(String.format(
        "DELETE FROM [%s] WHERE ", 
        row.getOwner().getRelationName()
        ));

    first.value = true;
    row.getOwner().forEachColumn(column -> {
      if (!key.hasColumn(column))
        return;

      if (!first.value)
        sb.append(" AND ");
      sb.append(String.format(
          "[%s]=%s", 
          column.getColumnName(),
          objectToString(row.get(column), column.getColumnDataType())
          ));
      first.value = false;
    });

    statement.addBatch(sb.toString());

    return true;
  }
  
}
