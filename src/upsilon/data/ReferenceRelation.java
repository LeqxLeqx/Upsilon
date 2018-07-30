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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import upsilon.types.Ptr;

public class ReferenceRelation 
			implements Relation<ReferenceRow,ReferenceColumn> {

	static <RowType extends Row, ColumnType extends Column> ReferenceRelation 
	createFromSelect(
			DataRelation origin,
			Relation<RowType,ColumnType> source, 
			final Predicate<RowType> predicate
			) {
		
 		final Ptr<Integer> columnIndexPtr;
		final List<ReferenceRow> rows;
		final List<ReferenceColumn> columns;

		if (predicate == null)
			throw new IllegalArgumentException("predicate may not be null");

		columnIndexPtr = new Ptr<>();
		rows= new LinkedList<>();
		columns = new LinkedList<>();

		source.forEachRow(row -> {
			if (predicate.test(row))
				rows.add(ReferenceRow.ofOneReference(row, source.getColumnCount()));
		});
		columnIndexPtr.value = 0;
		source.forEachColumn(column -> {
			columns.add(new ReferenceColumn(
					column.getColumnName(), 
					columnIndexPtr.value++,
					column
					));
		});
		
		return new ReferenceRelation(rows, columns, origin);
	}
	
	static <RowType extends Row, ColumnType extends Column> ReferenceRelation 
	createFromSort(
			DataRelation origin,
			Relation<RowType,ColumnType> source,
			Comparator<RowType> comparator
			) {
		
		
 		final Ptr<Integer> columnIndexPtr;
		final List<ReferenceRow> rows;
		final List<ReferenceColumn> columns;
		final List<RowType> sourceRows;
		
		if (comparator == null)
			throw new IllegalArgumentException("comparator may not be null");

		columnIndexPtr = new Ptr<>();
		rows= new LinkedList<>();
		columns = new LinkedList<>();
		sourceRows = new ArrayList<>();

		source.forEachRow(sourceRows::add);
		sourceRows.sort(comparator);
		sourceRows.forEach(row -> {
			rows.add(ReferenceRow.ofOneReference(row, source.getColumnCount()));
		});

		columnIndexPtr.value = 0;
		source.forEachColumn(column -> {
			columns.add(new ReferenceColumn(
					column.getColumnName(), 
					columnIndexPtr.value++,
					column
					));
		});
		
		return new ReferenceRelation(rows, columns, origin);
	}

	static <RowType extends Row, ColumnType extends Column> ReferenceRelation
	createFromProject(
			DataRelation origin,
			Relation<RowType,ColumnType> source,
			Predicate<ColumnType> predicate
			) {
		
 		final Ptr<Integer> columnIndexPtr;
		final List<ReferenceRow> rows;
		final List<ReferenceColumn> columns;

		if (predicate == null)
			throw new IllegalArgumentException("predicate may not be null");

		columnIndexPtr = new Ptr<>();
		rows= new LinkedList<>();
		columns = new LinkedList<>();

		source.forEachRow(row -> {
			rows.add(ReferenceRow.ofOneReference(row, source.getColumnCount()));
		});
		columnIndexPtr.value = 0;
		source.forEachColumn(column -> {
			if (predicate.test(column))
				columns.add(new ReferenceColumn(
						column.getColumnName(), 
						columnIndexPtr.value++,
						column
						));
		});
		
		return new ReferenceRelation(rows, columns, origin);
	}

  private final List<ReferenceRow> rows;
  private final List<ReferenceColumn> columns;
  private final HashMap<String, ReferenceColumn> columnsMap;
  private final DataRelation origin;

  private ReferenceRelation(
			List<ReferenceRow> rows,
			List<ReferenceColumn> columns,
			DataRelation origin
			) {

		String columnName, originalColumnName;

		this.rows = rows;
		this.columns = columns;
		this.columnsMap = new HashMap<>();
		this.origin = origin;

		this.rows.forEach(r -> r.setOwner(this));
		this.columns.forEach(c -> c.setOwner(this));

		for (ReferenceColumn column : this.columns) {
			originalColumnName = column.getColumnName();
			if (originalColumnName == null)
				continue;

			columnName = originalColumnName;
			if (origin.getRules().getIgnoreCase())
				columnName = columnName.toLowerCase();

			if (this.columnsMap.containsKey(columnName))
				throw new IllegalArgumentException(String.format(
						"cannot create reference relation with " +
						"columns of duplicate name %s",
						originalColumnName
						));
			this.columnsMap.put(columnName, column);
		}
  }

  @Override
  public ReferenceColumn getColumn(String name) {
    String convertedName;
    ReferenceColumn column;
    
    if (name == null)
      throw new IllegalArgumentException("cannot get column with null name");

    convertedName = convertColumnName(name);
    column = this.columnsMap.getOrDefault(convertedName, null);
    if (column == null)
      throw new IllegalArgumentException(String.format(
        "no such column with name `%s'",
        name
        ));

    return column;
  }
  @Override
  public ReferenceColumn getColumn(int index) {
    if (index >= columns.size())
      throw new IllegalArgumentException(String.format(
        "column index out of bounds (%d >= %d)",
        index,
        columns.size()
        ));
    if (index < 0)
      throw new IllegalArgumentException("column index must be non-negative");

    return this.columns.get(index);
  }

  @Override
  public ReferenceColumn[] getColumns() {
    return this.columns.toArray(new ReferenceColumn[this.columns.size()]);
  }

  @Override
  public ReferenceRow[] getRows() {
    return this.rows.toArray(new ReferenceRow[this.rows.size()]);
  }
	
	@Override
	public int getRowCount() {
		return this.rows.size();
	}
	@Override
	public int getColumnCount() {
		return this.columns.size();
	}

  @Override
  public Relation select(Predicate<ReferenceRow> predicate) {
		return createFromSelect(origin, this, predicate);
  }

  @Override
  public Relation sort(Comparator<ReferenceRow> comparator) {
		return createFromSort(origin, this, comparator);
  }

  @Override
  public Relation project(Predicate<ReferenceColumn> predicate) {
		return createFromProject(origin, this, predicate);
  }
  
	@Override
	public void forEachRow(Consumer<ReferenceRow> consumer) {
		this.rows.forEach(consumer);
	}

	@Override
	public void forEachColumn(Consumer<ReferenceColumn> consumer) {
		this.columns.forEach(consumer);
	}

  /* HELPERS */

  private String convertColumnName(String name) {
    if (origin.getRules().getIgnoreCase())
      return name.toLowerCase();
    else
      return name;
  }
  
}
