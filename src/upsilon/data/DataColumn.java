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

import java.util.function.Supplier;
import upsilon.sanity.InsaneException;

public final class DataColumn implements Column<DataRelation> {

  private boolean nullable, readOnly;
  private int index;
	private String columnName;
	private DataType columnDataType;
	private DataRelation owner;
	private Supplier<Object> defaultObjectCallback;

	public DataColumn() {
    this.nullable = true;
		this.columnName = null;
		this.columnDataType = DataType.NONE;
		this.owner = null;
		this.defaultObjectCallback = () -> null;
	}
  public DataColumn(String name, DataType dataType) {
    this();
    setColumnName(name);
    setColumnDataType(dataType);
  }

  @Override
  public ColumnType getColumnType() {
    return ColumnType.DATA;
  }
  @Override
  public boolean isNullable() {
    return this.nullable;
  }
  @Override
  public boolean isReadOnly() {
    return this.readOnly;
  }
  @Override
  public int getIndex() {
    return this.index;
  }
  @Override
  public String getColumnName() {
    return this.columnName;
  }
  @Override
  public DataType getColumnDataType() {
    return this.columnDataType;
  }
  @Override
  public DataRelation getOwner() {
    return this.owner;
  }
  public Object getDefault() {
    Object ret;

    ret = defaultObjectCallback.get();
    if (ret != null && !this.columnDataType.accepts(ret.getClass()))
      throw new IllegalStateException(
          "default object was not compatable with the columnd data type"
          );
    if (ret == null && !this.nullable) {
      if (
        this.owner != null && 
        this.owner.getRules().getAutoNullToDefault() &&
        this.columnDataType.nonNullDefault != null
        )
        ret = this.columnDataType.nonNullDefault;
			else
				throw new IllegalStateException(
					"default object was null in a non-nullable column"
					);
		}

    return ret;
  }

  public DataColumn setIsNullable(boolean nullable) {
    if (
      nullable && 
      owner != null && 
      owner.getPrimaryKey() != null &&
      owner.getPrimaryKey().hasColumn(this)
      )
      throw new DataConstraintException(
          "this column is owned by a data relation whose primary key " +
          "contains this column. As such, it may not be set to a nullable state"
          );

    this.nullable = nullable;
    return this;
  }
  public DataColumn setIsReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
    return this;
  }
  DataColumn setIndexImp(int index) {
    if (index < 0)
      throw new IllegalArgumentException("column index must be non-negative");

    this.index = index;

    return this;
  }
  public DataColumn setIndex(int index) {
    int original = this.index;

    assertOwned();
    setIndexImp(index);

    try {
      this.owner.notifyColumnIndexChanged(this, original);
    } catch (InvalidChildModificationException e) {
      setIndexImp(original);
      throw new IllegalArgumentException(e.getMessage());
    }
    return this;
  }
  public DataColumn setColumnName(String columnName) {
    String original = this.columnName;

    this.columnName = columnName;
    if (this.owner != null) {
      try {
        this.owner.notifyColumnNameChanged(this, original);
      } catch (InvalidChildModificationException e) {
        this.columnName = original;
        throw new IllegalArgumentException(e.getMessage());
      }
    }
    
    return this;
  }
  public DataColumn setColumnDataType(DataType dataType) {
    if (dataType == null)
      throw new IllegalArgumentException(
        "column data type cannot be set to null"
        );
    if (owner != null && owner.forAnyRow(row -> row.get(this) != null))
      throw new IllegalStateException(
        "column data type cannot be updated while there exist rows within the" +
        "owning table which are non-null"
        );

    this.columnDataType = dataType;

    return this;
  }
  DataColumn setOwner(DataRelation owner) {

    this.owner = owner;
    return this;
  }
  public DataColumn setDefault(Object object) {
    
    this.defaultObjectCallback = () -> object;
    return this;
  }
  public DataColumn setDefault(Supplier<Object> producer) {

    this.defaultObjectCallback = producer;
    return this;
  }

  @Override
  public String toString() {
    if (columnName != null)
      return columnName;
    else if (owner != null)
      return String.format("%d", getIndex());
    else
      return "unspecified";
      
  }


  @Override
  public void checkSanity() throws InsaneException {
    
    InsaneException.assertTrue(columnDataType != null);
    InsaneException.assertTrue(defaultObjectCallback != null);

    if (this.owner != null && this.owner.getPrimaryKey() != null) {
      if (this.owner.getPrimaryKey().hasColumn(this))
        InsaneException.assertTrue(!nullable);
    }

  }

  void assertAccepts(Object object) {

    if (object == null && !isNullable())
      throw new DataConstraintException(String.format(
          "column `%s' does not allow nulls",
          toString()
          ));
    if (
      object != null && 
      !getColumnDataType().accepts(object.getClass())
      )
      throw new DataConstraintException(String.format(
          "column `%s' of type `%s' is incompatable with the type of the " +
          "provided object `%s'",
          toString(),
          getColumnDataType(),
          object.getClass().getName()
          ));
  }
	
}
