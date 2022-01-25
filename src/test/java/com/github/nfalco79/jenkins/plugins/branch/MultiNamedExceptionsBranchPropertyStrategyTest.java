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
package com.github.nfalco79.jenkins.plugins.branch;


import static com.github.nfalco79.jenkins.plugins.branch.MultiNamedExceptionsBranchPropertyStrategy.Named.isMatch;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.nfalco79.jenkins.plugins.branch.MultiNamedExceptionsBranchPropertyStrategy.Named;

import jenkins.branch.BranchProperty;
import jenkins.branch.BranchPropertyStrategy;
import jenkins.scm.api.SCMHead;

public class MultiNamedExceptionsBranchPropertyStrategyTest {

    @Test
    public void test_that_strategy_aggregates_properties_from_matching_branches() throws Exception {
        BranchProperty defaultProp = Mockito.mock(BranchProperty.class);

        BranchProperty prop1 = Mockito.mock(BranchProperty.class);
        BranchProperty prop2 = Mockito.mock(BranchProperty.class);
        BranchProperty prop3 = Mockito.mock(BranchProperty.class);

        BranchPropertyStrategy strategy = new MultiNamedExceptionsBranchPropertyStrategy( //
            new BranchProperty[] { defaultProp }, //
            new Named[] { //
                          new Named("master", new BranchProperty[] { prop1 }), //
                          new Named("master,support/*", new BranchProperty[] { prop2 }), //
                          new Named("support/*", new BranchProperty[] { prop3 }), //
            });

        List<BranchProperty> matches = strategy.getPropertiesFor(new SCMHead("master"));
        assertThat(matches, Matchers.containsInAnyOrder(prop1, prop2));
    }

    @Test
    public void examplesFromHelpText() throws Exception {
        // "production"  matches one and only one branch
        assertThat(isMatch("production", "production"), is(true));
        assertThat(isMatch("Production", "production"), is(true));
        assertThat(isMatch("PRODUCTION", "production"), is(true));
        assertThat(isMatch("proDuctIon", "production"), is(true));
        assertThat(isMatch("staging", "production"), is(false));
        // "sandbox/*" matches sandbox/acme but not sandbox/coyote/wiley
        assertThat(isMatch("trunk", "sandbox/*"), is(false));
        assertThat(isMatch("sandbox/acme", "sandbox/*"), is(true));
        assertThat(isMatch("sandbox/coyote/wiley", "sandbox/*"), is(false));
        // "sandbox/**" matches sandbox/acme and sandbox/coyote/wiley
        assertThat(isMatch("trunk", "sandbox/**"), is(false));
        assertThat(isMatch("sandbox/acme", "sandbox/**"), is(true));
        assertThat(isMatch("sandbox/coyote/wiley", "sandbox/**"), is(true));
        // "production,staging" matches two specific branches
        assertThat(isMatch("production", "production,staging"), is(true));
        assertThat(isMatch("staging", "production,staging"), is(true));
        assertThat(isMatch("test", "production,staging"), is(false));
        // "production,staging*" matches the production branch and any branch starting with staging
        assertThat(isMatch("production", "production,staging*"), is(true));
        assertThat(isMatch("staging", "production,staging*"), is(true));
        assertThat(isMatch("staging2", "production,staging*"), is(true));
        assertThat(isMatch("test", "production,staging*"), is(false));
        // "!staging/**,staging/test/**" matches any branch that is not the a staging branch, but will match the staging/test branches
        assertThat(isMatch("production", "!staging/**,staging/test/**"), is(true));
        assertThat("lack of trailing / matches /**", isMatch("staging", "!staging/**,staging/test/**"), is(false));
        assertThat(isMatch("staging/", "!staging/**,staging/test/**"), is(false));
        assertThat(isMatch("staging/acme", "!staging/**,staging/test/**"), is(false));
        assertThat(isMatch("staging/acme/foo", "!staging/**,staging/test/**"), is(false));
        assertThat("lack of trailing / matches /**", isMatch("staging/test", "!staging/**,staging/test/**"), is(true));
        assertThat(isMatch("staging/test/foo", "!staging/**,staging/test/**"), is(true));
        // simple escape
        assertThat(isMatch("\\!starts-with-invert", "\\!starts-with-invert"), is(false));
        assertThat(isMatch("!starts-with-invert", "\\!starts-with-invert"), is(true));
        // escape escape
        assertThat(isMatch("\\!starts-with-escape", "\\\\!starts-with-escape"), is(true));
        assertThat(isMatch("\\\\!starts-with-escape", "\\\\!starts-with-escape"), is(false));
        // no internal escapes needed
        assertThat(isMatch("no-internal-!-escape", "no-internal-!-escape"), is(true));
        assertThat(isMatch("no-internal-!-escape", "no-internal-\\!-escape"), is(false));
        assertThat(isMatch("no-internal-\\!-escape", "no-internal-\\!-escape"), is(true));
        assertThat(isMatch("no-internal-\\-escape", "no-internal-\\-escape"), is(true));
        assertThat(isMatch("no-internal-\\-escape", "no-internal-\\\\-escape"), is(false));
        assertThat(isMatch("no-internal-\\\\-escape", "no-internal-\\\\-escape"), is(true));
    }
}
