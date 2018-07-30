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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import upsilon.types.Ptr;


public final class DataRelation implements Relation<DataRow,DataColumn> {

 
	private String relationName;
  private final DataRelationRules rules;
	private final List<DataColumn> dataColumns;
  private final HashMap<String, DataColumn> dataColumnsMap;
	private final List<DataRow> dataRows;

	private ForEachRowContext forEachRowContext;
	private ForEachColumnContext forEachColumnContext;

	public DataRelation() {
    this.rules = new DataRelationRules();
		this.relationName = null;
		this.dataColumns = new ArrayList<>();
    this.dataColumnsMap = new HashMap<>();
		this.dataRows = new ArrayList<>();
		this.forEachRowContext = null;
		this.forEachColumnContext = null;

    this.rules.setOwner(this);
	}
  
  public DataRelationRules getRules() {
    return this.rules;
  }

  public String getRelationName() {
    return relationName;
  }
  public DataColumn[] getColumns() {
    return dataColumns.toArray(new DataColumn[dataColumns.size()]);
  }
  public DataRow[] getRows() {
    return dataRows.toArray(new DataRow[dataRows.size()]);
  } 

  public DataRelation setRules(DataRelationRules rules) {
    if (rules == null)
      throw new IllegalArgumentException("rules cannot be null");

    this.rules.copyFrom(rules);
    return this;
  }
  public DataRelation setRelationName(String relationName) {
    this.relationName = relationName;

    return this;
  }
  @Override
  public DataColumn getColumn(String name) {
    String convertedName;
    DataColumn column;
    
    if (name == null)
      throw new IllegalArgumentException("cannot get column with null name");

    convertedName = convertColumnName(name);
    column = this.dataColumnsMap.getOrDefault(convertedName, null);
    if (column == null)
      throw new IllegalArgumentException(String.format(
        "no such column with name `%s'",
        name
        ));

    return column;
  }
  @Override
  public DataColumn getColumn(int index) {
    if (index >= dataColumns.size())
      throw new IllegalArgumentException(String.format(
        "column index out of bounds (%d >= %d)",
        index,
        dataColumns.size()
        ));
    if (index < 0)
      throw new IllegalArgumentException("column index must be non-negative");

    return this.dataColumns.get(index);
  }
  public DataColumn getColumnOrNull(String name) {
    String convertedName;
    
    if (name == null)
      throw new IllegalArgumentException("cannot get column with null name");
    
    convertedName = convertColumnName(name);
    return dataColumnsMap.getOrDefault(convertedName, null);
  }
  public DataRow getRow(Predicate<DataRow> predicate) {
    if (predicate == null)
      throw new IllegalArgumentException("predicate cannot be null");

    for (DataRow row : dataRows) {
      if (predicate.test(row))
        return row;
    }
    
    return null;
  }
  public DataRow[] getRows(Predicate<DataRow> predicate) {
    List<DataRow> ret;

    if (predicate == null)
      throw new IllegalArgumentException("predicate cannot be null");

    ret = new LinkedList<>();
    for (DataRow row : dataRows) {
      if (predicate.test(row))
        ret.add(row);
    }
  
    return ret.toArray(new DataRow[ret.size()]);
  }
	@Override
	public int getColumnCount() {
		return this.dataColumns.size();
	}
	@Override
	public int getRowCount() {
		return this.dataRows.size();
	}


  public void addRow(DataRow row) {
    if (row == null)
      throw new IllegalArgumentException("cannot add null row");
    assertUnowned(row, "row");
		
		if (this.forEachRowContext == null)
			this.dataRows.add(row);
		else
			this.forEachRowContext.add.add(row);

		row.setOwner(this);
		if (rules.getActionsModifyRowState())
			row.setRowState(DataRowState.ADDED);
  }
  public void addColumn(DataColumn column) {

    String columnName;

    if (column == null)
      throw new IllegalArgumentException("cannot add null column");
    assertUnowned(column, "column");
    
		if (this.forEachColumnContext == null) {
			column.setIndexImp(this.dataColumns.size());
			this.dataColumns.add(column);
		}
		else {
			column.setIndexImp(
					this.dataColumns.size() + this.forEachColumnContext.add.size()
					);
			this.forEachColumnContext.add.add(column);
		}

    column.setOwner(this);

    columnName = column.getColumnName();

    if (columnName != null)
      this.dataColumnsMap.put(convertColumnName(columnName), column);
  }

