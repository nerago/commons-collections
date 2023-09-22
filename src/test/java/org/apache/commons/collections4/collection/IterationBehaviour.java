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
package org.apache.commons.collections4.collection;

public enum IterationBehaviour {
    UNKNOWN, UNORDERED, CONSISTENT_SEQUENCE_UNTIL_MODIFY, STABLE_SEQUENCE, FULLY_SORTED;

    public boolean couldToArrayOrderVary() {
        return this == UNKNOWN || this == UNORDERED;
    }

    public boolean couldSpliteratorOrderVary() {
        return this == UNKNOWN || this == UNORDERED;
    }

    public boolean couldIteratorOrdersVary() {
        return this == UNKNOWN || this == UNORDERED;
    }

    public boolean shouldSpliteratorBeOrdered() {
        return this == STABLE_SEQUENCE || this == FULLY_SORTED;
    }

    public boolean shouldSpliteratorBeSorted() {
        return this == FULLY_SORTED;
    }
}
