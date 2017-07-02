package characther;

import game.Actor;
import game.GameState;
import map.Point;
import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.Timer;
import main.Main;
import map.ChunkedMap;
import map.ChunkedMap.Chunk;
import map.CollisionEvent;
import map.CollisionTest;

/**
 * Classe abstrata, estendida por todos os ghosts
 * Inclui informações sobre onde o alvo (Pacman) está localizado 
 */
/*MovementEvent, RenderEvent, CollisionEvent, StateListener*/
abstract class Ghost extends Actor implements CollisionEvent {

    //diametro do corpo do ghost
    private static final int HITBOX = 22;
    
    private final Pacman player;

    //Gerador de numeros aleatorios utilizado no "frightened"-mode
    private final Random random;
    private int rand_x;
    private int rand_y;

    protected int x;
    protected int y;
    private boolean isCaged;
    private boolean isEaten; //ghost comido pelo pacman
    //verifica se o ghost pode ser comido
    private boolean isEatable;
    
    private enum Speed{
        NORMAL(MOVE_PER_PAINT), SLOW(MOVE_PER_PAINT/2), FAST(MOVE_PER_PAINT*2);
        
        private final int pixel_per_move;
        private Speed(int ppm){
            this.pixel_per_move = ppm;
        }
    }
    
    private Speed current_speed;
    private Speed next_speed;

    //estados do ghost (Perseguindo, em fuga, assustado, retornando, piscando)
    public enum Mode{
        CHASE, SCATTER, FRIGHTENED, RETURNING, BLINKING
    }
    private Mode current_mode;
    private Point start_point;

    private CollisionTest.NextDirection currentDirection;
    private CollisionTest.NextDirection nextDirection;
    private final List<CollisionTest.NextDirection> possible_directions;

    private final static int MOVE_PER_PAINT = 2;
    private int pixel_moved_count;
    
    //imagens dos ghosts
    protected final Image[] blinking;
    protected final Image[] frightened;
    protected final Image dead_right;
    protected final Image dead_down;
    protected final Image dead_left;
    protected final Image dead_up;
    private Image[] ghost_left;
    private Image[] ghost_right;
    private Image[] ghost_up;
    private Image[] ghost_down;
    
    //numero de ghosts comidos no FRIGHTENED-mode */
    protected static int kill_combo = 0;

    protected Ghost(Pacman player){
        this.player = player;
        random = new Random();
        isCaged = true;
        isEaten = false;
        isEatable = false;
        current_speed = Speed.NORMAL;
        next_speed = current_speed;
        current_mode = Mode.SCATTER;
        currentDirection = CollisionTest.NextDirection.UP;
        nextDirection = currentDirection;
        possible_directions = new ArrayList<CollisionTest.NextDirection>(4);
        GameState.INSTANCE.addStateListener(this);
        blinking = new Image[]{
                loadImageResource("ghosts_general/blinking_1.png"),
                loadImageResource("ghosts_general/blinking_2.png")
        };
        frightened = new Image[]{
                loadImageResource("ghosts_general/frightened_1.png"),
                loadImageResource("ghosts_general/frightened_2.png")
        };
        dead_down = loadImageResource("ghosts_general/dead_down.png");
        dead_up = loadImageResource("ghosts_general/dead_up.png");
        dead_left = loadImageResource("ghosts_general/dead_left.png");
        dead_right = loadImageResource("ghosts_general/dead_right.png");
    }
   
    /**
     * Método utilizado para implementar os diferentes comportamentos dos ghosts no modo perseguição
     * Cada ghost tem um comportamento diferente ao perseguir o Pacman
     */
    protected abstract Point getTargetChunk(Pacman player);

    //pontos ao comer ghost
    private int kill_bonus;
    //local onde ghost foi morto
    private Point kill_location = new Point(0, 0);
    
