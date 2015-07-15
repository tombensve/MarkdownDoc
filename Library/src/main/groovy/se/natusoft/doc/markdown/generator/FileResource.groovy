/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         1.4
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
 *     tommy ()
 *         Changes:
 *         2015-07-15: Created!
 *         
 */
package se.natusoft.doc.markdown.generator

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

/**
 * Provides file resources.
 */
@CompileStatic
@TypeChecked
class FileResource {
    //
    // Properties
    //

    /** The root dir provided to all generators for handling relative paths. Can be null! */
    @Nullable File rootDir

    /** Root dir provided by user options. */
    @Nullable String optsRootDir

    //
    // Methods
    //

    /**
     * Returns a File object pointing to the specified resource.
     *
     * @param path The path of the resource to get.
     *
     * @throws IOException If resource file cannot be found.
     */
    File getResourceFile(@NotNull String path) throws IOException {
        File resourceFile

        if (this.rootDir == null && this.optsRootDir != null && !this.optsRootDir.isEmpty()) {
            this.rootDir = new File(this.optsRootDir)
        }

        if (this.rootDir != null && !rootDir.isDirectory()) {
            this.rootDir = this.rootDir.getParentFile()
        }

        if (this.rootDir != null) {
            resourceFile = new File(this.rootDir, path)
            if (!resourceFile.exists()) throw new FileNotFoundException("Invalid resource path: ${path} !")
        }
        else {
            resourceFile = resolveFile(new File(""), path)
            if (resourceFile == null) { throw new FileNotFoundException("Invalid resource path: ${path} !") }
        }

        resourceFile
    }

    /**
     * Tries to resolve a relative path by going up the file tree to find a match.
     *
     * @param root The root to start at. Should probably be new File("").
     * @param path The relative path to find.
     */
    private @Nullable File resolveFile(@NotNull File root, @NotNull String path) {
        root = root.absoluteFile
        File file = new File(root, path)
        if (file.exists()) { return file }
        if (root.parentFile == null) { return null }

        resolveFile(root.parentFile, path)
    }

    /**
     * Returns an InputStream to the specified path if found.
     *
     * @param path The path to get InputStream for.
     *
     * @throws IOException if file reference in path cannot be found.
     */
    @NotNull InputStream getResource(@NotNull String path) throws IOException {
        new FileInputStream(getResourceFile(path))
    }

    /**
     * Adds file: if no protocol is specified.
     *
     * In the case of a file: path it resolved starting at current directory and then going up to root,
     * trying the path in the passed url. This means that you should always make file references from
     * the top root of your project, since even if a build is built at lower levels it will resolve
     * the path.
     * <p/>
     * The only real difference between this method and getResourceFile is that it handles and URL, which
     * among other things iText needs for loading images. So if an external URL reference is passed then
     * it will just be returned as passed. But when a file: reference is passed then we try to resolve
     * the file.
     *
     * @param url The DocItem item provided url.
     */
    @NotNull String resolveUrl(@NotNull final String url) {
        String resolvedUrl = url.trim()

        if (!resolvedUrl.startsWith("file:") && !resolvedUrl.startsWith("http") && !resolvedUrl.startsWith("jar:")) {
            resolvedUrl = "file:${resolvedUrl}"
        }

        if (resolvedUrl.startsWith("file:")) {
            String resPath = resolvedUrl

            if (resPath.startsWith("file:")) {
                resPath = resPath.substring(5)
            }

            File testFile = getResourceFile(resPath)

            resolvedUrl = "file:" + testFile.absolutePath

        }

        return resolvedUrl
    }

    /**
     * Makes sure the path ends file File.separator.
     *
     * @param path The path to ensure.
     *
     * @return Possibly updated path.
     */
    private static @NotNull String ensureSeparatorAtEnd(@NotNull String path) {
        if (!path.trim().endsWith(File.separator)) {
            path = path + File.separator
        }

        path
    }
}
