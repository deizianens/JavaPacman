package game;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

class GameCanvas extends JPanel {

    private Image dbImage;
    private Graphics dbg;
    private List<RenderContainer> renderEvents;

    /**
     * Construtor package-private. É inicializado apenas por GameLoop
     */
    GameCanvas() {
        dbg = this.getGraphics();
    }

    void setRenderEvents(List<RenderContainer> renderEvents) {
        this.renderEvents = renderEvents;
        Collections.sort(this.renderEvents);
    }

    public Graphics getBufferedGraphics() {
        return dbg;
    }

    @Override
    public void paint(Graphics g) {
        if (renderEvents == null || renderEvents.size() < 1) {
            return;
        }
        for (RenderContainer event : renderEvents) {
            try {
                event.getEvent().render(g);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void update(Graphics g) {
        // Inicializa os DoubleBuffers
        if (dbImage == null) {
            dbImage = createImage(this.getSize().width, this.getSize().height);
            dbg = dbImage.getGraphics();
        }
        
        //Limpa background
        dbg.setColor(getBackground());
        dbg.fillRect(0, 0, this.getSize().width, this.getSize().height);

        dbg.setColor(getForeground());
        paint(dbg);

        g.drawImage(dbImage, 0, 0, this);
    }

}
