/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.fuseki.main.access;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.auth.Auth;
import org.apache.jena.fuseki.auth.AuthPolicy;
import org.apache.jena.fuseki.build.FusekiConfig;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.RDFDataMgr;

public class TestAuthorized {

    private static Graph allowedUsersGraph = RDFDataMgr.loadGraph("testing/Access/allowedUsers.ttl");

    @Test public void auth_anon() {
        AuthPolicy auth = Auth.ANY_ANON;
        assertTrue(auth.isAllowed(null));
        assertTrue(auth.isAllowed("user1"));
    }

    @Test public void auth_anyLoggedIn_1() {
        AuthPolicy auth = Auth.ANY_USER;
        assertFalse(auth.isAllowed(null));
        assertTrue(auth.isAllowed("user1"));
    }

    @Test public void auth_anyLoggedIn_2() {
        AuthPolicy auth = Auth.policyAllowSpecific("*");
        assertFalse(auth.isAllowed(null));
        assertTrue(auth.isAllowed("user1"));
    }

    @Test public void auth_anyLoggedIn_3() {
        AuthPolicy auth = Auth.policyAllowSpecific("!");
        assertFalse(auth.isAllowed(null));
        assertFalse(auth.isAllowed("user1"));
    }

    @Test public void auth_anyLoggedIn_4() {
        String level = LogCtl.getLevel(Fuseki.configLog);
        try {
            LogCtl.set(Fuseki.configLog, "Error");
            // "!" with anything else causes a warning.
            AuthPolicy auth = Auth.policyAllowSpecific("!", "user2");
            assertFalse(auth.isAllowed(null));
            assertFalse(auth.isAllowed("user1"));
            assertFalse(auth.isAllowed("user2"));
        } finally {
            LogCtl.set(Fuseki.configLog, level);
        }
    }

    @Test public void auth_noOne() {
        AuthPolicy auth = Auth.DENY_ALL;
        assertFalse(auth.isAllowed(null));
        assertFalse(auth.isAllowed("user1"));
    }

    @Test public void auth_user_1() {
        AuthPolicy auth = Auth.policyAllowSpecific("user1", "user2");
        assertFalse(auth.isAllowed(null));
        assertTrue(auth.isAllowed("user1"));
        assertTrue(auth.isAllowed("user2"));
        assertFalse(auth.isAllowed("user3"));
    }

    private static Node node(String uri) {
        return NodeFactory.createURI(uri);
    }

    @Test public void auth_parse_no_info_1() {
        Node r = node("<http://example/notInData>");
        AuthPolicy auth = FusekiConfig.allowedUsers(allowedUsersGraph, r);
        assertNull(auth);
    }

    @Test public void auth_parse_no_info_2() {
        Node r = node("http://example/none");
        AuthPolicy auth = FusekiConfig.allowedUsers(allowedUsersGraph, r);
        assertNull(auth);
    }

    @Test public void auth_parse_1() {
        Node r = node("http://example/r1");
        AuthPolicy auth = FusekiConfig.allowedUsers(allowedUsersGraph, r);
        assertNotNull(auth);
        assertFalse(auth.isAllowed(null));
        assertTrue(auth.isAllowed("user1"));
        assertTrue(auth.isAllowed("user2"));
        assertFalse(auth.isAllowed("user3"));
    }

    @Test public void auth_parse_2() {
        Node r = node("http://example/r2");
        AuthPolicy auth = FusekiConfig.allowedUsers(allowedUsersGraph, r);
        assertNotNull(auth);
        assertFalse(auth.isAllowed(null));
        assertTrue(auth.isAllowed("user1"));
        assertTrue(auth.isAllowed("user2"));
        assertFalse(auth.isAllowed("user3"));
    }

    @Test public void auth_parse_loggedIn() {
        Node r = node("http://example/rLoggedIn");
        AuthPolicy auth = FusekiConfig.allowedUsers(allowedUsersGraph, r);
        assertNotNull(auth);
        assertFalse(auth.isAllowed(null));
        assertTrue(auth.isAllowed("user1"));
        assertTrue(auth.isAllowed("user3"));
    }
}
