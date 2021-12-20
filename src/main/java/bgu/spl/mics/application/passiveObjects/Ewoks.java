package bgu.spl.mics.application.passiveObjects;

import java.util.ArrayList;
import java.util.List;

/**
 * Passive object representing the resource manager.
 * <p>
 * This class must be implemented as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private methods and fields to this class.
 */
public class Ewoks {
    private List<Ewok> ewoks;
    private int serialNumbers = 1;

    private static class SingletonHolder {
        private static Ewoks instance = new Ewoks();
    }

    private Ewoks(){
        ewoks = new ArrayList();
    }

    public static Ewoks getInstance(){
        return Ewoks.SingletonHolder.instance;
    }

    public void addEwok(){
        ewoks.add(new Ewok(serialNumbers));
        serialNumbers++;
    }

    public void acquireEwok(int serial){
        ewoks.get(serial - 1).acquire();
    }

    public void releaseEwok(int serial){
        ewoks.get(serial - 1).release();
    }
}
