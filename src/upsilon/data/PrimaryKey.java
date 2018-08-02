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
import java.util.Arrays;
import java.util.LinkedList;
import upsilon.types.Ptr;

public class PrimaryKey implements Ownable<DataRelation> {

  private final List<DataColumn> dataColumns;
  private DataRelation owner;

  public PrimaryKey(List<DataColumn> columns) {
    this.dataColumns = new ArrayList<>();
    setColumns(columns);
  }

  @Override
  public DataRelation getOwner() {
    return this.owner;
  }

  public DataColumn[] getColumns() {
    assertOwned();
    return dataColumns.toArray(new DataColumn[dataColumns.size()]);
  }

  public PrimaryKey setColumns(List<DataColumn> columns) {

    List<DataColumn> hold;

    if (columns == null || columns.contains(null))
      throw new IllegalArgumentException("cannot set primary key with nulls");
    if (columns.isEmpty())
      throw new IllegalArgumentException(
          "primary key cannot be composed of zero columns"
          );
    if (DataTools.containsDuplicateReference(columns))
      throw new IllegalArgumentException(
          "column list contains duplicate references"
          );
    if (columns.stream().anyMatch(c -> c.isNullable()))
      throw new IllegalArgumentException(
          "column list must not contain nullable columns"
          );
    
    hold = new LinkedList<>();
    hold.addAll(this.dataColumns);
    this.dataColumns.clear();

    this.dataColumns.addAll(columns);

    if (owner == null)
      return this;

    try {
      owner.notifyPrimaryKeyChanged();
    } catch (InvalidChildModificationException e) {
      this.dataColumns.clear();
      this.dataColumns.addAll(hold);
      throw new DataConstraintException(e.getMessage());
    }
    return this;
  }
  public PrimaryKey setColumns(DataColumn... columns) {
    if (columns == null)
      throw new IllegalArgumentException("cannot set primary key with nulls");
    setColumns(Arrays.asList(columns));
    return this;
  }

  public PrimaryKey addColumn(DataColumn column) {

    if (column == null)
      throw new IllegalArgumentException(
          "cannot add null column to primary key"
          );
    if (this.dataColumns.contains(column))
      throw new IllegalArgumentException(
          "column is already part of the primary key"
          );
    if (column.isNullable())
      throw new IllegalArgumentException("column must not be nullable");

    this.dataColumns.add(column);

    if (owner == null)
      return this;

    try {
      owner.notifyPrimaryKeyChanged();
    } catch (InvalidChildModificationException e) {
      this.dataColumns.remove(column);
      throw new DataConstraintException(e.getMessage());
    }
    return this;
  }

  public boolean hasColumn(final DataColumn column) {
    
    assertOwned();
    if (getOwner() != column.getOwner())
      throw new IllegalArgumentException(
          "data column is not owned by same relation as this primary key"
          );

    return this.dataColumns.stream().anyMatch(c -> c == column);
  }

  int getHashOfRow(DataRow row) {
    
    final Ptr<Integer> hash;

    assertOwned();
    if (getOwner() != row.getOwner())
      throw new IllegalArgumentException(
          "data row is not owned by same relation as this primary key"
          );
    
    hash = new Ptr<>();
    hash.value = 0;

    this.dataColumns.forEach(column -> {
      Object object;
      object = row.get(column);
      hash.value ^= object.hashCode();
      /* designed to fail if the object is null (on purpose) */
    });

    return hash.value;
  }
  int getHashOfValueList(Object[] values) {

    int hash;

    assertOwned();
    if (values.length != this.dataColumns.size())
      throw new IllegalArgumentException("values list is of incorrect size");

    hash = 0;
    for (int k = 0; k < values.length; k++) {
      if (!dataColumns.get(k).getColumnDataType().accepts(
              values[k].getClass())
        )
        throw new IllegalArgumentException(String.format(
            "value at index %d is incompatable with the data type of the " +
            "associated primary key column (%s)",
            k,
            dataColumns.get(k).getColumnDataType().toString()
            ));

      hash ^= values[k].hashCode();
    }

    return hash;
  }
  boolean rowsAreEqual(DataRow rowA, DataRow rowB) {

    Object a, b;

    for (DataColumn column : this.dataColumns) {
      a = rowA.get(column);
      b = rowB.get(column);

      /* designed to fail if the object is null (on purpose) */
      if (!a.equals(b))
        return false;
    }

    return true;
  }
  boolean rowsAreEqual(DataRow rowA, Object[] rowB) {

    Object a, b;
    int k = 0;

    for (DataColumn column : this.dataColumns) {
      a = rowA.get(column);

      /* designed to fail if the object is null (on purpose) */
      if (!a.equals(rowB[k]))
        return false;

      k++;
    }

    return true;
  }

  PrimaryKey setOwner(DataRelation owner) {
    if (!this.dataColumns.stream().allMatch(c -> c.getOwner() == owner))
      throw new IllegalArgumentException(
          "at least one column is not of the specified data relation"
          );
    this.owner = owner;
    if (this.owner == null)
      this.dataColumns.clear();
    return this;
  }

  
}
