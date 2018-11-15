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
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import upsilon.tools.ArrayTools;
import upsilon.tools.StringTools;
import upsilon.types.Ptr;

public class DemandReader implements AutoCloseable {

  private InputStreamReader reader;
  private LinkedList<Character> hold;

  public DemandReader(InputStreamReader reader) {
    
    if (reader == null)
      throw new IllegalArgumentException("reader cannot be null");

    this.reader = reader;
    this.hold = new LinkedList<>();
  }

  @Override
  public void close() throws IOException {
    if (this.reader != null) {
      this.reader.close();
      this.reader = null;
    }
  }

  
  public void skip(long count) throws IOException {
    assertNotClosed();

    reader.skip(count);
  }

  public void putBack(char c) {
    this.hold.add(0, c);
  }
  public void putBack(String string) {
    for (int k = string.length() - 1; k >= 0; k--) {
      this.hold.add(0, string.charAt(k));
    }
  }

  public char getNext() throws IOException {
    
    final Ptr<Character> ptr;

    ptr = new Ptr<>();
    if (!tryGetNext(ptr))
      throw new IllegalStateException("no next value");

    return ptr.value;
  }

  public boolean hasMore() throws IOException {

    int read;

    if (!this.hold.isEmpty())
      return true;

    read = reader.read();
    if (read == -1)
      return false;
    else {
      putBack((char) read);
      return true;
    }
    
  }
  
  public boolean tryGetNext(Ptr<Character> ptr) throws IOException {

    int read;

    assertNotClosed();

    if (!this.hold.isEmpty()) {
      ptr.value = hold.remove(0);
      return true;
    }
    
    read = reader.read();
    if (read == -1)
      return false;
    else {
      ptr.value = (char) read;
      return true;
    }
  }

  public String readUntil(char c) throws IOException {
    return readUntil(r -> r == c);
  }

  public String readUntil(
      String[] strings,
      BiPredicate<String,String> equalityPredicate
      ) throws IOException {
    
    String matched;
    StringBuilder sb;
    LinkedList<Character> buffer;
    int maxStringLength;
    
    if (ArrayTools.isNullOrContainsNull(strings) || strings.length == 0)
      throw new IllegalArgumentException("string(s) cannot be null or empty");
    if (equalityPredicate == null)
      throw new IllegalArgumentException("equalityPredicate cannot be null");

    assertNotClosed();

    buffer = new LinkedList<>();
    sb = new StringBuilder();
    maxStringLength = Arrays
        .stream(strings)
        .max((a, b) -> a.length() - b.length())
        .get()
        .length();

    do
    {
      buffer.add(getNext());
      if (buffer.size() > maxStringLength)
        sb.append(buffer.remove());
    }
    while (
        !Arrays.stream(strings).anyMatch(string -> {
          return equalityPredicate.test(
            StringTools
              .newString(buffer.stream().toArray(Character[]::new))
              .substring(maxStringLength - string.length()),
            string
            );
        }));

    matched = Arrays.stream(strings).filter(string -> {
        return equalityPredicate.test(
          StringTools
            .newString(buffer.stream().toArray(Character[]::new))
            .substring(maxStringLength - string.length()),
          string
          );
        }).findFirst().get();

    for (int k = matched.length(); k < maxStringLength; k++)
      sb.append(buffer.removeFirst());
    while (!buffer.isEmpty())
      putBack(buffer.removeLast());

    return sb.toString();
  }
  
  public String readUntil(String string) throws IOException {
    return readUntil(new String[] { string }, String::equals);
  }

  public String readUntilIgnoreCase(String string) throws IOException {
    return readUntil(new String[] { string }, String::equalsIgnoreCase);
  }

  public String readUntilAny(String... strings) throws IOException {
    return readUntil(strings, String::equals);
  }

  public String readUntilAnyIgnoreCase(String... strings) throws IOException {
    return readUntil(strings, String::equalsIgnoreCase);
  }



  public String readUntil(Predicate<Character> predicate) throws IOException {

    StringBuilder sb;
    char read;

    assertNotClosed();
    sb = new StringBuilder();

    while (!predicate.test(read = getNext()))
      sb.append(read);
    putBack(read);

    return sb.toString();
  }

  public boolean accept(char value) throws IOException {
    char actual;

    actual = getNext();

    if (actual == value)
      return true;
    else {
      putBack(actual);
      return false;
    }

  }

  public boolean accept(String value) throws IOException {

    if (StringTools.isNullOrEmpty(value))
      throw new IllegalArgumentException("value cannot be null or empty");

    for (int k = 0; k < value.length(); k++) {
      if (!accept(value.charAt(k))) {
        for (int i = k - 1; i >= 0; i--)
          putBack(value.charAt(i));
        return false;
      }
    }

    return true;
  }

  private void assertNotClosed() {
    if (this.reader == null)
      throw new IllegalStateException("DemandReader object is closed");
  }
  
}
