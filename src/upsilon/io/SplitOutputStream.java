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
package upsilon.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import upsilon.tools.ArrayTools;

public class SplitOutputStream extends OutputStream {

  public static OutputStream create(OutputStream... components) {

    SplitOutputStream ret;

    if (components == null)
      throw new IllegalArgumentException("array cannot be null");
    if (ArrayTools.containsNull(components))
      throw new IllegalArgumentException("array cannot contain nulls");

    ret = new SplitOutputStream();
    Collections.addAll(ret.components, components);

    return ret;
  }
  
  private List<OutputStream> components;
  
  private SplitOutputStream() {
    super();
    
    this.components = new LinkedList<>();
  }

  @Override
  public void write(int i) throws IOException {
    for (OutputStream stream : components) {
      stream.write(i);
    }
  }
  
  @Override
  public void close() throws IOException {
    super.close();

    for (OutputStream stream : components) {
      stream.close();
    }
  }

  
}
