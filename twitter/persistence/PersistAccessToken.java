package twitter.persistence;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

import java.io.*;
import java.util.Scanner;
/**
 * clase que crea, controla y limpia los tokens de autenticacion de usuario
 * @author fsancheztemprano
 */
public class PersistAccessToken implements Persistable {
    /**
     * parametro con la ubicacion del archivo de guardado de tokens
     */
    public static final File file = new File("token.dat");
    //public static final File file = new File(System.getProperty("user.home")+"/consumer.txt".replace("\\","/"));
    
    private String token;
    private String secretToken;

    private Scanner scan;

    /**
     * constructor por defecto
     */
    public PersistAccessToken() {
    }

    /**
     * constructor que recibe los tokens de autorizacion
     *
     * @param token
     * @param secretTk
     */
    public PersistAccessToken(String token, String secretTk) {
        this.token = token;
        this.secretToken = secretTk;
    }

    @Override
    public void setDefault() {
        token = "**************************************************";
        secretToken = "*********************************************";
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSecretToken() {
        return secretToken;
    }

    public void setSecretToken(String secretToken) {
        this.secretToken = secretToken;
    }

    /**
     * guardamos los tokens autenticados en el archivo file para guardar la session
     */
    @Override
    public void saveKey() {
        try (PrintWriter pw = new PrintWriter(file)) {
            pw.println(token);
            pw.println(secretToken);
        } catch (IOException e) {
            System.out.println("IOException on saving tokens ***");
            e.printStackTrace();
        }
        System.out.println("Token saved.");
    }

    /**
     * metodo que lee las tokens del file si existe
     * @throws FileNotFoundException  si el file no existe
     */
    @Override
    public void readKey() throws FileNotFoundException {
        scan = new Scanner(file);
        this.token = scan.nextLine();
        this.secretToken = scan.nextLine();
        System.out.println("Read token OK");
    }

    /**
     * elimina el file que contiene las tokes si existe
     */
    public void removeKey() {
        if (scan != null)
            scan.close();
        file.delete();
        //System.out.println("Token Killed");
    }

    /**
     * metodo que procesa la validacion de los api key y la autenticacion del cliente
     * @param consumer
     * @throws IOException
     * @throws TwitterException
     */
    public void createAccessToken(PersistConsumerKey consumer) throws IOException, TwitterException {
        ConfigurationBuilder configBuilder = new ConfigurationBuilder();
        configBuilder.setDebugEnabled(true)
                .setOAuthConsumerKey(consumer.getApikey())
                .setOAuthConsumerSecret(consumer.getApisecret());
        Twitter OAuthTwitter = new TwitterFactory(configBuilder.build()).getInstance();
        RequestToken requestToken = null;
        AccessToken accessToken = null;
        String url = null;
        do {
            try {
                requestToken = OAuthTwitter.getOAuthRequestToken();
                //System.out.println("Request Tokens obtenidos con éxito.");
                //System.out.println("Request Token: " + requestToken.getToken());
                //System.out.println("Request Token secret: " + requestToken.getTokenSecret());
                url = requestToken.getAuthorizationURL();
                System.out.println("URL:");
                System.out.println(requestToken.getAuthorizationURL());
            } catch (TwitterException ex) {
                //Logger.getLogger(JTwit.class.getName()).log(Level.SEVERE, null, ex);
            }
            BufferedReader lectorTeclado = new BufferedReader(new InputStreamReader(System.in));
            //Abro el navegador. Firefox, en este caso.
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("firefox " + url);
            } catch (Exception e) {
                System.out.println("Cant find firefox. Copy console link;");
            }
            //Nos avisa de que introduciremos el PIN a continuación
            System.out.print("\n\nOAuth PIN: ");
            //Leemos el PIN
            String pin = lectorTeclado.readLine();
            if (pin.length() > 0) {
                accessToken = OAuthTwitter.getOAuthAccessToken(requestToken, pin);
            } else {
                accessToken = OAuthTwitter.getOAuthAccessToken(requestToken);
            }
        } while (accessToken == null);
        System.out.println("\n\nAccess Tokens OK\n Access Granted!\n");
        token = accessToken.getToken();
        secretToken = accessToken.getTokenSecret();
        //System.out.println("Access Token: " + accessToken.getToken());
        //System.out.println("Access Token secret: " + accessToken.getTokenSecret());
    }
}
