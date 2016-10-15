/*
 *
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         2.0.0
 *     
 *     Description
 *         Parses markdown and generates HTML and PDF.
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
package se.natusoft.doc.markdown.util

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

/**
 */
@CompileStatic
@TypeChecked
class SourcePath {

    //
    // Private Members
    //

    /** The path under which to look for source files. */
    private File path

    /** If true source files will be searched for recursively, otherwise only in the direct path. */
    private boolean recursive = false

    /** A regular expression filter to apply to each found file. Example ".*.java" */
    private String fileRegexpFilter = null

    private File origProjRoot = null

    //
    // Constructors
    //

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
     * @param pathExpression The path expression as explained above.
     */
    SourcePath(@NotNull final String pathExpression) {
        this(new File(pathExpression), "")
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
    SourcePath(@NotNull final File projRoot, @NotNull final String pathExpression) {
        this.origProjRoot = projRoot
        File pathFile = new File(projRoot, pathExpression)
        // We translate '!' to '\' to support regular expression escaping in windows which would treat '\' in the
        // original path as a path separator. In unix this is of course not a problem.
        if (isRegularExpression(pathFile.getName().replace('!', '\\'))) {
            this.fileRegexpFilter = pathFile.getName().replace('!', '\\')
            pathFile = pathFile.getParentFile()
        }

        if (pathFile.getName().equals("**")) {
            this.recursive = true
            pathFile = pathFile.getParentFile()
        }

        this.path = pathFile
    }

    /**
     * Creates a new instance.
     *
     * @param path The path to supply source files for.
     */
    SourcePath(@NotNull final File path) {
        this.path = path
    }

    /**
     * Creates a new instance.
     *
     * @param path The path to supply source files for.
     * @param recursive If true source files will be searched for recursively down the file structure.
     * @param fileRegexpFilter A filter to apply for each file. Example ".*.java". This can be null which means no filter.
     */
    SourcePath(@NotNull final File path, final boolean recursive, @Nullable final String fileRegexpFilter) {
        this.path = path
        this.recursive = recursive
        this.fileRegexpFilter = fileRegexpFilter
    }

    //
    // Methods
    //

    /**
     * Returns true if the name is a regular expression.
     *
     * @param name The name to check.
     */
    private static final boolean isRegularExpression(@NotNull final String name) {
        boolean result =  name.contains("*")  || name.contains("[") || name.contains("?") || name.contains("(")
        final int dot = name.indexOf(".")
        final int bs = name.indexOf("\\")
        if (!result && dot >= 0 && bs != (dot - 1)) {
            result = true
        }

        result
    }

    /**
     * Sets recursive path or not.
     *
     * @param recursive If true source files will be searched for recursively down the file structure.
     */
    @SuppressWarnings("GroovyUnusedDeclaration")
    void setRecursive(final boolean recursive) {
        this.recursive = recursive
    }

    /**
     * Supply a file filter using a regular expression.
     *
     * @param fileRegexpFilter A filter to apply for each file. Example ".*.java". This can be null which means no filter.
     */
    @SuppressWarnings("GroovyUnusedDeclaration")
    void setFileRegexpFilter(@Nullable final String fileRegexpFilter) {
        this.fileRegexpFilter = fileRegexpFilter
    }

    /**
     * @return Returns all the source files matching the path criteria.
     */
    @NotNull List<File> getSourceFiles() {
        final List<File> sourceFiles = new ArrayList<File>()

        findSourceFiles(this.path, sourceFiles)

        sourceFiles
    }

    /**
     * Support method to recursively find files.
     *
     * @param currentPath The path to start finding files in.
     * @param sourceFiles Found files are added to this list.
     */
    private void findSourceFiles(@NotNull final File currentPath, @NotNull final List<File> sourceFiles) {
        if (currentPath != null) {
            if (currentPath.exists()) {
                for (final File file : currentPath.listFiles()) {
                    if (file.isDirectory() && this.recursive) {
                        findSourceFiles(file, sourceFiles)
                    }
                    else if (file.isFile()) {
                        if (this.fileRegexpFilter != null) {
                            if (file.getName().matches(this.fileRegexpFilter)) {
                                provideFile(sourceFiles, file)
                            }
                        }
                        else {
                            provideFile(sourceFiles, file)
                        }
                    }
                }
            }
        }
    }

    /**
     * Provides the specified file by adding it to sourceFiles, or if the file is a file-set file (.fs) all files
     * specified by that file are added.
     *
     * @param sourceFiles The list of files to add to.
     * @param file The file to add as is or the files it lists.
     */
    private void provideFile(@NotNull final List<File> sourceFiles, @NotNull final File file) {
        if (file.getName().endsWith(".fs")) {
            final String fileSet = loadFileSetFile(file)
            if (fileSet != null) {
                SourcePaths sourcePaths = this.origProjRoot != null ?
                        new SourcePaths(this.origProjRoot, fileSet) : new SourcePaths(fileSet)

                if (sourcePaths.hasSourceFiles()) {
                    sourcePaths.getSourceFiles().each { final File fsFile ->
                        sourceFiles.add(fsFile)
                    }
                }
                else {
                    // The paths in the fs file is not relative to provided root or current directory.
                    // So try relative to fileset file.
                    sourcePaths = new SourcePaths(file.parentFile, fileSet)
                    if (sourcePaths.hasSourceFiles()) {
                        sourcePaths.getSourceFiles().each { final File fsFile ->
                            sourceFiles.add(fsFile)
                        }
                    }
                    else {
                        // The above also failed, so as a last effort, see if this is a maven project, that is
                        // there are pom.xml files in fileset file dir or above. If so we find the top pom.xml
                        // and then try again with the top pom.xml directory as root.
                        File mavenRoot = file.parentFile
                        mavenRoot = findFirstMavenPom(mavenRoot)
                        if (mavenRoot.parentFile != null) {
                            mavenRoot = getMavenRootDir(mavenRoot)
                            if (mavenRoot.parentFile != null) {
                                sourcePaths = new SourcePaths(mavenRoot, fileSet)
                                sourcePaths.getSourceFiles().each { final File fsFile ->
                                    sourceFiles.add(fsFile)
                                }
                            }
                        }
                    }
                }
            }
        }
        else {
            sourceFiles.add(file)
        }
    }

    /**
     * Returns the directory containing the first found pom.xml file looking upwards, or the root directory.
     *
     * @param dir The directory to start looking at.
     */
    private static File findFirstMavenPom(File dir) {
        while (dir.parentFile != null && !havePOM(dir.listFiles())) {
            dir = dir.parentFile
        }

        return dir
    }

    /**
     * Returns a File representing the directory whose parent directory does not have a pom.xml.
     * In other words, the root of a multi-module build.
     */
    private static File getMavenRootDir(File root) {
        while (havePOM(root.getParentFile().listFiles())) {
            root = root.getParentFile()
        }

        return root
    }

    /**
     * Checks if any of the passed files is a pom.xml.
     *
     * @param files The files to check.
     *
     * @return true if found, false otherwise.
     */
    private static boolean havePOM(File[] files) {
        for (File file : files) { // The Groovy .each {...} fails here and only loops once even if there are more than one file!
            if (file.name == "pom.xml") {
                return true
            }
        }

        return false
    }

    /**
     * Loads a file-set file returning its content as a String.
     *
     * @param fsFile The file to load.
     *
     * @return A string with its content or null on failure to load.
     */
    private @Nullable static final String loadFileSetFile(@NotNull final File fsFile) {
        String fsSetFiles = null

        try {
            final StringBuilder sb = new StringBuilder()
            final BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fsFile)))

            String line = ""
            while (line != null) {
                if (!line.trim().startsWith("#"))
                    sb.append(line)

                line = br.readLine()
            }

            br.close()
            fsSetFiles = sb.toString()
        }
        catch (final IOException ioe) {
            System.err.println("Failed to load file-set file: " + ioe.getMessage())
        }

        fsSetFiles
    }

    /**
     * @return A String representation of the path.
     */
    @Override
    @NotNull String toString() {
        final StringBuilder sb = new StringBuilder()
        sb.append(this.path)
        if (this.recursive) {
            sb.append("/**")
        }
        if (this.fileRegexpFilter != null) {
            sb.append("/")
            sb.append(this.fileRegexpFilter);
        }
        sb.toString()
    }
}
