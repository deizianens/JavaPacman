package characther;

import java.awt.*;
import map.ChunkedMap.Chunk;
import map.CollisionTest;
import map.Point;
/**
 *Inky é o ghost azul
 */
public class Inky extends Ghost{

    private static final Point HOME_CORNER = new Point(Chunk.CHUNK_SIZE*26, Chunk.CHUNK_SIZE*31);
    private final Blinky blinky;

    private Point target;

    private final Image[] ghost_left;
    private final Image[] ghost_right;
    private final Image[] ghost_up;
    private final Image[] ghost_down;

    protected Inky(Pacman player, Blinky blinky) {
        super(player);
        target = new Point(0, 0);
        this.blinky = blinky;
        ghost_down = new Image[]{
                loadImageResource("inky/inky_down_1.png"),
                loadImageResource("inky/inky_down_2.png")
        };
        ghost_left = new Image[]{
                loadImageResource("inky/inky_left_1.png"),
                loadImageResource("inky/inky_left_2.png")
        };
        ghost_up = new Image[]{
                loadImageResource("inky/inky_up_1.png"),
                loadImageResource("inky/inky_up_2.png")
        };
        ghost_right = new Image[]{
                loadImageResource("inky/inky_right_1.png"),
                loadImageResource("inky/inky_right_2.png")
        };
    }

    @Override
    protected Point getTargetChunk(Pacman player) {
        int offset_x = 0, offset_y = 0;
        // Pega posição do mapa dois pedaços antes do Pacman
        switch (player.getCurrentDirection()){
            case UP:
                offset_x = player.getX();
                offset_y = player.getY()-2*Chunk.CHUNK_SIZE;
                break;
            case DOWN:
                offset_x = player.getX();
                offset_y = player.getY()+2*Chunk.CHUNK_SIZE;
                break;
            case RIGHT:
                offset_x = player.getX()+2*Chunk.CHUNK_SIZE;
                offset_y = player.getY();
                break;
            case LEFT:
                offset_x = player.getX()-2*Chunk.CHUNK_SIZE;
                offset_y = player.getY();
        }
        int triangle_a = offset_x - blinky.x;
        int triangle_b = offset_y - blinky.y;
        target.setX(offset_x + triangle_a);
        target.setY(offset_y + triangle_b);
        return target;
    }

    @Override
    protected Image getGhostImage(CollisionTest.NextDirection direction, int image_index) {
        switch (direction){
            case UP:
                return ghost_up[image_index];
            case DOWN:
                return ghost_down[image_index];
            case LEFT:
                return ghost_left[image_index];
            case RIGHT:
                return ghost_right[image_index];
            default:
                throw new IllegalArgumentException("Direção inválida: "+direction);
        }
    }

    @Override
    protected Point getHomeCorner() {
        return HOME_CORNER;
    }
}
