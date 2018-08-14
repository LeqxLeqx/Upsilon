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

public class Tuple4<T0, T1, T2, T3> extends Tuple {

	public T0 value0;
	public T1 value1;
	public T2 value2;
	public T3 value3;


	public Tuple4() {
		this.value0 = null;
		this.value1 = null;
		this.value2 = null;
		this.value3 = null;
	}

	public Tuple4(T0 value0, T1 value1, T2 value2, T3 value3) {
		this.value0 = value0;
		this.value1 = value1;
		this.value2 = value2;
		this.value3 = value3;
	}


	@Override
	public int size() {
		return 4;
	}

	@Override
	public Object getImp(int index) {
		switch (index) {
			case 0:
				return value0;
			case 1:
				return value1;
			case 2:
				return value2;
			case 3:
				return value3;
			default:
				throw new RuntimeException();
		}
	}

	
}
