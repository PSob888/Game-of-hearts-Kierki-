import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**Class representing client handler on a server
 *
 */
public class ClientHandler implements Runnable{

    public static final int MAX_PLAYERS = 4;
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    public static ArrayList<Room> rooms = new ArrayList<>();
    private String name;
    private int roomNumber = 0;
    private int myTurn = 0;
    private int invited = 0;
    private int points = 0;
    private ArrayList<Card> playerCards = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    /**Constructor
     *
     * @param socket
     */
    public ClientHandler(Socket socket) {
        try{
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String tempName = bufferedReader.readLine();
            while(checkForName(tempName)){
                sendToUser("no login");
                throw new IOException();
            }
            this.name = tempName;
            clientHandlers.add(this);
            System.out.println("Client " + this.name + " Connected");
            sendToUser("login");
            sendToUser("Połączono z serwerem");
            sendToUser("Dostepne komendy: create, join <numer pokoju>, leave, list, invite <nazwa uzytkownika>, chat <wiadomosc>(tylko w pokoju), play <karta>(tylko w pokoju)");
            sendToUser("Wytłumaczenie kart: t - trefl, k - karo, s - kier, p - pik, 11 - Walet, 12 - Dama, 13 - Król, 14 - As");
        } catch (IOException e) {
            closeEverything(socket, bufferedWriter, bufferedReader, false);
        }
    }

    /**Constructor for admin user
     *
     */
    public ClientHandler(){
        roomNumber = 0;
        invited = 0;
        points = 0;
        myTurn = 0;
    }

    /**Creates a room
     *
     * @throws IOException
     */
    public void createRoom() throws IOException {
        if(this.roomNumber == 0){
            roomNumber = checkForRoomNumber();
            Room room = new Room(this, roomNumber);
            rooms.add(room);
            sendToUser("Stworzono pokój o numerze " + roomNumber);
            System.out.println("Client " + this.name + " created room" + roomNumber);
        }
        else{
            sendToUser("Jesteś już w pokoju " + roomNumber + ", aby założyć nowy musisz z niego wyjść");
        }
    }

    /**Chceck for available room number
     *
     * @return avaiable room number
     */
    public int checkForRoomNumber(){
        int number = 1;
        int checker = 0;

        while(true){
            for(Room room : rooms){
                if(number == room.getRoomNumber())
                    checker = 1;
            }
            if(checker == 0)
                break;
            else
                number++;
            checker = 0;
        }

        return number;
    }

    /**Player joins the room
     *
     * @param number
     * @throws IOException
     */
    public void joinRoom(int number) throws IOException {
        if(this.roomNumber == 0){
            int roomIndex = getRoomIndex(number);
            if(roomIndex != -1){
                if(rooms.get(roomIndex).getNumberOfPlayers() != MAX_PLAYERS){
                    roomNumber = number;
                    rooms.get(roomIndex).roomAddPlayer(this);
                    sendToUser("Dolaczono do pokoju " + number);
                    System.out.println("Client " + this.name + " joined room" + roomNumber);
                    sendToOthersInRoom(name + " dołączył do pokoju");
                    if(rooms.get(roomIndex).getNumberOfPlayers() == MAX_PLAYERS){
                        rooms.get(roomIndex).startGame();
                    }
                }
                else{
                    sendToUser("Ten pokoj jest juz pelny");
                }
            }
            else{
                sendToUser("Nie ma takiego pokoju");
            }
        }
        else{
            sendToUser("Jesteś już w pokoju " + roomNumber + ", aby dolaczyc do innego musisz z niego wyjść");
        }
    }

    /**Player leaves the room
     *
     * @throws IOException
     */
    public void leaveRoom() throws IOException {
        int index = getRoomIndex(roomNumber);
        if(index != -1){
            rooms.get(index).roomRemovePlayer(this);
            System.out.println("Client " + this.name + " leaved room" + roomNumber);
            sendToUser("Wyszedłeś z pokoju " + roomNumber);
            if(rooms.get(index).getNumberOfPlayers() == 0){
                roomClose(rooms.get(index));
            }
            else{
                sendToOthersInRoom(name + " wyszedł z pokoju");
            }
            roomNumber = 0;
            points = 0;
            playerCards.clear();
            myTurn = 0;
        }
        else{
            sendToUser("Nie jestes w zadnym pokoju");
        }
    }

    /**Room is being closed
     *
     * @param room
     */
    public void roomClose(Room room){
        rooms.remove(room);
    }