  public void removeRow(DataRow row) {
    if (row == null)
      throw new IllegalArgumentException("cannot remove null row");
    assertOwned(row, "row");
		
		if (this.forEachRowContext == null)
			this.dataRows.remove(row);
		else
			this.forEachRowContext.remove.add(row);

		row.setOwner(null);
		if (rules.getActionsModifyRowState())
			row.setRowState(DataRowState.NONE);
  }
  public void removeColumn(DataColumn column) {
		if (column == null)
			throw new IllegalArgumentException("cannot remove null column");
		assertOwned(column, "column");

		if (this.forEachColumnContext == null)
			this.dataColumns.remove(column);
		else
			this.forEachColumnContext.remove.add(column);

		column.setOwner(null);
  }



  public void forEachRow(Consumer<DataRow> consumer) {
		try {
			startForEachRowContext();
			this.dataRows.forEach(consumer);
		} finally {
			endForEachRowContext();
		}
  }
  public void forEachRowPair(BiConsumer<DataRow, DataRow> biConsumer) {
		try {
			startForEachRowContext();

			for (int k = 0; k < this.dataRows.size() - 1; k++) {
				for (int i = k + 1; i < this.dataRows.size(); i++) {
					biConsumer.accept(this.dataRows.get(k), this.dataRows.get(i));
				}
			}

		} finally {
			endForEachRowContext();
		}
  }
  public void forEachColumn(Consumer<DataColumn> consumer) {
		try {
			startForEachColumnContext();
			this.dataColumns.forEach(consumer);
		} finally {
			endForEachColumnContext();
		}
  }
  public void forEachColumnPair(BiConsumer<DataColumn, DataColumn> biConsumer) {
		try {
			startForEachColumnContext();

			for (int k = 0; k < this.dataColumns.size() - 1; k++) {
				for (int i = k + 1; i < this.dataColumns.size(); i++) {
					biConsumer.accept(this.dataColumns.get(k), this.dataColumns.get(i));
				}
			}

		} finally {
			endForEachColumnContext();
		}
  }
  public boolean forAnyRow(Predicate<DataRow> predicate) {
		boolean ret;

		try {
			startForEachRowContext();

			if (predicate == null)
				throw new IllegalArgumentException("predicate cannot be null");

			ret = this.dataRows.stream().anyMatch(predicate);
		} finally {
			endForEachRowContext();
		}

		return ret;
  }
  public boolean forAllRows(Predicate<DataRow> predicate) {
		boolean ret;

		try {
			startForEachRowContext();

			if (predicate == null)
				throw new IllegalArgumentException("predicate cannot be null");

			ret = this.dataRows.stream().allMatch(predicate);
		} finally {
			endForEachRowContext();
		}

		return ret;
  }
	
  @Override
	public Relation select(Predicate<DataRow> predicate) {
		return ReferenceRelation.createFromSelect(this, this, predicate);
  }

  @Override
  public Relation sort(Comparator<DataRow> comparator) {
		return ReferenceRelation.createFromSort(this, this, comparator);
  }

  @Override
  public Relation project(Predicate<DataColumn> predicate) {
		return ReferenceRelation.createFromProject(this, this, predicate);
  }


  /* CONVENIENCE METHODS */

  public DataRow createRow() {
    DataRow row = new DataRow();
    addRow(row);
    return row;
  }

  public void normalizeChanges() {
    forEachRow(row -> {
      switch (row.getRowState()) {
        case DELETED:
          removeRow(row);
          break;
        case ADDED:
        case UPDATED:
          row.setRowState(DataRowState.UNMODIFIED);
          break;
        default:
          break;
      }
    });
  }


  /* CHILD MODIFICATION NOTIFIERS  */


  void notifyRulesIgnoreCaseChanged() 
          throws InvalidChildModificationException {
    if (rules.getIgnoreCase() && !columnNamesAreUnique())
      throw new InvalidChildModificationException(
          "columns are not unique when ignoring case"
          );
  }

