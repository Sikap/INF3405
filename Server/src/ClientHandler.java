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

public class ClientHandler extends Thread {
		
		private Socket socket;
		private int clientNumber;
		private Path actualPath;
        private String IpAddressClient;
        private String portClient;
        private PrintWriter out;      
        private BufferedReader in;
        
        /**
		 * Constructeur de ClientHandler.
		 * @param socket 
		 * @param clientNumber 
		 */
		public ClientHandler(Socket socket, int clientNumber) {
			this.socket = socket;
			this.clientNumber = clientNumber;
 			System.out.println("New connection with client#" + clientNumber + " at " + socket);
		}
		
		/**
		 * Assigne a input la premier partie de la command qui corespond seulement a la commande.
		 * @param command 
		 * @return input
		 */
        private String firstInput(String command) {
        	String input = "";
            if (command.contains(" ")){
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
        private String secondInput(String command) {
        	String input = "";
            if (command.contains(" ")) {
            	input = command.substring(command.indexOf(" ") + 1, command.length());
            }
            return input;
        }
        
    	/**
		 * Switch case qui appelle la fonction qui traite la comand.
		 * @param command
		 */
        private void command(String command) {
        	switch(firstInput(command)) 
        	{
        	case "cd" :
        		processCd(secondInput(command));
        		break;
        	
        	case "ls" :
        		processLs();
        		break;
        		
        	case "mkdir" :
        		processMkdir(secondInput(command));
        		break;
        		
        	case "upload" :
        		try {
        			saveFile(secondInput(command));
        		} 
        		catch (IOException e) {
        			e.printStackTrace();
        		}
        		break;
        	
        	case "download" :
        		try {
    				if (isFileExist(secondInput(command))){    					
    					sendFile(secondInput(command));
    				}
    			} 
        		catch (IOException e) {
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
		 * Fonction qui traite la comande cd
		 * @param directory le fichier de répertoire
		 */
        private void processCd(String directory) {
        	
        	Path desiredPath = actualPath.subpath(0, actualPath.getNameCount()-1);
        	
        	Path path = Paths.get(directory);
        	for (int i = 0; i < path.getNameCount(); i++) {   		
        		String subpath = path.subpath(i, i+1).toString();
        		if (subpath.equals("..")) {
        			desiredPath = desiredPath.subpath(0, desiredPath.getNameCount()-1);
        	
        		} 
        		else {
        			desiredPath = desiredPath.resolve(subpath);
        		}
        	}
        	desiredPath = actualPath.getRoot().resolve(desiredPath);
        	desiredPath = desiredPath.resolve(".");
        	
        	if (desiredPath.toFile().isDirectory()) {
        		actualPath = desiredPath;
        		out.println("Vous etes dans le dossier " + actualPath.subpath(actualPath.getNameCount()-2, actualPath.getNameCount()-1).toString());
        	} 
        	else {
        		out.println("Le dossier " + actualPath.subpath(desiredPath.getNameCount()-2, desiredPath.getNameCount()-1).toString() + " n'existe pas");
        	}
        	
        }
        
    	/**
		 * Fonction qui traite la comande ls
		 */
        private void processLs() {
        	File[] files = new File(actualPath.toString()).listFiles();
        	for (File file : files) {
        		if (file.isFile()) {
        			out.println("[File] " + file.getName());
        		} 
        		else if (file.isDirectory()) {
        			out.println("[Folder] " + file.getName());
        		}
        	}   	
        	if (files.length == 0) {
        		out.println("Aucun fichier dans le repertoire");
        	}
        }
        
    	/**
		 * Fonction qui traite la comande mkdir
		 * @param folder nom du fichier a crée
		 */
        private void processMkdir(String folder) { 	
		    if (new File(actualPath.resolve(folder).toString()).mkdirs()) {
		  	  	out.println("Le dossier " + folder + " a bien ete cree");
		    } 
		    else {
		    	out.println("Le dossier " + folder + " n'a pas ete cree");
		    }
        }
        
    	/**
		 * Upload un fichier du client vers le server
		 * @param fileName 
		 */
    	private void saveFile(String fileName) throws IOException {
    		DataInputStream dis = new DataInputStream(socket.getInputStream());
    		FileOutputStream fos = new FileOutputStream(fileName);
    		byte[] buffer = new byte[4096];
    		long fileSize = dis.readLong();
    		int read = 0;
    		while(fileSize > 0 && (read = dis.read(buffer)) > 0) {
    			fos.write(buffer, 0, read);
    			fileSize -= read;
    		}
    		fos.close();
    		out.println("Le fichier " + fileName + " a bien ete televerse");
    	}
    	
    	/**
		 * Verifie que le fichier existe.
		 * @param fileName
		 * @return  true si le fichier exist
		 */
        private boolean isFileExist(String fileName){ 	
        	File file = actualPath.resolve(fileName).toFile();
        	if (!(file.isFile())){
        		out.println("Ce fichier n'existe pas.");
        		return false;
        	} 
        	else {
        		out.println("Telechargement");
        		return true;
        	}
        }
        
    	/**
		 * Download un fichier du server vers le client.
		 * @param fileName 
		 */
    	private void sendFile(String fileName) throws IOException {  		
    		File file = actualPath.resolve(fileName).toFile();
    		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
    		FileInputStream fis = new FileInputStream(file.toString());
    		byte[] buffer = new byte[4096];
    		int read;
    		dos.writeLong(file.length());
    		while ((read=fis.read(buffer)) > 0) {
    			dos.write(buffer, 0, read);
    		}
    		fis.close();
    		out.println("Le fichier " + fileName + " a bien ete telecharge");
    	}
    	
    	/**
		 * Lis la commade de l'utilisateur et lance le traitement de la commande.
		 */
		public void run() {
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);
				
				out.println("Hello from server - you are client#" + clientNumber + ".");
				
				String input = in.readLine();
				actualPath = Paths.get(input);
				
				input = in.readLine();
				IpAddressClient = firstInput(input);
				portClient = secondInput(input);
				
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
			catch (IOException e) {
				System.out.println("Error handling client# " + clientNumber + ": " + e);
			} 
			finally {
				try {
					socket.close();
				} 
				catch (IOException e) {
					System.out.println("Couldn't close a socket, what's going on?");
				}
				System.out.println("Connection with client # " + clientNumber + " closed");
			}
		}
		
		/**
		 * Affiche la commande utiliser dans client avec affichage de l'address IP, le port et la date.
		 * @param message de commande utiliser.
		 */
		private void commandLog(String message) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd@HH:mm:ss");
			Date date = new Date();
			System.out.println("[" + IpAddressClient + ":" + portClient + ":" + socket.getPort() + "-" +  dateFormat.format(date) + "]: " + message);
		}
		
	}