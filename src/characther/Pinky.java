package characther;

import java.awt.*;
import map.ChunkedMap.Chunk;
import map.CollisionTest;
import map.Point;

/**
 * Pinky é o ghost rosa 
 */
public class Pinky extends Ghost {

    private static final Point HOME_CORNER = new Point(Chunk.CHUNK_SIZE, 0);

    private Point target;

    private final Image[] ghost_left;
    private final Image[] ghost_right;
    private final Image[] ghost_up;
    private final Image[] ghost_down;

    protected Pinky(Pacman player) {
        super(player);
        target = new Point(0, 0);
        ghost_down = new Image[]{
                loadImageResource("pinky/pinky_down_1.png"),
                loadImageResource("pinky/pinky_down_2.png")
        };
        ghost_left = new Image[]{
                loadImageResource("pinky/pinky_left_1.png"),
                loadImageResource("pinky/pinky_left_2.png")
        };
        ghost_up = new Image[]{
                loadImageResource("pinky/pinky_up_1.png"),
                loadImageResource("pinky/pinky_up_2.png")
        };
        ghost_right = new Image[]{
                loadImageResource("pinky/pinky_right_1.png"),
                loadImageResource("pinky/pinky_right_2.png")
        };
    }

    @Override
    protected Point getTargetChunk(Pacman player) {
        switch (player.getCurrentDirection()){
            case UP:
                target.setX(player.getX());
                target.setY(player.getY()-4*Chunk.CHUNK_SIZE);
                return target;
            case DOWN:
                target.setX(player.getX());
                target.setY(player.getY()+4*Chunk.CHUNK_SIZE);
                return target;
            case RIGHT:
                target.setX(player.getX()+4*Chunk.CHUNK_SIZE);
                target.setY(player.getY());
                return target;
            case LEFT:
                target.setX(player.getX()-4*Chunk.CHUNK_SIZE);
                target.setY(player.getY());
                return target;
            default:
                throw new IllegalArgumentException("Direção inválida "+player.getCurrentDirection());
        }
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
                throw new IllegalArgumentException("The direction can't be "+direction);
        }
    }

    @Override
    protected Point getHomeCorner() {
        return HOME_CORNER;
    }
}
