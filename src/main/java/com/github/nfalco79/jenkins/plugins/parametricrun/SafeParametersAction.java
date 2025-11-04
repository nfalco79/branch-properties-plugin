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
package com.github.nfalco79.jenkins.plugins.parametricrun;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.Job;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Run;
import hudson.model.TaskListener;

/**
 * Action container that enhance at runtime a build with parameters value.
 *
 * @author Nikolas Falco
 */
@Restricted(NoExternalUse.class)
public class SafeParametersAction extends ParametersAction {

    @NonNull
    private final List<ParameterValue> parameters;

    /**
     * At this point the list of parameter values is guaranteed to be safe,
     * which is parameter defined either at top level or release wrapper level.
     *
     * @param parameters Parameters to be passed. All of them will be considered
     *        as safe
     */
    public SafeParametersAction(@NonNull List<ParameterValue> parameters) {
        this.parameters = parameters;
    }

    /**
     * Returns all parameters allowed by the job (defined as regular job
     * parameters) and the parameters allowed by release-specific parameters
     * definition.
     */
    @Override
    public List<ParameterValue> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    /**
     * Returns the parameter if defined as a regular parameters or it is a
     * release-specific parameter defined by the release wrapper.
     *
     * @param name of the parameter
     * @return the value for the specified parameter
     */
    @Override
    public ParameterValue getParameter(String name) {
        for (ParameterValue p : parameters) {
            if (p == null) {
                continue;
            }
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    @Extension
    public static final class SafeParametersActionEnvironmentContributor extends EnvironmentContributor {

        @Override
        public void buildEnvironmentFor(@SuppressWarnings("rawtypes") @NonNull Job job,
                                        @NonNull EnvVars envs,
                                        @NonNull TaskListener listener) throws IOException, InterruptedException {
            job.getActions(SafeParametersAction.class).forEach(a -> envs.putAll(asParamValues(a)));
        }

        @Override
        public void buildEnvironmentFor(@SuppressWarnings("rawtypes") @NonNull Run run,
                                        @NonNull EnvVars envs,
                                        @NonNull TaskListener listener) throws IOException, InterruptedException {
            SafeParametersAction action = run.getAction(SafeParametersAction.class);
            if (action != null) {
                envs.putAll(asParamValues(action));
            }
        }

        private Map<String, String> asParamValues(@NonNull SafeParametersAction action) {
            Map<String, String> paramValues = new HashMap<>();

            for (ParameterValue p : action.getParameters()) {
                paramValues.put(p.getName(), String.valueOf(p.getValue()));
            }

            return paramValues;
        }
    }

}
