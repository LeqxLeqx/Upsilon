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
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import upsilon.sanity.InsaneException;
import upsilon.tools.ArrayTools;
import upsilon.types.Ptr;


public final class DataRelation implements Relation<DataRow,DataColumn> {

 
	private String relationName;
  private PrimaryKey primaryKey;
  private final DataRelationRules rules;
	private final List<DataColumn> dataColumns;
  private final HashMap<String, DataColumn> dataColumnsMap;
	private final List<DataRow> dataRows, pendingDataRows;
  private final HashMap<Integer, List<DataRow>> dataRowsMap;

	public DataRelation() {
    this.rules = new DataRelationRules();
    this.primaryKey = null;
		this.relationName = null;
		this.dataColumns = new ArrayList<>();
    this.dataColumnsMap = new HashMap<>();
		this.dataRows = new ArrayList<>();
    this.pendingDataRows = new LinkedList<>();
    this.dataRowsMap = new HashMap<>();

    this.rules.setOwner(this);
	}
  
  public PrimaryKey getPrimaryKey() {
    return this.primaryKey; 
  }
  public DataRelationRules getRules() {
    return this.rules;
  }

  public String getRelationName() {
    return relationName;
  }
  @Override
  public DataColumn[] getColumns() {
    return dataColumns.toArray(new DataColumn[dataColumns.size()]);
  }
  @Override
  public DataRow[] getRows() {
    return dataRows.toArray(new DataRow[dataRows.size()]);
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
  public DataRow getRowByPrimaryKey(Object... values) {
    
    int hash;
    List<DataRow> rows;

    if (ArrayTools.isNullOrContainsNull(values))
      throw new IllegalArgumentException(
          "primary key value list cannot contain null values"
          );
    if (this.primaryKey == null)
      throw new IllegalStateException(
          "this relation does not specify a primary key"
          );

    hash = this.primaryKey.getHashOfValueList(values);
    rows = this.dataRowsMap.getOrDefault(hash, null);

    if (rows == null)
      return null;

    for (DataRow row : rows) {
      if (this.primaryKey.rowsAreEqual(row, values))
        return row;
    }

    return null;
  }
  @Override
  public int getColumnCount() {
    return this.dataColumns.size();
  }
  @Override
  public int getRowCount() {
    return this.dataRows.size();
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
  public DataRelation setPrimaryKey(PrimaryKey key) {
    this.dataRowsMap.clear();
    if (key == null) {
      return this;
    }
    assertUnowned(key, "primary key");
    assertPrimaryKeyIsUniqueOverRelation(key);

    if (this.primaryKey != null)
      this.primaryKey.setOwner(null);
    this.primaryKey = key;
    this.primaryKey.setOwner(this);
    
    recalculateRowMap();

    return this;
  }



  public void addRow(DataRow row) {
    if (row == null)
      throw new IllegalArgumentException("cannot add null row");
    if (row.getRowState() == DataRowState.PENDING) {
      addRowFromPending(row);
      return;
    }

    assertUnowned(row, "row"); 
    this.dataRows.add(row);
    row.setOwner(this);
    addSingleRowToDataRowsMap(row);

    row.setRowState(DataRowState.ADDED);
  }
  public void addPendingRow(DataRow row) {
    if (row == null)
      throw new IllegalArgumentException("cannot add null row");
    assertUnowned(row, "row");

    this.pendingDataRows.add(row);
    row.setRowState(DataRowState.PENDING);
    row.setOwner(this);

  }
  public void addColumn(DataColumn column) {

    String columnName;

    if (column == null)
      throw new IllegalArgumentException("cannot add null column");
    if (!this.pendingDataRows.isEmpty())
      throw new IllegalArgumentException(
          "cannot add column while this relation holds pending data rows"
          );
    assertUnowned(column, "column");
    
    column.setIndexImp(this.dataColumns.size());
    this.dataColumns.add(column);

    column.setOwner(this);

    columnName = column.getColumnName();

    if (columnName != null)
      this.dataColumnsMap.put(convertColumnName(columnName), column);
  }

  public void removeRow(DataRow row) {

    if (row == null)
      throw new IllegalArgumentException("cannot remove null row");
    assertOwned(row, "row");
    if (row.getRowState() == DataRowState.PENDING)
      throw new IllegalArgumentException("cannot remove row in pending state");

    if (!this.dataRows.remove(row))
      throw new IllegalStateException("failed to remove row");

    attemptRemoveRowFromDataRowsMap(row);
		row.setOwner(null);
    row.setRowState(DataRowState.NONE);
  }
  
  
  public void removeColumn(DataColumn column) {
		if (column == null)
			throw new IllegalArgumentException("cannot remove null column");
    if (!this.pendingDataRows.isEmpty())
      throw new IllegalArgumentException(
          "cannot remove column while this relation holds pending data rows"
          );
		assertOwned(column, "column");

    this.dataColumns.remove(column);
    if (column.getColumnName() != null)
      this.dataColumnsMap.remove(column.getColumnName());

		column.setOwner(null);
  }


  @Override
  public void forEachRow(Consumer<DataRow> consumer) {
    this.dataRows.forEach(consumer);
  }
  public void forEachRowPair(BiConsumer<DataRow, DataRow> biConsumer) {

    for (int k = 0; k < this.dataRows.size() - 1; k++) {
      for (int i = k + 1; i < this.dataRows.size(); i++) {
        biConsumer.accept(this.dataRows.get(k), this.dataRows.get(i));
      }
    }
  }
  @Override
  public void forEachColumn(Consumer<DataColumn> consumer) {
		this.dataColumns.forEach(consumer);
  }
  public void forEachColumnPair(BiConsumer<DataColumn, DataColumn> biConsumer) {

    for (int k = 0; k < this.dataColumns.size() - 1; k++) {
      for (int i = k + 1; i < this.dataColumns.size(); i++) {
        biConsumer.accept(this.dataColumns.get(k), this.dataColumns.get(i));
      }
    }
  }
  public boolean forAnyRow(Predicate<DataRow> predicate) {
		boolean ret;

    if (predicate == null)
      throw new IllegalArgumentException("predicate cannot be null");

    ret = this.dataRows.stream().anyMatch(predicate);

		return ret;
  }
  public boolean forAllRows(Predicate<DataRow> predicate) {
		boolean ret;

    if (predicate == null)
      throw new IllegalArgumentException("predicate cannot be null");

    ret = this.dataRows.stream().allMatch(predicate);

		return ret;
  }
	
  @Override
	public Relation<? extends Row,? extends Column> select(
      Predicate<DataRow> predicate
      ) {
		return ReferenceRelation.createFromSelect(this, this, predicate);
  }

  @Override
  public Relation<? extends Row,? extends Column> sort(
      Comparator<DataRow> comparator
      ) {
		return ReferenceRelation.createFromSort(this, this, comparator);
  }

  @Override
  public Relation<? extends Row,? extends Column> project(
      Predicate<DataColumn> predicate
      ) {
		return ReferenceRelation.createFromProject(this, this, predicate);
  }

  @Override
  public Relation<? extends Row,? extends Column> rename(
      Function<DataColumn,String> function
      ) {
    return ReferenceRelation.createFromRename(this, this, function);
  }

  @Override
  public Relation<? extends Row,? extends Column> rename(
      String... names
      ) {
    return ReferenceRelation.createFromRename(this, this, names);
  }

  @Override
  public <R extends Row,C extends Column> 
  Relation<? extends Row,? extends Column> product(Relation<R,C> other) {
    return ReferenceRelation.createFromProduct(this, this, other);
  }

  @Override
  public <R extends Row, C extends Column> 
  Relation<? extends Row, ? extends Column> join(
      Relation<R, C> other, 
      BiPredicate<DataRow, R> predicate
      ) {
    return ReferenceRelation.createFromJoin(this, this, other, predicate);
  }

  @Override
  public <R extends Row, C extends Column>
  Relation<? extends Row, ? extends Column> naturalJoin(Relation<R, C> other) {
    return ReferenceRelation.createFromNaturalJoin(this, this, other);
  }

  @Override
  public <R extends Row, C extends Column> 
  Relation<? extends Row,? extends Column> equiJoin(
      Relation<R,C> relation, String... names
      ) {
    return ReferenceRelation.createFromEquiJoin(this, this, relation, names);
  }

  @Override
  public <R extends Row, C extends Column> 
  Relation<? extends Row,? extends Column> semiJoin(Relation<R,C> relation) {
    return ReferenceRelation.createFromSemiJoin(this, this, relation);
  }

  @Override
  public <R extends Row, C extends Column> 
  Relation<? extends Row,? extends Column> antiJoin(Relation<R,C> relation) {
    return ReferenceRelation.createFromAntiJoin(this, this, relation);
  }

  public DataRow createRow() {
    DataRow row = new DataRow();
    addPendingRow(row);
    return row;
  }

  private void clearPendingDataRows() {
    this.pendingDataRows.forEach(r -> r.setOwner(null));
    this.pendingDataRows.clear();
  }
  

  @Override
  public void checkSanity() throws InsaneException {

    DataColumn column;
    DataRow row;
    String columnName;

    for (int k = 0; k < dataColumns.size(); k++) {
      column = this.dataColumns.get(k);
      InsaneException.assertTrue(column.getOwner() == this);
      InsaneException.assertTrue(column.getIndex() == k);
      columnName = column.getColumnName();
      if (columnName != null) {
        columnName = convertColumnName(columnName);
        InsaneException.assertTrue(this.dataColumnsMap.containsKey(columnName));
        InsaneException.assertTrue(
            this.dataColumnsMap.get(columnName) == column
            );
      }

      column.checkSanity();
    }
    for (int k = 0; k < dataRows.size(); k++) {
      row = this.dataRows.get(k);
      InsaneException.assertTrue(row.getOwner() == this);
      if (this.primaryKey != null) {
        InsaneException.assertTrue(
            getRowByPrimaryKey(row.getPrimaryKeyValuesArray()) == row
            );
      }
      row.checkSanity();
    }
    for (int k = 0; k < pendingDataRows.size(); k++) {
      row = this.pendingDataRows.get(k);
      InsaneException.assertTrue(row.getOwner() == this);
      InsaneException.assertTrue(row.getRowState() == DataRowState.PENDING);
      row.checkSanity();
    }

  }
 

  /* CONVENIENCE METHODS */

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
  void notifyPrimaryKeyChanged() 
          throws InvalidChildModificationException {
   
    try {
      assertPrimaryKeyIsUniqueOverRelation(this.primaryKey);
    } catch (DataConstraintException e) {
      throw new InvalidChildModificationException(e.getMessage());
    }

    recalculateRowMap();
  }
  void notifyRowRequiresRehashing(
      DataRow row, 
      Object original, 
      DataColumn change
      ) {

    int oldHash, newHash;
    
    if (this.primaryKey == null)
      throw new IllegalStateException("primary key is null");

    newHash = this.primaryKey.getHashOfRow(row);
    oldHash = newHash ^ original.hashCode() ^ row.get(change).hashCode();

    if (!this.dataRowsMap.get(oldHash).remove(row))
      throw new IllegalStateException("failed to remove row form map");
    
    addSingleRowToDataRowsMap(row);
  }


  /* HELPERS */

  private void addRowFromPending(final DataRow row) {
    if (row.getOwner() != this)
      throw new IllegalArgumentException(
          "pendng row addition is not owned by this data relation"
          );
    
    this.dataColumns.forEach(column -> {
      column.assertAccepts(row.get(column));
    });

    if (this.primaryKey != null)
      addSingleRowToDataRowsMap(row);
    if (!this.pendingDataRows.remove(row)) {
      attemptRemoveRowFromDataRowsMap(row);
      throw new IllegalStateException(
          "pending row could not be removed from list of pending rows"
          );
    }
    this.dataRows.add(row);
    row.setRowState(DataRowState.ADDED);
  }

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

  private void assertPrimaryKeyIsUniqueOverRelation(final PrimaryKey key) {
    HashMap<Integer,List<DataRow>> map = new HashMap<>();

    /* addSingleRowToListMap should throw exception if primary key 
     * is not unique over this relation
     */
    this.dataRows.forEach(row -> addSingleRowToListMap(key, map, row));
  }

  private void recalculateRowMap() {
    this.dataRowsMap.clear();
    this.dataRows.forEach(this::addSingleRowToDataRowsMap);
  }

  private void addSingleRowToListMap(
      final PrimaryKey key, 
      Map<Integer,List<DataRow>> map,
      final DataRow row
      ) {

    int hash;
    List<DataRow> rowList;

    hash = key.getHashOfRow(row);
    rowList = map.getOrDefault(hash, null);
    if (rowList == null) {
      rowList = new LinkedList<>();
      map.put(hash, rowList);
    }
    else if (rowList.stream().anyMatch(r -> key.rowsAreEqual(r, row))) {
        throw new DataConstraintException(
            "primary key is not unique over this relation"
            );
    }


    rowList.add(row);
  }
  
  private void addSingleRowToDataRowsMap(DataRow row) {

    if (this.primaryKey == null)
      throw new RuntimeException("primary key is null");

    addSingleRowToListMap(this.primaryKey, this.dataRowsMap, row);
  }
  
  private void attemptRemoveRowFromDataRowsMap(DataRow row) {
    
    int hash;
    List<DataRow> rowList;

    if (this.primaryKey != null) {
      hash = this.primaryKey.getHashOfRow(row);
      rowList = this.dataRowsMap.get(hash);
      if (rowList != null) {
        rowList.remove(row);
        if (rowList.isEmpty())
          this.dataRowsMap.remove(hash);
      }
    }
  }

	
}
