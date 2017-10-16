/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.homunculusframework.factory.connection;

/**
 * A connection to a local or remote controller, providing only asynchronous calls to it. Defines
 * methods as follows:
 * <ul>
 * <li>The result type is always Task< Result< T>> where T is the original return type of the synchronous method</li>
 * <li>The name and parameters of the method must be equal</li>
 * <li>Declared exception must not be repeated, they are forwarded in the result</li>
 * <li>If a controller already returns a result, it is automatically re-used without double wrapping</li>
 * </ul>
 * <p>
 * Example:
 * <pre>
 *     public class MyController{
 *         public ModelX query1(ParamY param) throws QueryException{
 *             ...
 *         }
 *
 *         public Result< ModelX> query2(ParamY param) throws QueryException{
 *             ...
 *         }
 *     }
 * </pre>
 * <p>
 * becomes:
 * <pre>
 *     public interface MyControllerConnection extends Connection< MyController>{
 *         Task< Result< ModelX>> query1(ParamY param);
 *         Task< Result< ModelX>> query2(ParamY param);
 *     }
 * </pre>
 *
 * @param <T> the controller type, which must be available in the container
 * @author Torben Schinke
 * @since 1.0
 */
public interface Connection<T> {
}
