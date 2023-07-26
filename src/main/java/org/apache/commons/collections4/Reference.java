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
package org.apache.commons.collections4;

/**
 * A {@link Reference} allows us to return something through a Method's
 * argument list. An alternative would be to an Array with a length of
 * one (1) but that leads to compiler warnings. Computationally and memory
 * wise there's no difference (except for the need to load the
 * {@link Reference} Class but that happens only once).
 */
public final class Reference<E> {
    public E item;

    public Reference() {
        this.item = null;
    }

    public Reference(E item) {
        this.item = item;
    }

    public void set(final E item) {
        this.item = item;
    }

    public E get() {
        return item;
    }
}