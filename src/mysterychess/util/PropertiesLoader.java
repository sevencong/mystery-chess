package mysterychess.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <code>ApplicationConfiguration</code> deals with application configuration.
 * This class stores user's options in a file and reload them in the next time.
 *
 * @author Tin Bui-Huy
 * @version 1.0, 12/31/09
 */
public class PropertiesLoader {

    /** Location where our configuration file will be stored */
    private File propsFile = null;
    private Properties parameters = null;

    public PropertiesLoader(String directory, String propertiesFileName)
            throws FileNotFoundException, IOException {
        propsFile = new File(directory, propertiesFileName);
        load();
        if (parameters == null) {
            parameters = new Properties();
        }
    }

    public PropertiesLoader(String propertiesFileName) throws FileNotFoundException, IOException {
        this(Util.DEFAULT_BASE_DIRECTORY, propertiesFileName);
    }

    /**
     * Returns the value of a parameter.<p>
     *
     * @param parameterName name of the parameter
     *
     * @return the value of the parameter
     */
    public String getParameter(String parameterName) {
        return parameters.getProperty(parameterName);
    }

    public String getParameter(String parameterName, String defaultValue) {
        return parameters.getProperty(parameterName, defaultValue);
    }

    public void setParameter(String parameterName, String value) throws IOException {
        parameters.setProperty(parameterName, value);
    }

    /**
     * Saves the parameters to a file so that they can be used again next time
     * when the application starts.
     *
     * @throws IOException if any problem happens while accessing file
     */
    public void store() throws IOException {
        synchronized (propsFile) {
            if (propsFile.exists()) {
                propsFile.delete();
            }
            FileOutputStream fos = null;
            try {
                propsFile.createNewFile();
                fos = new FileOutputStream(propsFile);
                parameters.store(fos, Util.getApplicationName() + "-" + Util.getVersion());
            } finally {
                try {
                    fos.close();
                } catch (IOException ioe) {
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
                            "Cannot close the stream", ioe);
                }
            }
        }
    }

    /**
     * Loads the saved parameters from the file.
     *
     * @throws IOException if problem happens while loading file
     */
    private void load() throws IOException {

        if (propsFile.exists() && propsFile.canRead()) {
            synchronized (propsFile) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(propsFile);
                    parameters = new Properties();
                    parameters.load(fis);
                } finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException ex) {
                            Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
                                    "Cannot close the stream", ex);
                        }
                    }
                }
            }
        }
    }
}
