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

 	private static boolean verifyIp(String ip) {
 		if (ip == null) {
	            return false;
	     }
       if (!IP_PATTERN.matcher(ip).matches())
           return false;
       String[] segments = ip.split("\\.");
       try {
           for (String segment: segments) {
               if (Integer.parseInt(segment) > 255 || (segment.length() > 1 && segment.startsWith("0"))) {
                   return false;
               }
           }
       }
       catch(NumberFormatException e) {
           return false;
       }
       return true;
 	}
	
	public boolean verifyPort(int port) {
		return (port >= 5000 && port <= 5050);
	}
	
	public int startPort() throws NumberFormatException, IOException{	
    	Scanner scanner = new Scanner(System.in);
    	System.out.println("Entrer votre port d'�coute: ");
		int port = scanner.nextInt();
        
        while (!verifyPort(port)) {      	
        	System.out.println("Erreur: Votre port d'ecoute dois etre entre 5000 et 5050 ");
			System.out.println("Entrer un port d'�coute valide: ");
			port = scanner.nextInt();
        }
        return port;
    }
	
	public String startIp() throws IOException{    
  	    Scanner scanner = new Scanner(System.in);
		System.out.println("Entrer votre adresse IP: ");
		String adressIP = scanner.nextLine();
  	    
        while (!verifyIp(adressIP)) {      	
        	System.out.println("Erreur: Votre adresse IP est invalide");
			System.out.println("Entrer une  adresse IP  valide: ");
			adressIP = scanner.nextLine();
        }
        return adressIP;
    }
    
    private boolean verifyCommand(String command) {  		
    	if (command.equals("ls") || command.equals("exit") || input1(command).equals("cd") || input1(command).equals("mkdir") || input1(command).equals("upload") 
    			|| input1(command).equals("download")) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }
    
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
    
    private String input2(String command) {
    	String input = "";
        if (command.contains(" ")) {
        	input = command.substring(command.indexOf(" ") + 1, command.length());
        }
        return input;
    }
    
    private boolean verifyFile(String fileName) {
    	File file = new File(fileName);
    	if (!(file.isFile())) {
    		System.out.println("Le fichier n'existe pas.");
    		return false;
    	}
    	return true;
    }
    
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
    
    public void connectToServer() throws IOException {
    	reader = new BufferedReader(new InputStreamReader(System.in));
    	serverAddress = startIp();
    	port = startPort();
        socket = new Socket(serverAddress, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }
    
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
                System.out.println("Couldn't close a socket, what's going on?");
            }
        	System.out.println("Vous avez ete deconnecte avec succes.");
        }
    }

    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.run();
    }

}