    /**Player invites another player to join the room
     *
     * @param name
     * @throws IOException
     */
    public void invitePlayer(String name) throws IOException {
        if(checkForName(name)){
            for(ClientHandler clientHandler : clientHandlers){
                if(clientHandler.getName().equals(name)){
                    if(clientHandler.getRoomNumber() == 0){
                        clientHandler.setInvited(roomNumber);
                        clientHandler.getBufferedWriter().write(name + " Zaprosil cie do pokoju, zaakceptowac? (yes/no)");
                        clientHandler.getBufferedWriter().newLine();
                        clientHandler.getBufferedWriter().flush();
                    }
                }
            }
            sendToUser("Zaproszono gracza " + name);
        }
        else{
            sendToUser("Nie ma takiego uzytkownika");
        }
    }

    /**Player accepts invitation
     *
     * @throws IOException
     */
    public void acceptInvite() throws IOException {
        if(invited != 0){
            joinRoom(invited);
            invited = 0;
        }
        else{
            sendToUser("Nikt cie nie zaprosił");
        }
    }

    /**Player declines invitation
     *
     * @throws IOException
     */
    public void declineInvite() throws IOException {
        if(invited != 0){
            invited = 0;
            sendToUser("Odrzuciłeś zaproszenie");
        }
        else{
            sendToUser("Nikt cie nie zaprosił");
        }
    }

    /**Player sends a message in chat
     *
     * @param message
     * @throws IOException
     */
    public void chatInRoom(String message) throws IOException {
        if(roomNumber != 0){
            sendToOthersInRoom(name + ": " + message);
        }
        else{
            sendToUser("Czatować z innymi można tylko w pokoju");
        }
    }

    /**player plays a card
     *
     * @param card
     * @throws IOException
     */
    public void playCard(String card) throws IOException {
        if(myTurn == 0){
            sendToUser("To nie twoja kolej na gre");
        }
        else{
            if(checkIfPlayerHasCard(card)){
                if(checkIfIsColorMatched(card)){
                    sendCardToRoom(card);
                    myTurn = 0;
                }
                else{
                    if(checkIfPlayerHasColorMatched()){
                        sendToUser("Masz karte tego koloru, musisz ją rzucić");
                    }
                    else{
                        sendCardToRoom(card);
                        myTurn = 0;
                    }
                }
            }
            else{
                sendToUser("Nie masz takiej karty sprobuj jeszcze raz");
            }
        }
    }

    /**Sending card to room
     *
     * @param card
     * @throws IOException
     */
    private void sendCardToRoom(String card) throws IOException {
        int roomIndex = getRoomIndex(roomNumber);
        Card c = new Card(card);
        removeCardFromPlayersDeck(c);
        rooms.get(roomIndex).playerPlayedCard(c);
    }

    /**Checks if player has card of the same color
     *
     * @return if player has card of the same color boolean
     */
    public boolean checkIfPlayerHasColorMatched(){
        int roomIndex = getRoomIndex(roomNumber);
        for(Card c : playerCards){
            if(rooms.get(roomIndex).getCardsPile().get(0).getColor().equals(c.getColor())){
                return true;
            }
        }
        return false;
    }

    /**Checks if cards has the same colors
     *
     * @param card
     * @return if card is color matched boolean
     */
    public boolean checkIfIsColorMatched(String card){
        Card cardToCheck = new Card(card);
        int roomIndex = getRoomIndex(roomNumber);
        if(rooms.get(roomIndex).getCardsPile().size() == 0)
            return true;
        else if(rooms.get(roomIndex).getCardsPile().get(0).getColor().equals(cardToCheck.getColor()))
            return true;
        return false;
    }

    /**Check if player has this card
     *
     * @param card
     * @return if player has card boolean
     */
    public boolean checkIfPlayerHasCard(String card){
        try{
            Card cardToCheck = new Card(card);
            for(Card c : playerCards){
                if(c.equals(cardToCheck)){
                    return true;
                }
            }
        }
        catch(RuntimeException e){
            return false;
        }

        return false;
    }

    /**Removes card from players deck
     *
     * @param card
     */
    public void removeCardFromPlayersDeck(Card card){
        for(Card c : playerCards){
            if(c.equals(card)){
                playerCards.remove(c);
                return;
            }
        }
    }

    /**Shows all rooms to player
     *
     * @throws IOException
     */
    public void listAllRooms() throws IOException {
        for(Room room : rooms){
            sendToUser(room.printRoom());
        }
    }

    /**Checks for player names
     *
     * @param name
     * @return player exist boolean
     */
    public boolean checkForName(String name){
        for(ClientHandler clientHandler : clientHandlers){
            if(clientHandler.getName().equals(name)){
                return true;
            }
        }
        return false;
    }

