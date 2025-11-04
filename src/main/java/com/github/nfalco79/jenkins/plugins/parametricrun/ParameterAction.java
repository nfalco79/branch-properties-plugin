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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;
import org.kohsuke.stapler.interceptor.RequirePOST;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Util;
import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Run;
import jakarta.servlet.ServletException;
import jenkins.model.ParameterizedJobMixIn;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Allows to run a project to run manually decorated by configured parameters.
 *
 * @author Nikolas Falco
 */
public class ParameterAction implements Action {

    private final String actionLabel;
    private final String badgeTemplate;
    private final List<ParameterDefinition> parameterDefinitions;
    protected final Job<?, ?> job;

    public ParameterAction(Job<?, ?> job,
                           @NonNull String actionLabel,
                           @CheckForNull String badgeTemplate,
                           @CheckForNull List<ParameterDefinition> parameterDefinitions) {
        this.actionLabel = actionLabel;
        this.badgeTemplate = Util.fixEmptyAndTrim(badgeTemplate);
        this.job = job;
        this.parameterDefinitions = parameterDefinitions != null ? parameterDefinitions : Collections.<ParameterDefinition> emptyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIconFileName() {
        return isActionEnabled() ? "package.png" : null;
    }

    protected boolean isActionEnabled() {
        return this.job.isBuildable() && ParameterActionPermissionHelper.hasParametricRunPermission(job);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        return actionLabel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrlName() {
        return "runWith";
    }

    public Job<?, ?> getJob() {
        return job;
    }

    // needed for UI
    public List<ParameterDefinition> getParameterDefinitions() {
        return parameterDefinitions;
    }

    public String getBadgeTemplate() {
        return badgeTemplate;
    }

    /**
     * This method is invoked by the release page to schedule a new build.
     *
     * @param request HTTP with the POST form data
     * @param response HTTP to return
     * @throws IOException
     * @throws ServletException
     */
    @RequirePOST
    public void doSubmit(StaplerRequest2 request, StaplerResponse2 response) throws IOException, ServletException {
        verifyPermission();

        // bind development / release version
        request.bindParameters(this);

        // create parameter list
        List<ParameterValue> paramValues = getDefaultParametersValues();

        if (parameterDefinitions != null && !parameterDefinitions.isEmpty()) {
            JSONObject formData = request.getSubmittedForm();

            JSONArray a = JSONArray.fromObject(formData.get("parameter"));

            for (Object o : a) {
                JSONObject jo = (JSONObject) o;
                String name = jo.getString("name");

                ParameterDefinition paramDefinition = getParameterDefinition(name);
                if (paramDefinition == null) {
                    throw new IllegalArgumentException("No such parameter definition: " + name);
                }

                ParameterValue paramValue = paramDefinition.createValue(request, jo);
                if (paramValue != null) {
                    paramValues.add(paramValue);
                }
            }
        }

        // schedule release build
        if (ParameterizedJobMixIn.scheduleBuild2(getJob(), 0, buildAction(paramValues)) != null) {
            // TODO redirect to error page?
        }

        // redirect to status page
        response.sendRedirect(job.getAbsoluteUrl());
    }

    protected void verifyPermission() {
        ParameterActionPermissionHelper.checkParametricRunPermission(job);
    }

    protected Action[] buildAction(List<ParameterValue> paramValues) {
        return new Action[] { new SafeParametersAction(paramValues),
                              new CauseAction(new Cause.UserIdCause())
        };
    }

    private List<ParameterValue> getDefaultParametersValues() {
        ParametersDefinitionProperty paramDefProp = job.getProperty(ParametersDefinitionProperty.class);
        ArrayList<ParameterValue> defValues = new ArrayList<>();

        /*
         * This check is made ONLY if someone will call this method even if
         * isParametrized() is false.
         */
        if (paramDefProp == null) {
            return defValues;
        }

        /* Scan for all parameter with an associated default values */
        for (ParameterDefinition paramDefinition : paramDefProp.getParameterDefinitions()) {
            ParameterValue defaultValue = paramDefinition.getDefaultParameterValue();

            if (defaultValue != null) {
                defValues.add(defaultValue);
            }
        }

        return defValues;
    }

    /**
     * Returns all build parameter defined for the specified run.
     *
     * @param build from which extract parameters
     * @return a list of parameter value for the given run
     */
    public List<ParameterValue> getParametersForBuild(Run<?, ?> build) {
        ParametersAction parameters = build.getAction(ParametersAction.class);

        if (parameters != null) {
            return parameters.getParameters();
        }

        return Collections.emptyList();
    }

    /**
     * Gets the {@link ParameterDefinition} of the given name, including the
     * ones from the build parameters, if any.
     *
     * @param name The parameter name
     * @return the parameter definition with the given name
     */
    @Nullable
    public ParameterDefinition getParameterDefinition(String name) {
        ParametersDefinitionProperty buildParamsDefProp = job.getProperty(ParametersDefinitionProperty.class);

        List<ParameterDefinition> buildParameterDefinitions = new LinkedList<>(parameterDefinitions);
        if (buildParamsDefProp != null) {
            buildParameterDefinitions.addAll(buildParamsDefProp.getParameterDefinitions());
        }

        for (ParameterDefinition pd : buildParameterDefinitions) {
            if (pd.getName().equals(name)) {
                return pd;
            }
        }

        return null;
    }

    /**
     * Returns the descriptor for the given object. Used by web page when
     * parameters request check or fill method URL.
     *
     * @param className the descriptor class to resolve
     * @return the requested descriptor
     */
    public Descriptor<?> getDescriptorByName(String className) {
        return job.getDescriptorByName(className);
    }

}