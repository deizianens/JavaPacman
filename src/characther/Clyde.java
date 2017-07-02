package characther;

import java.awt.*;
import map.ChunkedMap.Chunk;
import map.CollisionTest.NextDirection;
import map.Point;

/**
 * Clyde é o ghost laranja
 */
public class Clyde extends Ghost{

    private static final Point HOME_CORNER = new Point(0, Chunk.CHUNK_SIZE*31);

    private Point target;

    private final Image[] ghost_left;
    private final Image[] ghost_right;
    private final Image[] ghost_up;
    private final Image[] ghost_down;

    protected Clyde(Pacman player) {
        super(player);
        target = new Point(0, 0);
        ghost_down = new Image[]{
                loadImageResource("clyde/clyde_down_1.png"),
                loadImageResource("clyde/clyde_down_2.png")
        };
        ghost_left = new Image[]{
                loadImageResource("clyde/clyde_left_1.png"),
                loadImageResource("clyde/clyde_left_2.png")
        };
        ghost_up = new Image[]{
                loadImageResource("clyde/clyde_up_1.png"),
                loadImageResource("clyde/clyde_up_2.png")
        };
        ghost_right = new Image[]{
                loadImageResource("clyde/clyde_right_1.png"),
                loadImageResource("clyde/clyde_right_2.png")
        };
    }

    @Override
    protected Point getTargetChunk(Pacman player) {
        if ( (distanceToPacman(player.getX(), player.getY()) / Chunk.CHUNK_SIZE) <= 8){
            // Muito perto, vai para longe
            target.setX(HOME_CORNER.getX());
            target.setY(HOME_CORNER.getY());
            return target;
        } else {
            // Não está perto, persegue o Pacman
            target.setX(player.getX());
            target.setY(player.getY());
            return target;
        }
    }

    private int distanceToPacman(int x, int y){
        int triangle_x = x - this.x;
        int triangle_y = y - this.y;
        return (int) Math.sqrt((triangle_x*triangle_x)+(triangle_y*triangle_y));
    }

    @Override
    protected Image getGhostImage(NextDirection direction, int image_index) {
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
