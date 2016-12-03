import java.net.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.io.*;
import java.util.*;
import java.nio.charset.MalformedInputException;
/*
* Runtime args:
* args[0] = clientName
* args[1] = serverPort
* format for broadcast message: broadcast message "hi to all"
* format for broadcast file: broadcast file "..\File1.txt"
* format for unicast message: broadcast message "hi to all" client1
* format for unicast file: unicast file "..\File1.txt" client1
* format for blockcast message: blockcast message "hi to all" client1 client2
* format for blockcast message: blockcast file "..\File1.txt" client1 client2
* to stop the client: logout
*/

public class Client {
	private static Socket requestSocket;           //socket connect to the server
	private static String clientID;
	private static int serverPort;
	private static ObjectOutputStream out;
	private static ObjectInputStream in;
	
	public static class SendingThread extends Thread {
		private String clientInput;
		private MessageBean outBean;
		private Scanner scanner;
		private ObjectOutputStream out;         //stream write to the socket
 	
		public SendingThread(ObjectOutputStream out){
			try{
			//get OutputStream to send messages
			this.out = out;
			out.flush();
			//get Input from standard input
			scanner = new Scanner(System.in);
			}catch(IOException e){e.printStackTrace();}
		}
		
		@SuppressWarnings("resource")
		public void run() 
		{
			try {				
				while(true)
				{	System.out.print("Please enter command as string: \n");
					//read a sentence from the standard input
					clientInput = scanner.nextLine();
					if(clientInput.equalsIgnoreCase("logout")){
						out.flush();
						out.close();
						System.exit(1);
					}
					try{					
					setOutBean(clientInput);
					out.writeObject(outBean);
					out.flush();
					System.out.println("Message sent");
					}catch(IndexOutOfBoundsException e){ 
						System.out.println("Please enter input in correct format\n");
					}catch(MalformedInputException e){
						if(e.getInputLength() == 0){
							System.out.println("Please enter the first input field correctly: broadcast or unicast or blockcast\n");
						}
						if(e.getInputLength() == 1){
							System.out.println("Please enter the second input field correctly: message or file\n");
						}
						if(e.getInputLength() == 2){
							System.out.println("Please enter the last input field - client name correctly \n");
						}
					}catch(IOException e){ 
						System.out.println("Please enter input in correct format\n");
					}
					
				}
			}catch(IOException ioException){
				try {
					out.flush();
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
			
		}
		
		public String getOutBean() {
			return this.outBean.getClientID()+this.outBean.getMessageMode();
		}
		public void setOutBean(String clientInput) throws IOException {
			//Split the clientInput and set the MessageBean
			String[] clientInputArray = clientInput.split(" ");
			outBean = new MessageBean();
			outBean.setClientID(clientID);
			outBean.setMessageMode(clientInputArray[0]);
			//If user doesnot enter any of broadcast, unicast and blockcast messaging modes
			if(!outBean.getMessageMode().equalsIgnoreCase("broadcast")&&!outBean.getMessageMode().equalsIgnoreCase("unicast")&&!outBean.getMessageMode().equalsIgnoreCase("blockcast")){
				throw new MalformedInputException(0);
			}
			if(clientInputArray[1].equalsIgnoreCase("message")){
				outBean.setTextMessage(true);
				outBean.setTextMessage(clientInput.substring(clientInput.indexOf('"')+1, clientInput.lastIndexOf('"')));
				outBean.setFile(false);
				outBean.setFileBytes(null);
				outBean.setFileName("");
			}else if(clientInputArray[1].equalsIgnoreCase("file")){
				outBean.setFile(true);
				String filePath = clientInput.substring(clientInput.indexOf('"')+1, clientInput.lastIndexOf('"'));
				outBean.setFileName(filePath.substring(filePath.lastIndexOf("\\")+1));
				File f = new File(filePath);
				byte[] fileBytes = Files.readAllBytes(f.toPath());
				outBean.setFileBytes(fileBytes);
				outBean.setTextMessage(false);
				outBean.setTextMessage(null);
			}else{
				// If user doesnt enter message or file input types
				throw new MalformedInputException(1);
			}
			String[] clientArray = clientInput.substring(clientInput.lastIndexOf('"')+1).split(" ");
			ArrayList<String> clientList = new ArrayList<String>();
			Collections.addAll(clientList, clientArray);
			clientList.removeAll(Arrays.asList(null,""));
			outBean.setClientList(clientList);
			//If user doesnot enter destination clients in unicast or blockcast mode
			if(outBean.getMessageMode().equalsIgnoreCase("unicast")||outBean.getMessageMode().equalsIgnoreCase("blockcast")){
				if(outBean.getClientList().size() == 0){
					throw new MalformedInputException(2);
				}
			}
		}
		
	}
	public static class ReceivingThread extends Thread{
	
		private MessageBean inBean;
		private ObjectInputStream in;          //stream read from the socket
 	
		public MessageBean getInBean() {
			return inBean;
		}

		public void setInBean(MessageBean inBean) {
			this.inBean = inBean;
		}

		public ReceivingThread(ObjectInputStream in){
			this.in = in;
		}
		
		@SuppressWarnings("resource")
		public void run(){
			try {
				MessageBean next = new MessageBean();
				while((next = (MessageBean)in.readObject()) != null){	
						setInBean(next);
						if(inBean.isTextMessage()){
							System.out.println("@"+inBean.getClientID()+": "+inBean.getTextMessage());
						}else if(inBean.isFile()){
							String fileName = System.currentTimeMillis()+"_"+inBean.getFileName();
							System.out.println("@"+inBean.getClientID()+" sent file: "+fileName);
							File file = new File(fileName);
							if(file.exists()){
								System.out.println("This file already exists");
							}else{file.createNewFile();}
							Files.write(file.toPath(), inBean.getFileBytes(),StandardOpenOption.APPEND);
						}
					}
			}catch(SocketException e) {
				System.exit(1);
			}catch (ClassNotFoundException e){
				e.printStackTrace();
				try {
					in.close();
				}catch (IOException ef) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}catch(IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				try {
					in.close();
				}catch (IOException ef) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
	}
	
	//main method
	public static void main(String args[]) throws Exception 
	{	
		//Connecting to server
		//Take serverPort and clientID from args
		serverPort = Integer.parseInt(args[1]);
		clientID = args[0];
		try{
			//create a socket to connect to the server at localhost. create output and input streams.
			requestSocket = new Socket("localhost", serverPort);
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			in = new ObjectInputStream(requestSocket.getInputStream());
			//register clientID with the client.
			out.writeObject(new String(clientID));
			out.flush();
			//Starting two new threads: One for sending requests to the server, Other for receiving messages from the server.
			new Thread(new SendingThread(out)).start();
			new Thread(new ReceivingThread(in)).start();
			} catch(SocketException e){
        		System.out.println("Cannot connect to Server");
        	}
	}
}