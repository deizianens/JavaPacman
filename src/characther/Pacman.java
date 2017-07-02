package characther;

import game.Actor;
import game.GameState;
import game.InputEvent;
import game.MovementEvent;
import game.RenderEvent;
import game.StateListener;
import map.ChunkedMap;
import map.ChunkedMap.Chunk;
import map.CollisionEvent;
import map.CollisionTest;
import sound.SoundManager;
import main.Main;
import map.Point;

import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;
///*RenderEvent, InputEvent, CollisionEvent, MovementEvent, StateListener */
public class Pacman extends Actor implements CollisionEvent, InputEvent {

    //graus necessarios para "boca totalmente aberta" do Pacman
    private final static int MOUTH_MAX = 75;
    //graus necessarios para "boca totalmente fechada" do Pacman
    private final static int MOUTH_MIN = 0;
    //movimento do pacman
    private final static int MOVE_PER_PAINT = 2; 
    //velocidade da boca do pacman
    private final static int MOUTH_SPEED = 10;
    //diametro do pacman 
    public final static int HITBOX = 28;
    
    private final static int ZINDEX = 1;
    
    //cor do pacman
    public static final Color BODY_COLOR = new Color(255, 255, 87);

    private int x;
    private int y;
    private final Point start_point;

    // Conta a quantidade de pixels movidos desde a última mudança de direção 
    private int pixel_moved_count;
    //indica colisão
    private boolean has_collided;
    //mostra se pacman está indo pro céu
    private boolean isDieing;

    private int mouth_degrees;
    private boolean mouth_closing;

    //direçoes que pacman pode se mover
    private enum FacingDirection{
        UP(0), DOWN(180), LEFT(90), RIGHT(270);
        
        private final int degrees;
        private FacingDirection(int degrees){
            this.degrees = degrees;
        }

        private CollisionTest.NextDirection convertToNextDirection(){
            switch (this){
                case DOWN:
                    return CollisionTest.NextDirection.DOWN;
                case UP:
                    return CollisionTest.NextDirection.UP;
                case LEFT:
                    return CollisionTest.NextDirection.LEFT;
                case RIGHT:
                    return CollisionTest.NextDirection.RIGHT;
                default:
                    throw new IllegalStateException("Impossivel mover '"+this.toString()+"'");
            }
        }
    }
    private FacingDirection current_direction;
    private FacingDirection next_direction;
    private boolean direction_change_possible;

    //novo Pacman animado
    public Pacman(Point point){
        this.start_point = point;
        reset();
        GameState.INSTANCE.addStateListener(this);
    }
    
    public int getZIndex(){
        return ZINDEX;
    }

    @Override
    public void move() {
        // Checa se mudança de direção é possivel
        if (pixel_moved_count % ChunkedMap.Chunk.CHUNK_SIZE == 0){
            if (direction_change_possible)
                current_direction = next_direction;
            pixel_moved_count = 0;
        }
        // Move Pacman
        if (!has_collided)
            switch (current_direction){
                case UP:
                    this.y -= MOVE_PER_PAINT;
                    pixel_moved_count += MOVE_PER_PAINT;
                    break;
                case RIGHT:
                    this.x += MOVE_PER_PAINT;
                    pixel_moved_count += MOVE_PER_PAINT;
                    break;
                case DOWN:
                    this.y += MOVE_PER_PAINT;
                    pixel_moved_count += MOVE_PER_PAINT;
                    break;
                case LEFT:
                    this.x -= MOVE_PER_PAINT;
                    pixel_moved_count += MOVE_PER_PAINT;
                    break;
            }
        // movimentos da boca
        if (mouth_degrees < MOUTH_MAX && !mouth_closing){
            // boca abrindo
            if (!has_collided) 
                mouth_degrees += MOUTH_SPEED;
        } else if (mouth_degrees > MOUTH_MIN) {
            if (!has_collided){ 
                mouth_degrees -= MOUTH_SPEED;
                mouth_closing = true;
            }
        } else {
            // Boca fechada, abre novamente
            mouth_closing = false;
        }
    }
    
