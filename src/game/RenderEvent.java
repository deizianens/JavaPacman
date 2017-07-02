package game;

import java.awt.*;
import java.io.IOException;

public interface RenderEvent {

    public void render(Graphics g) throws IOException;

}