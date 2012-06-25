package de.lgohlke.MavenVersion.handler;

import de.lgohlke.MavenVersion.sonar.DependencyVersionMavenRule;
import de.lgohlke.MavenVersion.sonar.MavenRule;
import de.lgohlke.MavenVersion.sonar.PluginVersionMavenRule;

public enum GOAL {
  DISPLAY_DEPENDENCY_UPDATES {
    @Override
    public String goal() {
      return "versions:display-dependency-updates";
    }

    @Override
    public Class<? extends UpdateHandler> handler() {
      return DisplayDependencyUpdatesHandler.class;
    }

    @Override
    public MavenRule rule() {
      return new DependencyVersionMavenRule();
    }
  },
  DISPLAY_PLUGIN_UPDATES
  {
    @Override
    public String goal() {
      return "versions:display-plugin-updates";
    }

    @Override
    public Class<? extends UpdateHandler> handler() {
      return DisplayPluginUpdatesHandler.class;
    }

    @Override
    public MavenRule rule() {
      return new PluginVersionMavenRule();
    }
  };
  public abstract String goal();

  public abstract MavenRule rule();

  public abstract Class<? extends UpdateHandler> handler();
}
