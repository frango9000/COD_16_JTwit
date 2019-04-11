package twitter;

import twitter.persistence.PersistAccessToken;
import twitter.persistence.PersistConsumerKey;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.IOException;
import java.util.logging.Level;

/**
 * Clase session, contiene la logica de creacion y utilizacion de una
 * session en twitter
 *
 * @author fsancheztemprano
 */
class Session {
    /**
     * parametro twitter que contiene un objeto de interfaz twitter y que
     * sera creado por el TwitterFactory con los consumer key y token
     */
    private final Twitter twitter;
    /**
     * parametro PersistConsumerKey que contiene el procesado de una consumer
     * key preestablecida o la carga de una consumer key desde el
     * archivo consumer.dat
     * <p>
     * estos son los codigos de autenticacion api de este cliente.
     */
    private final PersistConsumerKey consumer;
    /**
     * String q contiene el screen name del usuario autenticado
     */
    private final String screenName;
    /**
     * parametro PersistAccessToken que contiene el procesado de una token
     * key preestablecida o la carga de una token key desde el
     * archivo consumer.dat
     * <p>
     * estos son los codigos de autenticacion del usuario que inicia sesion
     */
    private final PersistAccessToken token;

    /**
     * constructor por defecto de una nueva session
     *
     * @throws TwitterException si falla la autenticacion
     */
    public Session() throws TwitterException {
        this(false);
    }

