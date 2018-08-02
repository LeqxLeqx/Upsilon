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
import java.time.LocalDateTime;
import java.util.Arrays;
import upsilon.data.DataRelation;
import upsilon.data.DataRow;
import upsilon.data.DataType;
import upsilon.tools.StringTools;
import upsilon.types.Ptr;

public abstract class DataWriter {

  public static DataWriter getDefault() {
    return new DefaultDataWriter();
  }



  protected DataWriter() { }
   
  public boolean addToBatch(
      final Statement statement, 
      final DataRelation relation
      ) throws SQLException {

    final Ptr<Boolean> ret;

    if (statement == null)
      throw new IllegalArgumentException("statement cannot be null");
    if (relation == null)
      throw new IllegalArgumentException("relation cannot be null");
    if (relation.getPrimaryKey()== null)
      throw new IllegalArgumentException(
          "relation does not define a primary key"
          );
    if (StringTools.isNullOrEmpty(relation.getRelationName()))
      throw new IllegalArgumentException(
          "relation name cannot be null or empty"
          );
    if (
        Arrays.stream(relation.getColumns())
            .anyMatch(c -> c.getColumnName() == null)
      )
      throw new IllegalArgumentException(
          "relation cannot hold null-named columns"
          );
    
    
    ret = new Ptr<>();
    ret.value = false;

    for (DataRow row : relation.getRows()) {
      if (row.getRowState().isActionable()) {
        if (addToBatch(statement, row))
          ret.value = true;
      }
    }
  
    return ret.value;
  }
  

  public boolean addToBatch(Statement statement, DataRow row) 
          throws SQLException {

    if (statement == null)
      throw new IllegalArgumentException("statement cannot be null");
    if (row == null)
      throw new IllegalArgumentException("row cannot be null");
    if (row.getOwner() == null)
      throw new IllegalArgumentException("row is not owned by a relation");
    if (row.getOwner().getPrimaryKey() == null)
      throw new IllegalArgumentException(
          "row's owner does not specify a primary key"
          );
    if (StringTools.isNullOrEmpty(row.getOwner().getRelationName()))
      throw new IllegalArgumentException(
          "relation name cannot be null or empty"
          );
    if (
        Arrays.stream(row.getOwner().getColumns())
            .anyMatch(c -> c.getColumnName() == null)
      )
      throw new IllegalArgumentException(
          "relation cannot hold null-named columns"
          );

    switch (row.getRowState()) {
      case ADDED:
        return addInsertToBatch(statement, row);
      case UPDATED:
        return addUpdateToBatch(statement, row);
      case DELETED:
        return addDeleteToBatch(statement, row);
      default:
        return false;
    }
  }

  protected abstract boolean addInsertToBatch(Statement statement, DataRow row)
          throws SQLException;
  protected abstract boolean addUpdateToBatch(Statement statement, DataRow row)
          throws SQLException;
  protected abstract boolean addDeleteToBatch(Statement statement, DataRow row)
          throws SQLException;


  protected String objectToString(Object object, DataType dataType) {

    if (dataType == null)
      throw new IllegalArgumentException("data type cannot be null");
    if (dataType == DataType.NONE)
        throw new IllegalArgumentException("data type cannot be NONE");

    if (object == null)
      return nullToString();

    switch (dataType) {
      case OBJECT:
        return objectToString(object);
      case INTEGER:
        return integerToString((Long) object);
      case REAL:
        return realToString((Double) object);
      case LOGICAL:
        return logicalToString((Boolean) object);
      case STRING:
        return stringToString((String) object);
      case DATETIME:
        return datetimeToString((LocalDateTime) object);
      default:
        throw new RuntimeException("unprogrammed data type: " + dataType);

    }
  }

  protected String nullToString() {
    return "NULL";
  }

  protected String objectToString(Object object) {
    throw new IllegalArgumentException(
        "cannot convert arbitrary object to SQL string"
        );
  }
  protected String integerToString(Long object) {
    return object.toString();
  }
  protected String realToString(Double object) {
    return object.toString();
  }
  protected String logicalToString(Boolean object) {
    return object ? "1" : "0";
  }
  protected String stringToString(String object) {
    /* TODO: VERIFY */
    return String.format("'%s'", object.replace("'", "''"));
  }
  protected String datetimeToString(LocalDateTime object) {
    return String.format(
        "'%04d-%02d-%02d %02d:%02d:%02d'",
        object.getYear(),
        object.getMonth().ordinal(),
        object.getDayOfMonth(),
        object.getHour(),
        object.getMinute(),
        object.getSecond()
        );
  }
  protected String rawToString(byte[] data) {
    StringBuilder sb;

    /* TODO: WHAT TO DO FOR EMPTY? */

    sb = new StringBuilder();
    sb.append("0x");
    for (byte b : data) {
      sb.append(String.format("%02d", b & 0xFF));
    }

    return sb.toString();
  }
  
}
