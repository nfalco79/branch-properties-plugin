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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.nfalco79.jenkins.plugins.parametricrun.ParameterAction;
import com.github.nfalco79.jenkins.plugins.parametricrun.ParameterBranchProperty;

import hudson.Util;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.Run;

/**
 * Allows to run a job decorated with some configured parameters for release.
 *
 * @author Nikolas Falco
 */
public class ReleaseAction extends ParameterAction {

    public ReleaseAction(Job<?, ?> job, String badgeTemplate, List<ParameterDefinition> parameterDefinitions) {
        super(job, Messages.ReleaseAction_diplayName(), badgeTemplate, parameterDefinitions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrlName() {
        return "release";
    }

    @Override
    protected Action[] buildAction(List<ParameterValue> paramValues) {
        Action[] baseActions = super.buildAction(paramValues);

        Map<String, String> paramEnvVars = new LinkedHashMap<>();
        paramValues.forEach(param -> paramEnvVars.put(param.getName(), String.valueOf(param.getValue())));

        Action[] actions = new Action[baseActions.length + 1];
        System.arraycopy(baseActions, 0, actions, 0, baseActions.length);

        String badgeTip = Util.replaceMacro(getBadgeTemplate() != null
                ? getBadgeTemplate()
                : ParameterBranchProperty.DEFAULT_BADGE_TEMPLATE, paramEnvVars);
        actions[baseActions.length] = new ReleaseBuildBadgeAction(badgeTip);

        return actions;
    }

    /**
     * Gathers all previous build marked with a badge release.
     *
     * @return a map of previous build numbers and release version identifiers
     */
    public List<Run<?, ?>> getPreviousParametricBuilds() {
        List<Run<?, ?>> previousReleaseBuilds = new ArrayList<>();

        for (Run<?, ?> build : getJob().getBuilds()) {
            ReleaseBuildBadgeAction badge = build.getAction(ReleaseBuildBadgeAction.class);

            if (badge != null) {
                previousReleaseBuilds.add(build);
            }
        }

        return previousReleaseBuilds;
    }

    public String getBadgeTipOfBuild(Run<?, ?> build) {
        ReleaseBuildBadgeAction badge = build.getAction(ReleaseBuildBadgeAction.class);

        return badge.getBadgeTip();
    }

    @Override
    protected boolean isActionEnabled() {
        return this.job.isBuildable() && ReleasePermissionHelper.hasReleasePermission(job);
    }

    @Override
    protected void verifyPermission() {
        ReleasePermissionHelper.checkReleasePermission(job);
    }

}