    @Override
    public void detectCollision(CollisionTest tester) {
        if (pixel_moved_count % ChunkedMap.Chunk.CHUNK_SIZE != 0 && !isCaged()) return;
        //checa se pacman comeu ghost
        if ((current_mode == Mode.FRIGHTENED || current_mode == Mode.BLINKING) && gotPlayer(x, y)){
            ghostEaten();
        } else if (current_mode != Mode.RETURNING && gotPlayer(x, y)){ //verifica se Pacman foi pego pelo ghost
            pacmanEaten();
        } else if (current_mode == Mode.RETURNING && tester.checkCollision(x, y, ChunkedMap.Chunk.CAGE_DOOR)){
            isEaten = false;
            isEatable = false;
            current_mode = next_mode;
            nextDirection = currentDirection.opposite();
            next_speed = Speed.NORMAL;
        }
        if ((this.y - GameState.MAP_SPACER) / Chunk.CHUNK_SIZE == 14 &&
                (this.x <= Chunk.CHUNK_SIZE*4 || this.x >= Chunk.CHUNK_SIZE*24)){
            next_speed = Speed.SLOW;
        } else if ((current_mode != Mode.FRIGHTENED && current_mode != Mode.BLINKING) && current_mode != Mode.RETURNING) {
            next_speed = Speed.NORMAL;
        }
        if (tester.checkCollision(this.x, this.y, ChunkedMap.Chunk.JUMPER)){
            if (this.x <= ChunkedMap.Chunk.CHUNK_SIZE-3){ 
                this.x = ChunkedMap.Chunk.CHUNK_SIZE * 27;
            } else {
                this.x = ChunkedMap.Chunk.CHUNK_SIZE;
            }
        }
        if (nextDirection != null) return;
        // Checa proximas direções possiveis
        checkNextDirection(tester);
    }
    
