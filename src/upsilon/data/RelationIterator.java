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

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

class RelationIterator<RowType extends Row, ColumnType extends Column> 
				implements Iterator<RowType>{

	private final Relation<RowType,ColumnType> relation;	
	private List<RowType> rows;
	private Iterator<RowType> iterator;

	RelationIterator(Relation<RowType,ColumnType> relation) {
		this.relation = relation;
		this.rows = new LinkedList<>();
		this.iterator = null;
	}

	private void init() {
		if (this.iterator == null) {
			this.rows = Arrays.asList(relation.getRows());
			this.iterator = rows.iterator();
		}
	}

	/* TODO : IMPLEMENT WITH forEachContext ENGAGING PROPERLY */

	@Override
	public boolean hasNext() {
		init();
		return this.iterator.hasNext();
	}

	@Override
	public RowType next() {
		init();
		return this.iterator.next();
	}


	
}
