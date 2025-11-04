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
package com.github.nfalco79.jenkins.plugins.branch;


import com.github.nfalco79.jenkins.plugins.branch.MultiNamedExceptionsBranchPropertyStrategy.Named;
import java.util.List;
import jenkins.branch.BranchProperty;
import jenkins.branch.BranchPropertyStrategy;
import jenkins.scm.api.SCMHead;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.github.nfalco79.jenkins.plugins.branch.MultiNamedExceptionsBranchPropertyStrategy.Named.isMatch;
import static org.assertj.core.api.Assertions.assertThat;

class MultiNamedExceptionsBranchPropertyStrategyTest {

    @Test
    void test_that_strategy_aggregates_properties_from_matching_branches() throws Exception {
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
        assertThat(matches).containsExactlyInAnyOrder(prop1, prop2);
    }

    @Test
    void examplesFromHelpText() throws Exception {
        // "production"  matches one and only one branch
        assertThat(isMatch("production", "production")).isTrue();
        assertThat(isMatch("Production", "production")).isTrue();
        assertThat(isMatch("PRODUCTION", "production")).isTrue();
        assertThat(isMatch("proDuctIon", "production")).isTrue();
        assertThat(isMatch("staging", "production")).isFalse();
        // "sandbox/*" matches sandbox/acme but not sandbox/coyote/wiley
        assertThat(isMatch("trunk", "sandbox/*")).isFalse();
        assertThat(isMatch("sandbox/acme", "sandbox/*")).isTrue();
        assertThat(isMatch("sandbox/coyote/wiley", "sandbox/*")).isFalse();
        // "sandbox/**" matches sandbox/acme and sandbox/coyote/wiley
        assertThat(isMatch("trunk", "sandbox/**")).isFalse();
        assertThat(isMatch("sandbox/acme", "sandbox/**")).isTrue();
        assertThat(isMatch("sandbox/coyote/wiley", "sandbox/**")).isTrue();
        // "production,staging" matches two specific branches
        assertThat(isMatch("production", "production,staging")).isTrue();
        assertThat(isMatch("staging", "production,staging")).isTrue();
        assertThat(isMatch("test", "production,staging")).isFalse();
        // "production,staging*" matches the production branch and any branch starting with staging
        assertThat(isMatch("production", "production,staging*")).isTrue();
        assertThat(isMatch("staging", "production,staging*")).isTrue();
        assertThat(isMatch("staging2", "production,staging*")).isTrue();
        assertThat(isMatch("test", "production,staging*")).isFalse();
        // "!staging/**,staging/test/**" matches any branch that is not the a staging branch, but will match the staging/test branches
        assertThat(isMatch("production", "!staging/**,staging/test/**")).isTrue();
        assertThat(isMatch("staging", "!staging/**,staging/test/**")).describedAs("lack of trailing / matches /**").isFalse();
        assertThat(isMatch("staging/", "!staging/**,staging/test/**")).isFalse();
        assertThat(isMatch("staging/acme", "!staging/**,staging/test/**")).isFalse();
        assertThat(isMatch("staging/acme/foo", "!staging/**,staging/test/**")).isFalse();
        assertThat(isMatch("staging/test", "!staging/**,staging/test/**")).describedAs("lack of trailing / matches /**").isTrue();
        assertThat(isMatch("staging/test/foo", "!staging/**,staging/test/**")).isTrue();
        // simple escape
        assertThat(isMatch("\\!starts-with-invert", "\\!starts-with-invert")).isFalse();
        assertThat(isMatch("!starts-with-invert", "\\!starts-with-invert")).isTrue();
        // escape escape
        assertThat(isMatch("\\!starts-with-escape", "\\\\!starts-with-escape")).isTrue();
        assertThat(isMatch("\\\\!starts-with-escape", "\\\\!starts-with-escape")).isFalse();
        // no internal escapes needed
        assertThat(isMatch("no-internal-!-escape", "no-internal-!-escape")).isTrue();
        assertThat(isMatch("no-internal-!-escape", "no-internal-\\!-escape")).isFalse();
        assertThat(isMatch("no-internal-\\!-escape", "no-internal-\\!-escape")).isTrue();
        assertThat(isMatch("no-internal-\\-escape", "no-internal-\\-escape")).isTrue();
        assertThat(isMatch("no-internal-\\-escape", "no-internal-\\\\-escape")).isFalse();
        assertThat(isMatch("no-internal-\\\\-escape", "no-internal-\\\\-escape")).isTrue();
    }
}
