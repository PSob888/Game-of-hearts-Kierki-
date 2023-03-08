/**Class that represents single card
 *
 */
public class Card {
    private String color; //trefl(t) - ta taka koniczynka, karo - kopnięty kwadrat(k), kier(s) - serce, pik(p) - czarne serce
    private int number;

    /**Constructor
     *
     * @param color
     * @param number
     */
    public Card(String color, int number){
        this.color=color;
        this.number=number;
    }

    /**Constructor to make card from string
     *
     * @param card
     */
    public Card(String card){
        try{
            String n = card.substring(0, card.length()-1);
            String c = card.substring(card.length()-1);

            this.color=c;
            this.number=Integer.parseInt(n);
        }
        catch (NumberFormatException e){
            throw new RuntimeException();
        }
    }

    /**Getter color
     *
     * @return color of card
     */
    public String getColor() {
        return color;
    }

    /**Getter number
     *
     * @return number of card
     */
    public int getNumber() {
        return number;
    }

    /**Card printer
     *
     * @return card in string
     */
    public String print(){
        return number + color;
    }

    /**Function to check if card equals another card
     *
     * @param o
     * @return is card equal boolean
     */
    public boolean equals(Card o) {
        if(this.number == o.getNumber()){
            if(this.color.equals(o.getColor()))
                return true;
        }
        return false;
    }
}
