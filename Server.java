import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

	private static int serverPort;   
	private static ServerSocket serverSocket;
	private static ConcurrentHashMap<String, ObjectOutputStream> connMap; 
	private static ObjectInputStream in;	
    	private static ObjectOutputStream out;    
    
    	public static void main(String[] args) throws Exception {
		System.out.println("The server is running."); 
		serverPort = Integer.parseInt(args[0]);
        	serverSocket = new ServerSocket(serverPort);
        	connMap = new ConcurrentHashMap<String, ObjectOutputStream>(); 
	    	try {
            		while(true) {
					Socket connection = serverSocket.accept();
					out = new ObjectOutputStream(connection.getOutputStream());
					out.flush();
					in = new ObjectInputStream(connection.getInputStream());
					new ReceivingThread(out,in).start();
	        		}
        	} catch(SocketException e){	
        	}finally {
            		serverSocket.close();
        	} 
     	}
	/**
     	* A handler thread class.  Handlers are spawned from the listening
     	* loop and are responsible for dealing with a single client's requests.
     	*/
    	private static class ReceivingThread extends Thread {
    	private ObjectInputStream in;	//stream read from the socket
		private ObjectOutputStream out;    //stream write to the socket
		private String clientId;
		private MessageBean clientBean;
		
        	public MessageBean getClientBean() {
			return clientBean;
		}

		public void setClientBean(MessageBean clientBean) {
			this.clientBean = clientBean;
		}

		public ReceivingThread(ObjectOutputStream out, ObjectInputStream in) {
          		try{
				this.out = out;
				this.out.flush();
				this.in = in;
				}catch(Exception e){e.printStackTrace();}
        	}

        	public void run() {
 			try{
				//initialize Input and Output streams
				while(true)
				{
					//receive the message sent from the client
					Object clientObject = in.readObject();
					if(clientObject instanceof String){
						clientId = (String)clientObject;
						connMap.put(clientId, out);
						System.out.println("Client "+clientId+" connected");
					}else{
						setClientBean((MessageBean)clientObject);
						new SendingThread(out,in,clientBean).start();						
					}	
				}
 			}catch(IOException e){
				try{
					System.out.println("Disconnect with Client " + clientId );
					connMap.remove(clientId,out);
					in.close();
					out.flush();
					out.close();
				}catch(IOException ioException){
					System.out.println("Disconnect with Client " +clientId);
					ioException.printStackTrace();
				}
			}catch(ClassNotFoundException e){
 				try{
					System.out.println("Disconnect with Client " + clientId );
					connMap.remove(clientId,out);
					in.close();
					out.flush();
					out.close();
				}catch(IOException ioException){
					System.out.println("Disconnect with Client " + clientId );
					ioException.printStackTrace();
				}
			}
 		
		}
    }
    	
    	private static class SendingThread extends Thread {
    		private ObjectInputStream in;	//stream read from the socket
    		private ObjectOutputStream out;    //stream write to the socket
    	
    		private MessageBean clientBean;
    		
           	public MessageBean getClientBean() {
    			return clientBean;
    		}

    		public void setClientBean(MessageBean clientBean) {
    			this.clientBean = clientBean;
    		}

    		public SendingThread(ObjectOutputStream out, ObjectInputStream in, MessageBean clBean) {
              		try{
    				this.out = out;
    				this.out.flush();
    				this.in = in;
    	    			this.clientBean = clBean;
              		}catch(Exception e){e.printStackTrace();}
            	}

            	public void run() {
     		try{
    				Set<String> clients = connMap.keySet();
    				if(clientBean.getMessageMode().equalsIgnoreCase("broadcast")){
    					for(String client: clients){
    						if(!client.equalsIgnoreCase(getClientBean().getClientID())){
    							ObjectOutputStream tempout = connMap.get(client);
    							tempout.writeObject(getClientBean());
    							tempout.flush();
    						}
    					}
					String messageContent = (clientBean.isFile())?"file" : "message";
					System.out.println(clientBean.getMessageMode()+" "+messageContent+" sent by Client " + clientBean.getClientID());
    				}else if(this.clientBean.getMessageMode().equalsIgnoreCase("unicast")){
    					ObjectOutputStream tempout = connMap.get(getClientBean().getClientList().get(0));
					if(tempout!=null){
						tempout.writeObject(getClientBean());
						tempout.flush();
						String messageContent = (clientBean.isFile())?"file" : "message";
						System.out.println(clientBean.getMessageMode()+" "+messageContent+" sent by Client " + clientBean.getClientID()+" to "+clientBean.getClientList());
					}
				}else if(this.clientBean.getMessageMode().equalsIgnoreCase("blockcast")){
    					for(String client: clients){
    						if(!client.equalsIgnoreCase(getClientBean().getClientID()) && !(getClientBean().getClientList().contains(client))){
    							ObjectOutputStream tempout = connMap.get(client);
    							tempout.writeObject(getClientBean());
    							tempout.flush();
    						}
					}
					String messageContent = (clientBean.isFile())?"file" : "message";
					System.out.println(clientBean.getMessageMode()+" "+messageContent+" sent by Client " + clientBean.getClientID()+" except "+clientBean.getClientList());
    				}
    			}catch(IOException e){
    				System.out.println("Disconnect with Client " + clientBean.getClientID() );
				e.printStackTrace();
    				try{
    					connMap.remove(clientBean.getClientID(),out);
    					in.close();
    					out.flush();
    					out.close();
    				}catch(IOException ioException){
    					System.out.println("Disconnect with Client " + clientBean.getClientID() );
					ioException.printStackTrace();
    				}
    			}
     	   	}
        }
}

