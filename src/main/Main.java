package main;

import characther.Cage;
import characther.Pacman;
import game.GameLoop;
import game.GameState;
import map.ChunkedMap;
import sound.Sound;
import sound.SoundManager;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Timer;
import java.util.TimerTask;


public class Main {

    private JFrame f;
    //previne que o jogo inicie quando a intro estiver sendo exibida
    private boolean first_launch;

    private Main(){
        first_launch = true;
        populateWindow();
        addFigures();
        addSounds();
        // Inicia jogo
        GameLoop.INSTANCE.startLoop();
        // Pausa jogo para exibir intro
        GameLoop.INSTANCE.pause();
        SoundManager.INSTANCE.play("intro");
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                GameLoop.INSTANCE.play();
                first_launch = false;
            }
        }, 4000);
    }

    private void addSounds(){
        SoundManager.INSTANCE.addSound(new Sound("intro",
                Main.class.getResource("res/sound/intro.wav")));
        SoundManager.INSTANCE.addSound(new Sound("dieing",
                Main.class.getResource("res/sound/dieing.wav")));
        SoundManager.INSTANCE.addSound(new Sound("round_over",
                Main.class.getResource("res/sound/round_over.wav")));
        SoundManager.INSTANCE.addSound(new Sound("eat",
                Main.class.getResource("res/sound/eat.wav")));
        SoundManager.INSTANCE.addSound(new Sound("eat_fruit",
                Main.class.getResource("res/sound/eat_fruit.wav")));
    }

    private void addFigures(){
        ChunkedMap map = new ChunkedMap( // Usa tamanho de canvas para gerar mapa
                GameLoop.INSTANCE.getView().getWidth(),
                GameLoop.INSTANCE.getView().getHeight()
        );
        GameLoop.INSTANCE.setMap(map);
        GameLoop.INSTANCE.addRenderEvent(map, map.getZIndex());
        GameLoop.INSTANCE.addRenderEvent(GameState.INSTANCE, 0);
        Pacman pacman = new Pacman(map.getStartPoint());
        GameLoop.INSTANCE.addRenderEvent(pacman,pacman.getZIndex());
        GameLoop.INSTANCE.addInputEvent(pacman);
        GameLoop.INSTANCE.addCollisionEvent(pacman);
        GameLoop.INSTANCE.addMovementEvent(pacman);
        Cage cage = new Cage(map.getCagePoint(), pacman);
        GameLoop.INSTANCE.addRenderEvent(cage, 2);
    }

    private void populateWindow(){
        f = new JFrame("Pacman");
        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                GameLoop.INSTANCE.stopLoop();
                //Fecha aplicação
                System.exit(0);
            }
        });
        f.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                if (!first_launch){
                    GameLoop.INSTANCE.play();
                    SoundManager.INSTANCE.unpauseAll();
                }
            }
            @Override
            public void windowLostFocus(WindowEvent e) {
                GameLoop.INSTANCE.pause();
                SoundManager.INSTANCE.pauseAll();
            }
        });
        f.setSize(470, 580);
        f.add(GameLoop.INSTANCE.getView());
        f.setVisible(true);
        f.setLocationRelativeTo(null);
        f.addKeyListener(GameLoop.INSTANCE);
    }

    public static void main(String[] args){
        new Main();
    }
}