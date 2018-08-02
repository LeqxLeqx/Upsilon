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
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import upsilon.sanity.Sane;
import upsilon.tools.ArrayTools;

public interface Relation <TypeOfRow extends Row, TypeOfColumn extends Column>
				extends Iterable<TypeOfRow>, Sane {

  Column getColumn(int index);
  Column getColumn(String name);

  TypeOfRow[] getRows();
  TypeOfColumn[] getColumns();

	int getColumnCount();
	int getRowCount();

	void forEachRow(Consumer<TypeOfRow> consumer);
	void forEachColumn(Consumer<TypeOfColumn> consumer);

	@Override
	default Iterator<TypeOfRow> iterator() {
		return new RelationIterator(this);
	}


  /* SELECT */
	
	Relation<? extends Row,? extends Column> select(
      Predicate<TypeOfRow> predicate
      );
	default Relation<? extends Row,? extends Column> select() {
		return select(row -> true);
	}

  /* SORT */
	
	Relation<? extends Row,? extends Column> sort(
      Comparator<TypeOfRow> comparator
      );

  /* PROJECT */

	Relation<? extends Row,? extends Column> project(
      Predicate<TypeOfColumn> predicate
      );
	default Relation<? extends Row, ? extends Column> project(
      final TypeOfColumn... columns
      ) {
		if (ArrayTools.isNullOrContainsNull(columns))
			throw new IllegalArgumentException("column list cannot contain nulls");

		return project(column -> ArrayTools.contains(columns, column));
	}
	default Relation<? extends Row, ? extends Column> project(
      final String... columnNames
      ) {
		if (ArrayTools.isNullOrContainsNull(columnNames))
			throw new IllegalArgumentException(
					"column name list cannot contain nulls"
					);

		return project(
				column -> ArrayTools.contains(columnNames, column.getColumnName())
				);
	}

  /* RENAME */

  Relation<? extends Row,? extends Column> rename(
      Function<TypeOfColumn,String> function
      );
  Relation<? extends Row,? extends Column> rename(final String... names);
  
  /* CARTESIAN PRODUCT */
  <R extends Row, C extends Column> Relation<? extends Row,? extends Column> 
  product(Relation<R,C> relation);

  /* JOIN */
  <R extends Row, C extends Column> Relation<? extends Row,? extends Column> 
  join(Relation<R,C> relation, BiPredicate<TypeOfRow, R> predicate);
  
  <R extends Row, C extends Column> Relation<? extends Row,? extends Column> 
  naturalJoin(Relation<R,C> relation);

  <R extends Row, C extends Column> Relation<? extends Row,? extends Column>
  equiJoin(Relation<R,C> relation, String... names);

  <R extends Row, C extends Column> Relation<? extends Row,? extends Column>
  semiJoin(Relation<R,C> relation);

  <R extends Row, C extends Column> Relation<? extends Row,? extends Column>
  antiJoin(Relation<R,C> relation);

  /*
  <R extends Row, C extends Column> Relation<? extends Row,? extends Column>
  divide(Relation<R,C> relation);
  
  <R extends Row, C extends Column> Relation<? extends Row,? extends Column>
  fullOuterJoin(Relation<R,C> relation);

  <R extends Row, C extends Column> Relation<? extends Row,? extends Column>
  leftOuterJoin(Relation<R,C> relation);

  <R extends Row, C extends Column> Relation<? extends Row,? extends Column>
  rightOuterJoin(Relation<R,C> relation);
  */

  


}
