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
package org.homunculus.codegen.generator;

import com.github.javaparser.Range;

import org.homunculus.codegen.parse.javaparser.SrcFile;

/**
 * The generator is capable of detecting some misconfigurations which is thrown as this kind of exception
 * which already provides a correct msg format so that IntelliJ/AndroidStudio interprets that as a clickable
 * link.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class LintException extends RuntimeException {
    public LintException(String msg, SrcFile unit, Range range) {
        super("\n" + unit.getFile() + ":" + range.begin.line + ": error: " + msg);
    }
}
