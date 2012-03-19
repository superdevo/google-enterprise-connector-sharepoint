//Copyright 2011 Google Inc.
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.enterprise.connector.adgroups;

/**
 * Provides methods that are needs to be implemented by the cache
 * implementation.
 *
 * @author nageswara_sura
 */
public interface IUserGroupsCache<K, V> {

  /**
   * Returns an object from the cache for a given key.
   *
   * @param key the name of the object you'd like to get
   * @return the object for the given name
   */
  V get(K key);

  /**
   * Put an object into the cache and it's value.
   *
   * @param key the object will be referenced with this name in the cache
   * @param obj the object
   */
  void put(K key, V obj);

  /**
   * Returns true if the key found in cache.
   *
   * @param t the name of the object to lookup in the cache
   * @return true if the object is found; false otherwise
   */
  boolean contains(K t);

  /**
   * To clear cache store force fully.
   */
  void clearCache();

  /**
   * @return size of the cache store.
   */
  int getSize();

}
