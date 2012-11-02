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
package com.orientechnologies.orient.server.network.protocol.http.command;

import java.io.IOException;
import java.util.List;

import com.orientechnologies.common.concur.lock.OLockException;
import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.exception.ODatabaseException;
import com.orientechnologies.orient.core.exception.OSecurityAccessException;
import com.orientechnologies.orient.core.serialization.serializer.OStringSerializerHelper;
import com.orientechnologies.orient.server.db.OSharedDocumentDatabase;
import com.orientechnologies.orient.server.network.protocol.http.OHttpRequest;
import com.orientechnologies.orient.server.network.protocol.http.OHttpRequestException;
import com.orientechnologies.orient.server.network.protocol.http.OHttpSession;
import com.orientechnologies.orient.server.network.protocol.http.OHttpSessionManager;
import com.orientechnologies.orient.server.network.protocol.http.OHttpUtils;

/**
 * Database based authenticated command. Authenticats against the database taken as second parameter of the URL. The URL must be in
 * this format:
 * 
 * <pre>
 * <command>/<database>[/...]
 * </pre>
 * 
 * @author Luca Garulli
 * 
 */
public abstract class OServerCommandAuthenticatedDbAbstract extends OServerCommandAbstract {

  public static final char   DBNAME_DIR_SEPARATOR   = '$';
  public static final String SESSIONID_UNAUTHORIZED = "-";
  public static final String SESSIONID_LOGOUT       = "!";

  @Override
  public boolean beforeExecute(final OHttpRequest iRequest) throws IOException {
    final String[] urlParts = iRequest.url.substring(1).split("/");
    if (urlParts.length < 2)
      throw new OHttpRequestException("Syntax error in URL. Expected is: <command>/<database>[/...]");

    iRequest.databaseName = urlParts[1].replace(DBNAME_DIR_SEPARATOR, '/');
    final List<String> authenticationParts = iRequest.authorization != null ? OStringSerializerHelper.split(iRequest.authorization,
        ':') : null;

    if (iRequest.sessionId == null || iRequest.sessionId.length() == 1) {
      // NO SESSION
      if (iRequest.authorization == null || SESSIONID_LOGOUT.equals(iRequest.sessionId)) {
        sendAuthorizationRequest(iRequest, iRequest.databaseName);
        return false;
      } else
        return authenticate(iRequest, authenticationParts, iRequest.databaseName);

    } else {
      // CHECK THE SESSION VALIDITY
      final OHttpSession currentSession = OHttpSessionManager.getInstance().getSession(iRequest.sessionId);
      if (currentSession == null) {
        // SESSION EXPIRED
        sendAuthorizationRequest(iRequest, iRequest.databaseName);
        return false;

      } else if (!currentSession.getDatabaseName().equals(iRequest.databaseName)) {

        // SECURITY PROBLEM: CROSS DATABASE REQUEST!
        OLogManager.instance().warn(this,
            "Session %s is trying to access to the database '%s', but has been authenticated against the database '%s'",
            iRequest.sessionId, iRequest.databaseName, currentSession.getDatabaseName());
        sendAuthorizationRequest(iRequest, iRequest.databaseName);
        return false;

      } else if (authenticationParts != null && !currentSession.getUserName().equals(authenticationParts.get(0))) {

        // SECURITY PROBLEM: CROSS DATABASE REQUEST!
        OLogManager.instance().warn(this,
            "Session %s is trying to access to the database '%s' with user '%s', but has been authenticated with user '%s'",
            iRequest.sessionId, iRequest.databaseName, authenticationParts.get(0), currentSession.getUserName());
        sendAuthorizationRequest(iRequest, iRequest.databaseName);
        return false;
      }

      return true;
    }
  }

  protected boolean authenticate(final OHttpRequest iRequest, final List<String> iAuthenticationParts, final String iDatabaseName)
      throws IOException {
    ODatabaseDocumentTx db = null;
    try {
      db = OSharedDocumentDatabase.acquire(iDatabaseName, iAuthenticationParts.get(0), iAuthenticationParts.get(1));

      // AUTHENTICATED: CREATE THE SESSION
      iRequest.sessionId = OHttpSessionManager.getInstance().createSession(iDatabaseName, iAuthenticationParts.get(0));
      return true;

    } catch (OSecurityAccessException e) {
      // WRONG USER/PASSWD
    } catch (OLockException e) {
      OLogManager.instance().error(this, "Cannot access to the database '" + iDatabaseName + "'", ODatabaseException.class, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      OLogManager.instance().error(this, "Cannot access to the database '" + iDatabaseName + "'", ODatabaseException.class, e);
    } finally {
      if (db != null)
        OSharedDocumentDatabase.release(db);
      else
        // WRONG USER/PASSWD
        sendAuthorizationRequest(iRequest, iDatabaseName);
    }
    return false;
  }

  protected void sendAuthorizationRequest(final OHttpRequest iRequest, final String iDatabaseName) throws IOException {
    // UNAUTHORIZED
    iRequest.sessionId = SESSIONID_UNAUTHORIZED;
    sendTextContent(iRequest, OHttpUtils.STATUS_AUTH_CODE, OHttpUtils.STATUS_AUTH_DESCRIPTION,
        "WWW-Authenticate: Basic realm=\"OrientDB db-" + iDatabaseName + "\"", OHttpUtils.CONTENT_TEXT_PLAIN, "401 Unauthorized.",
        false);
  }

  protected ODatabaseDocumentTx getProfiledDatabaseInstance(final OHttpRequest iRequest) throws InterruptedException {
    if (iRequest.authorization == null)
      throw new OSecurityAccessException(iRequest.databaseName, "No user and password received");

    final List<String> parts = OStringSerializerHelper.split(iRequest.authorization, ':');

    return OSharedDocumentDatabase.acquire(iRequest.databaseName, parts.get(0), parts.get(1));
  }
}