package sound;

import javax.sound.sampled.Clip;
import java.util.HashMap;
import java.util.Map;

public enum SoundManager {

    INSTANCE;

    private final Map<String, Sound> sounds;
    private final Map<String, Integer> paused;

    //Singleton
    private SoundManager(){
        sounds = new HashMap<String, Sound>(8);
        paused = new HashMap<String, Integer>(8);
    }

    public void unpauseAll(){
        if (paused.size() == 0) return;
        for (Map.Entry<String, Integer> entry : paused.entrySet()){
            if (entry.getValue() > 0){
                //Som num loop de n vezes
                sounds.get(entry.getKey()).getAudioClip().loop(entry.getValue());
            } else if (entry.getValue() < 0){
                // Som que estava num loop infinito
                sounds.get(entry.getKey()).getAudioClip().loop(-1);
            } else
                sounds.get(entry.getKey()).getAudioClip().start();
        }
    }

    public void pauseAll(){
        paused.clear();
        for (Sound sound : sounds.values()){
            if (sound.getAudioClip().isActive()){
                sound.getAudioClip().stop();
                paused.put(sound.getEventName(), sound.getLoopCycles());
            }
        }
    }

    /**
     * Toca o som, identificado pelo nome do evento, apenas uma vez
     * Quando este metodo é chamado, qualquer som tocando anteriormente tocado vai parar e começar do inicio
     */
    public int play(String event_name){
        if (!sounds.containsKey(event_name))
            throw new IllegalArgumentException("Não há som para o evento: '"+event_name+"'");
        Clip clip = sounds.get(event_name).getAudioClip();
        clip.stop();
        clip.setFramePosition(0);
        clip.start();
        return (int) (clip.getMicrosecondLength()/1000);
    }

    /**
     * Coloca som num loop
     * Ideal para tocar sons de background
     */
    public void loop(String event_name, int loop_cycles){
        if (!sounds.containsKey(event_name))
            throw new IllegalArgumentException("Não há som para o evento: '"+event_name+"'");
        if (sounds.get(event_name).getLoopCycles() != 0) return;
        Clip clip = sounds.get(event_name).getAudioClip();
        if (loop_cycles <= 0){
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            sounds.get(event_name).setLoopCycles(-1);
        } else {
            clip.loop(loop_cycles);
            sounds.get(event_name).setLoopCycles(loop_cycles);
        }
    }

    public void stop(String event_name){
        // Checa se som está na biblioteca
        if (!sounds.containsKey(event_name))
            throw new IllegalArgumentException("Não há som para o evento: '"+event_name+"'");
        // Para de tocar
        Clip clip = sounds.get(event_name).getAudioClip();
        sounds.get(event_name).setLoopCycles(0);
        clip.stop();
        clip.setFramePosition(0);
    }

    public void addSound(Sound sound){
        if (sound == null) throw  new NullPointerException("Som não pode ser null.");
        sounds.put(sound.getEventName(), sound);
    }
}