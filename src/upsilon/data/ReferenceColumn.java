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

public class ReferenceColumn implements Column<ReferenceRelation> {

  private ReferenceRelation owner;
  private final String columnName;
  private final Column reference;
  private final int index;

  ReferenceColumn(
      String columnName,
      int index,
      Column reference
      ) {
    this.index = index;
    this.columnName = columnName;
    this.reference = reference;
  }


  @Override
  public ColumnType getColumnType() {
    return ColumnType.REFERENCE;
  }

  @Override
  public String getColumnName() {
    return this.columnName;
  }

  @Override
  public DataType getColumnDataType() {
    return this.reference.getColumnDataType();
  }

  @Override
  public int getIndex() {
    return this.index;
  }

  @Override
  public ReferenceRelation getOwner() {
    return this.owner;
  }

  Column getReference() {
    return this.reference;
  }

	ReferenceColumn setOwner(ReferenceRelation owner) {
		this.owner = owner;
		return this;
	}
  
}
