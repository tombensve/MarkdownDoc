package se.natusoft.doc.markdown.generator

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
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
    File getResourceFile(String path) throws IOException {
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

        return resourceFile
    }

    /**
     * Tries to resolve a relative path by going up the file tree to find a match.
     *
     * @param root The root to start at. Should probably be new File("").
     * @param path The relative path to find.
     */
    private File resolveFile(File root, String path) {
        root = root.absoluteFile
        File file = new File(root, path)
        if (file.exists()) { return file }
        if (root.parentFile == null) { return null }
        return resolveFile(root.parentFile, path)
    }

    /**
     * Returns an InputStream to the specified path if found.
     *
     * @param path The path to get InputStream for.
     *
     * @throws IOException if file reference in path cannot be found.
     */
    InputStream getResource(String path) throws IOException {
        return new FileInputStream(getResourceFile(path))
    }

    /**
     * - Adds file: if no protocol is specified.
     * - If file: then resolved to full path if not found with relative path.
     *
     * @param url The DocItem item provided url.
     * @param parseFile The source file of the DocItem item.
     * @param optsResultFile The result file specified in generator options.
     */
    String resolveUrl(String url, File parseFile, String optsResultFile) {
        String resolvedUrl = url
        if (!resolvedUrl.startsWith("file:") && !resolvedUrl.startsWith("http")) {
            resolvedUrl = "file:" + resolvedUrl
        }
        String fallbackUrl = resolvedUrl

        if (resolvedUrl.startsWith("file:")) {
            // Try for absolute path or relative to current directory first.
            String path = resolvedUrl.substring(5)
            File testFile = new File(path)

            if (!testFile.exists()) {
                // Then try relative to parseFile.
                int ix = parseFile.canonicalPath.lastIndexOf(File.separator)
                if (ix >= 0) {
                    // Since this is based on parseFile.canonicalPath it will be a full path from the filesystem root.
                    resolvedUrl = "file:" + ensureSeparatorAtEnd(parseFile.canonicalPath.substring(0, ix + 1))  + path
                    testFile = new File(ensureSeparatorAtEnd(parseFile.canonicalPath.substring(0, ix + 1))  + path)
                }

                if (!testFile.exists()) {
                    // Then try relative to result file.
                    File resultFile = new File(optsResultFile)
                    ix = resultFile.canonicalPath.lastIndexOf(File.separator)
                    if (ix >= 0) {
                        resolvedUrl = "file:" + ensureSeparatorAtEnd(resultFile.canonicalPath.substring(0, ix + 1)) + path
                        testFile = new File(ensureSeparatorAtEnd(resultFile.canonicalPath.substring(0, ix + 1)) + path)

                        if (!testFile.exists()) {
                            // Finally try root dir.
                            if (this.rootDir != null) {
                                ix = this.rootDir.canonicalPath.lastIndexOf(File.separator)
                                if (ix > 0) {
                                    resolvedUrl = "file:" + ensureSeparatorAtEnd(this.rootDir.canonicalPath.substring(0, ix + 1) + path)
                                    testFile = new File(ensureSeparatorAtEnd(this.rootDir.canonicalPath.substring(0, ix + 1)) + path)
                                    if (!testFile.exists()) {
                                        // Give up!
                                        resolvedUrl = fallbackUrl
                                    }
                                }
                                else {
                                    // Give up!
                                    resolvedUrl = fallbackUrl
                                }
                            }
                            else {
                                // Give up!
                                resolvedUrl = fallbackUrl
                            }
                        }
                    }
                }
            }
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
    private static String ensureSeparatorAtEnd(String path) {
        if (!path.trim().endsWith(File.separator)) {
            path = path + File.separator
        }

        return path
    }
}
