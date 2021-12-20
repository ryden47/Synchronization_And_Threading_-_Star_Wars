package bgu.spl.mics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {
	private ConcurrentHashMap<Class<? extends Event>, ConcurrentLinkedQueue<MicroService>> eventMap;
	private ConcurrentHashMap<Class<? extends Broadcast>, List<MicroService>> broadcastMap;
	private ConcurrentHashMap<MicroService, ConcurrentLinkedQueue<Message>> microserviceMessageQueue;
	private ConcurrentHashMap<Event, Future> eventFutureObjects;

	private static class SingletonHolder {
		private static MessageBusImpl instance = new MessageBusImpl();
	}

	private MessageBusImpl(){
		eventMap = new ConcurrentHashMap<>();
		broadcastMap = new ConcurrentHashMap<>();
		microserviceMessageQueue = new ConcurrentHashMap<>();
		eventFutureObjects= new ConcurrentHashMap<>();
	}

	public static MessageBusImpl getInstance(){
		return SingletonHolder.instance;
	}

	/**
	 * We add the a microservice @param m to the queue of
	 * events of type @param type.
	 * @param type The type to subscribe to,
	 * @param m    The subscribing micro-service.
	 * @param <T>
	 */
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		synchronized (this) {
			if (eventMap.get(type) == null) {
				eventMap.put(type, new ConcurrentLinkedQueue<>());
			}
		}
		eventMap.get(type).add(m);
	}

	/**
	 * We add the a microservice @param m to the queue of
	 * broadcasts of type @param type.
	 * @param type 	The type to subscribe to.
	 * @param m    	The subscribing micro-service.
	 */
	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		synchronized (this) {
			if(broadcastMap.get(type) == null){
				broadcastMap.put(type, new ArrayList<>());
			}
			broadcastMap.get(type).add(m);
		}
	}

	/**
	 * If the event exists in the event-future hashmap (eventFutureObjects)
	 * we resolve the future object.
	 * @param e      The completed event.
	 * @param result The resolved result of the completed event.
	 * @param <T>
	 */
	@Override @SuppressWarnings("unchecked")
	public <T> void complete(Event<T> e, T result) {
		if(eventFutureObjects.get(e) != null)
			eventFutureObjects.get(e).resolve(result);
	}

	/**
	 * We send @param b to every microservice in the
	 * broadcast-queue hashmap (broadcastMap), by adding to the corresponding queue.
	 * We also notify every thread, that a new message has appeared.
	 * @param b 	The message to added to the queues.
	 */
	@Override
	public synchronized void sendBroadcast(Broadcast b) {
		for (MicroService microService : broadcastMap.get(b.getClass())) {
			if(microService != null) {
				if (microserviceMessageQueue.get(microService) == null) /*might be redundant*/
					register(microService);
				microserviceMessageQueue.get(microService).add(b);
				notifyAll();
			}
		}
	}

	/**
	 * We get the first microservice from the queue in the event-queue hash map,
	 * add @param e to its corresponding queue and return the microservice to
	 * the end of the queue, to get the round-robin effect.
	 * We also notify every thread, that a new message has appeared.
	 * @param e     	The event to add to the queue.
	 * @param <T>
	 * @return
	 */
	@Override
	public synchronized <T> Future<T> sendEvent(Event<T> e) {
		if(eventMap.get(e.getClass()) != null) {
			MicroService m = eventMap.get(e.getClass()).poll();
			if(m == null)
				return null;
			microserviceMessageQueue.get(m).add(e);
			eventMap.get(e.getClass()).add(m);
			Future<T> f = new Future<>();
			eventFutureObjects.put(e, f);
			notifyAll();
			return f;
		}
		return null;
	}

	/**
	 * We create a new queue for @param m in the microservice-queue hash map.
	 * @param m the micro-service to create a queue for.
	 */
	@Override
	public void register(MicroService m) {
		if(!microserviceMessageQueue.contains(m)){
			microserviceMessageQueue.put(m, new ConcurrentLinkedQueue<>());
		}
	}

	/**
	 * We remove @param m from the microservice-queue hash map,
	 * and also go through every broadcast/event queue, and remove it
	 * from there, if it exists.
	 * @param m the micro-service to unregister.
	 */
	@Override
	public void unregister(MicroService m) {
		if (microserviceMessageQueue.get(m) == null)
			return;

		microserviceMessageQueue.remove(m);

		Collection<List<MicroService>> broadcastValues = broadcastMap.values();
		for (List<MicroService> lstM : broadcastValues) {
			lstM.remove(m);
		}

		Collection<ConcurrentLinkedQueue<MicroService>> eventValues = eventMap.values();
		for (ConcurrentLinkedQueue<MicroService> EventQueueM : eventValues){
			EventQueueM.remove(m);
		}
	}

	/**
	 * If there are no messages in the queue, we wait until one appears, or another
	 * thread has notified us.
	 * We return the first message from the queue.
	 * @param m The micro-service requesting to take a message from its message
	 *          queue.
	 * @return
	 * @throws InterruptedException
	 */
	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		synchronized (this) {
			if (microserviceMessageQueue.get(m) == null) {
				throw new InterruptedException("This MicroService was never registered to the MessageBus");
			}
			while (microserviceMessageQueue.get(m).isEmpty()) {
				wait(100);
			}
			notifyAll();
			return microserviceMessageQueue.get(m).poll();
		}
	}
}