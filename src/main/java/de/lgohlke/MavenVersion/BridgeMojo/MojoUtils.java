package de.lgohlke.MavenVersion.BridgeMojo;

import de.lgohlke.MavenVersion.BridgeMojo.MojoUtilsTest.A;
import org.apache.maven.plugin.MojoExecutionException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MojoUtils {

  public static Object invokePrivateMethod(final Class<?> clazz, final String methodname, final Object[] args, final Class<?>[] parameterTypes) throws MojoExecutionException {
    Method method = null;
    try {
      method = clazz.getDeclaredMethod(methodname, parameterTypes);
      method.setAccessible(true);
      return method.invoke(clazz, args);
    } catch (NoSuchMethodException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    } catch (IllegalAccessException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    } catch (InvocationTargetException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    } finally {
      if (method != null) {
        method.setAccessible(false);
      }
    }

  }

  public static Object invokePrivateMethod(final Class<A> clazz, final String methodname) throws MojoExecutionException {
    return invokePrivateMethod(clazz, methodname, new Object[] {}, new Class<?>[] {});
  }
}
