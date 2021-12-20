package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.passiveObjects.Attack;
import bgu.spl.mics.application.passiveObjects.Diary;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * LeiaMicroservices Initialized with Attack objects, and sends them as  {@link AttackEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link AttackEvent}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LeiaMicroservice extends MicroService {
	private Attack[] attacks;
	private AtomicInteger attacksSucceeded;
	
    public LeiaMicroservice(Attack[] attacks) {
        super("Leia");
		this.attacks = attacks;
        attacksSucceeded = new AtomicInteger(0);
    }

    /**
     * Leia subscribes to 2 messages:
     * 1. BombDetonatedBroadcast, which Lando sends and this informs Leia to terminate.
     * 2. AttackSucceededBroadcast, which C3PO and Han Solo send,
     *    Leia counts how many broadcasts of that type she received and when she
     *    receives the same amount as the attacks she had issued, she informs R2D2 that
     *    it can deactivate the shields using a DeactivationEvent.
     *
     * Leia sends all of her attacks using the sendEvent function.
     */
    @Override
    protected void initialize() {
        try {
            //Making Leia sleep for a few milliseconds so that other microservices will have the time to subscribe to events/broadcasts.
            Thread.sleep(700);
        } catch (InterruptedException e) { }

        subscribeBroadcast(BombDetonatedBroadcast.class, (BombDetonatedBroadcast b) -> {
            terminate();
        });

        subscribeBroadcast(AttackSucceededBroadcast.class, (AttackSucceededBroadcast b) -> {
            attacksSucceeded.compareAndSet(attacksSucceeded.intValue(), attacksSucceeded.intValue() + 1);

            if(attacksSucceeded.intValue() == attacks.length) {
                sendEvent(new DeactivationEvent());
            }
        });

        for(int i = 0; i < attacks.length; i++){
            AttackEvent attackEvent = new AttackEvent(attacks[i].getSerials(), attacks[i].getDuration());
            sendEvent(attackEvent);
        }
    }

    @Override
    protected void close() {
        Diary.getInstance().setLeiaTerminate(System.currentTimeMillis());
    }
}
