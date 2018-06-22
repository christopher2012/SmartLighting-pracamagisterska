package smart_lighting;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
	public boolean simulationIsWorking = false;

	public boolean normal = false;
	public boolean visibility = false;
	public boolean eastwest = false;
	public boolean school = false;
	public boolean groupPeople = false;
	public boolean cyclic = false;
	public boolean crossStreet = false;
	public boolean isDangerous = false;
	public boolean isPark = false;

	public ActorsSimulation actorsSimulation;

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
		Circle circle = new Circle(0, 0, 6, Paint.valueOf("FFFFFF"));
		canvas.getChildren().add(circle);

		return new ActorModel(id, circle);
	}

	public ActorModel addGroup(String id) {

		Pane pane = new Pane();

		Circle circle1 = new Circle(-12, -12, 5, Paint.valueOf("FFFFFF"));
		canvas.getChildren().add(circle1);
		Circle circle2 = new Circle(-12.0, 12, 5, Paint.valueOf("FFFFFF"));
		canvas.getChildren().add(circle2);
		Circle circle3 = new Circle(0, 0, 5, Paint.valueOf("FFFFFF"));
		canvas.getChildren().add(circle3);
		Circle circle4 = new Circle(12, 12, 5, Paint.valueOf("FFFFFF"));
		canvas.getChildren().add(circle4);
		Circle circle5 = new Circle(12, -12, 5, Paint.valueOf("FFFFFF"));
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
		dateTimePicker.setDateTimeValue(LocalDateTime.parse(currentTimeLabel.getText(), formatter));
		Button button1 = new Button("Uruchom symulacje");
		button1.setMinWidth(180);
		button1.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				ClockSimulation.getInstance().startTimer();
				actorsSimulation.start();
				simulationIsWorking = true;
				ResultsGenerator.instance().generate();
			}
		});
		Button button2 = new Button("Zatrzymaj symulacje");
		button2.setMinWidth(180);
		button2.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				ClockSimulation.getInstance().stopTimer();
				actorsSimulation.stop();
				simulationIsWorking = false;
				ResultsGenerator.instance().stop();
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
		Alert alert = new Alert(AlertType.CONFIRMATION, "Uruchom ponownie symulator aktorów.");
		alert.setTitle("Komunikat");
		alert.setHeaderText(null);

		CheckBox checkbox = new CheckBox("Uczestnicy ruchu drogowego\n w nocy");
		checkbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
				normal = new_val;
				if (normal) {
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern(AstronomicalClockAgent.TIME_FORMAT);
					dateTimePicker.setDateTimeValue(LocalDateTime.parse("30.06.2018 23:00:00", formatter));
					Simulation.db.setDefaultCoordinates();
					button3.fire();
					alert.show();
				}
			}
		});
		CheckBox checkbox1 = new CheckBox("Szko³a");
		checkbox1.selectedProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
				school = new_val;
				if (school) {
					Simulation.db.setSchoolCoordinates();
					alert.show();
				}
			}
		});
		CheckBox checkbox2 = new CheckBox("Ograniczona widocznoœæ");
		checkbox2.selectedProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
				visibility = new_val;
				if (visibility) {
					locationchoiceBox.getSelectionModel().select(3);
				}
			}
		});
		CheckBox checkbox3 = new CheckBox("Wschód i zachód");
		

		dateTimePicker.valueProperty().addListener(new ChangeListener<LocalDate>() {
			@Override
			public void changed(ObservableValue<? extends LocalDate> observable, LocalDate oldValue,
					LocalDate newValue) {
				if (!oldValue.equals(newValue))
					dateTimePicker.setValue(newValue.plusDays(0));
			}
		});
		checkbox3.selectedProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
				eastwest = new_val;
				if (eastwest) {
					dateTimePicker.setDateTimeValue(LocalDateTime.parse("29.06.2018 20:50:00", formatter));
					dateTimePicker.getEditor().commitValue();
					button3.fire();
				}
			}
		});
		CheckBox checkbox4 = new CheckBox("Grupa ludzi");
		checkbox4.selectedProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
				groupPeople = new_val;
				if (groupPeople) {
					actorsSimulation.actors.add(GuiGenerator.instance().addGroup("2"));
					Simulation.db.activeGroup();
				} else {
					Simulation.db.deactiveGroup();
					actorsSimulation.deleteActor("2");
				}
				alert.show();
			}
		});
		CheckBox checkbox5 = new CheckBox("Cykliczny przejazd");
		checkbox5.selectedProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
				cyclic = new_val;
			}
		});
		CheckBox checkbox6 = new CheckBox("Skrzy¿owanie");
		checkbox6.selectedProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
				crossStreet = new_val;
			}
		});
		CheckBox checkbox7 = new CheckBox("Miejsca niebezpieczne");
		checkbox7.selectedProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
				isDangerous = new_val;
				if (isDangerous) {
					Simulation.db.setDangerousCoordinates();
					alert.show();
				}
			}
		});
		CheckBox checkbox8 = new CheckBox("Park");
		checkbox8.selectedProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
				isPark = new_val;
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
		VBox.setMargin(checkbox1, new Insets(0, 0, 10, 0));
		VBox.setMargin(checkbox4, new Insets(0, 0, 10, 0));
		VBox.setMargin(checkbox5, new Insets(0, 0, 10, 0));
		VBox.setMargin(checkbox6, new Insets(0, 0, 10, 0));
		VBox.setMargin(checkbox7, new Insets(0, 0, 10, 0));
		VBox.setMargin(checkbox8, new Insets(0, 0, 10, 0));
		VBox.setMargin(locationchoiceBox, new Insets(0, 0, 10, 0));
		VBox.setMargin(separator, new Insets(5, 0, 15, 0));
		VBox.setMargin(separator3, new Insets(5, 0, 15, 0));
		VBox.setMargin(separator4, new Insets(5, 0, 15, 0));
		VBox.setMargin(separator5, new Insets(5, 0, 15, 0));

		vbox.getChildren().addAll(label2, currentTimeLabel, separator5, button1, button2, separator, label1,
				dateTimePicker, button3, separator3, label3, checkbox, checkbox2, checkbox3, checkbox1, checkbox4,
				checkbox5, checkbox6, checkbox7, checkbox8, separator4, label4, locationchoiceBox);

		canvas = new Pane();

		ScrollPane scrollPane = new ScrollPane();
		border.setLeft(vbox);
		canvas.getStyleClass().addAll("pane");

		try {

			Image image = new Image(new FileInputStream("\\images\\szkola.png"));
			ImageView imageView = new ImageView(image);

			imageView.setX(550);
			imageView.setY(250);

			imageView.setFitHeight(120);
			imageView.setFitWidth(120);

			Image imgzebra = new Image(new FileInputStream("C:\\Users\\HP\\Desktop\\zebra.png"));
			ImageView imgzebraView = new ImageView(imgzebra);

			imgzebraView.setX(455);
			imgzebraView.setY(70);

			imgzebraView.setFitHeight(60);
			imgzebraView.setFitWidth(40);

			Image tree = new Image(new FileInputStream("C:\\Users\\HP\\Desktop\\tree.png"));
			ImageView tree1 = new ImageView(tree);
			ImageView tree2 = new ImageView(tree);
			ImageView tree3 = new ImageView(tree);
			ImageView tree4 = new ImageView(tree);

			tree1.setX(600);
			tree1.setY(575);
			tree2.setX(650);
			tree2.setY(625);
			tree3.setX(485);
			tree3.setY(575);
			tree4.setX(740);
			tree4.setY(625);

			tree1.setFitHeight(50);
			tree1.setFitWidth(50);
			tree2.setFitHeight(50);
			tree2.setFitWidth(50);
			tree3.setFitHeight(50);
			tree3.setFitWidth(50);
			tree4.setFitHeight(50);
			tree4.setFitWidth(50);

			imageView.setPreserveRatio(true);
			Group root = new Group(imageView);
			imageView.setPreserveRatio(true);
			Group rootzebra = new Group(imgzebraView);
			imageView.setPreserveRatio(true);
			Group root1 = new Group(tree1);
			tree1.setPreserveRatio(true);
			Group root2 = new Group(tree2);
			tree2.setPreserveRatio(true);
			Group root3 = new Group(tree3);
			tree3.setPreserveRatio(true);
			Group root4 = new Group(tree4);
			tree4.setPreserveRatio(true);
			canvas.getChildren().add(root);
			canvas.getChildren().add(rootzebra);
			canvas.getChildren().add(root1);
			canvas.getChildren().add(root2);
			canvas.getChildren().add(root3);
			canvas.getChildren().add(root4);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		scrollPane.setContent(canvas);
		border.setCenter(scrollPane);
		border.setMargin(scrollPane, new Insets(10, 10, 10, 0));
		Scene scene = new Scene(border, 1050, 850);
		scene.getStylesheets().add("css/layoutstyles.css");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

}
