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
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import upsilon.sanity.InsaneException;
import upsilon.types.Ptr;

public class ReferenceRelation 
			implements Relation<ReferenceRow,ReferenceColumn> {

	static <TypeOfRow extends Row, TypeOfColumn extends Column> ReferenceRelation 
	createFromSelect(
			DataRelation origin,
			Relation<TypeOfRow,TypeOfColumn> source, 
			final Predicate<TypeOfRow> predicate
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
	
	static <TypeOfRow extends Row, TypeOfColumn extends Column> ReferenceRelation 
	createFromSort(
			DataRelation origin,
			Relation<TypeOfRow,TypeOfColumn> source,
			Comparator<TypeOfRow> comparator
			) {
		
		
 		final Ptr<Integer> columnIndexPtr;
		final List<ReferenceRow> rows;
		final List<ReferenceColumn> columns;
		final List<TypeOfRow> sourceRows;
		
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

	static <TypeOfRow extends Row, TypeOfColumn extends Column> ReferenceRelation
	createFromProject(
			DataRelation origin,
			Relation<TypeOfRow,TypeOfColumn> source,
			Predicate<TypeOfColumn> predicate
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

  static <TypeOfRow extends Row, TypeOfColumn extends Column> ReferenceRelation 
  createFromRename(
			DataRelation origin,
			Relation<TypeOfRow,TypeOfColumn> source,
			Function<TypeOfColumn, String> function
      ) {
		
 		final Ptr<Integer> columnIndexPtr;
		final List<ReferenceRow> rows;
		final List<ReferenceColumn> columns;

		if (function == null)
			throw new IllegalArgumentException("function may not be null");

		columnIndexPtr = new Ptr<>();
		rows= new LinkedList<>();
		columns = new LinkedList<>();

		source.forEachRow(row -> {
			rows.add(ReferenceRow.ofOneReference(row, source.getColumnCount()));
		});
		columnIndexPtr.value = 0;
		source.forEachColumn(column -> {
				columns.add(new ReferenceColumn(
						function.apply(column),
						columnIndexPtr.value++,
						column
						));
		});
		
		return new ReferenceRelation(rows, columns, origin);
  }

  static <TypeOfRow extends Row, TypeOfColumn extends Column> ReferenceRelation 
  createFromRename(
			final DataRelation origin,
			Relation<TypeOfRow,TypeOfColumn> source,
      final String... names
      ) {
    
    final BiPredicate<String, String> stringComparator;
    
    if (names == null)
      throw new IllegalArgumentException("names array cannot be null");
    if (names.length % 2 != 0)
      throw new IllegalArgumentException("names array must have even length");
    for (int k = 0; k < names.length; k += 2) {
      if (names[k] == null)
        throw new IllegalArgumentException("cannot rename null-named columns");
    }

    if (origin.getRules().getIgnoreCase())
      stringComparator = String::equalsIgnoreCase;
    else
      stringComparator = String::equals;

    return createFromRename(
        origin,
        source,
        column -> {
          String columnName = column.getColumnName();
          if (columnName == null)
            return null;
          for (int k = 0; k < names.length; k += 2) {
            if (stringComparator.test(columnName, names[k]))
              return names[k + 1];
          }
          return columnName;
        }
        );
  }
	
  static <SourceRow extends Row, OtherRow extends Row> ReferenceRelation
	createFromProduct(
			DataRelation origin,
			final Relation<SourceRow,? extends Column> source,
      final Relation<OtherRow,? extends Column> other 
			) {
		
 		final Ptr<Integer> columnIndexPtr;
		final List<ReferenceRow> rows;
		final List<ReferenceColumn> columns;

		if (other == null)
			throw new IllegalArgumentException(
          "cannot take product of null relation"
          );
    if (DataTools.containsDuplicateColumnNames(
          source, other, origin.getRules().getIgnoreCase()
        ))
      throw new IllegalArgumentException(
          "two or more columns between the two relations share a column name"
          );

		columnIndexPtr = new Ptr<>();
		rows= new LinkedList<>();
		columns = new LinkedList<>();

		source.forEachRow((final SourceRow sourceRow) -> {
      other.forEachRow((final OtherRow otherRow) -> {
        rows.add(
            ReferenceRow.ofTwoReferences(
                sourceRow, source.getColumnCount(),
                otherRow, other.getColumnCount()
                )
            );
        });
		});
		columnIndexPtr.value = 0;
		source.forEachColumn(column -> {
      columns.add(new ReferenceColumn(
          column.getColumnName(), 
          columnIndexPtr.value++,
          column
          ));
		});
    other.forEachColumn(column -> {
      columns.add(new ReferenceColumn(
          column.getColumnName(),
          columnIndexPtr.value++,
          column
          ));
    });
		
		return new ReferenceRelation(rows, columns, origin);
	}
  
  static <SourceRow extends Row, OtherRow extends Row> ReferenceRelation
	createFromJoin(
			DataRelation origin,
			final Relation<SourceRow,? extends Column> source,
      final Relation<OtherRow,? extends Column> other ,
      BiPredicate<SourceRow, OtherRow> predicate
			) {
    
 		final Ptr<Integer> columnIndexPtr;
		final List<ReferenceRow> rows;
		final List<ReferenceColumn> columns;

		if (other == null)
			throw new IllegalArgumentException(
          "cannot take product of null relation"
          );
    if (DataTools.containsDuplicateColumnNames(
          source, other, origin.getRules().getIgnoreCase()
        ))
      throw new IllegalArgumentException(
          "two or more columns between the two relations share a column name"
          );

		columnIndexPtr = new Ptr<>();
		rows= new LinkedList<>();
		columns = new LinkedList<>();

		source.forEachRow((final SourceRow sourceRow) -> {
      other.forEachRow((final OtherRow otherRow) -> {
        if (predicate.test(sourceRow, otherRow)) {
          rows.add(
              ReferenceRow.ofTwoReferences(
                  sourceRow, source.getColumnCount(),
                  otherRow, other.getColumnCount()
                  )
              );
        }
        });
		});
		columnIndexPtr.value = 0;
		source.forEachColumn(column -> {
      columns.add(new ReferenceColumn(
          column.getColumnName(), 
          columnIndexPtr.value++,
          column
          ));
		});
    other.forEachColumn(column -> {
      columns.add(new ReferenceColumn(
          column.getColumnName(),
          columnIndexPtr.value++,
          column
          ));
    });
		
		return new ReferenceRelation(rows, columns, origin);
  }

  static <SourceRow extends Row, OtherRow extends Row> ReferenceRelation
	createFromNaturalJoin(
			DataRelation origin,
			final Relation<SourceRow,? extends Column> source,
      final Relation<OtherRow,? extends Column> other
			) {
    /* TODO: IMPLEMENT */
    throw new RuntimeException("NOT IMPLEMENTED");
  }

  static <SourceRow extends Row, OtherRow extends Row> ReferenceRelation
	createFromEquiJoin(
			DataRelation origin,
			final Relation<SourceRow,? extends Column> source,
      final Relation<OtherRow,? extends Column> other,
      String... columnNames
			) {
    /* TODO: IMPLEMENT */
    throw new RuntimeException("NOT IMPLEMENTED");
  }

  static <SourceRow extends Row, OtherRow extends Row> ReferenceRelation
	createFromSemiJoin(
			DataRelation origin,
			final Relation<SourceRow,? extends Column> source,
      final Relation<OtherRow,? extends Column> other
			) {
    /* TODO: IMPLEMENT */
    throw new RuntimeException("NOT IMPLEMENTED");
  }

  static <SourceRow extends Row, OtherRow extends Row> ReferenceRelation
	createFromAntiJoin(
			DataRelation origin,
			final Relation<SourceRow,? extends Column> source,
      final Relation<OtherRow,? extends Column> other
			) {
    /* TODO: IMPLEMENT */
    throw new RuntimeException("NOT IMPLEMENTED");
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
  public Relation rename(Function<ReferenceColumn, String> function) {
    return createFromRename(origin, this, function);
  }
  @Override
  public Relation rename(String... names) {
    return createFromRename(origin, this, names);
  }

  @Override
  public <R extends Row,C extends Column> 
  Relation<? extends Row,? extends Column> product(Relation<R,C> other) {
    return createFromProduct(origin, this, other);
  }

  @Override
  public <R extends Row, C extends Column> 
  Relation<? extends Row, ? extends Column> join(
      Relation<R, C> other, 
      BiPredicate<ReferenceRow, R> predicate
      ) {
    return createFromJoin(origin, this, other, predicate);
  }

  @Override
  public <R extends Row, C extends Column>
  Relation<? extends Row, ? extends Column> naturalJoin(Relation<R, C> other) {
    return ReferenceRelation.createFromNaturalJoin(origin, this, other);
  }

  @Override
  public <R extends Row, C extends Column> 
  Relation<? extends Row,? extends Column> equiJoin(
      Relation<R,C> relation, String... names
      ) {
    return createFromEquiJoin(origin, this, relation, names);
  }

  @Override
  public <R extends Row, C extends Column> 
  Relation<? extends Row,? extends Column> semiJoin(Relation<R,C> relation) {
    return createFromSemiJoin(origin, this, relation);
  }

  @Override
  public <R extends Row, C extends Column> 
  Relation<? extends Row,? extends Column> antiJoin(Relation<R,C> relation) {
    return createFromAntiJoin(origin, this, relation);
  }

  
	@Override
	public void forEachRow(Consumer<ReferenceRow> consumer) {
		this.rows.forEach(consumer);
	}

	@Override
	public void forEachColumn(Consumer<ReferenceColumn> consumer) {
		this.columns.forEach(consumer);
	}
  
  @Override
  public void checkSanity() throws InsaneException {
    
    InsaneException.assertTrue(origin != null);
    ReferenceColumn column;
    String columnName;

    for (ReferenceRow row : rows) {
      InsaneException.assertTrue(row.getOwner() == this);
      row.checkSanity();
    }

    for (int k = 0; k < this.columns.size(); k++) {
      column = this.columns.get(k);
      columnName = column.getColumnName();

      InsaneException.assertTrue(column.getIndex() == k);
      if (columnName  == null)
        continue;
      columnName = convertColumnName(columnName);
      InsaneException.assertTrue(this.columnsMap.containsKey(columnName));
      InsaneException.assertTrue(this.columnsMap.get(columnName) == column);
      column.checkSanity();
    }

  }

  /* HELPERS */

  private String convertColumnName(String name) {
    if (origin.getRules().getIgnoreCase())
      return name.toLowerCase();
    else
      return name;
  }
  
}
