package characther;

import game.Actor;
import characther.Ghost.Mode;
import game.FoodListener;
import game.GameLoop;
import game.GameState;
import map.ChunkedMap;
import map.ChunkedMap.Chunk;
import sound.SoundManager;
import map.Point;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Gaiola onde iniciam os ghosts
 * Gerencia seus estados e mudança de modos
 */

/*RenderEvent, StateListener, MovementEvent,*/
public class Cage extends Actor implements FoodListener{
    
    private final Point p;
    private final Point door;
    private final Point ghost_start;

    //espaço onde pacman fica perto da gaiola
    private final static int EXTRA_SPACE = 10;

    private Timer release_timer;
    //tempo de duração dos modos
    private Timer mode_timer;
    private Timer freighted_timer;
    private long mode_timer_stamp;
    private int time_elapsed;
    private Mode global_mode;

    private final Map<String, Ghost> ghosts;
    private static final String BLINKY = "blinky";
    private static final String PINKY = "pinky";
    private static final String CLYDE = "clyde";
    private static final String INKY = "inky";
    
    public Cage(Point p, Pacman player){
        this.p = p;
        this.door = new Point(p.getX() + Chunk.CHUNK_SIZE*3,  p.getY());
        this.ghost_start = new Point(door.getX(), door.getY()+Chunk.CHUNK_SIZE);
        mode_timer = new Timer();
        time_elapsed = -1;
        ghosts = new HashMap<String, Ghost>(4);
        Ghost blinky = new Blinky(player);
        GameLoop.INSTANCE.addMovementEvent(blinky);
        GameLoop.INSTANCE.addCollisionEvent(blinky);
        GameLoop.INSTANCE.addRenderEvent(blinky, 0);
        blinky.moveTo(new Point(ghost_start.getX(), ghost_start.getY()-(2*Chunk.CHUNK_SIZE)));
        ghosts.put(BLINKY, blinky);
        // Add Pinky
        Ghost pinky = new Pinky(player);
        GameLoop.INSTANCE.addMovementEvent(pinky);
        GameLoop.INSTANCE.addCollisionEvent(pinky);
        GameLoop.INSTANCE.addRenderEvent(pinky, 0);
        pinky.moveTo(new Point(ghost_start.getX()+8, ghost_start.getY()+Chunk.CHUNK_SIZE));
        ghosts.put(PINKY, pinky);
        // Add Inky:
        Ghost inky = new Inky(player, (Blinky)blinky);
        GameLoop.INSTANCE.addMovementEvent(inky);
        GameLoop.INSTANCE.addCollisionEvent(inky);
        GameLoop.INSTANCE.addRenderEvent(inky, 0);
        inky.moveTo(
                new Point(ghost_start.getX()-Chunk.CHUNK_SIZE*2+8, ghost_start.getY() + Chunk.CHUNK_SIZE)
        );
        ghosts.put(INKY, inky);
        // Add Clyde:
        Ghost clyde = new Clyde(player);
        GameLoop.INSTANCE.addMovementEvent(clyde);
        GameLoop.INSTANCE.addCollisionEvent(clyde);
        GameLoop.INSTANCE.addRenderEvent(clyde, 0);
        clyde.moveTo(
                new Point(ghost_start.getX()+Chunk.CHUNK_SIZE*2+8, ghost_start.getY() + Chunk.CHUNK_SIZE)
        );
        ghosts.put(CLYDE, clyde);
        GameState.INSTANCE.addStateListener(this);
        GameLoop.INSTANCE.addMovementEvent(this);
        GameState.INSTANCE.addFoodListener(this);
    }

