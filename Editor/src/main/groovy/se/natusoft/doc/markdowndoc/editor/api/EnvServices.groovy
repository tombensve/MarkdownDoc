package se.natusoft.doc.markdowndoc.editor.api

/**
 * Created by tommy on 2014-07-05.
 */
public interface EnvServices {

    /**
     * Returns the editorPane GUI API.
     */
    GUI getGUI()

    /**
     * Returns the config API.
     */
    ConfigProvider getConfigProvider()

    /**
     * Returns the persistent properties provider.
     */
    PersistentProps getPersistentProps()

}
