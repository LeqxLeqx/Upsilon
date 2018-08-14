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

package upsilon.logging;

import java.nio.file.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.function.Function;

import upsilon.tools.StringTools;

public class Log {

	private static final char[] FORMAT_TERMINATORS = new char[] {
		'C', 'c', 'L', 'l', 'R', 'r', 'T', 't', 'H', 'h', 'Y',
    'y', 'M', 'm', 'D', 'd', 'P', 'p', 'F', 'f', 'S', 's',
		'J', 'j', 'A', 'a', 'U', 'u', 'N', 'n', 'B', 'b', 'Q',
		'q', 'W', 'w', 'E', 'e',
	};



  private Path path;
  private String format, defaultLevel, lineFeed;
	private Charset charset;
  private boolean throwsExceptions, useLocalTime;

	/**
	 * Creates a new log object at the given path
	 * 
	 * @param path location of log file
	 */
	public Log(String path) {
		this(path, false);
	}
	/**
	 * Creates a new log object at the given path
	 * 
	 * @param path location of log file
	 * @param throwsExceptions if true, this object will throw exceptions when
	 *												 errors occur
	 */
  public Log(String path, boolean throwsExceptions) {
    
    if (StringTools.isNullOrEmpty(path))
      throw new IllegalArgumentException("path string cannot be null or empty");

    this.path = Paths.get(path);
    this.format = "[%5l] %r %t : %c";
    this.defaultLevel = "TRACE";
		this.lineFeed = "\n";
    this.charset = StandardCharsets.US_ASCII;
    this.throwsExceptions = throwsExceptions;
		this.useLocalTime = false;
  }
  
  private void throwIOException(String text, Object... args) {
    if (this.throwsExceptions)
			throw new LogException(String.format(text, args));
  }

  private void throwArgException(String text, Object... args) {
    if (this.throwsExceptions)
      throw new IllegalArgumentException(String.format(text, args));
  }

  private void addImp(String level, String line) {
	  String string;
	
		if (level == null)
      level = this.defaultLevel;

    try {
	    string = StringTools.format(
		      this.format,
		      FORMAT_TERMINATORS,
		      new Formatter(level, line)
		    );

	    Files.write(
		      this.path,
		      (string + lineFeed).getBytes(this.charset),
		      StandardOpenOption.APPEND,
					StandardOpenOption.CREATE
		    );
    }
    catch (Exception e) {
			throwIOException(e.getMessage());
    }
  }


	/**
	 * sets the 'throwsException' property of this object. If true, this object
	 * will throw exceptions when errors occur
	 * 
	 * @param throwExceptions the new value to be assigned
	 */
  public void setThrowsExceptions(boolean throwExceptions) {
    this.throwsExceptions = throwExceptions;
  }
	/**
	 * set the 'format' property of this object. This property dictates how each
	 * line of the log file is formatted. For details on this string, see the 
	 * detailed documentation
	 * 
	 * @param format the format string
	 */
  public void setFormat(String format) {
    if (StringTools.isNullOrEmpty(format)) {
	    throwArgException("format cannot be empty or null");
      return;
    }
		this.format = format;
  }
	/**
	 * sets the default logging level
	 * 
	 * @param level the new value
	 */
  public void setDefaultLevel(String level) {
    if (StringTools.isNullOrEmpty(level)) {
	    throwArgException("default level cannot be empty or null");
      return;
    }
    this.defaultLevel = level;
  }
	/**
	 * sets the characters string to be used as a line-feed when delimiting the
	 * lines of the log file
	 * 
	 * @param lineFeed the character string to use in place of a line-feed
	 */
	public void setLineFeed(String lineFeed) {
    if (StringTools.isNullOrEmpty(lineFeed)) {
	    throwArgException("default level cannot be empty or null");
      return;
    }
		this.lineFeed = lineFeed;
	}
	/**
	 * sets the character set to be used in the log file
	 * 
	 * @param charset the character-set to use
	 */
  public void setCharset(Charset charset) {
    if (charset == null) {
			throwArgException("charset cannot be null");
      return;
    }
    this.charset = charset;
  }
	/**
	 * sets whether or not to use local-time for log entries. If this value is
	 * false, then GMT is used
	 * 
	 * @param useLocalTime the value to be set
	 */
	public void setUseLocalTime(boolean useLocalTime) {
		this.useLocalTime = useLocalTime;
	}


	/**
	 * gets the path to which this log is being written
	 * 
	 * @return the path to which this log is being written
	 */
  public Path getPath() {
    return this.path;
  }
	/**
	 * gets the value of the 'throwsException' property of this object
	 * 
	 * @return whether or not this object will throw exceptions
	 */
  public boolean getThrowsExceptions() {
    return this.throwsExceptions;
  }
	/**
	 * gets the currently set format string of this object
	 * 
	 * @return the current format string
	 */
  public String getFormat() {
    return this.format;
  }
	/**
	 * gets the default logging-level string for this object
	 * 
	 * @return the current default level
	 */
  public String getDefaultLevel() {
    return this.defaultLevel;
  }
	/**
	 * gets the current line-feed set for this object
	 * 
	 * @return the current line-feed for this 
	 */
	public String getLineFeed() {
		return this.lineFeed;
	}
	/**
	 * gets the current character set for this object
	 * 
	 * @return the current character set
	 */
  public Charset getCharset() {
    return this.charset;
  }
	/**
	 * gets the 'useLocalTime' attribute for this object
	 * 
	 * @return the 'useLocalTime' attribute
	 */
	public boolean getUseLocalTime() {
		return this.useLocalTime;
	}
  

