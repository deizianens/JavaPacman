package game;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import map.CollisionEvent;
import map.Map;

/**
 * O loop principal do jogo, chama todos os eventos
 */
public enum GameLoop implements KeyListener{

    INSTANCE;
    
    //indica jogo em execução
    private boolean isRunning;
    //jogo congelado
    private boolean isFrozen;
    //jogo pausado
    private boolean isPaused;

    private ScheduledExecutorService game_loop_executor;
    //Handler para main-game-thread, utilizada para para-lo */
    private ScheduledFuture game_loop_handler;
    
    private GameCanvas canvas;

    //ultimo key-event disparado pelo usuario
    private KeyEvent last_key_event;
    private InputEvent.KeyEventType last_key_type;
    
    private List<InputEvent> inputEvents;
    private List<MovementEvent> movementEvents;
    private List<RenderContainer> renderEvents;
    private List<CollisionEvent> collisionEvents;
    
    private Map game_field;

    /**
     * Padrão de projeto Singleton utilizado
     */
    private GameLoop(){
        inputEvents = new ArrayList<InputEvent>(4);
        movementEvents = new ArrayList<MovementEvent>(6);
        renderEvents = new ArrayList<RenderContainer>(20);
        collisionEvents = new ArrayList<CollisionEvent>(5);
        isRunning = false;
        isFrozen = false;
        isPaused = false;
        game_loop_executor = Executors.newSingleThreadScheduledExecutor();
        canvas = new GameCanvas();
    }
    
    //thread
    private Runnable game_loop = new Runnable() {
        @Override
        public void run() {
            try {
                if (!isFrozen() && !isPaused()){
                    // Eventos de input:
                    if (last_key_event != null && last_key_type != null) {
                        for (InputEvent event : inputEvents)
                            event.keyboardInput(last_key_event, last_key_type);
                        // Clear
                        last_key_type = null;
                        last_key_event = null;
                    }
                    //Eventos de colisão:
                    for (CollisionEvent event : collisionEvents)
                        event.detectCollision(game_field.getCollisionTest());
                    //Eventos de movimento:
                    for (MovementEvent event : movementEvents)
                        event.move();
                }
                // Eventos de renderização:
                canvas.repaint();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    };

   
    private void createMainLoop(){
        // Checa se há um mapa:
        if (this.game_field == null)
            throw new IllegalStateException("Jogo não pode começar sem um mapa!");
        canvas.setRenderEvents(this.renderEvents);
        //Inicia o executor do jogo:
        game_loop_handler = game_loop_executor.scheduleAtFixedRate(
                game_loop, 0L, 16L, TimeUnit.MILLISECONDS
        );
    }

    /**
     * Checa se o jogo já está sendo executado. Se estiver, o estado dos eventos são considerados bloqueados
     * Previne qualquer acesso de escrita nas listas enquanto outra thread as usa, o que causaria uma exceção 
     */
    private boolean isLocked(){
        return isRunning;
    }

    /**
     * Adiciona novo movimento ao jogo
     */
    public void addMovementEvent(MovementEvent event){
        if (!isLocked())
            this.movementEvents.add(event);
    }

    public void addRenderEvent(RenderEvent event, int zIndex){
        if (!isLocked()){
            RenderContainer re = new RenderContainer(zIndex,event);
            this.renderEvents.add(re);
        }
    }

    public void addInputEvent(InputEvent event){
        if (!isLocked())
            this.inputEvents.add(event);
    }

    public void addCollisionEvent(CollisionEvent event){
        if (!isLocked())
            this.collisionEvents.add(event);
    }

    public void setMap(Map map){
        if (!isLocked())
            this.game_field = map;
    }

    
     // Inicia game-loop.
    public void startLoop(){
        if (!isRunning){
            createMainLoop();
            isRunning = true;
        }
    }
    
    public void stopLoop(){
        game_loop_handler.cancel(true);
        game_loop_executor.shutdown();
        isRunning = false;
    }

    public void play(){
        this.isFrozen = false;
        this.isPaused = false;
    }

    public void freeze(){
        this.isFrozen = true;
    }

    public void pause(){
        this.isPaused = true;
    }

    public boolean isPaused(){
        return this.isPaused;
    }

    public boolean isFrozen(){
        return this.isFrozen;
    }

    public JComponent getView(){
        return canvas;
    }

    public void keyPressed(KeyEvent e) {
        last_key_event = e;
        last_key_type = InputEvent.KeyEventType.PRESSED;
    }
  
    public void keyReleased(KeyEvent e) {
        last_key_event = e;
        last_key_type = InputEvent.KeyEventType.RELEASED;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

}
