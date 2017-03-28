package edu.upc.bdma.project;

/**
 * Create initial Graph
 * 
 * @author Gerard
 */

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.upc.bdma.project.utils.DistanceCalculator;
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
	private static final String DATA_FOLDER = "D:/BigDataManagementAnalytics/Projecte/data/"; // Modify
	private static final String GRAPH_DB_DATA_FILE = "graph.db";
	private static final String AIRPORT_FILE = "airports.dat";
	private static final String ROUTE_FILE = "routes.dat";

	enum NodeType implements Label { Airport, Poi }
	enum RelationTypes implements RelationshipType { flights, has }

	/*
	 * Main
	 */
	public static void main(String[] args) {
		generate();
	}
	
	/**
	 * Generate schema
	 */
	private static void generate() {
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();

		File DB = new File(DATA_FOLDER + GRAPH_DB_DATA_FILE);

		GraphDatabaseService graph = null;
		Transaction tx = null;
		try {
			FileUtils.deleteDirectory(DB);
			graph = new GraphDatabaseFactory().newEmbeddedDatabase(DB);
			tx = graph.beginTx();

			// Airports

			CSVReader reader = new CSVReader(new FileReader(classLoader.getResource(AIRPORT_FILE).getFile()));
			List<String[]> airportsList = reader.readAll();
			reader.close();

			HashMap<String, Node> airports = new HashMap<String, Node>();

			// Airports
			for (int i=0; i<airportsList.size(); i++){
				String region = airportsList.get(i)[11];

				// Few regional airports don't have region value
				if (!region.equals("N")) {
					Node node = null;
					node = graph.createNode(NodeType.Airport);
					node.setProperty("id", airportsList.get(i)[0]);
					node.setProperty("name", airportsList.get(i)[1]);
					node.setProperty("city", airportsList.get(i)[2]);
					node.setProperty("country", airportsList.get(i)[3]);
					node.setProperty("region", region.substring(0, region.indexOf('/')));
					node.setProperty("code", airportsList.get(i)[4]);
					node.setProperty("lat", airportsList.get(i)[6]);
					node.setProperty("lon", airportsList.get(i)[7]);

					//[4] = Code
					airports.put(airportsList.get(i)[4], node);
				}
			}


			// Routes

			reader = new CSVReader(new FileReader(classLoader.getResource(ROUTE_FILE).getFile()));
			List<String[]> routeList = reader.readAll();
			reader.close();

			// Routes
			Collection<String> routePairs = new HashSet<String>();
			String tempRoute;
			for (int i=0; i<routeList.size(); i++){
				Node airportFrom = airports.get(routeList.get(i)[2]);
				Node airportTo = airports.get(routeList.get(i)[4]);
				tempRoute = routeList.get(i)[2] + "--" + routeList.get(i)[4];

				if (airportFrom != null && airportTo != null) {
					if (!routePairs.contains(tempRoute)) {
						Relationship flights = airportFrom.createRelationshipTo(airportTo, RelationTypes.flights);
						flights.setProperty("distance", DistanceCalculator.distance(Double.parseDouble(airportFrom.getProperty("lat").toString()), Double.parseDouble(airportFrom.getProperty("lon").toString()), Double.parseDouble(airportTo.getProperty("lat").toString()), Double.parseDouble(airportTo.getProperty("lon").toString()), "K"));
						routePairs.add(tempRoute);
					}
				}
			}

			tx.success();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			tx.close();
			graph.shutdown();
		}
	}
	
}
