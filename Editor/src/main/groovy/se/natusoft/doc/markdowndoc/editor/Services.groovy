package se.natusoft.doc.markdowndoc.editor

import org.jetbrains.annotations.NotNull
import se.natusoft.doc.markdowndoc.editor.api.ConfigProvider
import se.natusoft.doc.markdowndoc.editor.api.Configurable
import se.natusoft.doc.markdowndoc.editor.api.PersistentProps
import se.natusoft.doc.markdowndoc.editor.config.ConfigProviderHolder

/**
 * This class holds static services.
 */
class Services {

    // Holds all configurations.
    public static ConfigProvider configs = new ConfigProviderHolder()

    // Manages persistent properties.
    public static PersistentProps persistentPropertiesProvider = new PersistentPropertiesProvider()

    // All Configurable instances of components or filters are stored in this.
    public static List<Configurable> configurables = new LinkedList<>()

    //
    // Convenience Methods
    //

    static void addConfigurable(@NotNull final Configurable configurable) {
        configurables.add(configurable)
    }

    static void removeConfigurable(@NotNull final Configurable configurable) {
        configurables.remove(configurable)
    }
}
