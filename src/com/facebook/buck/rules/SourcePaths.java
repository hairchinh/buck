/*
 * Copyright 2013-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.facebook.buck.rules;

import com.google.common.collect.Iterables;

/**
 * Utilities for dealing with {@link SourcePath}s.
 */
public class SourcePaths {

  /** Utility class: do not instantiate. */
  private SourcePaths() {}

  /**
   * Takes an {@link Iterable} of {@link SourcePath} objects and filters those that are suitable to
   * be returned by {@link AbstractCachingBuildRule#getInputsToCompareToOutput()}.
   */
  public static Iterable<String> filterInputsToCompareToOutput(
      Iterable<? extends SourcePath> sources) {
    // Currently, the only implementation of SourcePath that should be included in the Iterable
    // returned by getInputsToCompareToOutput() is FileSourcePath, so it is safe to filter by that
    // and then use .asReference() to get its path.
    //
    // BuildTargetSourcePath should not be included in the output because it refers to a generated
    // file, and generated files are not hashed as part of a RuleKey.
    return Iterables.transform(
        Iterables.filter(sources, FileSourcePath.class),
        SourcePath.TO_REFERENCE);
  }

}
