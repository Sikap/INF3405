import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Pattern;

//https://stackoverflow.com/questions/5667371/validate-ipv4-address-in-java


public class Client {
	private BufferedReader in;
	private BufferedReader reader;
    private PrintWriter out;
    private Socket socket;
    private String serverAddress;
    private int port;
    
    private static final String IP_REGEX = "^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$" ;
	private static final Pattern IP_PATTERN = Pattern.compile(IP_REGEX);
	
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
				System.err.println("Erreur votre adresse IP n'a pas le bon format.");
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
	 * Verifie que ces une commande valide.
	 * @param command 
	 * @return true si la commande est valide.
	 */
    private boolean verifyCommand(String command) {  		
    	if (command.equals("ls") || command.equals("exit") || input1(command).equals("cd") || input1(command).equals("mkdir") || input1(command).equals("upload") 
    			|| input1(command).equals("download")) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }
    
    /**
	 * Assigne a input la premier partie de la command qui corespond seulement a la commande.
	 * @param command 
	 * @return input
	 */
    private String input1(String command){
    	String input = "";
        if (command.contains(" ")) {
        	input = command.substring(0, command.indexOf(" "));
        }
        else {
        	input = command;
        }
        return input;
    }
    
    /**
	 * Assigne a input la second partie de la command qui corespond au nom de fichier,dossier ou répertoire
	 * @param command 
	 * @return input
	 */
    private String input2(String command) {
    	String input = "";
        if (command.contains(" ")) {
        	input = command.substring(command.indexOf(" ") + 1, command.length());
        }
        return input;
    }
    
    /**
	 * Verifie que le fichier exist.
	 * @param fileName 
	 * @return true si le fichier exist.
	 */
    private boolean verifyFile(String fileName) {
    	File file = new File(fileName);
    	if (!(file.isFile())) {
    		System.out.println("Le fichier n'existe pas.");
    		return false;
    	}
    	return true;
    }
    
    /**
	 * Download un fichier du server vers le client.
	 * @param sock 
	 * @param fileName 
	 */
    private void download(Socket sock, String fileName) throws IOException {  
    	FileOutputStream output = new FileOutputStream(fileName);
    	DataInputStream input = new DataInputStream(sock.getInputStream());
    	int read = 0;
		
    	long fileSize = input.readLong();
		byte[] buffer = new byte[5000];	
		while(fileSize > 0 && (read = input.read(buffer)) > 0) {
			output.write(buffer, 0, read);
			fileSize -= read;
		}
		output.close();
    }
    
    /**
	 * Upload un fichier du client au server.
	 * @param sock 
	 * @param fileName 
	 */
    private void upload(Socket sock, File file) throws IOException { 
    	FileInputStream input = new FileInputStream(file.toString());
    	DataOutputStream output = new DataOutputStream(sock.getOutputStream());
    	int read;
		
    	output.writeLong(file.length());
		byte[] buffer = new byte[5000];
		while ((read = input.read(buffer)) > 0) {
			output.write(buffer, 0, read);
		}
		input.close();
    }
    
    /**
	 * Affiche le resultat du traitement d'un upload ou download.
	 */
    private void response(){
        String response = "";
        try {
        	do {
        		System.out.println(response);
        		response = in.readLine();
        	} 
        	while (!response.equals("done"));
        } 
        catch (IOException ex) {
        	response = "Erreur: " + ex;
        }
    }
    
    /**
	 * Connect le client au server.
	 */
    public void connectToServer() throws IOException {
    	reader = new BufferedReader(new InputStreamReader(System.in));
    	serverAddress = evaluateAddress();
    	port = evaluatePort();
        socket = new Socket(serverAddress, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }
    
    /**
	 * Connect au server puis lis les command de l'utilisateur jusqu'a la commande exite.
	 */
	public void run() throws IOException {	
		connectToServer();	
        System.out.println(in.readLine() + "\n");       
        out.println(new File(".").toPath().toAbsolutePath());
        out.println(serverAddress + " " + port);
        String command = "";
        
        while (!command.equals("exit")) {
	        System.out.println("\nTapez une commande:");
	        command = reader.readLine();
	        while (!verifyCommand(command)) {
	            System.out.println(command + " n'existe pas. Choisissez une autre commande:");
	        	command = reader.readLine();
	        }	        
	        if (input1(command).equals("upload")) {
	        	if (verifyFile(input2(command))) {
	    	        out.println(command);
	    	        upload(socket, new File(input2(command)));
	    	        response();
	        	}
	        }
	        else if (input1(command).equals("download")) {
    	        out.println(command);
    	        String response = in.readLine();
    	        if(response.equals("Telechargement")){    	        	
    	        	download(socket, input2(command));
    	        } 
    	        else {    	        	
    	        	System.out.println(response);
    	        }
	        	response();
	        }
	        else {
		        out.println(command);
	        	response();
	        }
    	}
        if (command.equals("exit")) {
            try {
                socket.close();
            } 
            catch (IOException e) {
                System.out.println("Impossible de fermer le socket, que se passe-t-il?");
            }
        	System.out.println("Vous avez ete deconnecte avec succes.");
        }
    }

	/**
	 * Construit un nouveau Client et appele run.
	 * @param args Unused 
	 */
    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.run();
    }

}
