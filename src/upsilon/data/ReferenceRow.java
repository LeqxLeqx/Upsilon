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

import upsilon.sanity.InsaneException;
import upsilon.tools.ArrayTools;

public class ReferenceRow implements Row<ReferenceRelation> {

	static ReferenceRow ofOneReference(Row reference, int size) {
		return new ReferenceRow(ArrayTools.fill(new Row[size], reference));
	}
  static ReferenceRow ofTwoReferences(
      Row reference1, final int size1,
      Row reference2, int size2
      ) {
    Row[] references = new Row[size1 + size2];

    ArrayTools.fill(references, k -> k < size1 ? reference1 : reference2);
    return new ReferenceRow(references);
  }


  private ReferenceRelation owner;
  private final Row[] references;

  ReferenceRow(
      Row[] references
      ) {
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
  public ReferenceRow set(Column<ReferenceRelation> column, Object object) {
    if (column == null)
      throw new IllegalArgumentException("column cannot be null");
    verifyColumn(column);

    setToReference((ReferenceColumn)column, object);
    return this;
  }
  @Override
  public ReferenceRow set(int index, Object object) {
    setToReference(index, object);
    return this;
  }
  @Override
  public ReferenceRow set(String columnName, Object object) {
    setToReference(owner.getColumn(columnName), object);
    return this;
  }


  @Override
  public DataType getDataType(String columnName) {
    return owner.getColumn(columnName).getColumnDataType();
  }

  @Override
  public DataType getDataType(int index) {
    return owner.getColumn(index).getColumnDataType();
  }

  @Override
  public void checkSanity() throws InsaneException {
    InsaneException.assertTrue(this.owner != null);
    InsaneException.assertTrue(!ArrayTools.isNullOrContainsNull(references));
  }


  
  private Object getFromReference(int index) {
    return getFromReference(owner.getColumn(index));
  }
  private Object getFromReference(ReferenceColumn column) {
    Row referencedRow = this.references[column.getIndex()];
    return referencedRow.get(column.getReference()); 
  }

  private void setToReference(int index, Object object) {
    setToReference(owner.getColumn(index), object);
  }
  private void setToReference(ReferenceColumn column, Object object) {
    Row referencedRow = this.references[column.getIndex()];
    referencedRow.set(column.getReference(), object);
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
