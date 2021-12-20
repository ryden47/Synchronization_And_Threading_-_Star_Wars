package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.BombDestroyerEvent;
import bgu.spl.mics.application.messages.BombDetonatedBroadcast;
import bgu.spl.mics.application.passiveObjects.Diary;

/**
 * LandoMicroservice
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LandoMicroservice  extends MicroService {
    private long duration;

    public LandoMicroservice(long duration) {
        super("Lando");
        this.duration = duration;
    }

    /**
     * Lando subscribes to 2 messages:
     * 1. BombDetonatedBroadcast, which Lando sends to inform others to terminate.
     * 2. BombDestroyerEVent, which R2D2 sends, Lando performs his action (sleeps) and sends a broadcast
     *    that the bomb has detonated.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(BombDetonatedBroadcast.class, (BombDetonatedBroadcast b) -> {
            terminate();
        });

        subscribeEvent(BombDestroyerEvent.class, (BombDestroyerEvent bombDestroyerEvent) -> {
            try {
                Thread.sleep(duration);
            } catch (InterruptedException e) { }
            complete(bombDestroyerEvent, true);
            sendBroadcast(new BombDetonatedBroadcast());
            terminate();
        });
    }

    @Override
    protected void close() {
        Diary.getInstance().setLandoTerminate(System.currentTimeMillis());
    }
}
