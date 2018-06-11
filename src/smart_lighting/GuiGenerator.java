package smart_lighting;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.neo4j.driver.v1.types.Node;

import agents.lowlevel.AstronomicalClockAgent;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import smart_lighting.DatabaseManager.Road;
import smart_lighting.Simulation.ActorModel;
import smart_lighting.Simulation.StreetLampModel;
import tornadofx.control.DateTimePicker;
import utils.StreetLightInfo;

public class GuiGenerator {

	private static GuiGenerator guiGenerator = null;
	private Pane canvas;
	private Label currentTimeLabel;
	private Map<String, StreetLightInfo> tooltips = new HashMap<>();

	public static GuiGenerator instance() {
		if (guiGenerator == null)
			guiGenerator = new GuiGenerator();

		return guiGenerator;
	}

	public StreetLightInfo getStreetLightInfo(String id) {
		return tooltips.get(id);
	}

	public void setCurrentTime(String currentTime) {

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				currentTimeLabel.setText(currentTime);
			}
		});
	}

	public void addRoads(DatabaseManager databaseManager) {
		for (Road road : databaseManager.getRoadsData()) {
			System.out.println(road.fromVertexName + " - " + road.toVertexName);
			Line line = new Line();
			line.setStartX(road.fromVertex.get("x").asDouble());
			line.setEndX(road.toVertex.get("x").asDouble());
			line.setStartY(road.fromVertex.get("y").asDouble());
			line.setEndY(road.toVertex.get("y").asDouble());
			line.setStrokeWidth(4);
			canvas.getChildren().add(line);
		}
	}

	public ArrayList<StreetLampModel> addStreetLamps(DatabaseManager databaseManager) {

		double maxX = 0.0;
		double maxY = 0.0;
		ArrayList<StreetLampModel> streetLamps = new ArrayList();

		for (Node node : databaseManager.getDataDevices()) {
			String id = node.get("node_id").asString();
			double x = node.get("x").asDouble();
			double y = node.get("y").asDouble();

			if (x > maxX)
				maxX = x;
			if (y > maxY)
				maxY = y;

			Circle circle = new Circle(x, y, 5, Paint.valueOf("FFFF80"));
			Circle circleTop = new Circle(x, y, 15, Paint.valueOf("9FA1A5"));
			circle.setOpacity(0.5);
			circleTop.setOpacity(0.7);
			/*
			 * RadialGradient gradient1 = new RadialGradient(0, .1, 100, 100,
			 * 20, false, CycleMethod.NO_CYCLE, new Stop(0, Color.RED), new
			 * Stop(1, Color.BLACK));
			 * 
			 * circle.setFill(gradient1);
			 */
			Tooltip tooltip = new Tooltip();
			Tooltip.install(circleTop, tooltip);
			tooltips.put(id, new StreetLightInfo(id, tooltip));

			canvas.getChildren().add(circle);
			canvas.getChildren().add(circleTop);
			canvas.setMinHeight(maxY + 100);
			canvas.setMinWidth(maxX + 100);
			streetLamps.add(new Simulation.StreetLampModel(circle, node));
		}

		return streetLamps;
	}

	public ActorModel addPedestrian(String id) {
		Circle circle = new Circle(0, 0, 8, Paint.valueOf("FFFFFF"));
		canvas.getChildren().add(circle);

		return new ActorModel(id, circle);
	}
	
	public ActorModel addGroup(String id) {

		Pane pane = new Pane();
		
		Circle circle1 = new Circle(-12, -12, 6, Paint.valueOf("FFFFFF"));
		canvas.getChildren().add(circle1);
		Circle circle2 = new Circle(-12.0, 12, 6, Paint.valueOf("FFFFFF"));
		canvas.getChildren().add(circle2);
		Circle circle3 = new Circle(0, 0, 6, Paint.valueOf("FFFFFF"));
		canvas.getChildren().add(circle3);
		Circle circle4 = new Circle(12, 12, 6, Paint.valueOf("FFFFFF"));
		canvas.getChildren().add(circle4);
		Circle circle5 = new Circle(12, -12, 6, Paint.valueOf("FFFFFF"));
		canvas.getChildren().add(circle5);

		pane.getChildren().add(circle1);
		pane.getChildren().add(circle2);
		pane.getChildren().add(circle3);
		pane.getChildren().add(circle4);
		pane.getChildren().add(circle5);
		
		canvas.getChildren().add(pane);

		return new ActorModel(id, pane);
	}
	
	public ActorModel addVehicle(String id) {
				
		Rectangle car = new Rectangle();

		car.setX(-10);
		car.setY(-10);
		car.setWidth(20.0f);
		car.setHeight(20.0f);

		car.setArcWidth(5);
		car.setArcHeight(5);
		canvas.getChildren().add(car);

		return new ActorModel(id, car);
	}

	public void generateWindow(Stage primaryStage) {

		BorderPane border = new BorderPane();
		SimpleDateFormat dateFormat = new SimpleDateFormat(AstronomicalClockAgent.TIME_FORMAT);
		currentTimeLabel = new Label();
		currentTimeLabel.setText(dateFormat.format(new Date()));
		currentTimeLabel.setMaxWidth(180);
		currentTimeLabel.setAlignment(Pos.CENTER);
		VBox vbox = new VBox();
		DateTimePicker dateTimePicker = new DateTimePicker();
		dateTimePicker.setMinWidth(180);
		dateTimePicker.setFormat(AstronomicalClockAgent.TIME_FORMAT);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(AstronomicalClockAgent.TIME_FORMAT);
		dateTimePicker.setValue(LocalDate.parse(currentTimeLabel.getText(), formatter));
		Button button1 = new Button("Uruchom symulacje");
		button1.setMinWidth(180);
		button1.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				ClockSimulation.getInstance().startTimer();
			}
		});
		Button button2 = new Button("Zatrzymaj symulacje");
		button2.setMinWidth(180);
		button2.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				ClockSimulation.getInstance().stopTimer();
			}
		});
		Button button3 = new Button("Zastosuj");
		button3.setMinWidth(180);
		button3.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				ClockSimulation.getInstance().updateTime(dateTimePicker.getDateTimeValue().format(formatter));
			}
		});
		vbox.setPadding(new Insets(10, 10, 10, 10));
		Label label1 = new Label();
		label1.setText("Zmiana czasu symulacji:");
		label1.setMaxWidth(180);
		label1.setAlignment(Pos.CENTER);
		label1.setStyle("-fx-font: normal bold 15px 'serif' ");
		Label label2 = new Label();
		label2.setText("Aktualny czas:");
		label2.setMaxWidth(180);
		label2.setAlignment(Pos.CENTER);
		label2.setStyle("-fx-font: normal bold 15px 'serif' ");
		Label label3 = new Label();
		label3.setText("Scenariusze:");
		label3.setMaxWidth(180);
		label3.setAlignment(Pos.CENTER);
		label3.setStyle("-fx-font: normal bold 15px 'serif' ");
		Label label4 = new Label();
		label4.setText("Zachmurzenie:");
		label4.setMaxWidth(180);
		label4.setAlignment(Pos.CENTER);
		label4.setStyle("-fx-font: normal bold 15px 'serif' ");

		CheckBox checkbox = new CheckBox("Samochód osobowy");
		CheckBox checkbox2 = new CheckBox("Zorganizowana demostracja");
		CheckBox checkbox3 = new CheckBox("Pieszy przy ulicy");

		ChoiceBox locationchoiceBox = new ChoiceBox();
		locationchoiceBox.getItems().addAll(CloudinessSimulation.getInstance().getCloudinnessItem());
		locationchoiceBox.setMinWidth(180);
		locationchoiceBox.getSelectionModel().selectFirst();
		locationchoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				CloudinessSimulation.getInstance().setCurrentItem(newValue.intValue());
			}
		});

		Line separator = new Line(0, 0, 180, 0);
		Line separator3 = new Line(0, 0, 180, 0);
		Line separator4 = new Line(0, 0, 180, 0);
		Line separator5 = new Line(0, 0, 180, 0);

		VBox.setMargin(dateTimePicker, new Insets(0, 0, 10, 0));
		VBox.setMargin(button1, new Insets(0, 0, 10, 0));
		VBox.setMargin(button2, new Insets(0, 0, 10, 0));
		VBox.setMargin(button3, new Insets(0, 0, 10, 0));
		VBox.setMargin(label2, new Insets(0, 0, 10, 0));
		VBox.setMargin(label1, new Insets(0, 0, 10, 0));
		VBox.setMargin(label3, new Insets(0, 0, 10, 0));
		VBox.setMargin(label4, new Insets(0, 0, 10, 0));
		VBox.setMargin(currentTimeLabel, new Insets(0, 0, 10, 0));
		VBox.setMargin(checkbox, new Insets(0, 0, 10, 0));
		VBox.setMargin(checkbox2, new Insets(0, 0, 10, 0));
		VBox.setMargin(checkbox3, new Insets(0, 0, 10, 0));
		VBox.setMargin(locationchoiceBox, new Insets(0, 0, 10, 0));
		VBox.setMargin(separator, new Insets(5, 0, 15, 0));
		VBox.setMargin(separator3, new Insets(5, 0, 15, 0));
		VBox.setMargin(separator4, new Insets(5, 0, 15, 0));
		VBox.setMargin(separator5, new Insets(5, 0, 15, 0));

		vbox.getChildren().addAll(label2, currentTimeLabel, separator5, button1, button2, separator, label1,
				dateTimePicker, button3, separator3, label3, checkbox, checkbox2, checkbox3, separator4, label4,
				locationchoiceBox);

		canvas = new Pane();

		ScrollPane scrollPane = new ScrollPane();
		border.setLeft(vbox);
		canvas.getStyleClass().addAll("pane");

		scrollPane.setContent(canvas);
		border.setCenter(scrollPane);
		border.setMargin(scrollPane, new Insets(10, 10, 10, 0));
		Scene scene = new Scene(border, 1200, 800);
		scene.getStylesheets().add("css/layoutstyles.css");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

}
