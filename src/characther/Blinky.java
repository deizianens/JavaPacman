package characther;

import java.awt.*;
import map.ChunkedMap.Chunk;
import map.CollisionTest;
import map.Point;

/**
 *Implementa ghost vermelho
 */
class Blinky extends Ghost{

    private static final Point HOME_CORNER = new Point(Chunk.CHUNK_SIZE*26, 0);

    private Point target;

    private final Image[] ghost_left;
    private final Image[] ghost_right;
    private final Image[] ghost_up;
    private final Image[] ghost_down;

    public Blinky(Pacman player){
        super(player);
        target = new Point(0, 0);
        ghost_down = new Image[]{
            loadImageResource("blinky/blinky_down_1.png"),
            loadImageResource("blinky/blinky_down_2.png")
        };
        ghost_left = new Image[]{
            loadImageResource("blinky/blinky_left_1.png"),
            loadImageResource("blinky/blinky_left_2.png")
        };
        ghost_up = new Image[]{
            loadImageResource("blinky/blinky_up_1.png"),
            loadImageResource("blinky/blinky_up_2.png")
        };
        ghost_right = new Image[]{
            loadImageResource("blinky/blinky_right_1.png"),
            loadImageResource("blinky/blinky_right_2.png")
        };
    }

    @Override
    protected Point getTargetChunk(Pacman player) {
        target.setX(player.getX());
        target.setY(player.getY());
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
                throw new IllegalArgumentException("Não pode se mover na direção: "+direction);
        }
    }

    @Override
    protected Point getHomeCorner() {
        return HOME_CORNER;
    }


}