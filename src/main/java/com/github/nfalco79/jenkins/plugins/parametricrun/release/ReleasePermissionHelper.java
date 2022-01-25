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
package com.github.nfalco79.jenkins.plugins.parametricrun.release;

import javax.annotation.Nonnull;

import com.github.nfalco79.jenkins.plugins.parametricrun.ParameterActionPermissionHelper;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.Job;
import hudson.security.Permission;
import hudson.security.PermissionScope;
import jenkins.model.Jenkins;

public final class ReleasePermissionHelper {

    /**
     * Permission to trigger release builds.
     */
    public static final Permission RELEASE_PERMISSION = new Permission(ParameterActionPermissionHelper.PERMISSIONS, "Release", Messages._ReleasePermissionHelper_description(), Jenkins.ADMINISTER, PermissionScope.ITEM);

    public static boolean hasReleasePermission(@Nonnull Job<?, ?> job) {
        return job.hasPermission(RELEASE_PERMISSION);
    }

    public static void checkReleasePermission(@Nonnull Job<?, ?> job) {
        job.checkPermission(RELEASE_PERMISSION);
    }

    /**
     * Earlier initialisation of permission before job configuration are read so the permission can be found in the parent group
     */
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "Needs to trigger initialisation")
    @Initializer(before = InitMilestone.JOB_LOADED)
    public static void init() {
        RELEASE_PERMISSION.getId();
    }

    private ReleasePermissionHelper() {
    }

}
