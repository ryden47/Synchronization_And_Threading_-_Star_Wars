package bgu.spl.mics.application;

import bgu.spl.mics.application.passiveObjects.Attack;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.Ewoks;
import bgu.spl.mics.application.services.*;

import com.google.gson.*;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** This is the Main class of the application. You should parse the input file,
 * create the different components of the application, and run the system.
 * In the end, you should output a JSON.
 */
public class Main {
	public static void main(String[] args) {
		long LandoLong = 0;
		long R2D2Long = 0;
		Attack[] attacks = null;
		Thread LandoThread;
		Thread R2D2Thread;
		Thread LeiaThread;
		Thread C3POThread;
		Thread HanSoloThread; //First we assign the variables

		//Using a JsonParser we parse the data from the file and insert
		//it to the right variable.
		JsonParser parser = new JsonParser();
		try { /* Parsing the json*/
			Object obj = parser.parse(new FileReader(args[0]));
			JsonObject jsonObject = (JsonObject) obj;

			createEwoks(jsonObject);

			LandoLong = jsonObject.get("Lando").getAsLong();

			R2D2Long = jsonObject.get("R2D2").getAsLong();

			attacks = createAttacks(jsonObject);
		} catch (Exception e) { }

		//We create every microservice and make a new thread using it.
		LandoMicroservice Lando = new LandoMicroservice(LandoLong);
		LandoThread = new Thread(Lando);

		R2D2Microservice R2D2 = new R2D2Microservice(R2D2Long);
		R2D2Thread = new Thread(R2D2);

		LeiaMicroservice Leia = new LeiaMicroservice(attacks);
		LeiaThread = new Thread(Leia);

		C3POMicroservice C3PO = new C3POMicroservice();
		C3POThread = new Thread(C3PO);

		HanSoloMicroservice HanSolo = new HanSoloMicroservice();
		HanSoloThread = new Thread(HanSolo);

		//Setting the threads name, for debugging purposes.
		LandoThread.setName("Lando");
		R2D2Thread.setName("R2D2");
		LeiaThread.setName("Leia");
		C3POThread.setName("C3PO");
		HanSoloThread.setName("HanSolo");

		//Starting the threads.
		LandoThread.start();
		R2D2Thread.start();
		C3POThread.start();
		HanSoloThread.start();
		LeiaThread.start();

		//Waiting until every thread has finished.
		try {
			LandoThread.join();
			R2D2Thread.join();
			C3POThread.join();
			HanSoloThread.join();
			LeiaThread.join();
		} catch (InterruptedException e) {
		}

		//Create the output file, with the path to it.
		createOutputFile(args[1]);
	}

	/**
	 * We parse the amount of ewoks from the file
	 * and add the same amount to Ewoks.
	 * @param jsonObject We receive the object that has the file data in it.
	 *                   We could also have made it static, so to not place it as a parameter.
	 */
	private static void createEwoks(JsonObject jsonObject){
		int ewoksAmount = jsonObject.get("Ewoks").getAsInt();
		for (int i = 0; i < ewoksAmount; i++) {
			Ewoks.getInstance().addEwok();
		}
	}

	/**
	 * We parse the array from the file as a JsonArray object,
	 * and then parse that data to our own Attack objects.
	 * @param jsonObject We receive the object that has the file data in it.
	 * 	 *               We could also have made it static, so to not place it as a parameter.
	 * @return the Attack array that Leia uses.
	 */
	private static Attack[] createAttacks(JsonObject jsonObject){
		JsonArray jsonArray = jsonObject.get("attacks").getAsJsonArray();
		Attack[] attacks = new Attack[jsonArray.size()];
		int attacksIndex = 0;
		for (Object o : jsonArray){
			JsonObject attack = (JsonObject) o;

			int duration = attack.get("duration").getAsInt();
			JsonArray serialsAsArray = attack.get("serials").getAsJsonArray();
			List<Integer> serials = new ArrayList<>();

			for (int i = 0; i < serialsAsArray.size(); i++) {
				serials.add(serialsAsArray.get(i).getAsInt());
			}

			attacks[attacksIndex] = new Attack(serials, duration);

			attacksIndex++;
		}
		return attacks;
	}

	/**
	 * We create a JsonObject and add the Diary data to it,
	 * we then write that object to a file.
	 * @param path the path to the output file.
	 */
	private static void createOutputFile(String path){
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("totalAttacks", new JsonPrimitive(Diary.getInstance().getTotalAttacks()));
		jsonObject.add("HanSoloFinish", new JsonPrimitive(Diary.getInstance().getHanSoloFinish()));
		jsonObject.add("C3POFinish", new JsonPrimitive(Diary.getInstance().getC3POFinish()));
		jsonObject.add("R2D2Deactivate", new JsonPrimitive(Diary.getInstance().getR2D2Deactivate()));
		jsonObject.add("LeiaTerminate", new JsonPrimitive(Diary.getInstance().getLeiaTerminate()));
		jsonObject.add("HanSoloTerminate", new JsonPrimitive(Diary.getInstance().getHanSoloTerminate()));
		jsonObject.add("C3POTerminate", new JsonPrimitive(Diary.getInstance().getC3POTerminate()));
		jsonObject.add("R2D2Terminate", new JsonPrimitive(Diary.getInstance().getR2D2Terminate()));
		jsonObject.add("LandoTerminate", new JsonPrimitive(Diary.getInstance().getLandoTerminate()));
		try {
			FileWriter file = new FileWriter(path);
			file.write(jsonObject.toString());
			file.close();
		} catch (IOException e) { }
	}
}
