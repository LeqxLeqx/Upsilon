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
import java.util.List;
import java.util.ArrayList;
import upsilon.tools.ArrayTools;


public final class DataRow implements Row<DataRelation> {

	private DataRowState rowState;
	private final List<Object> valuesArray;
	private DataRelation owner;

	public DataRow() {

		this.rowState = DataRowState.NONE;
	  this.valuesArray = new ArrayList<>();
		this.owner = null;

	} 

  @Override
  public RowType getRowType() {
    return RowType.DATA;
  }

  public DataRowState getRowState() {
    return rowState;
  }
  @Override
  public DataRelation getOwner() {
    return owner;
  }
  @Override
  public Object get(Column column) {
    return valuesArray.get(getIndex(column));
  }
  @Override
  public Object get(int index) {
    return valuesArray.get(getIndex(index));
  }
  @Override
  public Object get(String columnName) {
    return valuesArray.get(getIndex(columnName));
  }

  @Override
  public DataType getDataType(String columnName) {
    assertOwned();
    return owner.getColumn(columnName).getColumnDataType();
  }
  @Override
  public DataType getDataType(int index) {
    assertOwned();
    return owner.getColumn(index).getColumnDataType();
  }


  public DataRow setRowState(DataRowState state) {
    if (state == null)
      throw new IllegalArgumentException("row state cannot be set to null");

    this.rowState = state;

    return this;
  }
  DataRow setOwner(DataRelation owner) {

    this.owner = owner;
    this.valuesArray.clear();

    if (owner != null)
      fillWithDefaults();

    return this;
  }

  public DataRow set(DataColumn column, Object object) {

    assertOwned();
    assertNotDeleted();
    if (column == null)
      throw new IllegalArgumentException("column cannot be null");

    if (object == null && !column.isNullable())
      throw new DataConstraintException(String.format(
          "column `%s' does not allow nulls",
          column.toString()
          ));
    if (
      object != null && 
      !column.getColumnDataType().accepts(object.getClass())
      )
      throw new DataConstraintException(String.format(
          "column `%s' of type `%s' is incompatable with the type of the " +
          "provided object",
          column.toString(),
          column.getColumnDataType()
          ));

    this.valuesArray.set(column.getIndex(), object);
    if (
      this.owner.getRules().getActionsModifyRowState() &&
      this.rowState == DataRowState.UNMODIFIED
      )
      this.rowState = DataRowState.UPDATED;

    return this;
  }
  public DataRow set(int index, Object object) {
    assertOwned();

    return set(owner.getColumn(index), object);
  }
  public DataRow set(String name, Object object) {
    assertOwned();

    return set(owner.getColumn(name), object);
  }

  public void delete() {
    setRowState(DataRowState.DELETED);
  }


  /* TYPED SETTERS */

  public DataRow setLong(DataColumn column, long value) {
    return set(column, value);
  }
  public DataRow setLong(int index, long value) {
    return set(index, value);
  }
  public DataRow setLong(String columnName, long value) {
    return set(columnName, value);
  }
  
  public DataRow setInt(DataColumn column, int value) {
    return setLong(column, value);
  }
  public DataRow setInt(int index, int value) {
    return setLong(index, value);
  }
  public DataRow setInt(String columnName, int value) {
    return setLong(columnName, value);
  }
  
  public DataRow setShort(DataColumn column, short value) {
    return setLong(column, value);
  }
  public DataRow setShort(int index, short value) {
    return setLong(index, value);
  }
  public DataRow setShort(String columnName, short value) {
    return setLong(columnName, value);
  }
  
  public DataRow setByte(DataColumn column, byte value) {
    return setLong(column, value);
  }
  public DataRow setByte(int index, byte value) {
    return setLong(index, value);
  }
  public DataRow setByte(String columnName, byte value) {
    return setLong(columnName, value);
  }

  public DataRow setDouble(DataColumn column, double value) {
    return set(column, value);
  }
  public DataRow setDouble(int index, double value) {
    return set(index, value);
  }
  public DataRow setDouble(String columnName, double value) {
    return set(columnName, value);
  }

  public DataRow setFloat(DataColumn column, float value) {
    return setDouble(column, value);
  }
  public DataRow setFloat(int index, float value) {
    return setDouble(index, value);
  }
  public DataRow setFloat(String columnName, float value) {
    return setDouble(columnName, value);
  }

  public DataRow setBoolean(DataColumn column, boolean value) {
    return set(column, value);
  }
  public DataRow setBoolean(int index, boolean value) {
    return set(index, value);
  }
  public DataRow setBoolean(String columnName, boolean value) {
    return set(columnName, value);
  }

  public DataRow setString(DataColumn column, String value) {
    return set(column, value);
  }
  public DataRow setString(int index, String value) {
    return set(index, value);
  }
  public DataRow setString(String columnName, String value) {
    return set(columnName, value);
  }

  public DataRow setDateTime(DataColumn column, LocalDateTime value) {
    return set(column, value);
  }
  public DataRow setDateTime(int index, LocalDateTime value) {
    return set(index, value);
  }
  public DataRow setDateTime(String columnName, LocalDateTime value) {
    return set(columnName, value);
  }

  public DataRow setRaw(DataColumn column, byte[] value) {
    return set(column, value); /* TODO: consider cloning */
  }
  public DataRow setRaw(int index, byte[] value) {
    return set(index, value);
  }
  public DataRow setRaw(String columnName, byte[] value) {
    return set(columnName, value);
  }

  /* HELPERS */


  void recolumn(int oldIndex, int newIndex) {
    valuesArray.add(newIndex, valuesArray.remove(oldIndex));
  }


  private void assertNotDeleted() {
    if (rowState == DataRowState.DELETED)
      throw new IllegalArgumentException("row is deleted");
  }

  private void fillWithDefaults() {

    for (int k = 0; k < owner.getColumns().length; k++)
      this.valuesArray.add(null);

    ArrayTools.forEach(owner.getColumns(), (k, column) -> {
      set(column.getIndex(), column.getDefault());
    });
  }

  private void verifyColumn(Column column) {
    if (column.getOwner() != this.getOwner())
      throw new IllegalArgumentException(
        "given column does not belong to the same table as this row"
        );
  }
  private int getIndex(Column column) {
    assertOwned();
    if (column == null)
      throw new IllegalArgumentException("column cannot be null");
    verifyColumn((DataColumn) column);

    return column.getIndex();
  }
  private int getIndex(int index) {
    assertOwned();
    if (index >= this.valuesArray.size())
      throw new IllegalArgumentException(String.format(
        "index is out of bounds for this row (%d >= %d)",
        index,
        this.valuesArray.size()
        ));
    else if (index < 0)
      throw new IllegalArgumentException("index cannot be negative");

    return index;
  }
  private int getIndex(String columnName) {
    assertOwned();
    return getIndex(owner.getColumn(columnName));
  }

}
