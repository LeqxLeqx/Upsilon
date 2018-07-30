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

import upsilon.tools.ArrayTools;

public class ReferenceRow implements Row<ReferenceRelation> {

	static ReferenceRow ofOneReference(Row reference, int size) {
		return new ReferenceRow(ArrayTools.fill(new Row[size], reference));
	}


  private ReferenceRelation owner;
  private final Row[] references;

  ReferenceRow(
      Row[] references
      ) {
    this.owner = owner;
    this.references = references;
  }
  
  @Override
  public ReferenceRelation getOwner() {
    return owner;
  }

  @Override
  public RowType getRowType() {
    return RowType.REFERENCE;
  }

  @Override
  public Object get(Column column) {
    if (column == null)
      throw new IllegalArgumentException("column cannot be null");
    verifyColumn(column);

    return getFromReference((ReferenceColumn) column);
  }

  @Override
  public Object get(int index) {
    return getFromReference(index);
  }

  @Override
  public Object get(String columnName) {
    return getFromReference(owner.getColumn(columnName));
  }

  @Override
  public DataType getDataType(String columnName) {
    return owner.getColumn(columnName).getColumnDataType();
  }

  @Override
  public DataType getDataType(int index) {
    return owner.getColumn(index).getColumnDataType();
  }


  
  private Object getFromReference(int index) {
    return getFromReference(owner.getColumn(index));
  }
  private Object getFromReference(ReferenceColumn column) {
    Row referenceRow = this.references[column.getIndex()];
    return referenceRow.get(column.getReference()); 
  }

	ReferenceRow setOwner(ReferenceRelation owner) {
		this.owner = owner;
		return this;
	}

  private void verifyColumn(Column column) {
    if (column.getOwner() != this.getOwner())
      throw new IllegalArgumentException(
        "given column does not belong to the same relation as this row"
        );
  }
  
}