    /**Sends a message to user
     *
     * @param message
     * @throws IOException
     */
    public void sendToUser(String message) throws IOException {
        bufferedWriter.write(message);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

    /**Sends a message to other users in room
     *
     * @param message
     * @throws IOException
     */
    public void sendToOthersInRoom(String message) throws IOException {
        int index = getRoomIndex(roomNumber);
        ArrayList<ClientHandler> cHandlers = rooms.get(index).getClientHandlers();
        for(ClientHandler cH : cHandlers){
            if(cH != this){
                cH.getBufferedWriter().write(message);
                cH.getBufferedWriter().newLine();
                cH.getBufferedWriter().flush();
            }
        }
    }

    /**Analyzes player commands
     *
     * @param fromClient
     * @throws IOException
     */
    public void commandAnalyzer(String fromClient) throws IOException {
        String[] clientSplit = fromClient.split(" ",2);
        switch (clientSplit[0]) {
            case "create" -> createRoom();
            case "join" -> joinRoom(Integer.parseInt(clientSplit[1]));
            case "list" -> listAllRooms();
            case "invite" -> invitePlayer(clientSplit[1]);
            case "leave" -> leaveRoom();
            case "chat" -> chatInRoom(clientSplit[1]);
            case "play" -> playCard(clientSplit[1]);
            case "yes" -> acceptInvite();
            case "no" -> declineInvite();
            default -> sendToUser("Nie ma takiej komendy");
        }
    }

    /**Run function - Thread
     *
     */
    @Override
    public void run() {
        String fromClient;

        while(socket.isConnected()){
            try{
                fromClient=bufferedReader.readLine();
                commandAnalyzer(fromClient);
            } catch (IOException e) {
                closeEverything(socket, bufferedWriter, bufferedReader, true);
                break;
            }
        }
    }

    /**Closes every connection
     *
     * @param socket
     * @param bufferedWriter
     * @param bufferedReader
     * @param showMessage
     */
    public void closeEverything(Socket socket, BufferedWriter bufferedWriter, BufferedReader bufferedReader, boolean showMessage){
        if(showMessage){
            System.out.println("Client " + this.name + " Disconnected");
        }

        try{
            if(roomNumber != 0){
                leaveRoom2();
            }
            if(bufferedReader != null){
                bufferedReader.close();
            }
            if(bufferedWriter != null){
                bufferedWriter.close();
            }
            if(socket != null){
                socket.close();
            }
            clientHandlers.remove(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**Admin function to stop room
     *
     * @param number
     * @throws IOException
     */
    public void adminStopRoom(int number) throws IOException {
        int index = getRoomIndex(number);
        rooms.get(index).endGameRandomly();
    }

    /**Admin function to get printable room list
     *
     * @return room list in string
     */
    public String adminGetListString(){
        String s ="";
        for(Room room : rooms){
            s += room.printRoom() + "\n";
        }
        return s;
    }

    /**Gets room index from room number
     *
     * @param number
     * @return roomIndex
     */
    public int getRoomIndex(int number){
        int i=0;
        for(Room room : rooms){
            if(room.getRoomNumber()==number)
                return i;
            else i++;
        }
        return -1;
    }

    /**Gets user points
     *
     * @return points
     */
    public int getPoints() {
        return points;
    }

    /**Sets user points
     *
     * @param points
     */
    public void setPoints(int points) {
        this.points = points;
    }

    /**Sets user turn
     *
     * @param myTurn
     */
    public void setMyTurn(int myTurn) {
        this.myTurn = myTurn;
    }

    /**Sets user invited
     *
     * @param invited
     */
    public void setInvited(int invited) {
        this.invited = invited;
    }

    /**Gets user bufferedwriter
     *
     * @return bufferedWriter
     */
    public BufferedWriter getBufferedWriter() {
        return bufferedWriter;
    }

    /**Gets user roomnumber
     *
     * @return roomNumber
     */
    public int getRoomNumber() {
        return roomNumber;
    }

    /**Gets user cards
     *
     * @return playerCards
     */
    public ArrayList<Card> getPlayerCards() {
        return playerCards;
    }

    /**Gets user name
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**Leaves room 2
     *
     * @throws IOException
     */
    public void leaveRoom2() throws IOException {
        int index = getRoomIndex(roomNumber);
        if(index != -1){
            rooms.get(index).roomRemovePlayer(this);
            sendToOthersInRoom( name + " wyszedł z pokoju");
            System.out.println("Client " + this.name + " leaved room" + roomNumber);
            roomNumber = 0;
        }
    }
}
