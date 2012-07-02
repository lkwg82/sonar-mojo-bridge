package de.lgohlke.MavenVersion.BridgeMojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.testng.annotations.Test;

import static org.fest.assertions.Assertions.assertThat;

public class MojoUtilsTest {

  public static class A {
    @SuppressWarnings("unused")
    private static String helloWorld() {
      return "hello world";
    }

    public static void helloWorldPublic() {

    }
  }

  @Test
  public void invokePrivateMethodNoSuchMethod() {
    try {
      MojoUtils.invokePrivateMethod(A.class, "hello");
    } catch (MojoExecutionException e) {
      assertThat(e.getCause()).isExactlyInstanceOf(NoSuchMethodException.class);
    }
  }

  @Test
  public void invokePrivateMethod() throws MojoExecutionException {
    Object result = MojoUtils.invokePrivateMethod(A.class, "helloWorld");
    assertThat((String) result).isEqualTo("hello world");
  }

  @Test
  public void invokePublicMethod() throws MojoExecutionException {
    MojoUtils.invokePrivateMethod(A.class, "helloWorldPublic");
  }
}
