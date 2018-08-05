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

import java.util.List;
import java.util.ArrayList;
import upsilon.sanity.InsaneException;
import upsilon.tools.ArrayTools;


public final class DataRow implements Row<DataRelation> {

  /* TODO: ADD IN ROW HISTORY */

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
  public Object get(Column<DataRelation> column) {
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
  
  public Object[] getPrimaryKeyValuesArray() {
    
    Object[] ret;
    final DataColumn[] primaryKeyColumns;

    assertOwned();
    if (this.owner.getPrimaryKey() == null)
      throw new IllegalArgumentException(
          "this row's owner does not specify a primary key"
          );

    primaryKeyColumns = this.owner.getPrimaryKey().getColumns();
    ret = new Object[primaryKeyColumns.length];

    ArrayTools.fill(ret, k -> get(primaryKeyColumns[k]));
    return ret;
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

  @Override
  public DataRow set(Column<DataRelation> column, Object object) {

    DataRow conflictingRow;
    Object originalValue, adjustedObject;
    boolean valueSet = false;

    assertOwned();
    assertNotDeleted();
    if (column == null)
      throw new IllegalArgumentException("column cannot be null");
    if (getOwner() != column.getOwner())
      throw new IllegalArgumentException(
          "column is not owned by the same relation as this row"
          );

    adjustedObject = DataTools.reObject(object, column.getColumnDataType());
    ((DataColumn) column).assertAccepts(adjustedObject);
    if (this.rowState != DataRowState.PENDING) {
    
      if (((DataColumn)column).isReadOnly())
        throw new DataConstraintException(
            "specified column is read only"
            );
      if (
        this.owner.getPrimaryKey() != null &&
        this.owner.getPrimaryKey().hasColumn((DataColumn) column)
        ) {
        originalValue = this.valuesArray.get(column.getIndex());
        this.valuesArray.set(column.getIndex(), adjustedObject);
        conflictingRow = this.owner
                .getRowByPrimaryKey(getPrimaryKeyValuesArray());

        if (conflictingRow != null && conflictingRow != this) {
          this.valuesArray.set(column.getIndex(), originalValue);
          throw new DataConstraintException(
              "desired row update results in a primary key confict in the " +
              "owning relation"
              );
        }
        this.owner.notifyRowRequiresRehashing(
            this, 
            originalValue, 
            (DataColumn) column
            );
        valueSet = true;
      }
    }

    if (!valueSet)
      this.valuesArray.set(column.getIndex(), adjustedObject);
    
    if (this.rowState == DataRowState.UNMODIFIED)
      this.rowState = DataRowState.UPDATED;

    return this;
  }
  @Override
  public DataRow set(int index, Object object) {
    assertOwned();

    return set(owner.getColumn(index), object);
  }
  @Override
  public DataRow set(String name, Object object) {
    assertOwned();

    return set(owner.getColumn(name), object);
  }

  public void delete() {
    setRowState(DataRowState.DELETED);
  }

  @Override
  public void checkSanity() throws InsaneException {
    
    DataColumn column;

    if (rowState == DataRowState.NONE) {
      InsaneException.assertTrue(this.owner == null);
      return;
    }

    InsaneException.assertTrue(this.owner != null);
    InsaneException.assertTrue(
        this.valuesArray.size() == this.owner.getColumns().length
        );

    for (int k = 0; k < this.valuesArray.size(); k++) {

      column = this.owner.getColumn(k);

      if (this.valuesArray.get(k) == null)
        InsaneException.assertTrue(column.isNullable());
      else
        InsaneException.assertTrue(
            column.getColumnDataType().accepts(
              this.valuesArray.get(k).getClass()
              )
            );
    }

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

    if (this.rowState == DataRowState.PENDING)
      return;

    ArrayTools.forEach(owner.getColumns(), (k, column) -> {
      this.valuesArray.set(column.getIndex(), column.getDefault());
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
