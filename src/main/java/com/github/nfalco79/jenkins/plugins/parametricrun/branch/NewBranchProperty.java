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
package com.github.nfalco79.jenkins.plugins.parametricrun.branch;

import org.kohsuke.stapler.DataBoundConstructor;

import com.github.nfalco79.jenkins.plugins.parametricrun.ParameterBranchProperty;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.branch.BranchPropertyDescriptor;
import jenkins.branch.JobDecorator;

public class NewBranchProperty extends ParameterBranchProperty {

    private final String branchName;

    @DataBoundConstructor
    public NewBranchProperty(String actionLabel, String branchName) {
        super(actionLabel, false, null);
        this.branchName = branchName;
    }

    public String getBranchName() {
        return branchName;
    }

    @Override
    public <P extends Job<P, B>, B extends Run<P, B>> JobDecorator<P, B> jobDecorator(Class<P> clazz) {
        return new JobDecorator<P, B>() {

            @Override
            public P project(P project) {
                project.replaceAction(new NewBranchAction(project, getActionLabel(), getParameterDefinitions(), branchName));
                return super.project(project);
            }
        };
    }

    @Extension
    public static class DescriptorImpl extends BranchPropertyDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.NewBranchProperty_diplayName();
        }
    }
}
