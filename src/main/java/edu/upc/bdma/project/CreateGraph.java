package edu.upc.bdma.project;

/**
 * Create initial Graph
 * 
 * @author Gerard
 */

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import au.com.bytecode.opencsv.CSVReader;



public class CreateGraph {
	
	// Constants
	private static final String DATA_FOLDER = "D:/BigDataManagementAnalytics/Projecte/data/";
	private static final String GRAPH_DB_DATA_FILE = "graph.db";
	private static final String AIRPORT_FILE = "airports.dat.txt";
	private static final String ROUTE_FILE = "routes.dat.txt";

/*
 	private static final ROUTE_FILE = "";
	private static final ROUTE_FILE = "";
	private static final ROUTE_FILE = "";
*/	
	
	enum NodeType implements Label {
		 Airport, Poi
	}
	
	enum RelationTypes implements RelationshipType {
	     flights, has
	}

	/*
	 * Main
	 */
	public static void main(String[] args) {
		generate();
	}
	
	/**
	 * Generate schema
	 * @param nodes
	 */
	public static void generate() {
		File DB = new File(DATA_FOLDER + GRAPH_DB_DATA_FILE);
		Random random = new Random();

		GraphDatabaseService graph = null;
		Transaction tx = null;
		try {
			FileUtils.deleteDirectory(DB);
			graph = new GraphDatabaseFactory().newEmbeddedDatabase(DB);
			tx = graph.beginTx();

			CSVReader reader = new CSVReader(new FileReader(DATA_FOLDER + AIRPORT_FILE));
			List<String[]> airportsList = reader.readAll();

			HashMap<String, Node> airports = new HashMap<String, Node>();
			
			for (int i=0; i<airportsList.size(); i++){
				Node node = null;
				node = graph.createNode(NodeType.Airport);
				node.setProperty("id", airportsList.get(i)[0]);
				node.setProperty("name", airportsList.get(i)[1]);
				node.setProperty("city", airportsList.get(i)[2]);
				node.setProperty("code", airportsList.get(i)[4]);
				node.setProperty("lat", airportsList.get(i)[6]);
				node.setProperty("lon", airportsList.get(i)[7]);
				
				//[4] = Code
				airports.put(airportsList.get(i)[4], node);
				
//				System.out.println(airportsList.get(i)[2]);
			}
/*			
			HashMap<String, Node> websites = new HashMap<String, Node>();
			HashMap<String, Node> customers = new HashMap<String, Node>();
			for (int i = 0; i < nodes; i++) {
				Node node = null;
				if (i%3 > 0) {
					String number = Values.getPhoneNumber();
					if (!customers.containsKey(number)) {
						int age = Values.getAge();
						String phone = Values.getPhone();
						
						node = graph.createNode(NodeType.Customer);
						node.setProperty("id", number);
						node.setProperty("number", number);
						node.setProperty("age", age);
						node.setProperty("phone", phone);
						
						statement.addBatch("INSERT INTO customers VALUES("+
							"'"+number+"', "+
							"'"+number+"', "+
							age+", "+
							"'"+phone+"');");

						customers.put(number, node);
					}
				} else {
					String url = Values.getURL();
					if (!websites.containsKey(url)) {
						node = graph.createNode(NodeType.Website);
						node.setProperty("id", url);
						node.setProperty("url", url);
						
						statement.addBatch("INSERT INTO websites VALUES("+
							"'"+url+"', "+
							"'"+url+"');");
						
						websites.put(url, node);
					}
				}
			}
			
			ArrayList< ArrayList<Node> > families = new ArrayList< ArrayList<Node> >();
			int nFamilies = random.nextInt(5)+nodes/5;
			for (int i = 1; i <= nFamilies; i++) {
				families.add(new ArrayList<Node>());
			}
			for (Node customer : customers.values()) {
				int family = random.nextInt(nFamilies);
				families.get(family).add(customer);
			}
			for (ArrayList<Node> family : families) {
				for (int i = 0; i < family.size(); i++) {
					int edges = random.nextInt(family.size()*3);
					for (int j = 1; j <= edges; j++) {
						int action = random.nextInt(2);
						int edgeTo = random.nextInt(family.size());
						if (i != edgeTo) {
							if (action == 0) {
								double price = Values.getCallPrice();
								double duration = Values.getCallDuration();
								String day = Values.getDay();
								
								Relationship call = family.get(i).createRelationshipTo(family.get(edgeTo), RelationTypes.calls);
								call.setProperty("price", price);
								call.setProperty("duration", duration);
								call.setProperty("day", day);

								statement.addBatch("INSERT INTO calls VALUES("+
									"'"+family.get(i).getProperty("number")+"', "+
									"'"+family.get(edgeTo).getProperty("number")+"', "+
									price+", "+
									duration+", "+
									"'"+day+"\');");
							} else {
								double price = Values.getTextPrice();
								double length = Values.getTextLength();
								String day = Values.getDay();
								
								Relationship text = family.get(i).createRelationshipTo(family.get(edgeTo), RelationTypes.texts);
								text.setProperty("price", price);
								text.setProperty("length", length);
								text.setProperty("day", day);
								
								statement.addBatch("INSERT INTO texts VALUES("+
									"'"+family.get(i).getProperty("number")+"', "+
									"'"+family.get(edgeTo).getProperty("number")+"', "+
									price+", "+
									length+", "+
									"'"+day+"');");
							}
						}
					}
					edges = random.nextInt(nodes/5);
					for (int j = 1; j <= edges; j++) {
						int edgeTo = random.nextInt(websites.size());
						for (Node website : websites.values()) {
							if (edgeTo == 0) {
								String day = Values.getDay();
								
								Relationship visit = family.get(i).createRelationshipTo(website, RelationTypes.visits);
								visit.setProperty("day", day);
								
								statement.addBatch("INSERT INTO visits VALUES("+
									"'"+family.get(i).getProperty("number")+"', "+
									"'"+website.getProperty("url")+"', "+
									"'"+Values.getDay()+"');");
								break;
							} else {
								edgeTo--;
							}
						}
					}
				}
			}
			statement.executeBatch();
			*/
			tx.success();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			tx.close();
		}
	}
	
}
