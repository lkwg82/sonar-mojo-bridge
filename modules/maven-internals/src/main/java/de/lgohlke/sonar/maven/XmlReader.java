/*
 * sonar-mojo-bridge-maven-internals
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
package de.lgohlke.sonar.maven;

import com.thoughtworks.xstream.XStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

@Slf4j
public class XmlReader {

    private String readXmlFromFile(File projectDirectory, String pathToXmlReport) {
        final File xmlReport = new File(projectDirectory, pathToXmlReport);
        try {
            return FileUtils.readFileToString(xmlReport);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    public <T>T readXmlFromFile(File projectDir, String xmlReport, Class<T> clazz) {
        XStream xstream = new XStream();
        xstream.setClassLoader(getClass().getClassLoader());

        xstream.autodetectAnnotations(true);
        xstream.processAnnotations(clazz);
        String xml = readXmlFromFile(projectDir,xmlReport);
        return (T) xstream.fromXML(xml);
    }
}
