package game;

/**
 * Descreve um objeto que irá se mover
 * Quando o jogo está pausado/congelado o evento não é chamado
 */
public interface MovementEvent {

    public void move();
}