    /**
     *Indica que intro terminou
     * Libera ghosts
     */
    private void start(){
        // Blinky:
        ghosts.get(BLINKY).start(ghost_start);
        ghosts.get(BLINKY).moveTo(new Point(ghost_start.getX(), ghost_start.getY() - (2 * Chunk.CHUNK_SIZE)));
        // Pinky:
        ghosts.get(PINKY).stop(new Point(ghost_start.getX()+8, ghost_start.getY() + Chunk.CHUNK_SIZE));
        // Inky:
        ghosts.get(INKY).stop(
                new Point(ghost_start.getX()-Chunk.CHUNK_SIZE*2+8, ghost_start.getY() + Chunk.CHUNK_SIZE)
        );
        // Clyde:
        ghosts.get(CLYDE).stop(
                new Point(ghost_start.getX()+Chunk.CHUNK_SIZE*2+8, ghost_start.getY() + Chunk.CHUNK_SIZE)
        );
        release_timer = new Timer();
        release_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ghosts.get(PINKY).start(ghost_start);
            }
        }, 2*1000);
        release_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ghosts.get(INKY).start(ghost_start);
            }
        }, 3*1000);
        release_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ghosts.get(CLYDE).start(ghost_start);
            }
        }, 5*1000);
        global_mode = Mode.SCATTER;
        int timer = 0;
        //faz a alternação entre os modos 7 segundos em chase, 20 em scatter
        scheduleModeChange(Mode.CHASE, timer+=7);
        scheduleModeChange(Mode.SCATTER, timer+=20);
        scheduleModeChange(Mode.CHASE, timer+=7);
        scheduleModeChange(Mode.SCATTER, timer+=20);
        scheduleModeChange(Mode.CHASE, timer+=5);
        scheduleModeChange(Mode.SCATTER, timer+=20);
        scheduleModeChange(Mode.CHASE, timer+=5);
        mode_timer_stamp = System.currentTimeMillis();
    }

    /**
     * Coloca todos os ghosts de volta na gaiola
     */
    private void reset(){
        release_timer.cancel();
        for (Ghost g : ghosts.values())
            g.stop(ghost_start);
    }

    @Override
    public void render(Graphics grap) {
        Graphics2D g = (Graphics2D)grap;
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(2.0f));
        g.setColor(ChunkedMap.BLOCK_COLOR);
        g.drawRect(p.getX()+EXTRA_SPACE, p.getY()+EXTRA_SPACE,
                (Chunk.CHUNK_SIZE*8)-EXTRA_SPACE*2+6, (Chunk.CHUNK_SIZE*5)-EXTRA_SPACE*2+6);
        g.drawRect(p.getX()+EXTRA_SPACE+4, p.getY()+EXTRA_SPACE+4,
                (Chunk.CHUNK_SIZE*8)-EXTRA_SPACE*2-2, (Chunk.CHUNK_SIZE*5)-EXTRA_SPACE*2-2);
        // Desenha porta
        g.setColor(Color.WHITE);
        g.fillRect(door.getX()+2, door.getY()-2+EXTRA_SPACE, Chunk.CHUNK_SIZE*2+2, 8);
        g.setStroke(old);
    }

    @Override
    public void stateChanged(States state) {
        if (state == States.LIVE_LOST){
            GameLoop.INSTANCE.freeze();
            SoundManager.INSTANCE.play("dieing");
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    reset();
                    GameLoop.INSTANCE.play();
                }
            }, 2000);
        } else if (state == States.ROUND_WON || state == States.GAME_OVER){
            reset();
        }
        mode_timer.cancel();
        mode_timer = new Timer();
        if (freighted_timer != null) freighted_timer.cancel();
    }

    private Mode last_mode;
    @Override
    public void consumed(GameState.Food food){
        if (food == GameState.Food.BALL){
            pauseModeTimer();
            if (global_mode != Mode.FRIGHTENED && global_mode != Mode.BLINKING){
                last_mode = global_mode;
            } else if (global_mode == Mode.FRIGHTENED || global_mode == Mode.BLINKING) {
                freighted_timer.cancel();
            }
            // Reseta para modo anterior após alguns segundos
            freighted_timer = new Timer();
            freighted_timer.schedule(new ModeChangeTask(Mode.BLINKING){
                @Override
                public void run() {
                    global_mode = this.mode;
                    for (Ghost g : ghosts.values()){
                        if (g.getIndividualMode() == Mode.FRIGHTENED)
                            g.setCurrentMode(this.mode);
                    }
                }
            }, 3 * 1000);
            freighted_timer.schedule(new ModeChangeTask(last_mode) {
                @Override
                public void run() {
                    for (Ghost g : ghosts.values()){
                        g.setCurrentMode(this.mode);
                    }
                    unpauseModeTimer();
                }
            }, 5 * 1000);
            for (Ghost g : ghosts.values())
                g.setCurrentMode(Mode.FRIGHTENED);
            for (Ghost g : ghosts.values())
                g.forceDirectionChange();
        }
    }

    @Override
    public void move() {
        if (ghosts.get(BLINKY).isCaged()){
            start();
        }
    }

    /*
    *Muda modo apos um tempo
     */
    private void scheduleModeChange(Mode mode, int time_sec){
        if (time_sec <= 0)
            throw new IllegalArgumentException("Tempo não pode ser <= 0");
        mode_timer.schedule(new ModeChangeTask(mode), time_sec * 1000);
    }

    private void unpauseModeTimer(){
        if (time_elapsed == -1) return;
        mode_timer = new Timer();
        int timer = 0;
        if (time_elapsed < (timer+=7))
            scheduleModeChange(Mode.CHASE, timer-time_elapsed);
        if (time_elapsed < (timer+=20))
            scheduleModeChange(Mode.SCATTER, timer-time_elapsed);
        if (time_elapsed < (timer+=7))
            scheduleModeChange(Mode.CHASE, timer-time_elapsed);
        if (time_elapsed < (timer+=20))
            scheduleModeChange(Mode.SCATTER, timer-time_elapsed);
        if (time_elapsed < (timer+=5))
            scheduleModeChange(Mode.CHASE, timer-time_elapsed);
        if (time_elapsed < (timer+=20))
            scheduleModeChange(Mode.SCATTER, timer-time_elapsed);
        if (time_elapsed < (timer+=5))
            scheduleModeChange(Mode.CHASE, timer-time_elapsed);
        mode_timer_stamp = System.currentTimeMillis()-(time_elapsed*1000);
        time_elapsed = -1;
    }

    private void pauseModeTimer(){
        if (time_elapsed != -1) return;
        time_elapsed = (int) ((System.currentTimeMillis() - mode_timer_stamp) / 1000);
        mode_timer.cancel();
    }

    private class ModeChangeTask extends TimerTask {
        protected final Mode mode;

        public ModeChangeTask(Mode mode){
            this.mode = mode;
        }

        @Override
        public void run() {
            global_mode = this.mode;
            for (Ghost g : ghosts.values())
                g.forceDirectionChange();
            System.out.println("Mode change to " + mode);
            for (Ghost g : ghosts.values())
                g.setCurrentMode(mode);
        }
    }
}