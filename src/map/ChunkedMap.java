package map;

import game.FoodListener;
import game.GameLoop;
import game.GameState;
import game.RenderEvent;
import game.StateListener;
import javax.swing.*;
import java.awt.*;
import java.util.TimerTask;
import main.Main;
import sound.SoundManager;

/**
 * Implementaçao de um mapa, que divide a area do jogo em multiplos pedaços
 * (Chunks)
 */
public class ChunkedMap implements Map, RenderEvent, StateListener, FoodListener {

    // maze[lin][col] 
    // 28 x 31 
    public int maze[][] = {
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 4, 1, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        {1, 2, 2, 3, 2, 2, 2, 2, 2, 1, 0, 0, 0, 1, 2, 1, 0, 0, 0, 1, 2, 2, 3, 2, 1, 1, 2, 2, 2, 2, 1},
        {1, 2, 1, 1, 1, 2, 1, 1, 2, 1, 0, 0, 0, 1, 2, 1, 0, 0, 0, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1},
        {1, 2, 1, 1, 1, 2, 1, 1, 2, 1, 0, 0, 0, 1, 2, 1, 0, 0, 0, 1, 2, 1, 1, 2, 2, 2, 2, 1, 1, 2, 1},
        {1, 2, 1, 1, 1, 2, 1, 1, 2, 1, 0, 0, 0, 1, 2, 1, 0, 0, 0, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1},
        {1, 2, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1},
        {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 1},
        {1, 2, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1},
        {1, 2, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1},
        {1, 2, 1, 1, 1, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 1, 1, 2, 1},
        {1, 2, 1, 1, 1, 2, 1, 1, 2, 1, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1},
        {1, 2, 1, 1, 1, 2, 1, 1, 2, 1, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1},
        {1, 2, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 1},
        {1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 9, 1, 1, 1, 1, 1, 2, 1},
        {1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1},
        {1, 2, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 1},
        {1, 2, 1, 1, 1, 2, 1, 1, 2, 1, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1},
        {1, 2, 1, 1, 1, 2, 1, 1, 2, 1, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1},
        {1, 2, 1, 1, 1, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 1, 1, 2, 1},
        {1, 2, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1},
        {1, 2, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1},
        {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 1},
        {1, 2, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1},
        {1, 2, 1, 1, 1, 2, 1, 1, 2, 1, 0, 0, 0, 1, 2, 1, 0, 0, 0, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1},
        {1, 2, 1, 1, 1, 2, 1, 1, 2, 1, 0, 0, 0, 1, 2, 1, 0, 0, 0, 1, 2, 1, 1, 2, 2, 2, 2, 1, 1, 2, 1},
        {1, 2, 1, 1, 1, 2, 1, 1, 2, 1, 0, 0, 0, 1, 2, 1, 0, 0, 0, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1},
        {1, 2, 2, 3, 2, 2, 2, 2, 2, 1, 0, 0, 0, 1, 2, 1, 0, 0, 0, 1, 2, 2, 3, 2, 1, 1, 2, 2, 2, 2, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 4, 1, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
       };

    public static final Color BACKGROUND_COLOR = new Color(3, 3, 3);
    private static final Color PILLS_COLOR = new Color(255, 255, 171);
    public static final Color BLOCK_COLOR = new Color(87, 87, 255);

    //possiveis objetos de cada pedaço
    //Ponto, nada, bloco, bola(poder), fruta(bonus), ponto de inicio, porta da gaiola dos ghosts, jumper
    public enum Chunk {
        POINT, NOTHING, BLOCK, BALL, FRUIT, START, CAGE_DOOR, JUMPER;

        public static final int CHUNK_SIZE = 16;
    }

    //area do jogo
    private final Chunk[][] field;

    //ponto de inicio do pacman
    private final Point start_point;
    //ponto onde os ghosts estão presos
    private final Point cage_point;

    private final Image background;
    private final Image cherry;

    private final static int ZINDEX = 2;

    private final int w;
    private final int h;

    public ChunkedMap(int width, int height) {
        w = width;
        h = height;
        field = new Chunk[28][31];
        // cria labirinto
        setupMaze();
        background = new ImageIcon(Main.class.getResource("res/graphics/maze.png")).getImage();
        cherry = new ImageIcon(Main.class.getResource("res/graphics/cherry.png")).getImage();
        start_point = new Point(13 * Chunk.CHUNK_SIZE, 23 * Chunk.CHUNK_SIZE + GameState.MAP_SPACER);
        cage_point = new Point(10 * Chunk.CHUNK_SIZE, 12 * Chunk.CHUNK_SIZE + GameState.MAP_SPACER);
        GameState.INSTANCE.addStateListener(this);
        GameState.INSTANCE.addFoodListener(this);
    }

    /**
     * Método que cria bolas e blocos no mapa de acordo com o mapa da versão
     * arcade original do jogo
     */
    private void setupMaze() {
        addLevelBoundary(); //trata as bordas do mapa
        for(int i=0; i<28; i++){
            for(int j=0; j<31; j++){
                switch (maze[i][j]) {
                    case 0:
                        setChunk(i, j, Chunk.NOTHING);
                        break;
                    case 1:
                        setChunk(i, j, Chunk.BLOCK);
                        break;
                    case 2:
                        setChunk(i, j, Chunk.POINT);
                        break;
                    case 3:
                        setChunk(i, j, Chunk.BALL);
                        break;
                    case 4:
                        setChunk(i, j, Chunk.JUMPER);
                        break;
                    case 9:
                        setChunk(i, j, Chunk.START);
                        break;
                    default:
                        break;
                }
            }
        }
        addCagePoints(10, 12);
    }

    private void addCagePoints(int x_chunk, int y_chunk) {
        int width = 8;
        int height = 5;

        for (int x = x_chunk; x < (x_chunk + width); x++) {
            for (int y = y_chunk; y < (y_chunk + height); y++) {
                setChunk(x, y, Chunk.BLOCK);
            }
        }
        // adiciona porta a gaiola
        setChunk(x_chunk + (width / 2), y_chunk, Chunk.CAGE_DOOR);

        // Acima e abaixo
        int[] y_rows = {y_chunk - 1, y_chunk + height};
        for (int y : y_rows) {
            for (int x = x_chunk - 1; x < (x_chunk + width + 1); x++) {
                setChunk(x, y, Chunk.NOTHING);
            }
        }
        //Direita e esquerda
        int[] x_rows = {x_chunk - 1, x_chunk + width};
        for (int x : x_rows) {
            for (int y = y_chunk - 1; y < (y_chunk + height + 1); y++) {
                setChunk(x, y, Chunk.NOTHING);
            }
        }
    }
    
    //adiciona bordas
    private void addLevelBoundary() {

        int[] y_rows = {0, field[0].length - 1};
        for (int y : y_rows) {
            for (int x = 0; x < field.length; x++) {
                setChunk(x, y, Chunk.BLOCK);
            }
        }
        int[] x_rows = {0, field.length - 1};
        for (int x : x_rows) {
            for (int y = 0; y < field[0].length; y++) {
                setChunk(x, y, Chunk.BLOCK);
            }
        }
    }

    public int getZIndex() {
        return ZINDEX;
    }

    //contador de comida
    private int food_counter = 0;

    @Override
    public void consumed(GameState.Food food) {
        if (food == GameState.Food.BALL || food == GameState.Food.POINT) {
            food_counter++;
        }
        if (food_counter == 70 || food_counter == 170) {
            setChunk(13, 17, Chunk.FRUIT);
            new java.util.Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    // Remove bonus
                    setChunk(13, 17, Chunk.NOTHING);
                }
            }, 10 * 1000);
        }
    }

    @Override
    public void stateChanged(States state) {
        if (state == States.ROUND_WON) {
            GameLoop.INSTANCE.freeze();
            SoundManager.INSTANCE.play("round_over");
            new java.util.Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    GameLoop.INSTANCE.play();
                    setupMaze();
                    food_counter = 0;
                }
            }, 5000);
        } else if (state == States.GAME_OVER) {
            // Apenas reseta o mapa
            new java.util.Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    setupMaze();
                    food_counter = 0;
                    GameLoop.INSTANCE.play();
                }
            }, 2 * 1000);
        }
    }

    @Override
    public void render(Graphics g) {
        g.setColor(BACKGROUND_COLOR);
        g.fillRect(0, 0, w + 16, h);
        g.drawImage(background, 7, GameState.MAP_SPACER + 4,
                field.length * Chunk.CHUNK_SIZE - 4, field[0].length * Chunk.CHUNK_SIZE + 2, null);
        int object_spacer = 10; // pixels colocados entre os objetos
        // Desenha os pedaços
        for (int x = 0; x < field.length; x++) {
            for (int y = 0; y < field[0].length; y++) {
                // Desenha objetos
                switch (getChunk(x, y)) {
                    case POINT:
                        g.setColor(PILLS_COLOR);
                        g.fillRect(
                                x * Chunk.CHUNK_SIZE + object_spacer,
                                y * Chunk.CHUNK_SIZE + object_spacer
                                + GameState.MAP_SPACER,
                                4, 4
                        );
                        break;
                    case BALL:
                        g.setColor(PILLS_COLOR);
                        g.fillOval(
                                x * Chunk.CHUNK_SIZE + object_spacer - 4,
                                y * Chunk.CHUNK_SIZE + object_spacer - 4
                                + GameState.MAP_SPACER,
                                12, 12
                        );
                        break;
                    case FRUIT:
                        g.drawImage(cherry,
                                x * Chunk.CHUNK_SIZE + object_spacer - 4,
                                y * Chunk.CHUNK_SIZE + object_spacer - 4
                                + GameState.MAP_SPACER,
                                null);
                }
            }
        }
    }

    /**
     * Este metodo é usado para pegar o tipo de objeto colocado em determinada
     * coordenada ele irá checar se as coordenadas são validas, caso sejam
     * invalidas o metodo retornará um bloco ao inves de chamar uma exceção
     */
    private Chunk getChunk(int x, int y) {
        if (x < 0 || x >= field.length || y < 0 || y >= field[0].length) {
            return Chunk.BLOCK;
        }
        return field[x][y];
    }

    /**
     * Adiciona ou remove qualquer objeto do mapa. Valida coordenadas throws
     * exception caso coordenadas sejam invalidas
     */
    private void setChunk(int x, int y, Chunk object) {
        if (x < 0 || x >= field.length
                || y < 0 || y >= field[0].length) {
            throw new IllegalArgumentException("Ponto inválido: (" + x + "|" + y + ")");
        }
        field[x][y] = object;
    }

    public Point getStartPoint() {
        return start_point;
    }

    public Point getCagePoint() {
        return cage_point;
    }

    private CollisionTest collision_test = new CollisionTest() {

        private Chunk getObject(int you_x, int you_y) {
            you_y -= GameState.MAP_SPACER;
            int chunk_x = you_x / Chunk.CHUNK_SIZE;
            int chunk_y = you_y / Chunk.CHUNK_SIZE;
            return getChunk(chunk_x, chunk_y);
        }

        /**
         * Pega o pedaço onde a figura está no momento e a substitui. Retorna
         * objeto encontrado
         */
        private Chunk getAndReplaceObject(int you_x, int you_y, Chunk match, Chunk replace) {
            you_y -= GameState.MAP_SPACER;
            int chunk_x = you_x / Chunk.CHUNK_SIZE;
            int chunk_y = you_y / Chunk.CHUNK_SIZE;
            Chunk tmp = getChunk(chunk_x, chunk_y);
            if (tmp == match) {
                setChunk(chunk_x, chunk_y, replace);
            }
            return tmp;
        }

        @Override
        public boolean checkAnyCollision(int you_x, int you_y) {
            if (getObject(you_x, you_y) == Chunk.NOTHING) {
                return false;
            }
            return true;
        }

        @Override
        public <T> boolean checkCollision(int you_x, int you_y, T object) {
            Chunk found = null;
            if (object == Chunk.POINT) {
                found = getAndReplaceObject(you_x, you_y,
                        Chunk.POINT, Chunk.NOTHING);
            } else if (object == Chunk.BALL) {
                found = getAndReplaceObject(you_x, you_y,
                        Chunk.BALL, Chunk.NOTHING);
            } else if (object == Chunk.FRUIT) {
                found = getAndReplaceObject(you_x, you_y,
                        Chunk.FRUIT, Chunk.NOTHING);
            } else {
                found = getObject(you_x, you_y);
            }
            if (found != null && found == object) {
                return true;
            }
            return false;
        }

        @Override
        public <T> boolean checkNextCollision(int you_x, int you_y, T object, NextDirection next) {
            Chunk obj = null;
            // Pula pro proximo 
            switch (next) {
                case UP:
                    obj = getObject(you_x,
                            you_y - Chunk.CHUNK_SIZE);
                    break;
                case DOWN:
                    obj = getObject(you_x,
                            you_y + Chunk.CHUNK_SIZE);
                    break;
                case RIGHT:
                    obj = getObject(you_x + Chunk.CHUNK_SIZE,
                            you_y);
                    break;
                case LEFT:
                    obj = getObject(you_x - Chunk.CHUNK_SIZE,
                            you_y);
                    break;
            }
            // Checa a colisão:
            if (obj != null && obj == object) {
                return true;
            }
            return false;
        }
    };

    @Override
    public CollisionTest getCollisionTest() {
        return collision_test;
    }
}
