/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDoc Maven Plugin
 *     
 *     Code Version
 *         1.0
 *     
 *     Description
 *         A maven plugin for generating documentation from markdown.
 *         
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *     
 * LICENSE
 *     Apache 2.0 (Open Source)
 *     
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     
 *       http://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     
 * AUTHORS
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-11-19: Created!
 *         
 */
package se.natusoft.doc.markdowndoc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class SourcePath {

    //
    // Private Members
    //

    /** The path under which to look for source files. */
    private File path;

    /** If true source files will be searched for recursively, otherwise only in the direct path. */
    private boolean recursive = false;

    /** A regular expression filter to apply to each found file. Example "*.java" */
    private String fileRegexpFilter = null;

    //
    // Constructors
    //

    /**
     * Creates a new instance.
     * <p>
     * This parses a String that must be in one of the following formats:
     * <pre></pre>
     *     /my/path
     *         All files in the directory pointed to by the path.
     *
     *     /my/path/{star}{star}
     *         All files in the directory pointed to by the path and underlaying directories.
     *
     *     /my/path/{star}{star}/{regexp pattern}
     *         All files matching the pattern in the directory pointed to by the path and udnderlaying directories.
     *
     *     /my/path/{regexp pattern}
     *         All files matching the pattern in the directory pointed to by the path.
     * </pre>
     *
     * @param pathExpression The path expression as explained above.
     */
    public SourcePath(String pathExpression) {
        this(new File(pathExpression), "");
    }

    /**
     * Creates a new instance.
     * <p>
     * This parses a String that must be in one of the following formats:
     * <pre>
     *     /my/path
     *         All files in the directory pointed to by the path.
     *
     *     /my/path/{star}{star}
     *         All files in the directory pointed to by the path and underlaying directories.
     *
     *     /my/path/{star}{star}/{regexp pattern}
     *         All files matching the pattern in the directory pointed to by the path and udnderlaying directories.
     *
     *     /my/path/{regexp pattern}
     *         All files matching the pattern in the directory pointed to by the path.
     * </pre>
     *
     * @param projRoot The root dir of the project being built.
     * @param pathExpression The path expression as explained above.
     */
    public SourcePath(File projRoot, String pathExpression) {
        File pathFile = new File(projRoot, pathExpression);
        // We translate '!' to '\' to support regular expression escaping in windows which would treat '\' in the
        // original path as a path separator. In unix this is of course not a problem.
        if (isRegularExpression(pathFile.getName().replace('!', '\\'))) {
            this.fileRegexpFilter = pathFile.getName().replace('!', '\\');
            pathFile = pathFile.getParentFile();
        }

        if (pathFile.getName().equals("**")) {
            this.recursive = true;
            pathFile = pathFile.getParentFile();
        }

        this.path = pathFile;
    }

    /**
     * Creates a new instance.
     *
     * @param path The path to supply source files for.
     */
    public SourcePath(File path) {
        this.path = path;
    }

    /**
     * Creates a new instance.
     *
     * @param path The path to supply source files for.
     * @param recursive If true source files will be searched for recursively down the file structure.
     * @param fileRegexpFilter A filter to apply for each file. Example "*.java". This can be null which means no filter.
     */
    public SourcePath(File path, boolean recursive, String fileRegexpFilter) {
        this.path = path;
        this.recursive = recursive;
        this.fileRegexpFilter = fileRegexpFilter;
    }

    //
    // Methods
    //

    /**
     * Returns true if the name is a regular expression.
     *
     * @param name The name to check.
     */
    private final boolean isRegularExpression(String name) {
        boolean result =  name.contains("*")  || name.contains("[") || name.contains("?") || name.contains("(");
        int dot = name.indexOf(".");
        int bs = name.indexOf("\\");
        if (!result && dot >= 0 && bs != (dot - 1)) {
            result = true;
        }

        return result;
    }

    /**
     * Sets recursive path or not.
     *
     * @param recursive If true source files will be searched for recursively down the file structure.
     */
    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    /**
     * Supply a file filter using a regular expression.
     *
     * @param fileRegexpFilter A filter to apply for each file. Example "*.java". This can be null which means no filter.
     */
    public void setFileRegexpFilter(String fileRegexpFilter) {
        this.fileRegexpFilter = fileRegexpFilter;
    }

    /**
     * @return Returns all the source files matching the path criteria.
     */
    public List<File> getSourceFiles() {
        List<File> sourceFiles = new ArrayList<File>();

        findSourceFiles(this.path, sourceFiles);

        return sourceFiles;
    }

    /**
     * Support method to recursively find files.
     *
     * @param currentPath The path to start finding files in.
     * @param sourceFiles Found files are added to this list.
     */
    private void findSourceFiles(File currentPath, List<File> sourceFiles) {
        if (currentPath != null) {
            if (currentPath.exists()) {
                for (File file : currentPath.listFiles()) {
                    if (file.isDirectory() && this.recursive) {
                        findSourceFiles(file, sourceFiles);
                    }
                    else if (file.isFile()) {
                        if (this.fileRegexpFilter != null) {
                            if (file.getName().matches(this.fileRegexpFilter)) {
                                sourceFiles.add(file);
                            }
                        }
                        else {
                            sourceFiles.add(file);
                        }
                    }
                }
            }
        }
    }

    /**
     * @return A String representation of the path.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.path);
        if (this.recursive) {
            sb.append("/**");
        }
        if (this.fileRegexpFilter != null) {
            sb.append("/");
            sb.append(this.fileRegexpFilter);
        }
        return sb.toString();
    }
}
