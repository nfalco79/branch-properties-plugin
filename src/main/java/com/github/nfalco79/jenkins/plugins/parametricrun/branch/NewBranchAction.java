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
package com.github.nfalco79.jenkins.plugins.parametricrun.branch;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.nfalco79.jenkins.plugins.parametricrun.ParameterAction;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Util;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.StringParameterValue;

/**
 * Allows to run a job decorated with some configured parameters to create a new
 * branch.
 *
 * @author Nikolas Falco
 */
public class NewBranchAction extends ParameterAction {
    private static final String ENV_VARIABLE = "NEW_BRANCH_NAME";

    private final String branchName;

    public NewBranchAction(Job<?, ?> job, //
                           @NonNull String actionLabel, //
                           List<ParameterDefinition> parameterDefinitions,
                           @NonNull String branchName) {
        super(job, actionLabel, null, parameterDefinitions);
        this.branchName = branchName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrlName() {
        return "newBranch";
    }

    @Override
    protected Action[] buildAction(List<ParameterValue> paramValues) {
        Map<String, String> paramEnvVars = new LinkedHashMap<>();
        paramValues.forEach(param -> paramEnvVars.put(param.getName(), String.valueOf(param.getValue())));
        paramValues.add(new StringParameterValue(ENV_VARIABLE, Util.replaceMacro(branchName, paramEnvVars)));

        return super.buildAction(paramValues);
    }

    @Override
    protected boolean isActionEnabled() {
        return this.job.isBuildable() && NewBranchPermissionHelper.hasReleasePermission(job);
    }

    @Override
    protected void verifyPermission() {
        NewBranchPermissionHelper.checkReleasePermission(job);
    }
}