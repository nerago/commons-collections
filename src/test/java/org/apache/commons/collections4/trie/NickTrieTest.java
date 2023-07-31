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
package org.apache.commons.collections4.trie;

import org.apache.commons.collections4.CollectionCommonsRole;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.analyzer.StringKeyAnalyzer;

import java.util.SortedMap;

/**
 * JUnit tests for the PatriciaTrie.
 *
 * @since 4.0
 */
public class NickTrieTest<V extends Comparable<V>> extends AbstractPatriciaTrieTest<V> {

    public NickTrieTest() {
        super(NickTrieTest.class.getSimpleName());
    }

    @Override
    public Trie<String, V> makeObject() {
        return new NickTrie<>(new StringKeyAnalyzer());
    }

    @Override
    public CollectionCommonsRole collectionRole() {
        return CollectionCommonsRole.CONCRETE;
    }

//    @Override
//    public String[] getSampleKeys() {
//        return new String[] {
//                "blah", "foo", "bar", "baz", "tmp", "gosh", "golly", "gee",
//                "hello", "goodbye", "we'll", "see", "you", "all", "again",
//                "key",
//                "key2",
//                ""
//        };
//    }

    @Override
    public String getCompatibilityVersion() {
        return "4";
    }

}