  void notifyColumnNameChanged(DataColumn column, String old) 
          throws InvalidChildModificationException {

    String columnName;

    if (old != null)
      this.dataColumnsMap.remove(old);

    columnName = column.getColumnName();
    if (columnName != null) {
      if (!columnNameWillBeUnique(columnName))
        throw new InvalidChildModificationException(
            "column of name `%s' is already present within the relation",
            columnName
            );

      this.dataColumnsMap.put(convertColumnName(columnName), column);
    }
  }
  void notifyColumnIndexChanged(DataColumn column, int old) 
          throws InvalidChildModificationException {

    int newIndex = column.getIndex();

    if (newIndex >= this.dataColumns.size())
      throw new InvalidChildModificationException(
          "column index is out of bounds (%d >= %d)",
          newIndex,
          this.dataColumns.size()
          );

    for (int k = old + 1; k < newIndex + 1; k++)
      this.dataColumns.get(k).setIndexImp(k - 1);
    for (int k = newIndex; k < old; k++)
      this.dataColumns.get(k).setIndexImp(k + 1);

    this.dataColumns.remove(old);
    this.dataColumns.add(newIndex, column);

    this.dataRows.forEach(row -> {
      row.recolumn(old, newIndex);
    });
  }


  /* HELPERS */

  private String convertColumnName(String name) {
    if (rules.getIgnoreCase())
      return name.toLowerCase();
    else
      return name;
  }

  private boolean columnNamesAreUnique() {
    final Ptr<Boolean> ret = new Ptr<>();
    ret.value = true;
    forEachColumnPair((a, b) -> {
      if (!ret.value || a == null || b == null)
        return;
      if (convertColumnName(a.getColumnName())
          .equals(convertColumnName(b.getColumnName())))
        ret.value = false;
    });

    return ret.value;
  }

  private boolean columnNameWillBeUnique(String name) {
    if (name == null)
      return true;
    return !this.dataColumnsMap.containsKey(convertColumnName(name));
  }

  private void assertUnowned(Ownable<DataRelation> o, String typeName) {
    if (o.getOwner() != null) {
      if (o.getOwner() == this)
        throw new IllegalArgumentException(
          "given " + typeName + " already belongs to this relation"
          );
      else
        throw new IllegalArgumentException(
          "given " + typeName + " already belongs to another relation"
          );
    }
  }

	private void assertOwned(Ownable<DataRelation> o, String typeName) {
		if (o.getOwner() != this)
			throw new IllegalArgumentException(
				"given " + typeName + " does not belong to this relation"
				);
	}

	private void startForEachRowContext() {
		if (forEachRowContext == null)
			forEachRowContext = new ForEachRowContext();

		forEachRowContext.start();
	}
	private void endForEachRowContext() {
		if (forEachRowContext == null)
			throw new IllegalStateException("unexpected for-each row context state");

		forEachRowContext.complete();
		if (forEachRowContext.shouldRelease())
			forEachRowContext = null;
	}

	private void startForEachColumnContext() {
		if (forEachColumnContext == null)
			forEachColumnContext = new ForEachColumnContext();

		forEachColumnContext.start();
	}
	private void endForEachColumnContext() {
		if (forEachColumnContext == null)
			throw new IllegalStateException(
					"unexpected for-each column context state"
					);

		forEachColumnContext.complete();
		if (forEachColumnContext.shouldRelease())
			forEachColumnContext = null;
	}

	private abstract class ForEachContext<T> {

		final List<T> remove, add;
		int depth;

		ForEachContext() {
			this.remove = new LinkedList<>();
			this.add = new LinkedList<>();
			this.depth = 0;
		}

		abstract List<T> effected();

		void start() {
			this.depth++;
		}
		void complete() {
			this.depth--;
			if (depth == 0) {
				this.remove.forEach(effected()::remove);
				this.add.forEach(effected()::add);
			}
		}
		boolean shouldRelease() {
			return depth == 0;
		}
	}

	private class ForEachRowContext extends ForEachContext<DataRow> {
		@Override List<DataRow> effected() {
			return DataRelation.this.dataRows;
		}
	}

	private class ForEachColumnContext extends ForEachContext<DataColumn> {
		@Override List<DataColumn> effected() {
			return DataRelation.this.dataColumns;
		}
	}
	
}
