package twitter;

import twitter.persistence.PersistAccessToken;
import twitter4j.TwitterException;
import twitter4j.User;

import java.util.Scanner;

/**
 * Clase main, contiene la logica de creacion de una nueva session o continuar
 * con una sesion previa. Tambien contiene la logica de los comandos por consola
 *
 * @author fsancheztemprano
 */
public class Main {
    public static void main(String[] args) {
        Session session;
        //logica del cliente sin argumentos
        if (args.length == 0) {
            session = getSession();
            menu(session);
        } else {
            consoleLogic(args);
        }
    }

    /**
     * metodo que trata el uso de este programa con argumentos
     *
     * @param args - argumentos de ejecucion
     */
    private static void consoleLogic(String[] args) {
        Session session;//logica del cliente con argumentos de consola
        if (PersistAccessToken.file.exists()) {
            try {
                session = new Session(true);
                switch (args[0]) {
                    case "auth":
                        session = getSession();
                        askSaveSession(session);
                        System.exit(2);
                    case "timeline":
                        session.printTimeline();
                        System.exit(3);
                    case "tweet":
                        StringBuilder tweetsb = new StringBuilder();
                        for (int i = 1; i < args.length; i++) {
                            tweetsb.append(args[i]);
                            if (i != args.length - 1)
                                tweetsb.append(" ");
                        }
                        String tweet = tweetsb.toString();
                        tweet = tweet.substring(0, Math.min(139, tweet.length()));
                        session.updateStatus(tweet);
                        System.exit(4);
                    case "clear":
                        session.clearSession();
                        System.exit(5);
                    case "help":
                    default:
                        System.out.println("jtwit timeline\njtwit tweet <status message>\njtwit clear\njtwit help");
                        System.exit(6);
                }
            } catch (TwitterException e) {
                System.out.println("You need an authenticated session to use this command.\nTo authenticate use only : jtwit");
            }
        } else
            System.out.println("You need an authenticated session to use this command.\nTo authenticate use only : jtwit");
    }

    /**
     * muestra un simple menu de opciones y acciones para realizar cuando la sesion
     * ya se ha autenticado
     *
     * @param session - sesion autenticada correctamente
     */
    private static void menu(Session session) {
        String[] options = {"Timeline", "Tweet", "Search Tweets", "View DMs", "Search user", "Exit"};
        while (true) {
            int n = getPick(options, "@" + session.getScreenName());
            switch (n) {
                case 1://Timeline
                    session.printTimeline();
                    break;
                case 2://Tweet
                    String tweet = scanString("Enter Tweet: ");
                    tweet = tweet.substring(0, Math.min(139, tweet.length()));
                    session.updateStatus(tweet);
                    break;
                case 3://Search Tweets
                    String search = scanString("Enter search term: \n");
                    session.searchStatus(search);
                    break;
                case 4://View DMs
                    session.printDMs();
                    break;
                case 5://Search user
                    userQuery(session);
                    break;
                case 6://Exit
                    askSaveSession(session);
                    System.out.println("\nThank You");
                    System.exit(1);
                default:
                    System.out.println("Invalid Option.");

            }
        }
    }

    /**
     * metodo que le pregunta al usuario si quiere guardar la sesion autenticada y realiza la accion especificada
     *
     * @param session - sesion autenticada correctamente
     */
    private static void askSaveSession(Session session) {
        System.out.println("Save session? (Y/N) : ");
        Boolean answer = null;
        do {
            answer = consoleAssert();
        } while (answer == null);
        if (answer)
            session.saveSession();
        else
            session.clearSession();
        System.out.println("Session " + (PersistAccessToken.file.exists() ? "saved." : "cleared."));
    }


    /**
     * menu de consulta y seleccion de usuarios
     *
     * @param session - sesion autenticada correctamente
     */
    private static void userQuery(Session session) {
        String[] options = {"Search User", "Pick User", "Back"};
        while (true) {
            int n = getPick(options, "@" + session.getScreenName() + " | User Interaction Menu");
            switch (n) {
                case 1:
                    String userQuery = scanString("Enter search term: ");
                    session.searchUser(userQuery);
                    break;
                case 2:
                    interactionMenu(session);
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid Option.");
            }
        }
    }

