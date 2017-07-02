package sound;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import java.net.URL;

public class Sound {

    private Clip audio;
    //evento que dispara o som
    private final String event;
    private int loop_cycles;

    public Sound(String event, URL sound_res){
        this.event = event;
        try {
            audio = AudioSystem.getClip();
            audio.open(AudioSystem.getAudioInputStream(sound_res));
            audio.drain();
        } catch (LineUnavailableException e){
            System.err.println("Erro ao executar audio. Algo pode estar bloqueando a saida de audio.");
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    String getEventName(){
        return this.event;
    }

    Clip getAudioClip(){
        return this.audio;
    }

    /**
     * Se loop_cycles = 0 som não está num loop
     * Se loop_cycles < 0 o som está num loop infinito
     */
    int getLoopCycles() {
        return loop_cycles;
    }

    void setLoopCycles(int cycles) {
        loop_cycles = cycles;
    }
}
