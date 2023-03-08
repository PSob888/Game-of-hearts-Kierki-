import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**Class that represents room
 *
 */
public class Room{
    public static final int MAX_PLAYERS = 4;
    public static final int MAX_CARDS_AMOUNT = 13;
    public static final int LOWEST_CARD = 2;
    public static final int HIGHEST_CARD = 15;
    public static final int STOP_GAME_AMOUNT = 3;
    public static final int FOUR = 4;
    public static final int BEZ_LEW = 1;
    public static final int BEZ_KIEROW = 2;
    public static final int BEZ_DAM = 3;
    public static final int BEZ_PANOW = 4;
    public static final int BEZ_KROLA_KIER = 5;
    public static final int BEZ_SIO_LEWY = 6;
    public static final int ROZBOJNIK = 7;
    public static final int DAMA = 12;
    public static final int WALET = 11;
    public static final int KROL = 13;
    public static final int AS = 14;
    public ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private int roomNumber;
    private ArrayList<Card> cards = new ArrayList<>();
    private ArrayList<Card> cardsPile = new ArrayList<>();
    private ArrayList<Integer> roundsList = new ArrayList<>();
    private int counter = 0;
    private int countToFour = 0;
    private int lewCounter = 0;
    private int currentRoundIndex = 0;

    /**Constructor
     *
     * @param clientHandler
     * @param roomNumber
     */
    public Room(ClientHandler clientHandler, int roomNumber){ //dopisac try w create room
        try{
            clientHandlers.add(clientHandler);
            this.roomNumber = roomNumber;
            cardDeckMaker();
            XMLRoundsParse();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    /**Funtion that plays player card
     *
     * @param card
     * @throws IOException
     */
    public void playerPlayedCard(Card card) throws IOException {
        cardsPile.add(card);
        counter++;
        countToFour++;
        if(countToFour == FOUR){ // obliczanie wyniq
            lewCounter++;
            playersAddWynik(roundsList.get(currentRoundIndex));
            playersShowWyniks();
            if(lewCounter == 13){
                currentRoundIndex++;
                lewCounter = 0;
                if(currentRoundIndex == 7){
                    endGameNormaly();
                    return;
                }
                else{
                    showRoundType();
                }
            }
            sendMessageToAllPlayers("Położone karty: " + printCards(cardsPile));
            cardsPile.clear();
        }
        sendMessageToAllPlayers("Położone karty: " + printCards(cardsPile));
        whosTurn();
        showPlayersTheirCards();
    }

    /**Function that calculates wynik
     *
     * @param switchValue
     */
    public void playersAddWynik(int switchValue){
        int loserIndex = searchForLoserIndex();
        counter = loserIndex;
        switch (switchValue){
            case BEZ_LEW:{
                int p = clientHandlers.get(loserIndex).getPoints();
                p = p - 20;
                clientHandlers.get(loserIndex).setPoints(p);
                break;
            }
            case BEZ_KIEROW:{
                for(Card c : cardsPile){
                    if(c.getColor().equals("s")){
                        int p = clientHandlers.get(loserIndex).getPoints();
                        p = p - 20;
                        clientHandlers.get(loserIndex).setPoints(p);
                    }
                }
                break;
            }
            case BEZ_DAM:{
                for(Card c : cardsPile){
                    if(c.getNumber() == DAMA){
                        int p = clientHandlers.get(loserIndex).getPoints();
                        p = p - 60;
                        clientHandlers.get(loserIndex).setPoints(p);
                    }
                }
                break;
            }
            case BEZ_PANOW:{
                for(Card c : cardsPile){
                    if(c.getNumber() == WALET || c.getNumber() == KROL){
                        int p = clientHandlers.get(loserIndex).getPoints();
                        p = p - 30;
                        clientHandlers.get(loserIndex).setPoints(p);
                    }
                }
                break;
            }
            case BEZ_KROLA_KIER:{
                for(Card c : cardsPile){
                    if(c.getColor().equals("s") && c.getNumber() == KROL){
                        int p = clientHandlers.get(loserIndex).getPoints();
                        p = p - 150;
                        clientHandlers.get(loserIndex).setPoints(p);
                    }
                }
                break;
            }
            case BEZ_SIO_LEWY:{
                if(lewCounter == 7 || lewCounter == 13){
                    int p = clientHandlers.get(loserIndex).getPoints();
                    p = p - 150;
                    clientHandlers.get(loserIndex).setPoints(p);
                }
                break;
            }
            case ROZBOJNIK:{
                for(int i = 0; i < 6; i++){
                    playersAddWynik(i);
                }
                break;
            }

        }
    }

    /**Function to show wyniks
     *
     * @throws IOException
     */
    public void playersShowWyniks() throws IOException {
        for(ClientHandler clientHandler : clientHandlers){
            sendMessageToAllPlayers("Gracz " + clientHandler.getName() + ": " + clientHandler.getPoints() + "pkt");
        }
    }

    /**Funtion to search for loser index in playerlist
     *
     * @return index of loser
     */
    public int searchForLoserIndex(){
        Card startingCard = cardsPile.get(0);
        int index = getIndexOfWorstCard(startingCard);
        int playerIndex = counter - (MAX_PLAYERS - index);
        return playerIndex;
    }

    /**function to get the index of worst card in pile
     *
     * @param card
     * @return index of worst card
     */
    public int getIndexOfWorstCard(Card card){
        int index = 0;
        for(Card c : cardsPile){
            if(c.getColor().equals(card.getColor())){
                if(c.getNumber() > card.getNumber())
                    return index;
            }
            index++;
        }
        return 0;
    }


    /**function to start a game
     *
     * @throws IOException
     */
    public void startGame() throws IOException {
        cardsToPlayers();
        //wysylanie wiadomosci o starcie gry itp
        sendMessageToAllPlayers("Rozpoczęto gre");
        showRoundType();
        showPlayersTheirCards();
        whosTurn();
        counter = 0;
        countToFour = 0;
    }

    /**Funtion that show current round type
     *
     * @throws IOException
     */
    public void showRoundType() throws IOException {
        switch(roundsList.get(currentRoundIndex)){
            case 1 -> sendMessageToAllPlayers("Obecne rozdanie to: Bez lew");
            case 2 -> sendMessageToAllPlayers("Obecne rozdanie to: Bez kierow");
            case 3 -> sendMessageToAllPlayers("Obecne rozdanie to: Bez dam");
            case 4 -> sendMessageToAllPlayers("Obecne rozdanie to: Bez panow");
            case 5 -> sendMessageToAllPlayers("Obecne rozdanie to: Bez krola kier");
            case 6 -> sendMessageToAllPlayers("Obecne rozdanie to: Bez siodmej i ostatniej lewy");
            case 7 -> sendMessageToAllPlayers("Obecne rozdanie to: Rozbojnik");
        }
    }

    /**Funtion that ends game randomly
     *
     * @throws IOException
     */
    public void endGameRandomly() throws IOException {
        playersRemoveAllCards();
        playersShowRandomScore();
        counter = 0;
        countToFour = 0;
        lewCounter = 0;
        currentRoundIndex = 0;
        cardsPile.clear();
        cards.clear();
        cardDeckMaker();
        if(clientHandlers.size() == MAX_PLAYERS){
            sendMessageToAllPlayers("Koniec gry, zaraz rozpocznie się kolejna");
            startGame();
        }
    }

    /**function that ends game normally
     *
     * @throws IOException
     */
    public void endGameNormaly() throws IOException {
        sendMessageToAllPlayers("Koniec gry, zaraz rozpocznie się kolejna");
        playersRemoveAllCards();
        counter = 0;
        countToFour = 0;
        lewCounter = 0;
        currentRoundIndex = 0;
        cardsPile.clear();
        cards.clear();
        cardDeckMaker();
        startGame();
    }

    /**function that removes all cards from players
     *
     */
    public void playersRemoveAllCards(){
        for(ClientHandler cH : clientHandlers){
            cH.getPlayerCards().clear();
        }
    }

    /**function that show random score to players
     *
     * @throws IOException
     */
    public void playersShowRandomScore() throws IOException {
        sendMessageToAllPlayers("Gra została przerwana, wynik gry jest losowy");
        Random random = new Random();
        for(ClientHandler cH : clientHandlers){
            int p = random.nextInt(500) - 500;
            sendMessageToAllPlayers("Gracz " + cH.getName() + ": " + p + " pkt");
        }
    }

    /**function to calculate whos turn
     *
     * @throws IOException
     */
    private void whosTurn() throws IOException {
        String name = clientHandlers.get(counter % 4).getName();
        clientHandlers.get(counter % 4).setMyTurn(1);
        sendMessageToAllPlayers("Kolej gracza " + name);
    }

    /**function to send message to all players in room
     *
     * @param message
     * @throws IOException
     */
    private void sendMessageToAllPlayers(String message) throws IOException {
        for(ClientHandler clientHandler : clientHandlers){
            clientHandler.sendToUser(message);
        }
    }

    /**function to show players their cards
     *
     * @throws IOException
     */
    private void showPlayersTheirCards() throws IOException {
        for(ClientHandler clientHandler : clientHandlers){
            clientHandler.sendToUser("Twoje karty: " + printCards(clientHandler.getPlayerCards()));
        }
    }

    /**function to send cards to players
     *
     */
    private void cardsToPlayers() {
        int cardIndex = 0; // rozdanie graczom kart
        for(ClientHandler clientHandler : clientHandlers){
            for(int i = 0 ;i < MAX_CARDS_AMOUNT; i++){
                clientHandler.getPlayerCards().add(cards.get(cardIndex));
                cardIndex++;
            }
        }
    }

    /**function to parse round order from config.xml
     *
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    private void XMLRoundsParse() throws ParserConfigurationException, IOException, SAXException {
        File file = new File("config.xml");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("rounds");
        for (int temp = 0; temp < nList.getLength(); temp++){
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                roundsList.add(Integer.parseInt(eElement.getElementsByTagName("round1").item(0).getTextContent()));
                roundsList.add(Integer.parseInt(eElement.getElementsByTagName("round2").item(0).getTextContent()));
                roundsList.add(Integer.parseInt(eElement.getElementsByTagName("round3").item(0).getTextContent()));
                roundsList.add(Integer.parseInt(eElement.getElementsByTagName("round4").item(0).getTextContent()));
                roundsList.add(Integer.parseInt(eElement.getElementsByTagName("round5").item(0).getTextContent()));
                roundsList.add(Integer.parseInt(eElement.getElementsByTagName("round6").item(0).getTextContent()));
                roundsList.add(Integer.parseInt(eElement.getElementsByTagName("round7").item(0).getTextContent()));
            }
        }
    }

    /**function to print room
     *
     * @return string
     */
    public String printRoom(){
        String temp = "";
        for(ClientHandler clientHandler : clientHandlers){
            temp += clientHandler.getName() + ", ";
        }
        String names = temp.substring(0, temp.length()-2);
        int numOfPlayers = getNumberOfPlayers();
        String gra = "";
        if(numOfPlayers == MAX_PLAYERS)
            gra = ", GRA W TOKU";
        else
            gra = ", OCZEKIWANIE NA GRACZY";
        return roomNumber + " Graczy " + numOfPlayers + "/4, " + names + gra;
    }

    /**function to print cards
     *
     * @param cardsp
     * @return String of cards
     */
    public String printCards(ArrayList<Card> cardsp){
        String result = "";

        for(Card card : cardsp)
        {
            result+=card.print();
            result+=", ";
        }
        return result;
    }

    /**function to make card deck
     *
     */
    public void cardDeckMaker(){
        ArrayList<String> temp = new ArrayList<>();
        temp.add("t"); //trefl
        temp.add("k"); //karo
        temp.add("s"); //serce
        temp.add("p"); //pik
        ArrayList<Integer> tempNumbers = new ArrayList<>();
        for(int i = LOWEST_CARD; i< HIGHEST_CARD; i++){
            tempNumbers.add(i);
        }
        for(String color : temp){
            for(int number : tempNumbers){
                Card card = new Card(color, number);
                cards.add(card);
            }
        }
        Collections.shuffle(cards);
    }

    /**function to make round list
     *
     */
    public void roundsListMaker(){
        for(int i=1;i<8;i++)
            roundsList.add(i);
    }

    /**Getter carddsPile
     *
     * @return cardspile
     */
    public ArrayList<Card> getCardsPile() {
        return cardsPile;
    }

    /**function that add player to room
     *
     * @param clientHandler
     * @throws IOException
     */
    public void roomAddPlayer(ClientHandler clientHandler) throws IOException {
        clientHandlers.add(clientHandler);
    }

    /**function that removes player from room
     *
     * @param clientHandler
     * @throws IOException
     */
    public void roomRemovePlayer(ClientHandler clientHandler) throws IOException {
        clientHandlers.remove(clientHandler);
        if(clientHandlers.size() == STOP_GAME_AMOUNT){
            endGameRandomly();
        }
    }

    /**function that gets number of players
     *
     * @return size of clienthandlers
     */
    public int getNumberOfPlayers(){
        return clientHandlers.size();
    }

    /**Getter roomNumber
     *
     * @return roomnumber
     */
    public int getRoomNumber() {
        return roomNumber;
    }

    /**Getter clienHandlers
     *
     * @return clientandlers arraylist
     */
    public ArrayList<ClientHandler> getClientHandlers() {
        return clientHandlers;
    }


}
