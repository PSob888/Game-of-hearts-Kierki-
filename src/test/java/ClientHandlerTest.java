import static org.junit.jupiter.api.Assertions.*;

class ClientHandlerTest {

    @org.junit.jupiter.api.Test
    void checkIfPlayerHasCard1() {
        ClientHandler cH = new ClientHandler();
        cH.getPlayerCards().add(new Card("2k"));
        cH.getPlayerCards().add(new Card("3t"));
        cH.getPlayerCards().add(new Card("4s"));
        cH.getPlayerCards().add(new Card("5p"));
        assertFalse(cH.checkIfPlayerHasCard("5k"));
    }

    @org.junit.jupiter.api.Test
    void checkIfPlayerHasCard2() {
        ClientHandler cH = new ClientHandler();
        cH.getPlayerCards().add(new Card("2k"));
        cH.getPlayerCards().add(new Card("3t"));
        cH.getPlayerCards().add(new Card("4s"));
        cH.getPlayerCards().add(new Card("5p"));
        assertTrue(cH.checkIfPlayerHasCard("2k"));
    }
}