    @Override
    public void render(Graphics g) throws IOException {
    	URL url = Main.class.getResource("res/graphics/"+current_direction.toString()+".png");
        Image image = new ImageIcon(url).getImage();
    	g.drawImage(image, this.x-3, this.y-3, ChunkedMap.BACKGROUND_COLOR, null);
        g.setColor(ChunkedMap.BACKGROUND_COLOR);
        g.fillArc(this.x-3, this.y-3, HITBOX, HITBOX,
                calculateMouthSpacer(mouth_degrees)+current_direction.degrees,
                mouth_degrees
        );
        // Morte :(
        if (isDieing){
            mouth_degrees += MOUTH_SPEED+2;
            // Checa final da animação
            if (mouth_degrees >= 360){
                reset();
            }
        }
    }
    
    //Calcula posição da boca
    public int calculateMouthSpacer(int current_degrees){
        int element_space = current_degrees + 180;
        int usable_space = 360 - element_space;
        return (usable_space / 2);
    }
    
    @Override
    public void detectCollision(CollisionTest tester) {
        if (pixel_moved_count % ChunkedMap.Chunk.CHUNK_SIZE != 0) return;
        // Checa se foi para o jumper
        if (tester.checkCollision(this.x, this.y, ChunkedMap.Chunk.JUMPER)){
            if (this.x <= Chunk.CHUNK_SIZE-3){ 
                this.x = Chunk.CHUNK_SIZE * 27;
            } else {
                this.x = Chunk.CHUNK_SIZE;
            }
            return;
        }
        // Checa se está de encontro a um bloco:
        if (tester.checkNextCollision(this.x, this.y,
                Chunk.BLOCK, current_direction.convertToNextDirection())
            || tester.checkNextCollision(this.x, this.y,
                Chunk.CAGE_DOOR, current_direction.convertToNextDirection())){
            has_collided = true;
        }
        if (tester.checkNextCollision(this.x, this.y,
                Chunk.BLOCK, next_direction.convertToNextDirection())
            || tester.checkNextCollision(this.x, this.y,
                Chunk.CAGE_DOOR, next_direction.convertToNextDirection())){
            direction_change_possible = false;
        } else {
            direction_change_possible = true;
            has_collided = false;
        }
        // Checa se comeu algo
        if (tester.checkCollision(this.x, this.y, Chunk.POINT)){
            SoundManager.INSTANCE.loop("eat", Clip.LOOP_CONTINUOUSLY);
            GameState.INSTANCE.addScore(GameState.Food.POINT);
        } else if (tester.checkCollision(this.x, this.y, Chunk.BALL)){
            GameState.INSTANCE.addScore(GameState.Food.BALL);
        } else if (tester.checkCollision(this.x, this.y, Chunk.FRUIT)){
            GameState.INSTANCE.addScore(GameState.Food.BONUS);
            SoundManager.INSTANCE.play("eat_fruit");
        } else {
            SoundManager.INSTANCE.stop("eat");
        }
    }

    @Override
    public void stateChanged(States state) {
        //para som de "comer"
        SoundManager.INSTANCE.stop("eat");
        if (state == States.LIVE_LOST){
            isDieing = true;
            mouth_degrees = 0;
        } else if (state == States.ROUND_WON || state == States.GAME_OVER){
            reset();
        }
    }

    @Override
    public void keyboardInput(KeyEvent event, KeyEventType type) {
        has_collided = false;
        switch (event.getKeyCode()) {
            case KeyEvent.VK_UP:
                next_direction = FacingDirection.UP;
                break;
            case KeyEvent.VK_DOWN:
                next_direction = FacingDirection.DOWN;
                break;
            case KeyEvent.VK_LEFT:
                next_direction = FacingDirection.LEFT;
                break;
            case KeyEvent.VK_RIGHT:
                next_direction = FacingDirection.RIGHT;
                break;
            default:
                break;
        }
    }

    private void reset(){
        mouth_degrees = 45;
        mouth_closing = false;
        current_direction = FacingDirection.LEFT;
        next_direction = current_direction;
        direction_change_possible = true;
        has_collided = false;
        this.x = (int) start_point.getX();
        this.y = (int) start_point.getY();
        isDieing = false;
        pixel_moved_count = 0;
    }

    int getX(){
        return this.x;
    }

    int getY(){
        return this.y;
    }

    CollisionTest.NextDirection getCurrentDirection(){
        return this.current_direction.convertToNextDirection();
    }
}
