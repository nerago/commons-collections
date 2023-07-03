/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.collections4.set;

import org.apache.commons.collections4.collection.SynchronizedCollection;

import java.util.Set;

/**
 * Synchronized Set class.
 */
public class SynchronizedSet<T> extends SynchronizedCollection<T> implements Set<T> {
    /**
     * Serialization version
     */
    private static final long serialVersionUID = 20150629L;

    /**
     * Constructor.
     *
     * @param set  the set to decorate
     * @param lock the lock to use, shared with the parent object.
     */
    public SynchronizedSet(final Set<T> set, final Object lock) {
        super(set, lock);
    }
}
