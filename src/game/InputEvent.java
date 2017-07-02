package game;

import java.awt.event.KeyEvent;

public interface InputEvent {

     //Determina os tipos de eventos disparados pelo teclado
    public enum KeyEventType{
        PRESSED, RELEASED
    }

    public void keyboardInput(KeyEvent event, KeyEventType type);
}