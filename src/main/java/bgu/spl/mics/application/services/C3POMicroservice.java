package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.AttackSucceededBroadcast;
import bgu.spl.mics.application.messages.BombDetonatedBroadcast;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.Ewoks;

import java.util.Comparator;

/**
 * C3POMicroservices is in charge of the handling {@link AttackEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link AttackEvent}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class C3POMicroservice extends MicroService {
	
    public C3POMicroservice() {
        super("C3PO");
    }

    /**
     * C3PO subscribes to 2 messages:
     * 1. BombDetonatedBroadcast, which Lando sends and this informs C3PO to terminate.
     * 2. AttackEvent, which Leia sends, C3PO acquires the correct ewoks
     *    and then sleeps for the correct amount of time. Finally he releases the ewoks.
     *
     * C3PO sends a broadcast every time he finished an attack, to inform Leia.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(BombDetonatedBroadcast.class, (BombDetonatedBroadcast b) -> {
            terminate();
        });

        subscribeEvent(AttackEvent.class, (AttackEvent attackEvent) -> {
            //We sort the serial numbers of the ewoks to avoid deadlocks,
            //Like if HanSolo needs 1,2 and C3PO needs 2,1
            //But HanSolo got 1 and C3PO got 2.
            attackEvent.getSerials().sort(new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    if (o1 >= o2)
                        return o1;
                    else return o2;
                }
            });

            for (Integer i : attackEvent.getSerials()){
                Ewoks.getInstance().acquireEwok(i);
            }

            try {
                Thread.sleep((long)attackEvent.getDuration());
            } catch (InterruptedException e) { }

            Diary.getInstance().addAttack();
            Diary.getInstance().setC3POFinish(System.currentTimeMillis());
            complete(attackEvent, true);
            sendBroadcast(new AttackSucceededBroadcast());

            for (Integer i : attackEvent.getSerials()){
                Ewoks.getInstance().releaseEwok(i);
            }
        });
    }

    @Override
    protected void close() {
        Diary.getInstance().setC3POTerminate(System.currentTimeMillis());
    }
}
