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

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.Job;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import hudson.security.PermissionScope;
import jenkins.model.Jenkins;

public final class ParameterActionPermissionHelper {

    /**
     * Permission to trigger release builds.
     */
    public static final PermissionGroup PERMISSIONS = new PermissionGroup(ParameterAction.class, Messages._Permissions_title());
    public static final Permission PARAMETER_ACTION_PERMISSION = new Permission(PERMISSIONS, "ParametricRun", Messages._ParameterBuildPermissionHelper_description(), Jenkins.ADMINISTER, PermissionScope.ITEM);

    public static boolean hasParametricRunPermission(@NonNull Job<?, ?> job) {
        return job.hasPermission(PARAMETER_ACTION_PERMISSION);
    }

    public static void checkParametricRunPermission(@NonNull Job<?, ?> job) {
        job.checkPermission(PARAMETER_ACTION_PERMISSION);
    }

    /**
     * Earlier initialisation of permission before job configuration are read so the permission can be found in the parent group
     * @return
     */
    @Initializer(before = InitMilestone.JOB_LOADED)
    public static String init() {
        return PARAMETER_ACTION_PERMISSION.getId();
    }

    private ParameterActionPermissionHelper() {
    }

}
