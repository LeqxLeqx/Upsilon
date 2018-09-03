/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
 *                                                                         *
 *  Upsilon: A general utilities library for java                          *
 *  Copyright (C) 2018  LeqxLeqx                                           *
 *                                                                         *
 *  This program is free software: you can redistribute it and/or modify   *
 *  it under the terms of the GNU Lesser General Public License as         *
 *  published by the Free Software Foundation, either version 3 of the     *
 *  License, or (at your option) any later version.                        *
 *                                                                         *
 *  This program is distributed in the hope that it will be useful,        *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of         *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          *
 *  GNU Lesser General Public License for more details.                    *
 *                                                                         *
 *  You should have received a copy of the GNU Lesser General Public       *
 *  License along with this program.                                       *
 *  If not, see <http://www.gnu.org/licenses/>.                            *
 *                                                                         *
\* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package upsilon.types;

public abstract class Tuple {

	public abstract int size();
	protected abstract Object getImp(int index);

	public Object get(int index) {
		if (index < 0 || index >= size())
		  throw new IllegalArgumentException(String.format(
							"index out of range for tuple: %d (size: %d)",
							index,
							size()
							));

		return getImp(index);
	}
  
  @Override
  public int hashCode() {

    int ret = 0;
    Object object;

    for (int k = 0; k < size(); k++) {
      
      object = get(k);
      if (object != null)
        ret ^= object.hashCode();

    }

    return ret;
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof Tuple)
      return equals((Tuple) other);
    else
      return false;
  }

  private boolean equals(Tuple other) {

    Object thisObject, otherObject;

    if (size() != other.size())
      return false;

    for (int k = 0; k < size(); k++) {
      thisObject = get(k);
      otherObject = get(k);

      if (thisObject == null && otherObject == null)
        continue;
      if (thisObject == null || otherObject == null)
        return false;
      if (!thisObject.equals(otherObject))
        return false;
    }

    return true;
  }
	
	
}
