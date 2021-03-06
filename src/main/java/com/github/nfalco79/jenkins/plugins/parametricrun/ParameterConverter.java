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

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;

import hudson.init.InitMilestone;
import hudson.init.Initializer;
import jenkins.model.Jenkins;

public class ParameterConverter {

    private static final class ObjectConverter implements Converter {
        @Override
        public Object convert(@SuppressWarnings("rawtypes") Class type, Object value) {
            return value;
        }
    }

    @Initializer(before = InitMilestone.COMPLETED)
    public static void init(Jenkins jenkins) {
        ConvertUtils.register(new ObjectConverter(), Object.class);
    }

}