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

import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import upsilon.tools.ArrayTools;

public interface Relation <RowType extends Row, ColumnType extends Column>
				extends Iterable<RowType> {	

  Column getColumn(int index);
  Column getColumn(String name);

  RowType[] getRows();
  ColumnType[] getColumns();

	int getColumnCount();
	int getRowCount();

	void forEachRow(Consumer<RowType> consumer);
	void forEachColumn(Consumer<ColumnType> consumer);

	@Override
	default Iterator<RowType> iterator() {
		return new RelationIterator(this);
	}

	
	Relation<Row,Column> select(Predicate<RowType> predicate);
	default Relation select() {
		return select(row -> true);
	}
	
	Relation<Row,Column> sort(Comparator<RowType> comparator);

	Relation<Row,Column> project(Predicate<ColumnType> predicate);
	default Relation project(final ColumnType... columns) {
		if (ArrayTools.isNullOrContainsNull(columns))
			throw new IllegalArgumentException("column list cannot contain nulls");

		return project(column -> ArrayTools.contains(columns, column));
	}
	default Relation project(final String... columnNames) {
		if (ArrayTools.isNullOrContainsNull(columnNames))
			throw new IllegalArgumentException(
					"column name list cannot contain nulls"
					);

		return project(
				column -> ArrayTools.contains(columnNames, column.getColumnName())
				);
	}

	/* TODO: ADD IN RENAMES */
}
