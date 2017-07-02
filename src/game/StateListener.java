package game;

/**
 * Esse listener será notificado toda vez que houver uma mudança no estado do jogo
 * As mudanças são:
 * Rodada Ganha
 * Perda de Vidas
 * Game-Over
 */
public interface StateListener {

    /**
     * Estados possíveis do jogo
     */
    public enum States{
        ROUND_WON, LIVE_LOST, GAME_OVER
    }

    /**
     Método chamado quando há mudança de estado
     */
    public void stateChanged(States state);
}