     public void ghostEaten(){
        current_mode = Mode.RETURNING;
            next_speed = Speed.FAST;
            isEatable = false;
            isEaten = true;
            kill_combo++;
            GameState.INSTANCE.addKill(kill_combo);
            kill_bonus = kill_combo*400;
            kill_location.setX(this.x);
            kill_location.setY(this.y);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    kill_bonus = 0;
                }
            }, 800);
    }
    
    public void pacmanEaten(){
        GameState.INSTANCE.removeLive(); //remove vida do Pacman
            //reseta o resto
            currentDirection = CollisionTest.NextDirection.UP;
            nextDirection = currentDirection;
            possible_directions.clear();
            next_speed = Speed.NORMAL;
    }
    
    public void checkNextDirection(CollisionTest tester){
        int x_next = 0, y_next = 0;
        switch (currentDirection){
            case UP:
                y_next = this.y- ChunkedMap.Chunk.CHUNK_SIZE;
                x_next = this.x;
                break;
            case LEFT:
                y_next = this.y;
                x_next = this.x- ChunkedMap.Chunk.CHUNK_SIZE;
                break;
            case DOWN:
                y_next = this.y+ ChunkedMap.Chunk.CHUNK_SIZE;
                x_next = this.x;
                break;
            case RIGHT:
                y_next = this.y;
                x_next = this.x+ ChunkedMap.Chunk.CHUNK_SIZE;
        }
        findNextDirection(x_next, y_next, tester);
        findShortestDistance(x_next, y_next);
    }
    
    public void findNextDirection(int x_next, int y_next, CollisionTest tester){
        //Acha proximas direções
        possible_directions.clear();
        if (!tester.checkNextCollision(x_next, y_next, Chunk.BLOCK, CollisionTest.NextDirection.RIGHT)){
            //Exclui posição oposta
            if (currentDirection != CollisionTest.NextDirection.LEFT)
                possible_directions.add(CollisionTest.NextDirection.RIGHT);
        }
        if (!tester.checkNextCollision(x_next, y_next, Chunk.BLOCK, CollisionTest.NextDirection.DOWN)){
            if (currentDirection != CollisionTest.NextDirection.UP)
                if (tester.checkNextCollision(x_next, y_next, Chunk.CAGE_DOOR, CollisionTest.NextDirection.DOWN)
                        && current_mode != Mode.RETURNING){
                    // Só pode usar a porta da gaiola quando estiver em RETURNING
                } else possible_directions.add(CollisionTest.NextDirection.DOWN);
        }
        if (!tester.checkNextCollision(x_next, y_next, Chunk.BLOCK, CollisionTest.NextDirection.LEFT)){
            if (currentDirection != CollisionTest.NextDirection.RIGHT)
                possible_directions.add(CollisionTest.NextDirection.LEFT);
        }
        if (!tester.checkNextCollision(x_next, y_next, Chunk.BLOCK, CollisionTest.NextDirection.UP)){
            if (currentDirection != CollisionTest.NextDirection.DOWN)
                possible_directions.add(CollisionTest.NextDirection.UP);
        }
    }
    
    public void findShortestDistance(int x_next, int y_next){
        int shortest = Integer.MAX_VALUE;
        int current = 0;
        for (CollisionTest.NextDirection next : possible_directions){
            switch (next){
                case UP:
                    current = measureDistance(x_next, y_next- ChunkedMap.Chunk.CHUNK_SIZE);
                    break;
                case LEFT:
                    current = measureDistance(x_next- ChunkedMap.Chunk.CHUNK_SIZE, y_next);
                    break;
                case DOWN:
                    current = measureDistance(x_next, y_next+ ChunkedMap.Chunk.CHUNK_SIZE);
                    break;
                case RIGHT:
                    current = measureDistance(x_next+ ChunkedMap.Chunk.CHUNK_SIZE, y_next);
            }
            if (current <= shortest){
                nextDirection = next;
                shortest = current;
            }
        }
    }
    
    @Override
    public void move() {
        if (isCaged()){
            if (this.y > (14*Chunk.CHUNK_SIZE + GameState.MAP_SPACER +2))
                nextDirection = CollisionTest.NextDirection.UP;
            else if (this.y < (13*Chunk.CHUNK_SIZE + GameState.MAP_SPACER +6))
                nextDirection = CollisionTest.NextDirection.DOWN;
        }
        // Gera um novo alvo aleatório
        if (current_mode == Mode.FRIGHTENED || current_mode == Mode.BLINKING){
            rand_x = random.nextInt(28) * ChunkedMap.Chunk.CHUNK_SIZE;
            rand_y = random.nextInt(31) * ChunkedMap.Chunk.CHUNK_SIZE;
        }
        switch (currentDirection){
            case UP:
                this.y -= current_speed.pixel_per_move;
                pixel_moved_count += current_speed.pixel_per_move;
                break;
            case RIGHT:
                this.x += current_speed.pixel_per_move;
                pixel_moved_count += current_speed.pixel_per_move;
                break;
            case DOWN:
                this.y += current_speed.pixel_per_move;
                pixel_moved_count += current_speed.pixel_per_move;
                break;
            case LEFT:
                this.x -= current_speed.pixel_per_move;
                pixel_moved_count += current_speed.pixel_per_move;
        }
        // Checa se pode mudar direção e velocidade
        if (pixel_moved_count % ChunkedMap.Chunk.CHUNK_SIZE == 0){
            if (nextDirection != null) currentDirection = nextDirection;
            nextDirection = null;
            current_speed = next_speed;
            pixel_moved_count = 0;
        }
    }

    private int image_count;
    //faz ghost piscar no modo FRIGHTENED
    private int blink_count;
    
    @Override
    public void render(Graphics g) {
        int image_index = 0;
        image_count++;
        if (image_count < 4) image_index = 0;
        else if (image_count < 8) image_index = 1;
        else image_count = 0;
        if (isEatable()){
            if (isBlinking()){
                //simula ghost piscando
                blink_count++;
                if (blink_count < 8){
                    g.drawImage(frightened[image_index], this.x, this.y, null);
                } else if (blink_count < 16){
                    g.drawImage(blinking[image_index], this.x, this.y, null);
                } else blink_count = 0;
            } 
            else g.drawImage(frightened[image_index], this.x, this.y, null);
        } else if (isEaten()){
            // mostra pontos na tela
            if (kill_bonus != 0){
                g.setColor(GameState.BONUS_POINTS_COLOR);
                g.setFont(GameState.BONUS_POINTS_FONT);
                g.drawString(kill_bonus +"", kill_location.getX(), kill_location.getY()+8);
            }
            //Mostra olhos do ghost:
            switch (getNextDirection()){
                case UP:
                    g.drawImage(dead_up, this.x, this.y, null);
                    break;
                case DOWN:
                    g.drawImage(dead_down, this.x, this.y, null);
                    break;
                case LEFT:
                    g.drawImage(dead_left, this.x, this.y, null);
                    break;
                case RIGHT:
                    g.drawImage(dead_right, this.x, this.y, null);
            }
        } else {
           //apenas desenha o ghost no modo normal
            g.drawImage(getGhostImage(getNextDirection(), image_index), this.x, this.y, null);
        }
    }

    /**
     *Força mudança de direção
     * Usado somente pela classe Cage, ao mudar de estados
     */
    protected void forceDirectionChange(){
        this.nextDirection = currentDirection.opposite();
    }

    protected Mode getIndividualMode(){
        return this.current_mode;
    }

    private Mode next_mode;

    protected void setCurrentMode(Mode mode){
        if (current_mode == Mode.RETURNING){
            next_mode = mode;
            return;
        }
        // Reseta tudo depois do FRIGHTENED/BLINKING:
        if (mode != Mode.FRIGHTENED && mode != Mode.BLINKING){
            isEaten = false;
            isEatable = false;
            next_speed = Speed.NORMAL;
        } else if (mode == Mode.FRIGHTENED) { // Modo FRIGHTENED/BLINKING
            // deixa ghost lento
            next_speed = Speed.SLOW;
            isEatable = true;
            kill_combo = 0;
            // armazena ultimo estado do ghost
            next_mode = current_mode;
        }
        this.current_mode = mode;
    }

    protected boolean isBlinking(){
        return (current_mode == Mode.BLINKING);
    }

    protected boolean isEatable(){
        return  this.isEatable;
    }

    protected boolean isEaten(){
        return this.isEaten;
    }

    protected Image loadImageResource(String path){
        URL url = Main.class.getResource("res/graphics/"+path);
        if (url != null) return new ImageIcon(url).getImage();
        else throw new IllegalArgumentException("A imagem no pacote '" +
                "/res/graphics/"+path+"' não foi encontrada.");
    }

    protected CollisionTest.NextDirection getNextDirection(){
        if (nextDirection != null) return nextDirection;
        else return currentDirection;
    }

    /**
     * returna a imagem (Image) do ghost na direção dada
     */
    protected abstract Image getGhostImage(CollisionTest.NextDirection direction, int image_index);

    /**
     * Cada ghost irá para uma "home-corner" quando estiver no modo SCATTER(doido) 
     * método retorna um ponto fora da área do jogo
     */
    protected abstract Point getHomeCorner();

    /**
     * Liberta ghost da gaiola 
     */
    public void start(Point start){
        this.start_point = start;
        isCaged = false;
        this.x = start.getX();
        this.y = start.getY();
        current_mode = Mode.SCATTER;
        nextDirection = CollisionTest.NextDirection.UP;
        pixel_moved_count = 0;
    }

    void moveTo(Point p){
        this.x = p.getX();
        this.y = p.getY();
    }

    @Override
    public void stateChanged(States state){
        isEatable = false;
        isEaten = false;
    }

    /**
     *manda ghost de volta para gaiola
     */
    public void stop(Point cage_pos){
        isCaged = true;
        this.x = cage_pos.getX();
        this.y = cage_pos.getY();
        pixel_moved_count = 0;
        currentDirection = CollisionTest.NextDirection.UP;
    }

    protected boolean isCaged(){
        return this.isCaged;
    }

    //mede distancia entre ghost e alvo(Pacman)
    private int measureDistance(int x, int y){
        int target_x = 0, target_y = 0;
        switch (current_mode){
            case SCATTER:
                target_x = getHomeCorner().getX();
                target_y = getHomeCorner().getY();
                break;
            case CHASE:
                Point point = getTargetChunk(player);
                target_x = point.getX();
                target_y = point.getY();
                break;
            case BLINKING:
            case FRIGHTENED:
                target_x = rand_x;
                target_y = rand_y;
                break;
            case RETURNING:
                target_x = start_point.getX();
                target_y = start_point.getY();
        }
        int triangle_x = target_x - x;
        int triangle_y = target_y - y;
        return (int) Math.sqrt((triangle_x*triangle_x)+(triangle_y*triangle_y));
    }

    private boolean gotPlayer(int x, int y){
        int a_site = (player.getX()+Pacman.HITBOX/2) - (x+Ghost.HITBOX/2);
        int b_site = (player.getY()+Pacman.HITBOX/2) - (y+Ghost.HITBOX/2);
        double distance = Math.sqrt((a_site*a_site)+(b_site*b_site));
        if (distance < (Pacman.HITBOX/2 + Ghost.HITBOX/2)) return true;
        else return false;
    }
}
