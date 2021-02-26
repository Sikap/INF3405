import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Scanner;
import java.util.regex.Pattern;

//https://stackoverflow.com/questions/5667371/validate-ipv4-address-in-java


public class Server {

	private static final String IP_REGEX = "^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$" ;
	private static final Pattern IP_PATTERN = Pattern.compile(IP_REGEX);
	private static ServerSocket listener;
	
	/**
	 * Fais rouler un server en utilisant l'adresseIP et porte donner par l'utilisateur.
	 * @exception IOException si le IP adresse est invalid ou le port deja utiliser
	 * @param args Unused
	 */
	public static void main(String[] args) throws Exception {
		int clientNumber = 0;
		Scanner scanner = new Scanner(System.in);
		
		String serverAddress = "";
		int serverPort =0;
		listener = new ServerSocket();
		listener.setReuseAddress(true);
		while (true) {
			serverAddress = evaluateAddress();
			serverPort = evaluatePort();
			try {
				listener.bind(new InetSocketAddress(InetAddress.getByName(serverAddress), serverPort));
				break;
			} 
			catch (IOException e) {
				System.err.println("Erreur: Votre adresse IP est invalide");
			}
		}
		
		System.out.format("Le serveur roule sur %s:%d%n", serverAddress, serverPort);
		scanner.close();
		
		try {
			while(true) {
				new ClientHandler(listener.accept(), clientNumber++).start();
			}
		}
		finally {
			listener.close();
		}
	}
	
	/**
	 * Demande a l'utilisateur d'entrer un port et verifie que le numero de port est valide.
	 * @exception Exception si la valeur du port est pas entre 5000 et 5050.
	 * @return la valeur int du port.
	 */
	public static Integer evaluatePort(){
		Scanner scanner = new Scanner(System.in);
	    String port = "";
	    Boolean valeurValid = false;
	    while (!valeurValid) {
	    	try {
	    		System.out.println("Entrer une valeur pour le port entre 5000 et 5050 :");
	    		port = scanner.next();
	    		if (Integer.parseInt(port) >= 5000 && Integer.parseInt(port) <= 5050) {
	    			valeurValid = true;
	    		}
	    	} 
	    	catch (Exception e) {
	    		System.err.println("La valeur du port doit être un entier entre 5000 et 5050");
	    	}
	    }
	    return Integer.parseInt(port);
	}
	
	/**
	 * Demande a l'utilisateur d'entrer une adresse IP et verifie que l'adresse IP est valide.
	 * @exception Exception si la valeur de l'adresse IP est invalid.
	 * @return deviceAddress la valeur de l'adresse IP.
	 */
	public static String evaluateAddress() {
		Scanner scanner = new Scanner(System.in);
		String deviceAddress = "";
		Boolean adresseIpValide = false;
		while (!adresseIpValide) {
			try {
				System.out.println("Entrer votre adresse IP: ");
				deviceAddress=scanner.next();
	    		adresseIpValide = adresseIpValide(deviceAddress);
	    	} 
			catch (Exception e) {
				System.err.println("Erreur ");
	    	}
	    }
		return deviceAddress;
	}
	
	/**
	 * Verfie que l'adresse IP est dans le bon format.
	 * @param ip l'adresse IP a verifier. 
	 * @exception NumberFormatException si la valeur de l'adresse IP ne peut pas etre converti en nombre.
	 * @return true si l'adresse IP a le bon format.
	 */
	public static boolean adresseIpValide(String ip) {
		 if (ip == null) {
	            return false;
	     }
        if (!IP_PATTERN.matcher(ip).matches())
            return false;
        String[] parts = ip.split("\\.");
        try {
            for (String segment: parts) {
                if (Integer.parseInt(segment) > 255 || (segment.length() > 1 && segment.startsWith("0"))) {
                    return false;
                }
            }
        }
        catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}