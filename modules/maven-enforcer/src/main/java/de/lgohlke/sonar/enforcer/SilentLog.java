/*
 * sonar-maven-checks-maven-enforcer
 * Copyright (C) 2012 Lars Gohlke
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package de.lgohlke.sonar.enforcer;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.Logger;

/**
 * User: lars
 */
public class SilentLog implements Log, Logger {

  public boolean isDebugEnabled() {
    return false;
  }

  public void debug(CharSequence content) {
  }

  public void debug(CharSequence content, Throwable error) {
  }

  public void debug(Throwable error) {
  }

  public boolean isInfoEnabled() {
    return false;
  }

  public void info(CharSequence content) {
  }

  public void info(CharSequence content, Throwable error) {
  }

  public void info(Throwable error) {
  }

  public boolean isWarnEnabled() {
    return false;
  }

  public void warn(CharSequence content) {
  }

  public void warn(CharSequence content, Throwable error) {
  }

  public void warn(Throwable error) {
  }

  public boolean isErrorEnabled() {
    return false;
  }

  public void error(CharSequence content) {
  }

  public void error(CharSequence content, Throwable error) {
  }

  public void error(Throwable error) {
  }

  public void debug(String message) {

  }

  public void debug(String message, Throwable throwable) {

  }

  public void info(String message) {

  }

  public void info(String message, Throwable throwable) {

  }

  public void warn(String message) {

  }

  public void warn(String message, Throwable throwable) {

  }

  public void error(String message) {

  }

  public void error(String message, Throwable throwable) {

  }

  public void fatalError(String message) {

  }

  public void fatalError(String message, Throwable throwable) {

  }

  public boolean isFatalErrorEnabled() {
    return false;
  }

  public Logger getChildLogger(String name) {
    return null;
  }

  public int getThreshold() {
    return 0;
  }

  public String getName() {
    return null;
  }

  /* (non-Javadoc)
   * @see org.codehaus.plexus.logging.Logger#setThreshold(int)
   */
  public void setThreshold(int theThreshold) {

  }
}