	/**
	 * adds the given string to this log
	 * 
	 * @param line the format of the line content
	 * @param params parameters to the line content format
	 */
  public void add(String line, Object... params) {

    String formatted;

    if (StringTools.isNullOrEmpty(line)) {
      throwArgException("line cannot be null or empty");
      return;
    }

    try
    {
	    formatted = String.format(line, params);
    }
    catch (Exception e)
    {
			if (this.throwsExceptions)
        throw new RuntimeException(e);
      else
        return;
    }

		add(formatted);
  }

	/**
	 * adds the given string to this log
	 * 
	 * @param line the line to be added to the string
	 */
  public void add(String line) {
    if (StringTools.isNullOrEmpty(line)) {
      throwArgException("line cannot be null or empty");
      return;
    }

		addImp(null, line);
  }


  private class Formatter implements Function<String, String> {

    String level, line;

    Formatter(String level, String line) {
			this.level = level;
      this.line = line;
    }

		void raiseException(String arg) {
      throw new IllegalArgumentException(
				String.format("bad format argument: %%%s", arg)
				);
		}

		String getDateTimeIntString(int arg, int value) {
			if (arg == 0)
				return String.format("%d", value);
			else
				return StringTools.leftChop(
								StringTools.leftPad(
									String.format("%d", value),
									arg
								),
								arg
							);
		}

    @Override
    @SuppressWarnings("fallthrough")
    public String apply(String s) {
      
			char c = s.charAt(s.length() - 1);
			int integer;
      String pre = s.substring(0, s.length() - 1);
			LocalDateTime time = LocalDateTime.now();

			if (pre.isEmpty())
				integer = 0;
			else
			{
				try
				{
					integer = Integer.parseInt(pre);
				}
				catch (NumberFormatException e) {
					raiseException(s);
					return null;
				}
			}

      switch (c)
      {

				case 'c':
					return StringTools.leftPad(line, integer);
				case 'l':
					return StringTools.leftPad(level, integer);
				case 'r':
					if (integer != 0)
						raiseException(s);
					return String.format(
						"%04d-%02d-%02d",
						time.getYear(),
						time.getMonthValue(),
						time.getSecond()
						);
				case 't':
					if (integer != 0)
					  raiseException(s);
					return String.format(
						"%02d-%02d-%02d",
						time.getHour(),
						time.getMinute(),
						time.getSecond()
					  );
				case 'p':
					if (integer != 0)
						raiseException(s);
					return String.format(
						"%02d-%02d-%02d",
						time.getYear() % 100,
						time.getMonthValue(),
						time.getSecond()
						);
				case 'Y':
				case 'y':
					return getDateTimeIntString(integer, time.getYear());
				case 'M':
				case 'm':
					return getDateTimeIntString(integer, time.getMonthValue());
				case 'D':
				case 'd':
					return getDateTimeIntString(integer, time.getDayOfMonth());
				case 'H':
				case 'h':
					return getDateTimeIntString(integer, time.getHour());
				case 'F':
				case 'f':
					return getDateTimeIntString(integer, time.getMinute());
				case 'S':
				case 's':
					return getDateTimeIntString(integer, time.getSecond());
				case 'J':
				case 'j':
					return getDateTimeIntString(
									integer,
									time.getHour() % 12 == 0 ? 12 : time.getHour() % 12
								);
				case 'A':
					if (integer != 0)
						raiseException(s);
					else
						return time.getHour() > 11 ? "AM" : "PM";
				case 'a':
					if (integer != 0)
						raiseException(s);
					else
						return time.getHour() > 11 ? "am" : "pm";
				case 'U':
				case 'u':
					return String.format("%d", time.toEpochSecond(ZoneOffset.UTC));
				case 'N':
					return time
									.getMonth()
									.getDisplayName(TextStyle.FULL, Locale.getDefault())
									.toUpperCase();
				case 'n':
					return time
									.getMonth()
									.getDisplayName(TextStyle.SHORT, Locale.getDefault());
				case 'B':
					return time
									.getMonth()
									.getDisplayName(TextStyle.SHORT, Locale.getDefault())
									.toUpperCase();
				case 'b':
					return time
									.getMonth()
									.getDisplayName(TextStyle.SHORT, Locale.getDefault());
				case 'Q':
					return time
									.getDayOfWeek()
									.getDisplayName(TextStyle.FULL, Locale.getDefault())
									.toUpperCase();
				case 'q':
					return time
									.getDayOfWeek()
									.getDisplayName(TextStyle.FULL, Locale.getDefault());
				case 'W':
					return time
									.getDayOfWeek()
									.getDisplayName(TextStyle.SHORT, Locale.getDefault())
									.toUpperCase();
				case 'w':
					return time
									.getDayOfWeek()
									.getDisplayName(TextStyle.SHORT, Locale.getDefault());
				case 'E':
				case 'e':
					return String.format("%d", time.getDayOfWeek().ordinal());

        default:
					raiseException(s);
					return null;
      }
    }
  }

	
}












