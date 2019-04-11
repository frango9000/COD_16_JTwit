package twitter.persistence;

import java.io.*;
import java.util.Scanner;
/**
 * clase que crea, controla y limpia los tokens de autenticacion de api
 * @author fsancheztemprano
 */
public class PersistConsumerKey implements Persistable {

    /**
     * parametro con la ubicacion del archivo de guardado de tokens
     */
    private final File file = new File("consumer.dat");
    private String apikey;
    private String apisecret;
    //File file = new File(System.getProperty("user.home")+"/consumer.txt".replace("\\","/"));


    public PersistConsumerKey() {
    }

    /**
     * metodo para permitir usar otra api secret token
     * @param apikey - string con la key
     * @param apisecret - string con la secretkey
     */
    public PersistConsumerKey(String apikey, String apisecret) {
        this.apikey = apikey;
        this.apisecret = apisecret;
    }

    /**
     * valores por defecto asignados por el api de twitter
     */
    @Override
    public void setDefault() {
        apikey = "VK9nTYQvKx76Doj6fAPPZdGmm";
        apisecret = "8TvU3Sf5YwgCWbqvBdVDqGZppiOk3TUZcQgbH88xmxeeD4ATib";
    }

    public String getApikey() {
        return apikey;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    public String getApisecret() {
        return apisecret;
    }

    public void setApisecret(String apisecret) {
        this.apisecret = apisecret;
    }

    /**
     * guarda las tokens en el archivo file
     */
    @Override
    public void saveKey() {
        try (PrintWriter pw = new PrintWriter(file)) {
            pw.println(apikey);
            pw.println(apisecret);
            System.out.println(file.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("IOException on saving consumers ***");
            e.printStackTrace();
        }
    }

    /**
     * lee las tokens del archivo file si existe
     * @throws FileNotFoundException
     */
    @Override
    public void readKey() throws FileNotFoundException {
        Scanner scan = new Scanner(file);
        this.apikey = scan.nextLine();
        this.apisecret = scan.nextLine();
        System.out.println("Read consumer OK");
        //System.out.println(file.getAbsolutePath());
    }
}
