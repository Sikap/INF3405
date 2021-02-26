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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

//https://stackoverflow.com/questions/5667371/validate-ipv4-address-in-java


public class ClientHandler extends Thread {		
		private Socket socket;
		private String IpAddress;
        private String port;
		private Path destination;
		private int number;
        private PrintWriter out;      
        private BufferedReader in;
		
        /**
		 * Constructeur de ClientHandler.
		 * @param socket 
		 * @param clientNumber 
		 */
		public ClientHandler(Socket socket, int clientNumber) {
			this.number = clientNumber;
			this.socket = socket;
 			System.out.println("Connection du client " + clientNumber + " - " + socket);
		}
		
		/**
		 * Assigne a input la premier partie de la command qui corespond seulement a la commande.
		 * @param command 
		 * @return input
		 */
        private String input1(String command) {
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
		 * Fonction qui traite la comande ls
		 */
        private void commandLs() {
        	File[] files = new File(destination.toString()).listFiles();
        	for (File file : files) {
        		if (file.isFile()) {
        			out.println("[File] " + file.getName());
        		} 
        		else if (file.isDirectory()) {
        			out.println("[Folder] " + file.getName());
        		}
        	}   	
        	if (files.length == 0) {
        		out.println("Repertoire vide");
        	}
        }
        
        /**
		 * Fonction qui traite la comande mkdir
		 * @param folder nom du fichier a crée
		 */
        private void commandMkdir(String folder) { 	
		    if (new File(destination.resolve(folder).toString()).mkdirs()) {
		  	  	out.println("Le dossier " + folder + " a bien ete genere");
		    } 
		    else {
		    	out.println("Le dossier " + folder + " n'a pas ete genere");
		    }
        }
        
        /**
		 * Fonction qui traite la comande cd
		 * @param directory le fichier de répertoire
		 */
        private void commandCd(String directory) {        	
        	Path path = Paths.get(directory);
        	Path finalPath = destination.subpath(0, destination.getNameCount() - 1);      	
        	for (int i = 0; i < path.getNameCount(); i++) {   		
        		String subpath = path.subpath(i, i+1).toString();
        		if (subpath.equals("..")) {
        			finalPath = finalPath.subpath(0, finalPath.getNameCount() - 1);      	
        		} 
        		else {
        			finalPath = finalPath.resolve(subpath);
        		}
        	}
        	finalPath = destination.getRoot().resolve(finalPath);
        	finalPath = finalPath.resolve(".");       	
        	if (finalPath.toFile().isDirectory()) {
        		destination = finalPath;
        		out.println("Presentement dans le dossier " + destination.subpath(destination.getNameCount() - 2, destination.getNameCount() - 1).toString());
        	} 
        	else {
        		out.println("Le dossier " + destination.subpath(finalPath.getNameCount() - 2, finalPath.getNameCount()-1).toString() + " n'existe pas");
        	}      	
        }
        
        /**
		 * Upload un fichier du client vers le server
		 * @param fileName 
		 */
    	private void save(String fileName) throws Exception {
    		DataInputStream input = new DataInputStream(socket.getInputStream());
    		FileOutputStream output = new FileOutputStream(fileName);
    		int read = 0;
    		long fileSize = input.readLong();
    		byte[] buffer = new byte[5000];
    		while(fileSize > 0 && (read = input.read(buffer)) > 0) {
    			output.write(buffer, 0, read);
    			fileSize -= read;
    		}
    		output.close();
    		out.println("Le fichier " + fileName + " a ete upload");
    	}
    	
    	/**
		 * Download un fichier du server vers le client.
		 * @param fileName 
		 */
    	private void send(String fileName) throws Exception {  		
    		File file = destination.resolve(fileName).toFile();
    		FileInputStream input = new FileInputStream(file.toString());
    		DataOutputStream output = new DataOutputStream(socket.getOutputStream());
    		int read;
    		output.writeLong(file.length());
    		byte[] buffer = new byte[5000];
    		while ((read = input.read(buffer)) > 0) {
    			output.write(buffer, 0, read);
    		}
    		input.close();
    		out.println("Le fichier " + fileName + " a bien ete telecharge");
    	}
    	
    	/**
		 * Verifie que le fichier existe.
		 * @param fileName
		 * @return  true si le fichier exist
		 */
        private boolean verifyFile(String fileName) { 	
        	File file = destination.resolve(fileName).toFile();
        	if (!(file.isFile())) {
        		out.println("Le fichier n'existe pas.");
        		return false;
        	} 
        	else {
        		out.println("Telechargement");
        		return true;
        	}
        }
        

    	/**
		 * Switch case qui appelle la fonction qui traite la comand.
		 * @param command
		 */
        private void command(String command) {
        	switch(input1(command)) {
        		case "ls" :
        			commandLs();
        			break; 
        		case "cd" :
        			commandCd(input2(command));
        			break;   	   		
        		case "mkdir" :
        			commandMkdir(input2(command));
        			break; 	
        		case "download" :
        			try {
        				if (verifyFile(input2(command))) {    					
        					send(input2(command));
        				}
        			} 
        			catch (Exception e) {
        				e.printStackTrace();
        			}
        			break;
        		case "upload" :
        			try {
        				save(input2(command));
        			} 
        			catch (Exception e) {
        				e.printStackTrace();
        			}
        			break;   	
        		case "exit":
        			break;   	   
        		default:
        			break;
        	}
        }
		
        /**
		 * Lis la commade de l'utilisateur et lance le traitement de la commande.
		 */
		public void run() {
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);				
				out.println("Vous etes le client - " + number);				
				String input = in.readLine();
				destination = Paths.get(input);			
				input = in.readLine();
				IpAddress = input1(input);
				port = input2(input);			
				while (true) {
					input = in.readLine();
					if (input == null) {
						break;
					}
					commandLog(input);
					command(input);
					out.println("done");
				}
			} 
			catch (Exception e) {
				System.out.println("Erreur avec le client - " + number + ": " + e);
			} 
			finally {
				try {
					socket.close();
				} 
				catch (Exception e) {
					System.out.println("Probleme avec le socket");
				}
				System.out.println("Deconnection du client - " + number);
			}
		}
		
		/**
		 * Affiche la commande utiliser dans client avec affichage de l'address IP, le port et la date.
		 * @param message de commande utiliser.
		 */
		private void commandLog(String message) {
			Date date = new Date();
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd@HH:mm:ss");
			System.out.println("[" + IpAddress + ":" + port + ":" + socket.getPort() + "-" +  dateFormat.format(date) + "]: " + message);
		}
		
	}