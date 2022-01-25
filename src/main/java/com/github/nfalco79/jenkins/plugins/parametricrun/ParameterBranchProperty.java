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
package com.github.nfalco79.jenkins.plugins.parametricrun;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.export.Exported;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.Util;
import hudson.model.Job;
import hudson.model.ParameterDefinition;
import hudson.model.Run;
import hudson.model.ParameterDefinition.ParameterDescriptor;
import jenkins.branch.BranchProperty;
import jenkins.branch.BranchPropertyDescriptor;
import jenkins.branch.JobDecorator;

public class ParameterBranchProperty extends BranchProperty {

    public static final String DEFAULT_BADGE_TEMPLATE = "Run with #$PARAMETER";

    private final String actionLabel;
    private final boolean useBadge;
    private final String badgeTemplate;
    private List<ParameterDefinition> parameterDefinitions;

    @DataBoundConstructor
    public ParameterBranchProperty(@Nonnull String actionLabel, //
                                   boolean useBadge, //
                                   @Nullable String badgeTemplate) {
        this.actionLabel = actionLabel;
        this.useBadge = useBadge;
        if (useBadge) {
            this.badgeTemplate = StringUtils.isBlank(badgeTemplate)
                    ? DEFAULT_BADGE_TEMPLATE
                    : Util.fixEmptyAndTrim(badgeTemplate);
        } else {
            this.badgeTemplate = null;
        }
    }

    public boolean getUseBadge() {
        return useBadge;
    }

    public String getBadgeTemplate() {
        return badgeTemplate;
    }

    @Exported
    public List<ParameterDefinition> getParameterDefinitions() {
        return parameterDefinitions;
    }

    /**
     * Sets the parameter definitions.
     *
     * @param parameterDefinitions the parameter definitions.
     */
    @DataBoundSetter
    public void setParameterDefinitions(List<ParameterDefinition> parameterDefinitions) {
        this.parameterDefinitions = parameterDefinitions != null ? parameterDefinitions : Collections.<ParameterDefinition> emptyList();
    }

    public String getActionLabel() {
        return actionLabel;
    }

    @Override
    public <P extends Job<P, B>, B extends Run<P, B>> JobDecorator<P, B> jobDecorator(Class<P> clazz) {
        return new JobDecorator<P, B>() {

            @Override
            public P project(P project) {
                project.replaceAction(new ParameterAction(project, getActionLabel(), getBadgeTemplate(), getParameterDefinitions()));
                return super.project(project);
            }
        };
    }

    @Extension
    public static class DescriptorImpl extends BranchPropertyDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.ParameterBranchProperty_diplayName();
        }

        public DescriptorExtensionList<ParameterDefinition, ParameterDescriptor> getListParameterDefinitionsDescriptors() {
            return ParameterDefinition.all();
        }
    }
}