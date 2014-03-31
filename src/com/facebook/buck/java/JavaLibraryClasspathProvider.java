/*
 * Copyright 2014-present Facebook, Inc.
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

package com.facebook.buck.java;

import com.facebook.buck.rules.BuildRule;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSetMultimap;

import java.nio.file.Path;

import javax.annotation.Nullable;

public class JavaLibraryClasspathProvider {

  private JavaLibraryClasspathProvider() {
  }

  public static ImmutableSetMultimap<JavaLibraryRule, String> getOutputClasspathEntries(
      DefaultJavaLibraryRule javaLibraryRule,
      Optional<Path> outputJar) {
    ImmutableSetMultimap.Builder<JavaLibraryRule, String> outputClasspathBuilder =
        ImmutableSetMultimap.builder();
    Iterable<JavaLibraryRule> javaExportedLibraryDeps =
        getJavaLibraryDeps(javaLibraryRule.getExportedDeps());

    for (JavaLibraryRule rule : javaExportedLibraryDeps) {
      outputClasspathBuilder.putAll(rule, rule.getOutputClasspathEntries().values());
      // If we have any exported deps, add an entry mapping ourselves to to their,
      // classpaths so when suggesting libraries to add we know that adding this library
      // would pull in it's deps.
      outputClasspathBuilder.putAll(
          javaLibraryRule,
          rule.getOutputClasspathEntries().values());
    }

    if (outputJar.isPresent()) {
      outputClasspathBuilder.put(javaLibraryRule, outputJar.get().toString());
    }

    return outputClasspathBuilder.build();
  }

  public static ImmutableSetMultimap<JavaLibraryRule, String> getTransitiveClasspathEntries(
      DefaultJavaLibraryRule javaLibraryRule,
      Optional<Path> outputJar) {
    final ImmutableSetMultimap.Builder<JavaLibraryRule, String> classpathEntries =
        ImmutableSetMultimap.builder();
    ImmutableSetMultimap<JavaLibraryRule, String> classpathEntriesForDeps =
        Classpaths.getClasspathEntries(javaLibraryRule.getDeps());

    ImmutableSetMultimap<JavaLibraryRule, String> classpathEntriesForExportedsDeps =
        Classpaths.getClasspathEntries(javaLibraryRule.getExportedDeps());

    classpathEntries.putAll(classpathEntriesForDeps);

    // If we have any exported deps, add an entry mapping ourselves to to their classpaths,
    // so when suggesting libraries to add we know that adding this library would pull in
    // it's deps.
    if (!classpathEntriesForExportedsDeps.isEmpty()) {
      classpathEntries.putAll(
          javaLibraryRule,
          classpathEntriesForExportedsDeps.values());
    }

    // Only add ourselves to the classpath if there's a jar to be built.
    if (outputJar.isPresent()) {
      classpathEntries.put(javaLibraryRule, outputJar.get().toString());
    }

    return classpathEntries.build();
  }

  public static ImmutableSetMultimap<JavaLibraryRule, String> getDeclaredClasspathEntries(
      DefaultJavaLibraryRule javaLibraryRule) {
    final ImmutableSetMultimap.Builder<JavaLibraryRule, String> classpathEntries =
        ImmutableSetMultimap.builder();

    Iterable<JavaLibraryRule> javaLibraryDeps = getJavaLibraryDeps(javaLibraryRule.getDeps());

    for (JavaLibraryRule rule : javaLibraryDeps) {
      classpathEntries.putAll(rule, rule.getOutputClasspathEntries().values());
    }
    return classpathEntries.build();
  }

  private static FluentIterable<JavaLibraryRule> getJavaLibraryDeps(Iterable<BuildRule> deps) {
    return FluentIterable
        .from(deps)
        .transform(
            new Function<BuildRule, JavaLibraryRule>() {
              @Nullable
              @Override
              public JavaLibraryRule apply(BuildRule input) {
                if (input.getBuildable() instanceof JavaLibraryRule) {
                  return (JavaLibraryRule) input.getBuildable();
                }
                if (input instanceof JavaLibraryRule) {
                  return (JavaLibraryRule) input;
                }
                return null;
              }
            })
        .filter(Predicates.notNull());
  }
}
