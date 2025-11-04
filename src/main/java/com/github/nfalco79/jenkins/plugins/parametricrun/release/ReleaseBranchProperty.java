/*
 * Copyright 2018 Nikolas Falco
 *
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
package com.github.nfalco79.jenkins.plugins.parametricrun.release;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import com.github.nfalco79.jenkins.plugins.parametricrun.ParameterBranchProperty;

import hudson.Extension;
import hudson.Util;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.branch.BranchPropertyDescriptor;
import jenkins.branch.JobDecorator;

public class ReleaseBranchProperty extends ParameterBranchProperty {

    public static final String DEFAULT_RELEASE_VERSION_TEMPLATE = "Release #$RELEASE_VERSION";

    @DataBoundConstructor
    public ReleaseBranchProperty(String actionLabel, String badgeTemplate) {
        super(actionLabel, true, StringUtils.isBlank(badgeTemplate)
                ? DEFAULT_RELEASE_VERSION_TEMPLATE
                : Util.fixEmptyAndTrim(badgeTemplate));
    }

    @Override
    public <P extends Job<P, B>, B extends Run<P, B>> JobDecorator<P, B> jobDecorator(Class<P> clazz) {
        return new JobDecorator<P, B>() {

            @Override
            public P project(P project) {
                project.replaceAction(new ReleaseAction(project, getBadgeTemplate(), getParameterDefinitions()));
                return super.project(project);
            }
        };
    }

    @Extension
    public static class DescriptorImpl extends BranchPropertyDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.ReleaseBranchProperty_diplayName();
        }
    }
}
