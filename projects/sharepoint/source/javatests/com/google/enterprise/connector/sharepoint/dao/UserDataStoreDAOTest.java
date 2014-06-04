// Copyright 2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.sharepoint.dao;

import static com.google.enterprise.connector.spi.SpiConstants.CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE;
import static com.google.enterprise.connector.spi.SpiConstants.PrincipalType.UNQUALIFIED;

import com.google.common.collect.ImmutableSet;
import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.spi.Principal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;

/**
 * @author nitendra_thakur
 */
public class UserDataStoreDAOTest extends TestCase {
  String namespace;
  UserDataStoreDAO userDataStoreDAO;
  Set<UserGroupMembership> memberships;

  protected void setUp() throws Exception {
    super.setUp();
    userDataStoreDAO = new UserDataStoreDAO(
        TestConfiguration.getUserDataSource(),
        TestConfiguration.getUserDataStoreQueryProvider(),
        TestConfiguration.getUserGroupMembershipRowMapper());
    namespace = TestConfiguration.sharepointUrl;
    memberships = TestConfiguration.getMembershipsForNameSpace(namespace);
    userDataStoreDAO.addMemberships(memberships);
  }

  /**
   * Retrieves all the membership information pertaining to a user.
   *
   * @param username the user's login name, NOT the ID
   * @return list of {@link UserGroupMembership} representing memberships
   *     of the user
   */
  private List<UserGroupMembership> getAllMembershipsForUser(String username)
      throws SharepointException {
    return userDataStoreDAO.getAllMembershipsForSearchUserAndLdapGroups(
        ImmutableSet.<String>of(), username);
  }

  public void testAddMemberships() {
    try {
      for (UserGroupMembership membership : memberships) {
        List<UserGroupMembership> userMemberships =
            getAllMembershipsForUser(membership.getUserName());
        assertNotNull(userMemberships);
        assertTrue(userMemberships.contains(membership));
      }
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testRemoveUserMemberships() {
    try {
      Set<Integer> userIds = new TreeSet<Integer>();
      for (UserGroupMembership membership : memberships) {
        userIds.add(membership.getUserId());
        userIds.add(membership.getUserId());
        userIds.add(membership.getUserId());
      }
      userDataStoreDAO.removeUserMembershipsFromNamespace(userIds, namespace);
      for (UserGroupMembership membership : memberships) {
        List<UserGroupMembership> userMemberships =
            getAllMembershipsForUser(membership.getUserName());
        assertNotNull(userMemberships);
        assertFalse(userMemberships.contains(membership));
      }
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testRemoveGroupMemberships() {
    try {
      Set<Integer> groupIds = new TreeSet<Integer>();
      for (UserGroupMembership membership : memberships) {
        groupIds.add(membership.getGroupId());
        groupIds.add(membership.getGroupId());
        groupIds.add(membership.getGroupId());
      }
      userDataStoreDAO.removeGroupMembershipsFromNamespace(groupIds, namespace);
      for (UserGroupMembership membership : memberships) {
        List<UserGroupMembership> userMemberships =
            getAllMembershipsForUser(membership.getUserName());
        assertNotNull(userMemberships);
        assertFalse(userMemberships.contains(membership));
      }
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testNameSpaceMemberships() {
    try {
      Set<String> namespaces = new TreeSet<String>();
      namespaces.add(namespace);
      userDataStoreDAO.removeAllMembershipsFromNamespace(namespaces);
      for (UserGroupMembership membership : memberships) {
        List<UserGroupMembership> userMemberships =
            getAllMembershipsForUser(membership.getUserName());
        assertNotNull(userMemberships);
        assertFalse(userMemberships.contains(membership));
      }
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testSyncGroupMembership() {
    try {
      Map<Integer, Set<UserGroupMembership>> membershipMap = new HashMap<Integer, Set<UserGroupMembership>>();
      for (UserGroupMembership membership : memberships) {
        if (membershipMap.containsKey(membership.getGroupId())) {
          membershipMap.get(membership.getGroupId()).add(membership);
        } else {
          Set<UserGroupMembership> memberships = new HashSet<UserGroupMembership>();
          memberships.add(membership);
          membershipMap.put(membership.getGroupId(), memberships);
        }
      }
      userDataStoreDAO.syncGroupMemberships(membershipMap, namespace);
      for (UserGroupMembership membership : memberships) {
        List<UserGroupMembership> userMemberships =
            getAllMembershipsForUser(membership.getUserName());
        assertNotNull(userMemberships);
        assertTrue(userMemberships.contains(membership));
      }
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testGetAllMembershipsForUser() {
    try {
      String userName = TestConfiguration.searchUserID;
      List<UserGroupMembership> members = getAllMembershipsForUser(userName);
      assertNotNull(members);
      for (UserGroupMembership membership : members) {
        assertEquals(userName, membership.getUserName());
        assertNotNull(membership.getGroupName());
      }
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testGetAllMembershipsForUserWithNull() {
    try {
      List<UserGroupMembership> members = getAllMembershipsForUser("testuser1");
      assertTrue(members.isEmpty());
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testGetSharePointGroupsForSearchUserAndLdapGroups()
      throws SharepointException {
    Set<Principal> ldapGroups = ImmutableSet.of();
    Set<Principal> spGroups =
        userDataStoreDAO.getSharePointGroupsForSearchUserAndLdapGroups(
            "ns", ldapGroups, "user1");
    String expectedGroup = String.format("[%s]%s", namespace, "group1");
    Set<Principal> expectedGroups = ImmutableSet.of(
        new Principal(UNQUALIFIED, "ns", expectedGroup,
            EVERYTHING_CASE_INSENSITIVE));
    assertEquals(expectedGroups, spGroups);
  }
}
