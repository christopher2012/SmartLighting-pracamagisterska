package smart_lighting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.types.Node;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import smart_lighting.DatabaseManager.Road;
import tornadofx.control.DateTimePicker;

public class Main extends Application {

	Thread thread;

	public static void main(String[] args) {
		launch();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		generateView(primaryStage);

	}

	private void generateView(Stage primaryStage) throws Exception {
		try (DatabaseManager helloWorldExample = new DatabaseManager("bolt://localhost:7687", "neo4j", "test")) {

			BorderPane border = new BorderPane();
			VBox vbox = new VBox();
			DateTimePicker dateTimePicker = new DateTimePicker();
			dateTimePicker.setMinWidth(180);
			Button button1 = new Button("Uruchom symulacje");
			button1.setMinWidth(180);
			button1.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					thread.start();

				}
			});
			Button button2 = new Button("Zatrzymaj symulacje");
			button2.setMinWidth(180);
			button2.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					thread.stop();
					// TODO Auto-generated method stub

				}
			});
			Button button3 = new Button("Zastosuj");
			button3.setMinWidth(180);
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
			Label label5 = new Label();
			label5.setText("14.02.2018 10:11:12 (DZIEÑ)");
			label5.setMaxWidth(180);
			label5.setAlignment(Pos.CENTER);
			Label label6 = new Label();
			label6.setText("Szybkoœæ symulacji");
			label6.setMaxWidth(180);
			label6.setAlignment(Pos.CENTER);
			label6.setStyle("-fx-font: normal bold 15px 'serif' ");

			CheckBox checkbox = new CheckBox("Samochód osobowy");
			CheckBox checkbox2 = new CheckBox("Zorganizowana demostracja");
			CheckBox checkbox3 = new CheckBox("Pieszy przy ulicy");

			ChoiceBox locationchoiceBox = new ChoiceBox();
			locationchoiceBox.getItems().addAll("BRAK", "MA£E", "ŒREDNIE", "DU¯E");
			locationchoiceBox.setMinWidth(180);
			locationchoiceBox.getSelectionModel().selectFirst();
			ChoiceBox speed = new ChoiceBox();
			speed.getItems().addAll("3 sekundy", "15 sekund", "1 minuta", "30 minut", "1 godzina");
			speed.setMinWidth(180);
			speed.getSelectionModel().selectFirst();

			Line separator = new Line(0, 0, 180, 0);
			Line separator2 = new Line(0, 0, 180, 0);
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
			VBox.setMargin(label5, new Insets(0, 0, 10, 0));
			VBox.setMargin(label6, new Insets(0, 0, 10, 0));
			VBox.setMargin(checkbox, new Insets(0, 0, 10, 0));
			VBox.setMargin(checkbox2, new Insets(0, 0, 10, 0));
			VBox.setMargin(checkbox3, new Insets(0, 0, 10, 0));
			VBox.setMargin(locationchoiceBox, new Insets(0, 0, 10, 0));
			VBox.setMargin(speed, new Insets(0, 0, 10, 0));
			VBox.setMargin(separator, new Insets(5, 0, 15, 0));
			VBox.setMargin(separator2, new Insets(5, 0, 15, 0));
			VBox.setMargin(separator3, new Insets(5, 0, 15, 0));
			VBox.setMargin(separator4, new Insets(5, 0, 15, 0));
			VBox.setMargin(separator5, new Insets(5, 0, 15, 0));

			vbox.getChildren().addAll(label2, label5, separator5, button1, button2, separator, label1, dateTimePicker,
					button3, separator2, label6, speed, separator3, label3, checkbox, checkbox2, checkbox3, separator4,
					label4, locationchoiceBox);

			Pane pane = new Pane();

			double maxX = 0.0;
			double maxY = 0.0;

			for (Road road : helloWorldExample.getRoadsData()) {
				System.out.println(road.fromVertexName + " - " + road.toVertexName);
				Line line = new Line();
				line.setStartX(road.fromVertex.get("x").asDouble());
				line.setEndX(road.toVertex.get("x").asDouble());
				line.setStartY(road.fromVertex.get("y").asDouble());
				line.setEndY(road.toVertex.get("y").asDouble());
				line.setStrokeWidth(4);
				pane.getChildren().add(line);
			}

			Rectangle car = new Rectangle();

			// Setting the properties of the rectangle
			car.setX(-10);
			car.setY(-10);
			car.setWidth(20.0f);
			car.setHeight(20.0f);

			// Setting the height and width of the arc
			car.setArcWidth(5);
			car.setArcHeight(5);
			pane.getChildren().add(car);

			Runnable r = new Runnable() {
				public void run() {
					while (true) {

						try {
							requestData();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						// Translate translate = new Translate();
						// if (car.getTranslateX() != 100)
						// translate.setX(1);
						// else
						// translate.setY(1);

						// car.getTransforms().addAll(translate);
						// System.out.println("x1: " + car.getTranslateX());
						// System.out.println("x2: " + car.getLayoutX());
						// System.out.println("x3: " + car.getX());
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}

				private void requestData() throws IOException {

					String url = "http://localhost:8080/actors/KR01112";
					URL obj = new URL(url);
					HttpURLConnection con = (HttpURLConnection) obj.openConnection();
					// optional default is GET
					con.setRequestMethod("GET");
					// add request header
					con.setRequestProperty("User-Agent", "Mozilla/5.0");
					int responseCode = con.getResponseCode();
					System.out.println("\nSending 'GET' request to URL : " + url);
					System.out.println("Response Code : " + responseCode);
					BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
					String inputLine;
					StringBuffer response = new StringBuffer();
					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}
					in.close();
					// print in String
					// System.out.println(response.toString());
					// Read JSON response and print
					try {
						JSONObject myResponse = new JSONObject(response.toString());
						double x = myResponse.getJSONObject("currentCoords").getDouble("x");
						double y = myResponse.getJSONObject("currentCoords").getDouble("y");
						System.out.println("x = " + x + ", y = " + y);

						// Translate translate = new Translate();
						// translate.setX(x);
						// translate.setY(y);
						// car.getTransforms().addAll(translate);
						car.setTranslateX(x);
						car.setTranslateY(y);
						System.out.println("x1: " + car.getTranslateX());
						System.out.println("x2: " + car.getLayoutX());
						System.out.println("x3: " + car.getX());

						// System.out.println("Zapalam latarnie 1");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};

			thread = new Thread(r);

			for (Node node : helloWorldExample.getDataDevices()) {
				double x = node.get("x").asDouble();
				double y = node.get("y").asDouble();

				if (x > maxX)
					maxX = x;
				if (y > maxY)
					maxY = y;

				Circle circle = new Circle(x, y, 20, Paint.valueOf("FFFF80"));
				circle.setOpacity(0.5);
				/*
				 * RadialGradient gradient1 = new RadialGradient(0, .1, 100,
				 * 100, 20, false, CycleMethod.NO_CYCLE, new Stop(0, Color.RED),
				 * new Stop(1, Color.BLACK));
				 * 
				 * circle.setFill(gradient1);
				 */

				pane.getChildren().add(circle);
			}

			ScrollPane scrollPane = new ScrollPane();
			pane.setMinHeight(maxY + 100);
			pane.setMinWidth(maxX + 100);
			border.setLeft(vbox);
			pane.getStyleClass().addAll("pane");

			scrollPane.setContent(pane);
			border.setCenter(scrollPane);
			border.setMargin(scrollPane, new Insets(10, 10, 10, 0));
			Scene scene = new Scene(border, 1200, 800);
			scene.getStylesheets().add("css/layoutstyles.css");
			primaryStage.setScene(scene);
			primaryStage.show();
		}
	}
}
