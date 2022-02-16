/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.safetycenter.config;

import static  java.util.Objects.requireNonNull;

import android.annotation.IdRes;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.res.Resources;

import com.android.safetycenter.config.parser.XmlParser;

import java.io.InputStream;
import java.util.List;

/** Utility class to parse and validate a Safety Center Config */
public final class Parser {
    private Parser() {
    }

    /** Thrown when there is an error parsing the Safety Center Config */
    public static final class ParseException extends Exception {
        public ParseException(@NonNull String message) {
            super(message);
        }

        public ParseException(@NonNull String message, @NonNull Throwable ex) {
            super(message, ex);
        }
    }

    /**
     * Parses and validates the given raw XML resource into a {@link SafetyCenterConfig} object.
     *
     * <p>This method uses the XML parser auto generated by the xsdc tool from the
     * safety_center_config.xsd schema file and then applies extra validation on top of it.
     *
     * @param in              the raw XML resource representing the Safety Center configuration
     * @param resourcePkgName the name of the package that contains the Safety Center configuration
     * @param resources       the {@link Resources} retrieved from the package that contains the
     *                        Safety Center configuration
     */
    @Nullable
    public static SafetyCenterConfig parse(@NonNull InputStream in, @NonNull String resourcePkgName,
            @NonNull Resources resources) throws ParseException {
        requireNonNull(in);
        requireNonNull(resourcePkgName);
        requireNonNull(resources);
        com.android.safetycenter.config.parser.SafetyCenterConfig safetyCenterConfig;
        try {
            safetyCenterConfig = XmlParser.read(in);
        } catch (Exception e) {
            throw new ParseException("Exception while reading XML", e);
        }
        return convert(safetyCenterConfig, resourcePkgName, resources);
    }

    @NonNull
    static SafetyCenterConfig convert(
            @Nullable com.android.safetycenter.config.parser.SafetyCenterConfig
                    parserSafetyCenterConfig,
            @NonNull String resourcePkgName, @NonNull Resources resources)
            throws ParseException {
        if (parserSafetyCenterConfig == null) {
            throw new ParseException("Element safety-center-config missing");
        }
        com.android.safetycenter.config.parser.SafetySourcesConfig parserSafetySourcesConfig =
                parserSafetyCenterConfig.getSafetySourcesConfig();
        if (parserSafetySourcesConfig == null) {
            throw new ParseException("Element safety-sources-config missing");
        }
        SafetyCenterConfig.Builder builder = new SafetyCenterConfig.Builder();
        if (parserSafetySourcesConfig.getSafetySourcesGroup() == null) {
            throw new ParseException("Element safety-sources-config invalid");
        }
        int safetySourcesGroupSize = parserSafetySourcesConfig.getSafetySourcesGroup().size();
        for (int i = 0; i < safetySourcesGroupSize; i++) {
            com.android.safetycenter.config.parser.SafetySourcesGroup parserSafetySourcesGroup =
                    parserSafetySourcesConfig.getSafetySourcesGroup().get(i);
            builder.addSafetySourcesGroup(
                    convert(parserSafetySourcesGroup, resourcePkgName, resources));
        }
        try {
            return builder.build();
        } catch (IllegalStateException e) {
            throw new ParseException("Element safety-sources-config invalid", e);
        }
    }

