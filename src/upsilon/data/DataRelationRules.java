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

public final class DataRelationRules {
  
  private DataRelation
    owner;
  private boolean 
    ignoreCase,
    actionsModifyRowState,
		autoNullToDefault;

  public DataRelationRules() {
    this.owner = null;
    this.ignoreCase = true;
    this.actionsModifyRowState = true;
		this.autoNullToDefault = false;
  }

  public boolean getIgnoreCase() {
    return ignoreCase;
  }
  public boolean getActionsModifyRowState() {
    return actionsModifyRowState;
  }
	public boolean getAutoNullToDefault() {
		return autoNullToDefault;
	}


  DataRelationRules setOwner(DataRelation owner) {
    this.owner = owner;
    return this;
  }
  public DataRelationRules setIgnoreCase(boolean value) {
    boolean original = this.ignoreCase;

    this.ignoreCase = value;
    if (this.owner != null && original != value) {
      try {
        this.owner.notifyRulesIgnoreCaseChanged();
      } catch (InvalidChildModificationException e) {
        this.ignoreCase = original;
        throw new IllegalStateException(e.getMessage());
      }
    }
    return this;
  }
  public DataRelationRules setActionsModifyRowState(boolean value) {
    this.actionsModifyRowState = value;
    return this;
  }
	public DataRelationRules setAutoNullToDefault(boolean value) {
		this.autoNullToDefault = value;
		return this;
	}


  void copyFrom(DataRelationRules rules) {
    
    setIgnoreCase(rules.ignoreCase);
    setActionsModifyRowState(rules.actionsModifyRowState);

  }
  
}
