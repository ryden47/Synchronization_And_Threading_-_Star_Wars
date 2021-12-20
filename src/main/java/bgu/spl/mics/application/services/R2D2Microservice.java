package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.BombDestroyerEvent;
import bgu.spl.mics.application.messages.BombDetonatedBroadcast;
import bgu.spl.mics.application.messages.DeactivationEvent;
import bgu.spl.mics.application.passiveObjects.Diary;

/**
 * R2D2Microservices is in charge of the handling {@link DeactivationEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link DeactivationEvent}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class R2D2Microservice extends MicroService {
    private long duration;

    public R2D2Microservice(long duration) {
        super("R2D2");
        this.duration = duration;
    }

    /**
     * R2D2 subscribes to 2 messages:
     * 1. BombDetonatedBroadcast, which Lando sends and this informs R2D2 to terminate.
     * 2. DeactivationEvent, which Leia sends, R2D2 performs the action (sleeps) and lastly informs Lando with a BombDestroyerEvent.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(BombDetonatedBroadcast.class, (BombDetonatedBroadcast b) -> {
            terminate();
        });

        subscribeEvent(DeactivationEvent.class, (DeactivationEvent deactivationEvent) -> {
            try {
                Thread.sleep(duration);
            } catch (InterruptedException e) { }
            Diary.getInstance().setR2D2Deactivate(System.currentTimeMillis());
            complete(deactivationEvent, true);
            sendEvent(new BombDestroyerEvent());
        });
    }

    @Override
    protected void close() {
        Diary.getInstance().setR2D2Terminate(System.currentTimeMillis());
    }
}
