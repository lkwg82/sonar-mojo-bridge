package de.lgohlke.MavenVersion;

import de.lgohlke.MavenVersion.handler.GOAL;
import de.lgohlke.MavenVersion.handler.UpdateHandler;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import java.io.File;
import java.util.Collections;

public class MavenInvoker {

  private final UpdateHandler handler;
  private final File pom;

  public MavenInvoker(final File pom, final UpdateHandler handler) {
    this.pom = pom;
    this.handler = handler;
  }

  public void run(final GOAL goal) throws MavenInvocationException {
    InvocationRequest request = new DefaultInvocationRequest();
    request.setPomFile(pom);
    request.setOutputHandler(handler);
    request.setGoals(Collections.singletonList(goal.goal()));

    Invoker invoker = new DefaultInvoker();
    invoker.execute(request);
  }

}
