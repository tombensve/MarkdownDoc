package se.natusoft.doc.markdowndoc.editor.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This will load the same file as ServiceLoader but return Class object instead of instances.
 * This is basically a workaround where ServiceLoader fails to instantiate the service while a
 * Class.forName(...).newInstance() works fine!
 */
public class ServiceDefLoader implements Iterable<Class> {

    //
    // Private Members
    //

    private List<Class> services = new LinkedList<>();

    //
    // Constructors
    //

    /**
     * Creates a new ServiceDefLoader instance.
     *
     * @param serviceAPI The service API to load.
     */
    private ServiceDefLoader(Class serviceAPI) {
        BufferedReader svcReader = new BufferedReader(
                new InputStreamReader(
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/services/" + serviceAPI.getName())
                )
        );
        try {
            String line = svcReader.readLine();
            while (line != null) {
                if (!line.trim().startsWith("#") && line.trim().length() > 0) {
                    try {
                        Class svcClass = Class.forName(line.trim());
                        services.add(svcClass);
                    }
                    catch (ClassNotFoundException  cnfe) {
                        System.err.println("Bad entry in META-INF/services/" + serviceAPI.getName() + "! [" + line + "]");
                        cnfe.printStackTrace(System.err);
                    }
                }
                line = svcReader.readLine();
            }

            svcReader.close();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    //
    // Methods
    //

    /**
     * Returns an iterator over a set of elements of type T.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<Class> iterator() {
        return services.iterator();
    }

    /**
     * Loads the available services for the specified API.
     *
     * @param serviceAPI The service API to load services definitions for.
     */
    public static Iterable<Class> load(Class serviceAPI) {
        return new ServiceDefLoader(serviceAPI);
    }

    /**
     * Convenience to instantiate the specified service class.
     *
     * @param svcClass The class to instantiate.
     * @param <T> The type of the class.
     *
     * @return The instantiated instance or null on failure.
     */
    public static <T> T instantiate(Class<T> svcClass) {
        T instance = null;
        try {
            instance = (T)svcClass.newInstance();
        }
        catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace(System.err);
        }
        return instance;
    }
}
