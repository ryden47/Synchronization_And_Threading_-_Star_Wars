package bgu.spl.mics;

import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.passiveObjects.Attack;
import bgu.spl.mics.application.services.C3POMicroservice;
import bgu.spl.mics.application.services.HanSoloMicroservice;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageBusImplTest {
    MessageBus messageBus;
    MicroService m1;
    MicroService m2;

    @BeforeEach
    void setUp() {
        messageBus = MessageBusImpl.getInstance();
    }

    /*
    After every test we unregister each MicroService.
     */
    @AfterEach
    void tearDown() {
        if(m1 != null){
            messageBus.unregister(m1);
            m1 = null;
        }
        if(m2 != null){
            messageBus.unregister(m2);
            m2 = null;
        }
    }

    /*
    This test tests 3 main functions in MessageBusImpl:
    register, subscribeBroadcast, sendBroadcast.
    In order to send a broadcast to some MicroServices we should first
    register it and then subscribe it to the broadcast.
    Lastly, register and subscribeBroadcast cannot be tested without getters
    so this tests them without getters.
     */
    /*
    In this scenario we create 2 MicroService objects, register both of them,
    subscribe them to broadcast b and then send that broadcast.
    We get that message back using awaitMessage and then test if its the same one we sent, otherwise the test fails.
     */
    @Test
    void sendBroadcast() {
        m1 = new MicroService("basic microservice") {
            @Override
            protected void initialize() {

            }

            @Override
            protected void close(){

            }
        };
        m2 = new MicroService("basic microservice") {
            @Override
            protected void initialize() {

            }

            @Override
            protected void close(){

            }
        };
        Broadcast b = new Broadcast() {};
        messageBus.register(m1);
        messageBus.register(m2);
        messageBus.subscribeBroadcast(b.getClass(), m1);
        messageBus.subscribeBroadcast(b.getClass(), m2);
        messageBus.sendBroadcast(b);
        try {
            Message returned = messageBus.awaitMessage(m1);
            assertNotNull(returned);
            assertEquals(b, returned);
        } catch (InterruptedException e) { fail(); }
        try {
            Message returned = messageBus.awaitMessage(m2);
            assertNotNull(returned);
            assertEquals(b, returned);
        } catch (InterruptedException e) { fail(); }
    }

    /*
    This test tests 3 main functions in MessageBusImpl:
    register, subscribeEvent, sendEvent.
    In order to send an event to some MicroServices we should first
    register it and then subscribe it to the event.
    Lastly, register and subscribeEvent cannot be tested without getters
    so this tests them without getters.
     */
    /*
    In this scenario we create 2 MicroService objects, register both of them,
    subscribe them to events e1, e2 respectively and then send those events.
    We get those events back using awaitMessage and then test if its the same one we sent, otherwise the test fails.
    Also this tests the "round-robin" functionality, we also test that the first MicroService receives the first event
    and that the second MicroService receives the second event.
     */
    @Test
    void sendEvent() {
        m1 = new MicroService("basic microservice") {
            @Override
            protected void initialize() {

            }

            @Override
            protected void close(){

            }
        };
        m2 = new MicroService("basic microservice") {
            @Override
            protected void initialize() {

            }

            @Override
            protected void close(){

            }
        };
        AttackEvent e1 = new AttackEvent();
        AttackEvent e2 = new AttackEvent();
        messageBus.register(m1);
        messageBus.register(m2);
        messageBus.subscribeEvent(e1.getClass(), m1);
        messageBus.subscribeEvent(e1.getClass(), m2);
        messageBus.sendEvent(e1);
        messageBus.sendEvent(e2);
        try {
            Message returned = messageBus.awaitMessage(m1);
            assertNotNull(returned);
            assertEquals(e1, returned);
        } catch (InterruptedException e) { fail(); }
        try {
            Message returned = messageBus.awaitMessage(m2);
            assertNotNull(returned);
            assertEquals(e2, returned);
        } catch (InterruptedException e) { fail(); }
    }

    /*
    This test also tests the subscribeEvent and register functions.
    */
    /*
    In this test we test whether or not a MicroService receives a message that has been sent.
    The first try/catch tests if the function throws an exception when the microService hasn't been registered yet.
     */
    @Test
    void awaitMessage() {
        m1 = new MicroService("basic microservice") {
            @Override
            protected void initialize() {

            }

            @Override
            protected void close(){

            }
        };
        AttackEvent e = new AttackEvent();
        try {
            messageBus.awaitMessage(m1);
            fail();
        } catch (InterruptedException exception) { }
        messageBus.register(m1);
        messageBus.subscribeEvent(e.getClass(), m1);
        messageBus.sendEvent(e);
        try {
            assertEquals(e, messageBus.awaitMessage(m1));
        } catch (InterruptedException exception) {
            fail();
        }
    }

    /*
    This test also tests the subscribeEvent and register functions.
     */
    /*
    This test tests whether or not the complete function actually changes the Future object returned
    from sendEvent. We are looking for f.isDone = true and f.get()=true, because the result we send is 'true'.
     */
    @Test
    void complete() {
        m1 = new MicroService("basic microservice") {
            @Override
            protected void initialize() {

            }

            @Override
            protected void close(){

            }
        };
        AttackEvent e = new AttackEvent();
        messageBus.register(m1);
        messageBus.subscribeEvent(e.getClass(), m1);
        Future<Boolean> f = messageBus.sendEvent(e);
        assertFalse(f.isDone());
        messageBus.complete(e, true);
        assertTrue(f.isDone());
        assertTrue(f.get());
    }
}