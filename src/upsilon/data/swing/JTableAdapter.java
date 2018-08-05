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
package upsilon.data.swing;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import upsilon.data.Relation;

public class JTableAdapter implements TableModel {

  public static JTable bind(Relation relation, boolean userEditable) {
    JTableAdapter adapter;
    JTable table;

    if (relation == null)
      throw new IllegalArgumentException("relation cannot be null");
    
    adapter = new JTableAdapter(relation, userEditable);
    table = new JTable(adapter);

    return table;
  }
  public static JTable bind(Relation relation) {
    return bind(relation, true);
  }

  private List<TableModelListener> tableModelListeners;

  private Relation relation;
  private boolean userEditable;

  private JTableAdapter(Relation relation, boolean userEditable) {
    this.tableModelListeners = new ArrayList<>();
    this.relation = relation;
    this.userEditable = userEditable;
  }


  @Override
  public int getRowCount() {
    return relation.getRowCount();
  }

  @Override
  public int getColumnCount() {
    return relation.getColumnCount();
  }

  @Override
  public String getColumnName(int columnIndex) {
    return relation.getColumn(columnIndex).getColumnName();
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return relation.getColumn(columnIndex).getColumnDataType().storingClass;
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return !relation.getColumn(columnIndex).isReadOnly();
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    return relation.getRows()[rowIndex].get(columnIndex);
  }

  @Override
  public void setValueAt(Object value, int rowIndex, int columnIndex) {
    relation.getRows()[rowIndex].set(columnIndex, value);
  }

  @Override
  public void addTableModelListener(TableModelListener tl) {
    this.tableModelListeners.add(tl);
  }

  @Override
  public void removeTableModelListener(TableModelListener tl) {
    this.tableModelListeners.remove(tl);
  }
  
}
