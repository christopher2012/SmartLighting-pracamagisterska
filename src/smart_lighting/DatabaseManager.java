package smart_lighting;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;
import org.neo4j.driver.v1.types.Node;

import model.MicroEvironment;

public class DatabaseManager implements AutoCloseable {
	private final Driver driver;

	public DatabaseManager(String uri, String user, String password) {
		driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
	}

	@Override
	public void close() throws Exception {
		driver.close();
	}

	public ArrayList<Node> getDataDevices() {
		ArrayList<Node> nodes = new ArrayList<Node>();

		try (Session session = driver.session()) {
			String greeting = session.writeTransaction(new TransactionWork<String>() {
				@Override
				public String execute(Transaction tx) {
					StatementResult result = tx.run("match (n:lantern) return n");
					while (result.hasNext()) {
						Record record = result.next();
						Node node = record.get("n").asNode();
						nodes.add(node);

					}

					return "";
				}
			});
			System.out.println(greeting);
		}
		return nodes;
	}

	public ArrayList<Road> getRoadsData() {
		ArrayList<Road> roads = new ArrayList<Road>();

		try (Session session = driver.session()) {
			String greeting = session.writeTransaction(new TransactionWork<String>() {
				@Override
				public String execute(Transaction tx) {
					StatementResult result = tx.run("match (n:lantern)-[t]->(m:lantern) return n,m");
					while (result.hasNext()) {
						Record record = result.next();
						Node node1 = record.get("n").asNode();
						Node node2 = record.get("m").asNode();
						MicroEvironment.routeGraph.putEdge(node1.get("node_id").asString(),
								node2.get("node_id").asString());
						if (!Road.roadExists(roads, node1, node2))
							roads.add(new Road(node1, node2));
					}

					return "";
				}
			});
		}

		return roads;
	}

	public static class Road {
		String fromVertexName;
		String toVertexName;
		Node fromVertex;
		Node toVertex;

		public Road(Node fromVertex, Node toVertex) {
			this.fromVertex = fromVertex;
			this.toVertex = toVertex;
			fromVertexName = fromVertex.get("node_id").asString();
			toVertexName = toVertex.get("node_id").asString();
		}

		public static boolean roadExists(ArrayList<Road> roads, Node node1, Node node2) {
			for (Road road : roads) {
				if (road.fromVertexName.equals(node1.get("name").asString())
						&& road.toVertexName.equals(node2.get("name").asString())) {
					return true;
				}

				if (road.toVertexName.equals(node1.get("name").asString())
						&& road.fromVertexName.equals(node2.get("name").asString())) {
					return true;
				}
			}
			return false;
		}
	}
}