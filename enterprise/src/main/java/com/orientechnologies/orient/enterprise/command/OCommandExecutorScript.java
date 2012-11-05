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
package com.orientechnologies.orient.enterprise.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.orientechnologies.orient.core.command.OCommandExecutorAbstract;
import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.command.script.OCommandScript;
import com.orientechnologies.orient.core.command.script.OCommandScriptException;
import com.orientechnologies.orient.core.db.record.ODatabaseRecordTx;

/**
 * Executes Script Commands.
 * 
 * @see OCommandScript
 * @author Luca Garulli
 * 
 */
public class OCommandExecutorScript extends OCommandExecutorAbstract {
  protected static final String              DEF_LANGUAGE    = "javascript";
  protected static ScriptEngineManager       scriptEngineManager;
  protected static Map<String, ScriptEngine> engines;
  protected static String                    defaultLanguage = DEF_LANGUAGE;

  protected OCommandScript                   request;

  static {
    if (engines == null) {
      engines = new HashMap<String, ScriptEngine>();
      scriptEngineManager = new ScriptEngineManager();
      List<ScriptEngineFactory> factories = scriptEngineManager.getEngineFactories();
      for (ScriptEngineFactory f : factories) {
        engines.put(f.getLanguageName().toLowerCase(), f.getScriptEngine());

        if (defaultLanguage == null)
          defaultLanguage = f.getLanguageName();
      }

      if (!engines.containsKey(DEF_LANGUAGE)) {
        engines.put(DEF_LANGUAGE, scriptEngineManager.getEngineByName(DEF_LANGUAGE));
        defaultLanguage = DEF_LANGUAGE;
      }
    }
  }

  public OCommandExecutorScript() {
  }

  @SuppressWarnings("unchecked")
  public OCommandExecutorScript parse(final OCommandRequest iRequest) {
    request = (OCommandScript) iRequest;
    return this;
  }

  public Object execute(final Map<Object, Object> iArgs) {

    final String language = request.getLanguage();
    final String script = request.getText();

    if (language == null)
      throw new OCommandScriptException("No language was specified");

    if (!engines.containsKey(language.toLowerCase()))
      throw new OCommandScriptException("Unsupported language: " + language + ". Supported languages are: " + engines);

    if (script == null)
      throw new OCommandScriptException("Invalid script: null");

    final ScriptEngine scriptEngine = engines.get(language.toLowerCase());

    if (scriptEngine == null)
      throw new OCommandScriptException("Cannot find script engine: " + language);

    final Bindings binding = scriptEngine.createBindings();

    // BIND FIXED VARIABLES
    binding.put("db", new OScriptDocumentDatabaseWrapper((ODatabaseRecordTx) getDatabase()));
    binding.put("gdb", new OScriptGraphDatabaseWrapper((ODatabaseRecordTx) getDatabase()));

    // BIND PARAMETERS INTO THE SCRIPT
    if (iArgs != null)
      for (int i = 0; i < iArgs.size(); ++i) {
        binding.put("$" + i, iArgs.get(i));
      }

    try {
      Object result = null;
      result = scriptEngine.eval(script, binding);

      return result;
    } catch (ScriptException e) {
      throw new OCommandScriptException("Error on execution of the script", request.getText(), 0, e);
    }
  }

  public boolean isIdempotent() {
    return false;
  }

  @Override
  protected void throwSyntaxErrorException(String iText) {
    throw new OCommandScriptException("Error on execution of the script: " + iText, request.getText(), 0);
  }
}