    @NonNull
    static SafetySourcesGroup convert(
            @Nullable com.android.safetycenter.config.parser.SafetySourcesGroup
                    parserSafetySourcesGroup,
            @NonNull String resourcePkgName, @NonNull Resources resources)
            throws Parser.ParseException {
        if (parserSafetySourcesGroup == null) {
            throw new Parser.ParseException("Element safety-sources-group invalid");
        }
        SafetySourcesGroup.Builder builder = new SafetySourcesGroup.Builder();
        builder.setId(parserSafetySourcesGroup.getId());
        if (parserSafetySourcesGroup.getTitle() != null) {
            builder.setTitleResId(
                    parseReference(parserSafetySourcesGroup.getTitle(), resourcePkgName, resources,
                            "safety-sources-group", "title"));
        }
        if (parserSafetySourcesGroup.getSummary() != null) {
            builder.setSummaryResId(
                    parseReference(parserSafetySourcesGroup.getSummary(), resourcePkgName,
                            resources, "safety-sources-group", "summary"));
        }
        if (parserSafetySourcesGroup.getStatelessIconType() != 0) {
            builder.setStatelessIconType(parserSafetySourcesGroup.getStatelessIconType());
        }
        List<com.android.safetycenter.config.parser.SafetySource> parserSafetySourceList =
                parserSafetySourcesGroup.getSafetySource();
        int parserSafetySourceListSize = parserSafetySourceList.size();
        for (int i = 0; i < parserSafetySourceListSize; i++) {
            com.android.safetycenter.config.parser.SafetySource parserSafetySource =
                    parserSafetySourceList.get(i);
            builder.addSafetySource(
                    convert(parserSafetySource, resourcePkgName, resources));
        }
        try {
            return builder.build();
        } catch (IllegalStateException e) {
            throw new ParseException("Element safety-sources-group invalid", e);
        }
    }

    @NonNull
    static SafetySource convert(
            @Nullable com.android.safetycenter.config.parser.SafetySource parserSafetySource,
            @NonNull String resourcePkgName, @NonNull Resources resources)
            throws Parser.ParseException {
        if (parserSafetySource == null) {
            throw new Parser.ParseException("Element safety-source invalid");
        }
        SafetySource.Builder builder = new SafetySource.Builder();
        if (parserSafetySource.getType() != 0) {
            builder.setType(parserSafetySource.getType());
        }
        builder.setId(parserSafetySource.getId());
        builder.setPackageName(parserSafetySource.getPackageName());
        if (parserSafetySource.getTitle() != null) {
            builder.setTitleResId(
                    parseReference(parserSafetySource.getTitle(), resourcePkgName, resources,
                            "safety-source", "title"));
        }
        if (parserSafetySource.getTitleForWork() != null) {
            builder.setTitleForWorkResId(
                    parseReference(parserSafetySource.getTitleForWork(), resourcePkgName, resources,
                            "safety-source", "titleForWork"));
        }
        if (parserSafetySource.getSummary() != null) {
            builder.setSummaryResId(
                    parseReference(parserSafetySource.getSummary(), resourcePkgName, resources,
                            "safety-source", "summary"));
        }
        builder.setIntentAction(parserSafetySource.getIntentAction());
        if (parserSafetySource.getProfile() != 0) {
            builder.setProfile(parserSafetySource.getProfile());
        }
        if (parserSafetySource.getInitialDisplayState() != 0) {
            builder.setInitialDisplayState(parserSafetySource.getInitialDisplayState());
        }
        if (parserSafetySource.getMaxSeverityLevel() != 0) {
            builder.setMaxSeverityLevel(parserSafetySource.getMaxSeverityLevel());
        }
        if (parserSafetySource.getSearchTerms() != null) {
            builder.setSearchTermsResId(
                    parseReference(parserSafetySource.getSearchTerms(), resourcePkgName, resources,
                            "safety-source", "searchTerms"));
        }
        builder.setBroadcastReceiverClassName(parserSafetySource.getBroadcastReceiverClassName());
        if (parserSafetySource.isDisallowLogging()) {
            builder.setDisallowLogging(parserSafetySource.isDisallowLogging());
        }
        if (parserSafetySource.isAllowRefreshOnPageOpen()) {
            builder.setAllowRefreshOnPageOpen(parserSafetySource.isAllowRefreshOnPageOpen());
        }
        try {
            return builder.build();
        } catch (IllegalStateException e) {
            throw new ParseException("Element safety-source invalid", e);
        }
    }

    @IdRes
    static int parseReference(@NonNull String reference, @NonNull String resourcePkgName,
            @NonNull Resources resources, @NonNull String parent, @NonNull String name)
            throws ParseException {
        if (!reference.startsWith("@string/")) {
            throw new ParseException(
                    String.format("String %s in %s.%s is not a reference", reference, parent,
                            name));
        }
        int id = resources.getIdentifier(reference.substring(1), null, resourcePkgName);
        if (id == Resources.ID_NULL) {
            throw new ParseException(
                    String.format("Reference %s in %s.%s missing", reference, parent, name));
        }
        return id;
    }

}