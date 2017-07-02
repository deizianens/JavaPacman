package game;
/**
 *Listener que será notificado caso o Pacman coma algo
 */
public interface FoodListener {

    /**
     * Método chamado quando Pacman come alguma comida
     */
    public void consumed(GameState.Food food);
}