package se.natusoft.doc.markdown.generator

import se.natusoft.doc.markdown.api.Generator

/**
 * Provides a generator by its name. The name is what the generators getName() method returns, not its class name!
 */
class GeneratorProvider {

    /**
     * Provides a generator by its name or null if not found.
     *
     * @param name The name of the generator to get.
     *
     * @return A Generator or null if not found.
     */
    public static Generator getGeneratorByName(String name) {
        Generator generator = null

        ServiceLoader<Generator> generators = ServiceLoader.load(Generator.class)
        for (Generator loadedGenerator : generators) {
            if (loadedGenerator.getName().equals(name)) {
                generator = loadedGenerator
                break
            }
        }

        return generator
    }
}