    /**
     * menu de opciones para interactuar con otro usuario de twitter
     *
     * @param session - sesion autenticada correctamente
     */
    public static void interactionMenu(Session session) {
        String query = scanString("Enter user screen name: ");
        try {
            User user = session.pickUser(query);
            String[] options = {"View Timeline", "Send DM", "Back"};
            while (true) {
                int n = getPick(options, "@" + user.getScreenName());
                switch (n) {
                    case 1:
                        session.printTimeline(user.getScreenName());
                        break;
                    case 2://DM
                        String dm = scanString("Enter message to send directly to @" + user.getScreenName() + " :\n");
                        session.sendDM(user.getId(), dm);
                        break;
                    case 3:
                        return;
                    default:
                        break;
                }
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    /**
     * Intenta crear una nueva sesion, si detectamos que ya existe un token.dat con
     * una sesion previa intenta retomar dicha session, si esta no autentica
     * correctamente intentara crear una nueva sesion autenticando de nuevo
     *
     * @return session
     */
    private static Session getSession() {
        Session session = null;
        boolean persist = false;
        if (PersistAccessToken.file.exists()) {
            System.out.println("Session file found, sign in? (Y/N) : ");
            Boolean answer = null;
            do {
                answer = consoleAssert();
            } while (answer == null);
            if (answer) {
                persist = true;

                try {
                    session = new Session(true);
                } catch (TwitterException e) {
                    System.out.println("Error Authenticating. Try : jtwit clean");
                    System.exit(1);
                }
            }
        }
        if (!persist) {
            try {
                session = new Session();
            } catch (TwitterException e) {
                System.out.println("Error Authenticating. Bye");
                System.exit(1);
                //e.printStackTrace();
            }
        }
        return session;
    }

    /**
     * metodo que recibe un array de strings cuyos elementos representan una de las opciones de un
     * menu para interactuar con el usuario, imprime cada opcion (indice + 1), espera la entrada de texto
     * y por ultimo retorna la respuesta de usuario en un int
     *
     * @param options - array de strings con cada opcion del menu
     * @return - opcion escogida (indice + 1);
     */
    private static int getPick(String[] options) {
        for (int i = 0; i < options.length; i++) {
            System.out.println(i + 1 + ". " + options[i]);
        }
        String opt = "X";
        int n = 0;
        do {
            do {
                opt = new Scanner(System.in).next();
            } while (!isInteger(opt));
            n = Integer.parseInt(opt);
        } while (n < 1 || n > options.length);
        return n;
    }

    /**
     * sobrecarga del metodo getPick para imprimir un titulo antes de imprimir las opciones del menu
     *
     * @param options - array de strings con cada opcion del menu
     * @param title   - titulo a mostrar
     * @return - opcion escogida (indice + 1);
     */
    private static int getPick(String[] options, String title) {
        System.out.println("\n\n" + title);
        return getPick(options);
    }

    /**
     * assert que devuelve true si el string recibido es parseable a int
     *
     * @param str a comprobar
     * @return - true si el string recibido es parseable a int
     */
    private static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * procesamos el string introducido por el usuario en consola
     *
     * @return - true si el usuario introduce Y o y, false si N o n
     * null si no es ni N n Y y
     */
    private static Boolean consoleAssert() {
        switch ((int) scanChar()) {
            case 121: // 'y'
            case 89:  // 'Y'
                return true;
            case 110: // 'n'
            case 78:  // 'N'
                return false;
        }
        return null;
    }

    /**
     * generamos un scanner para recibir el char introducido por el usuario
     *
     * @return - char introducido por el usuario
     */
    private static char scanChar() {
        return new Scanner(System.in).next().charAt(0);
    }

    /**
     * crea un scanner y retorna el string introducido por el usuario
     *
     * @return - string introducido por el usuario
     */
    private static String scanString() {
        return new Scanner(System.in).nextLine();
    }

    /**
     * sobrecarga del metodo anterior que imprime un mensaje informando al usuario que debe introducir
     *
     * @param message - string informativo
     * @return - string introducido por el usuario
     */
    private static String scanString(String message) {
        System.out.print(message);
        return scanString();
    }
}
