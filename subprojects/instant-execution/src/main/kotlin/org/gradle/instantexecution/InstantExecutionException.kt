/*
 * Copyright 2019 the original author or authors.
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

package org.gradle.instantexecution

import org.gradle.initialization.StartParameterBuildOptions.ConfigurationCacheFailOnProblemsOption
import org.gradle.initialization.StartParameterBuildOptions.ConfigurationCacheMaxProblemsOption
import org.gradle.instantexecution.problems.PropertyProblem
import org.gradle.instantexecution.problems.buildConsoleSummary

import org.gradle.internal.exceptions.Contextual
import org.gradle.internal.exceptions.DefaultMultiCauseException

import java.io.File


/**
 * Marker interface for exception handling.
 */
internal
interface InstantExecutionThrowable


/**
 * State might be corrupted and should be discarded.
 */
@Contextual
class InstantExecutionError internal constructor(
    error: String,
    cause: Throwable? = null
) : Exception(
    "Instant execution state could not be cached: $error",
    cause
), InstantExecutionThrowable


@Contextual
sealed class InstantExecutionException private constructor(
    message: () -> String,
    causes: Iterable<Throwable>
) : DefaultMultiCauseException(message, causes), InstantExecutionThrowable


open class InstantExecutionProblemsException : InstantExecutionException {

    protected
    constructor(
        message: String,
        problems: List<PropertyProblem>,
        htmlReportFile: File
    ) : super(
        { "$message\n${buildConsoleSummary(problems, htmlReportFile)}" },
        problems.mapNotNull(PropertyProblem::exception)
    )

    internal
    constructor(
        problems: List<PropertyProblem>,
        htmlReportFile: File
    ) : this(
        "Instant execution problems found in this build.\n" +
            "This behavior can be changed via --no-${ConfigurationCacheFailOnProblemsOption.LONG_OPTION}.",
        problems,
        htmlReportFile
    )
}


class TooManyInstantExecutionProblemsException internal constructor(
    problems: List<PropertyProblem>,
    htmlReportFile: File
) : InstantExecutionProblemsException(
    "Maximum number of instant execution problems has been reached.\n" +
        "This behavior can be adjusted via --${ConfigurationCacheMaxProblemsOption.LONG_OPTION}=<integer>.",
    problems,
    htmlReportFile
)
