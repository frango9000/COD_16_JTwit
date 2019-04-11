package twitter.persistence;

import java.io.File;
import java.io.IOException;

/**
 * interfaz con metodos comunes entre las clases de control de
 * secret token y auth token
 *
 * @author fsancheztemprano
 */
interface Persistable {

    File file = null;

    void setDefault();

    void saveKey();

    void readKey() throws IOException;
}
