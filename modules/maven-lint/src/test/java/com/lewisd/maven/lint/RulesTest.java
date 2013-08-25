package com.lewisd.maven.lint;

import com.lewisd.maven.lint.rules.AbstractRule;
import de.lgohlke.sonar.maven.MavenRule;
import org.junit.Test;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.Set;

import static org.fest.assertions.api.Assertions.assertThat;

public class RulesTest {

  @Test
  public void testImplementedAllRulesFromModule() {
    Reflections reflections = new Reflections("com.lewisd.maven.lint");
    Set<Class<? extends AbstractRule>> rulesOffered = reflections.getSubTypesOf(AbstractRule.class);
    int countOfAbstractClasses = 0;
    for (Class<? extends AbstractRule> ruleOffered : rulesOffered) {
      if (Modifier.isAbstract(ruleOffered.getModifiers())) {
        countOfAbstractClasses++;
      }
    }

    Set<Class<? extends MavenRule>> rulesImplemented = reflections.getSubTypesOf(MavenRule.class);

    assertThat(rulesImplemented).hasSize(rulesOffered.size() - 1 - countOfAbstractClasses);
  }
}
