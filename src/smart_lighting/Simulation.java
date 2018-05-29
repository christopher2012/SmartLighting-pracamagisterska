package smart_lighting;

import java.util.ArrayList;

import org.neo4j.driver.v1.types.Node;

import analytic.Manager;
import javafx.application.Application;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class Simulation extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	public void start() {
		launch();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		GuiGenerator.instance().generateWindow(primaryStage);
		loadData();
	}

	private void loadData() {
		DatabaseManager databaseManager = new DatabaseManager("bolt://localhost:7687", "neo4j", "test");
		GuiGenerator.instance().addRoads(databaseManager);
		Manager.onDataLoadListener.onDataLoad(GuiGenerator.instance().addStreetLamps(databaseManager));
		ArrayList<ActorModel> actors = new ArrayList<>();
		actors.add(GuiGenerator.instance().addActor("KR01112"));
		actors.add(GuiGenerator.instance().addActor("KR01113"));
		Manager.onCreateActorsListener.onCreateActors(actors);
	}

	public static class StreetLampModel {
		public String id;
		public Node node;
		public Circle circle;

		public StreetLampModel(Circle circle, Node node) {
			this.node = node;
			this.circle = circle;
			this.id = node.get("node_id").asString();
		}
	}

	public static class ActorModel {
		public String id;
		public javafx.scene.Node node;

		public ActorModel(String id, javafx.scene.Node node) {
			this.id = id;
			this.node = node;
		}
	}

	public interface OnDataLoadListener {
		void onDataLoad(ArrayList<StreetLampModel> streetLamps);
	}

	public interface OnCreateActorsListener {
		void onCreateActors(ArrayList<ActorModel> actors);
	}

}
