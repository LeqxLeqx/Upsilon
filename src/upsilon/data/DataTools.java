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
import java.util.function.BiPredicate;

class DataTools { private DataTools() {}
  

  static <T> boolean containsDuplicateReference(List<T> list) {
    for (int k = 0; k < list.size() - 1; k++) {
      for (int i = 1; i < list.size(); i++) {
        if (list.get(k) == list.get(i))
          return true;
      }
    }
    return false;
  }

  static boolean containsDuplicateColumnNames(
      Relation<? extends Row,? extends Column> first,
      Relation<? extends Row,? extends Column> second,
      boolean ignoreCase
      ) {
  
    String c1Name, c2Name;
    BiPredicate<String, String> stringComparator;
    if (ignoreCase)
      stringComparator = String::equalsIgnoreCase;
    else
      stringComparator = String::equals;

    for (Column c1 : first.getColumns()) {
      c1Name = c1.getColumnName();
      if (c1Name == null)
        continue;
      for (Column c2 : second.getColumns()) {
        if (c2 == null)
          continue;
        c2Name = c2.getColumnName();

        if (stringComparator.test(c1Name, c2Name))
          return true;
      }
    }
    
    return false;
  }
	
}