    /**
     * consturctor base para las sesions
     *
     * @param persist - persist = true -> intenta retomar una session previamente autenticada
     *                * y guardada.
     *                *
     *                * persist = false -> borra cualquier sesion previa(si existe) e inicia
     *                * una nueva sesion
     * @throws TwitterException - lanzamos esta excepcion si hay error de autenticacion con twitter
     */
    public Session(boolean persist) throws TwitterException {
        //intentamos leer un consumer.dat, si no existe realizamos 
        //la autenticacion OAuth
        consumer = new PersistConsumerKey();
        try {
            consumer.readKey();
            //System.out.println("Consumer read from file.");
        } catch (IOException e) {
            //System.out.println("Consumer file not found");
            consumer.setDefault();
            //consumer.saveKey();
            System.out.println("Defaults consumer set!");
        }
        /*
          intentamos leer los tokens de una session previamente autenticada
          y si no es valida o no existe solicitaremos autenticacion para
          crear una nueva session
         */
        token = new PersistAccessToken();
        if (!persist)
            token.removeKey();
        try {
            token.readKey();
            System.out.println("Token read from file.");
        } catch (IOException e) {
            System.out.println("Token file not found -> OAuth");
            try {
                token.createAccessToken(consumer);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            //token.setDefault();TwitterException ex
            //e.printStackTrace();
        }
        /*
          con todos los codigos de autenticacion validamos instanciamos
          el Objeto twitter
         */
        ConfigurationBuilder configBuilder = new ConfigurationBuilder();
        configBuilder.setDebugEnabled(true)
                .setOAuthConsumerKey(consumer.getApikey())
                .setOAuthConsumerSecret(consumer.getApisecret())
                .setOAuthAccessToken(token.getToken())
                .setOAuthAccessTokenSecret(token.getSecretToken());
        twitter = new TwitterFactory(configBuilder.build()).getInstance();
        screenName = twitter.getScreenName();
        System.out.println("Authentication granted to @" + twitter.showUser(twitter.getScreenName()).getScreenName());
    }

    /**
     * devuelve un string con un formato de fecha hora basico a partir de un objeto Date
     *
     * @param date - objeto Date
     * @return - String formateado
     */
    public static String dateFormater(java.util.Date date) {
        return String.format("%02d:%02d:%02d %02d/%02d/%04d", date.getHours(), date.getMinutes(), date.getSeconds(), date.getDate(), date.getMonth(), date.getYear());
    }

    /**
     * getter del parametro twitter
     * con este metodo accedemos a las funciones adicionales (no implementadas) de la session de twitter
     *
     * @return - Twitter
     */
    public Twitter getTwitter() {
        return twitter;
    }

    /**
     * getter del parameto screenName
     *
     * @return - screenName
     */
    public String getScreenName() {
        return screenName;
    }

    /**
     * imprime por consola el timeline de la cuenta autenticada
     */
    public void printTimeline() {
        Paging pagina = new Paging();
        pagina.setCount(50);
        try {
            ResponseList<Status> listado = listado = twitter.getHomeTimeline(pagina);
            for (Status status : listado) {
                System.out.printf("%20s | %15s | %100s %n", dateFormater(status.getCreatedAt()), ("@" + status.getUser().getScreenName()), status.getText());
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    /**
     * metodo que imprime en consola el timeline de un usuario que determinamos con su screenName
     *
     * @param screenName - screenName del usuario a consultar
     */
    public void printTimeline(String screenName) {
        Paging pagina = new Paging();
        pagina.setCount(50);
        try {
            ResponseList<Status> listado = twitter.getUserTimeline(screenName);
            for (Status status : listado) {
                System.out.printf("%20s | %15s | %100s %n", dateFormater(status.getCreatedAt()), ("@" + status.getUser().getScreenName()), status.getText());
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    /**
     * metodo que publica un tweet
     * el string a ser publicado en el tweet lo recibimos como parametro
     *
     * @param string
     */
    public void updateStatus(String string) {
        try {
            twitter.updateStatus(string);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    /**
     * metodeo que devuelve los resultados de la busqueda del string que
     * recibe como parametro
     *
     * @param string
     */
    public void searchStatus(String string) {
        try {
            Query query = new Query(string);
            QueryResult result = twitter.search(query);
            result.getTweets().forEach((status) -> {
                System.out.printf("%20s | %15s | %100s %n", dateFormater(status.getCreatedAt()), ("@" + status.getUser().getScreenName()), status.getText());
            });
        } catch (TwitterException ex) {
            java.util.logging.Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * recibe el userId y devuelve el screeenName en String
     *
     * @param userId - del usuario a buscar
     * @return - screenName en String
     */
    public String getScreenName(long userId) {
        try {
            return twitter.showUser(userId).getScreenName();
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * metodo que recibe el string con una consulta de nombres de usuario e imprime por consola los screenName
     * que coincidan
     *
     * @param query - String a consultar
     */
    public void searchUser(String query) {
        int page = 1;
        try {
            ResponseList<User> users;
            start:
            do {
                users = twitter.searchUsers(query, page);
                String screenName = "";
                System.out.println();
                for (User user : users) {
                    if (user.getScreenName().equalsIgnoreCase(screenName)) {
                        break start;
                    }
                    if (user.getStatus() != null) {
                        System.out.println("@" + user.getScreenName() + " - " + user.getStatus().getText());
                    } else {
                        // the user is protected
                        System.out.println("@" + user.getScreenName());
                    }
                }
                page++;
            } while (users.size() != 0 && page < 2);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        System.out.println();
    }

    /**
     * nos devuelve el objeto User que representa a un usuario de twitter determinado por su screenName
     *
     * @param query - screenName del usuario
     * @return - objeto User
     * @throws TwitterException - si hay problemas de autenticacion/conexion con twitter
     */
    public User pickUser(String query) throws TwitterException {
        return twitter.showUser(query);
    }

    /**
     * envia un dm a un usuario determinado
     *
     * @param recipientId ID del usuario que recibira el dm
     * @param dmText      - String con el contenido del dm
     */
    public void sendDM(long recipientId, String dmText) {
        try {
            DirectMessage dm = twitter.sendDirectMessage(recipientId, dmText);
            System.out.println("Direct message successfully sent to " + getScreenName(dm.getRecipientId()));
            System.out.println("Message sent: " + dm.getText());
        } catch (TwitterException e) {
            System.out.println("Failed to send a direct message: " + e.getMessage());
        }
    }

    /**
     * metodo que imprime en consola los ultimos 20 dms en nuestro buzon, enviados y recibidos
     */
    public void printDMs() {
        try {
            int count = 20;
            DirectMessageList messages;
            messages = twitter.getDirectMessages(count);
            for (DirectMessage message : messages) {
                System.out.printf("%10s | %15s | %15s | %100s %n", dateFormater(message.getCreatedAt()), getScreenName(message.getSenderId()), getScreenName(message.getRecipientId()), message.getText());
            }
            System.out.println("done.");
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to get messages: " + te.getMessage());
        }
    }

    /**
     * metodo que guarda los token de acceso en un archivo para reiniciar session
     */
    public void saveSession() {
        token.saveKey();
    }

    /**
     * metodo que elimina un archivo de tokens de usuario
     */
    public void clearSession() {
        token.removeKey();
    }
}
