/*
 * Copyright 2010-2012 Luca Garulli (l.garulli--at--orientechnologies.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orientechnologies.orient.core.sql.functions.misc;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.orientechnologies.orient.core.command.OCommandExecutor;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.sql.functions.OSQLFunctionAbstract;

/**
 * Returns the current date time.
 * 
 * @author Luca Garulli (l.garulli--at--orientechnologies.com)
 * @see OSQLFunctionDate
 * 
 */
public class OSQLFunctionSysdate extends OSQLFunctionAbstract {
  public static final String NAME = "sysdate";

  private final Date         now;

  /**
   * Get the date at construction to have the same date for all the iteration.
   */
  public OSQLFunctionSysdate() {
    super(NAME, 0, 1);
    now = new Date();
  }

  public Object execute(final OIdentifiable iCurrentRecord, final Object[] iParameters, OCommandExecutor iRequester) {
    return now;
  }

  public boolean aggregateResults(final Object[] configuredParameters) {
    return false;
  }

  public String getSyntax() {
    return "Syntax error: sysdate()";
  }

  @Override
  public Object getResult() {
    return null;
  }
}
