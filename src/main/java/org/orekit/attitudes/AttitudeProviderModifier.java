/* Copyright 2002-2011 CS Communication & Systèmes
 * Licensed to CS Communication & Systèmes (CS) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * CS licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.orekit.attitudes;


/** This interface represents an attitude provider that modifies/wraps another underlying provider.
 * @author Luc Maisonobe
 * @since 5.1
 */
public interface AttitudeProviderModifier extends AttitudeProvider {

    /** Get the underlying attitude provider.
     * @return underlying attitude provider
     */
    AttitudeProvider getUnderlyingAttitudeProvider();

}
