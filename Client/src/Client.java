import java.util.Scanner;
import java.util.regex.Pattern;

public class Client {

	private static final String IP_REGEX = "^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$" ;
	private static final Pattern IP_PATTERN = Pattern.compile(IP_REGEX);

	public static void main(String[] args) 
	{
		Scanner scanner = new Scanner(System.in);
		System.out.println("Entrer votre adresse IP: ");
		String adresseIP = scanner.nextLine();
		while(!adresseIpValide(adresseIP)) {
			System.out.println("Erreur: Votre adresse IP est invalide");
			System.out.println("Entrer une  adresse IP  valide: ");
			adresseIP = scanner.nextLine();
		}
		System.out.println("Entrer votre port d'écoute: ");
		int port = scanner.nextInt();
		while(port < 5000 ||  port > 5050  ) {
			System.out.println("Erreur: Votre port d'ecoute dois etre entre 5000 et 5050 ");
			System.out.println("Entrer un port d'écoute valide: ");
			port = scanner.nextInt();
		}
		scanner.close();
	}
	 public static boolean adresseIpValide(String ip)
   	{
		 if (ip == null) 
		 {
	            return false;
	     }
        if (!IP_PATTERN.matcher(ip).matches())
            return false;
        String[] parts = ip.split("\\.");
        try 
        {
            for (String segment: parts) 
            {
                if (Integer.parseInt(segment) > 255 || (segment.length() > 1 && segment.startsWith("0"))) 
                {
                    return false;
                }
            }
        }catch(NumberFormatException e) 
        {
            return false;
        }
        return true;
    }

}
