import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**Class representing client
 *
 */
public class Client {

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String name;

    /**Constructor
     *
     * @param socket
     * @param name
     */
    public Client(Socket socket, String name) {
        try{
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.name = name;
            bufferedWriter.write(this.name);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything(socket, bufferedWriter, bufferedReader);
        }
    }

    /**function that sends commands to server
     *
     */
    public void sendToServer(){
        try {
            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                String messageToSend = scanner.nextLine();
                bufferedWriter.write(messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            closeEverything(socket, bufferedWriter, bufferedReader);
        }
    }

    /**function that listens for server response
     *
     */
    public void listenForMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String messageFromServer;
                while(socket.isConnected()){
                    try{
                        messageFromServer=bufferedReader.readLine();
                        System.out.println(messageFromServer);
                    } catch (IOException e) {
                        closeEverything(socket, bufferedWriter, bufferedReader);
                        break;
                    }
                }
            }
        }).start();
    }

    /**function that listen for server lesponse about login
     *
     * @return boolean
     */
    public boolean listenForLogin(){
        String messageFromServer;
        try{
            messageFromServer=bufferedReader.readLine();
            if(messageFromServer.equals("no login")){
                return false;
            }
        } catch (IOException e) {
            closeEverything(socket, bufferedWriter, bufferedReader);
        }
        return true;
    }

    /**function that closes everything
     *
     * @param socket
     * @param bufferedWriter
     * @param bufferedReader
     */
    public void closeEverything(Socket socket, BufferedWriter bufferedWriter, BufferedReader bufferedReader){
        System.out.println("Serwer nie odpowiada");
        try{
            if(bufferedReader != null){
                bufferedReader.close();
            }
            if(bufferedWriter != null){
                bufferedWriter.close();
            }
            if(socket != null){
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Wystapil nieoczekiwany blad");
        }
    }

    /**Client main
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        System.out.println("Podaj login");
        Scanner scanner = new Scanner(System.in);
        String name = scanner.nextLine();
        try {
            Client client;
            while (true) {
                Socket socket = new Socket("localhost", 1234);
                client = new Client(socket, name);
                if(client.listenForLogin()){
                    break;
                }
                else{
                    client.closeEverything(socket, client.getBufferedWriter(), client.getBufferedReader());
                    System.out.println("Osoba z takim loginem jest juz zalogowana, podaj inny");
                    name = scanner.nextLine();
                }
            }
            client.listenForMessage();
            client.sendToServer();
        }catch (IOException e){
            System.out.println("Brak polaczenia z serwerem");
        }
    }

    /**Bufferedreader getter
     *
     * @return bufferedreader
     */
    public BufferedReader getBufferedReader() {
        return bufferedReader;
    }

    /**Buffered writer gettter
     *
     * @return bufferedwriter
     */
    public BufferedWriter getBufferedWriter() {
        return bufferedWriter;
    }
}
