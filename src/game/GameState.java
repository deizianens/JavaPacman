package game;

import characther.Pacman;
import map.ChunkedMap;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A classe guarda informações sobre o estado atual do jogo como pontos e vidas.
 * A classe tem multiplos listeners, que vão notificar os observadores
 * O FoodLister será notificado quando qualquer tipo de "comida" for pega pelo Pacman (inclui pontos, bolas(poderes) ou frutas bonus)
 * O StateListener sera notificado se houver uma mudança de estado
 */
public enum GameState implements RenderEvent, StateListener {
    INSTANCE;

    public static final int MAP_SPACER = 40; //Pixels necessarios para desenhar o estado 
    private static final Font SCORE_FONT = new Font("Arial", Font.BOLD, 18);
    private static final Font PAUSE_FONT = new Font("Arial", Font.BOLD | Font.ITALIC, 18);
    public static final Color BONUS_POINTS_COLOR = new Color(54, 149, 131);
    public static final Font BONUS_POINTS_FONT = new Font("Arial", Font.BOLD, 12);

    private static final int EATABLE_ITEMS = 253; //número de itens a serem comidos
    private int food_eaten; 

    private int score;
    private int lives;
    private int last_bonus_points; //número de pontos do ultimo item bonus comido. Resetado após ser mostrado na tela
    
    public enum Food{
        POINT(10), BALL(50), BONUS(100);
        
        private final int points;
        private Food(int points){
            this.points = points;
        }
    }
    
    private List<StateListener> stateListeners;
    private List<FoodListener> foodListeners;

    private boolean game_over;

    /**
     * Padrão Singleton utilizado para garantir apenas uma instancia da classe
     */
    private GameState(){
        game_over = false;
        this.score = 0;
        this.lives = 2;
        this.food_eaten = 0;
        stateListeners = new ArrayList<StateListener>(2);
        foodListeners = new ArrayList<FoodListener>(2);
        addStateListener(this);
    }

    @Override
    public void stateChanged(States state) {
        if (state == States.GAME_OVER){
            game_over = true;
            GameLoop.INSTANCE.freeze();
            new java.util.Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    //reseta todos os parametros
                    food_eaten = 0;
                    score = 0;
                    lives = 2;
                    game_over = false;
                    GameLoop.INSTANCE.play();
                }
            }, 2 * 1000);
        } else if (state == States.ROUND_WON){
            //nivel ganho, reseta comida
            food_eaten = 0;
        }
    }

    @Override
    public void render(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(SCORE_FONT);
        g.drawString("SCORE", 40, 35);
        g.drawString(score+"", 110, 35);
        for (int i = 0; i < getLivesLeft(); i++){
            g.setColor(Pacman.BODY_COLOR);
            g.fillOval((400+30*i), 20, 20, 20);
            g.setColor(ChunkedMap.BACKGROUND_COLOR);
            g.fillArc((400+30*i), 20, 20, 20, 75+90, 30);
        }
        if (GameLoop.INSTANCE.isPaused()){
            g.setColor(Pacman.BODY_COLOR);
            g.setFont(PAUSE_FONT);
            g.drawString("READY!", 195, 332);
        } else if (game_over){
            g.setColor(Pacman.BODY_COLOR);
            g.setFont(PAUSE_FONT);
            g.drawString("GAME OVER...", 165, 332);
        }
        if (last_bonus_points != 0){
            g.setColor(BONUS_POINTS_COLOR);
            g.setFont(BONUS_POINTS_FONT);
            g.drawString(last_bonus_points+"", 215, 328);
        }
    }

    public void removeLive(){
        this.lives --;
        //notifica listeners de vida perdida
        if (this.lives == -1){
            for (StateListener listener : stateListeners)
                listener.stateChanged(StateListener.States.GAME_OVER);
        } else {
            for (StateListener listener : stateListeners)
                listener.stateChanged(StateListener.States.LIVE_LOST);
        }
    }

  
    public int getLivesLeft(){
        return this.lives;
    }

    public int getScore(){
        return this.score;
    }

    /**
    *Método utilizado caso um ghost tenha sido comido
    * o combo é um numero de 0 a 4, que indica quantos ghosts foram comidos
    */
    public void addKill(int combo){
        if (combo < 1 || combo > 4) return;
        this.score += combo * 400;
    }

    public void addScore(Food consumed){
        this.score += consumed.points;
        
        if (consumed != Food.BONUS)
            this.food_eaten++;
        if (food_eaten == EATABLE_ITEMS){
            for (StateListener listener : stateListeners)
                listener.stateChanged(StateListener.States.ROUND_WON);
            return;
        }
        // Notifica Listeners
        for (FoodListener listener : foodListeners)
            listener.consumed(consumed);
        // Caso seja um bonus, mostra o valor na tela
        if (consumed == Food.BONUS){
            last_bonus_points = consumed.points;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    last_bonus_points = 0;
                }
            }, 800);
        }
    }

    public void addStateListener(StateListener listener){
        stateListeners.add(listener);
    }

    public void addFoodListener(FoodListener listener){
        foodListeners.add(listener);
    }

}
