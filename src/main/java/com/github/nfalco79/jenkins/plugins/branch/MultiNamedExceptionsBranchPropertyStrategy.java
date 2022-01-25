/*
 * Copyright 2018 Falco Nikolas
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.github.nfalco79.jenkins.plugins.branch;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.types.selectors.SelectorUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import jenkins.branch.BranchProperty;
import jenkins.branch.BranchPropertyStrategy;
import jenkins.branch.BranchPropertyStrategyDescriptor;
import jenkins.scm.api.SCMHead;

/**
 * Allows matching named branches to get different properties from the rest.
 *
 * @author Nikolas Falco
 */
public class MultiNamedExceptionsBranchPropertyStrategy extends BranchPropertyStrategy {
    /**
     * The properties that all non-exception {@link SCMHead}s will get.
     */
    @Nonnull
    private final List<BranchProperty> defaultProperties;

    /**
     * The configured exceptions.
     */
    @Nonnull
    private final List<Named> namedExceptions;

    /**
     * Stapler's constructor.
     *
     * @param defaultProperties the properties.
     * @param namedExceptions the named exceptions.
     */
    @DataBoundConstructor
    public MultiNamedExceptionsBranchPropertyStrategy(@CheckForNull BranchProperty[] defaultProperties,
                                                 @CheckForNull Named[] namedExceptions) {
        this.defaultProperties =
                defaultProperties == null ? Collections.<BranchProperty>emptyList() : Arrays.asList(defaultProperties);
        this.namedExceptions =
                namedExceptions == null ? Collections.<Named>emptyList() : Arrays.asList(namedExceptions);
    }

    /**
     * Gets the default properties.
     *
     * @return the default properties.
     */
    @Nonnull
    @SuppressWarnings("unused")// by stapler
    public List<BranchProperty> getDefaultProperties() {
        return defaultProperties;
    }

    /**
     * Gets the named exceptions to the defaults.
     *
     * @return the named exceptions to the defaults.
     */
    @Nonnull
    @SuppressWarnings("unused")// by stapler
    public List<Named> getNamedExceptions() {
        return namedExceptions;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public List<BranchProperty> getPropertiesFor(SCMHead head) {
        List<BranchProperty> properties = new ArrayList<>();

        for (Named named : namedExceptions) {
            if (named.isMatch(head)) {
                properties.addAll(named.getProps());
            }
        }

        if (properties.isEmpty()) {
            // if no one defined adds default
            properties.addAll(defaultProperties);
        }
        return properties;
    }

    /**
     * Our {@link BranchPropertyStrategyDescriptor}.
     */
    @Symbol("multiNamedBranches")
    @Extension
    public static class DescriptorImpl extends BranchPropertyStrategyDescriptor {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return Messages.MultiNamedExceptionsBranchPropertyStrategy_DisplayName();
        }
    }

    /**
     * Holds the specific named exception details.
     */
    public static class Named extends AbstractDescribableImpl<Named> {
        /**
         * The properties that all {@link SCMHead}s will get.
         */
        @Nonnull
        private final List<BranchProperty> props;

        /**
         * The name to match
         */
        @Nonnull
        private final String name;

        /**
         * Constructor
         *
         * @param name the names to match.
         * @param props the properties that the matching branches will get.
         */
        @SuppressWarnings("unused") // via stapler
        @DataBoundConstructor
        public Named(@CheckForNull String name, @CheckForNull BranchProperty[] props) {
            this.name = Util.fixNull(name);
            this.props = props == null ? Collections.<BranchProperty>emptyList() : Arrays.asList(props);
        }

        /**
         * Returns the exception properties.
         *
         * @return the exception properties.
         */
        @Nonnull
        public List<BranchProperty> getProps() {
            return props;
        }

        /**
         * Returns the name(s) to match.
         *
         * @return the name(s) to match.
         */
        @Nonnull
        public String getName() {
            return name;
        }

        /**
         * Returns {@code true} if the head is a match.
         *
         * @param head the head.
         * @return {@code true} if the head is a match.
         */
        public boolean isMatch(@Nonnull SCMHead head) {
            return isMatch(head.getName(), this.name);
        }

        /**
         * Returns {@code true} if and only if the branch name matches one of the name(s).
         *
         * @param branchName the branch name.
         * @param names      the name(s) that are valid to match against.
         * @return {@code true} if and only if the branch name matches one of the name(s).
         */
        public static boolean isMatch(String branchName, String names) {
            for (String name : StringUtils.split(names, ",")) {
                name = name.trim();
                boolean invertMatch;
                if (name.startsWith("!")) {
                    name = name.substring(1);
                    invertMatch = true;
                } else if (name.startsWith("\\!") || name.startsWith("\\\\!")) {
                    // provide an escape hatch
                    name = name.substring(1);
                    invertMatch = false;
                } else {
                    invertMatch = false;
                }
                boolean match;
                if (name.indexOf('*') == -1 && name.indexOf('?') == -1) {
                    match = name.equalsIgnoreCase(branchName);
                } else {
                    name = name.replace('\\', File.separatorChar).replace('/', File.separatorChar);
                    branchName = branchName.replace('\\', File.separatorChar).replace('/', File.separatorChar);
                    match = SelectorUtils.matchPath(name, branchName, false);
                }
                if (invertMatch ? !match : match) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Our {@link hudson.model.Descriptor}
         */
        @Extension
        public static class DescriptorImpl extends Descriptor<Named> {

            /**
             * {@inheritDoc}
             */
            @Override
            public String getDisplayName() {
                return "Named exception";
            }
        }
    }
}
