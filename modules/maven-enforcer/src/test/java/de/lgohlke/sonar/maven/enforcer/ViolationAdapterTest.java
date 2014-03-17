package de.lgohlke.sonar.maven.enforcer;

import org.fest.assertions.data.MapEntry;
import org.testng.annotations.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class ViolationAdapterTest {

    @Test
    public void testAllViolationAdapterToHaveConfiguredNotToFail() {
        for (ViolationAdapter adapter : Configuration.RULE_ADAPTER_MAP.values()) {
            EnforceMavenPluginHandler handler = new EnforceMavenPluginHandler(null);
            adapter.configure(handler);

            assertThat(handler.getParameters()).contains(MapEntry.entry("fail", "false"));
        }}


}
