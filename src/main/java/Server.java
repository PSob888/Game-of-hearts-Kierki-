import java.io.IOException;
import java.lang.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Scanner;

/**Class representing server
 *
 */
public class Server {

    private ServerSocket serverSocket;

    /**Constructor
     *
     * @param serverSocket
     */
    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    /**function that starts server
     *
     */
    public void startServer(){
        System.out.println("Server Started");
        commandListener();
        try{
            while(!serverSocket.isClosed()){

                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch(IOException e){
            closeServer();
            System.out.println("Server Stopped");
        }
    }

    /**Function that closes server
     *
     */
    public void closeServer() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.out.println("Wystapil nieoczekiwany blad");
        }
    }

    /**Server command listener
     *
     */
    public void commandListener(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ClientHandler clientHandler = new ClientHandler();
                Scanner scanner = new Scanner(System.in);
                while(!serverSocket.isClosed()){
                    try{
                        String command = scanner.nextLine();
                        commandAnalyzer(command, clientHandler);
                    } catch (IOException e) {
                        break;
                    }
                }
            }
        }).start();
    }

    /**Server command analyzer
     *
     * @param command
     * @param cH
     * @throws IOException
     */
    public void commandAnalyzer(String command, ClientHandler cH) throws IOException {
        String[] commandSplit = command.split(" ",2);
        switch (commandSplit[0]) {
            case "list" -> listRooms(cH);
            case "stop" -> stopRoom(Integer.parseInt(commandSplit[1]), cH);
            default -> System.out.println("Nie ma takiej komendy");
        }
    }

    /**server lists rooms
     *
     * @param cH
     */
    public void listRooms(ClientHandler cH){
        String list = cH.adminGetListString();
        System.out.println(list);
    }

    /**server stops room
     *
     * @param number
     * @param cH
     * @throws IOException
     */
    public void stopRoom(int number, ClientHandler cH) throws IOException {
        cH.adminStopRoom(number);
        System.out.println("Zatrzymano pokój " + number);
    }

    /**Server main
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        Server server = new Server(serverSocket);
        server.startServer();
    }
}

