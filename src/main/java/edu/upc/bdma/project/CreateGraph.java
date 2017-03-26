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
	 * @param nodes
	 */
	public static void generate() {
		File DB = new File(DATA_FOLDER + GRAPH_DB_DATA_FILE);

		GraphDatabaseService graph = null;
		Transaction tx = null;
		try {
			FileUtils.deleteDirectory(DB);
			graph = new GraphDatabaseFactory().newEmbeddedDatabase(DB);
			tx = graph.beginTx();

			CSVReader reader = new CSVReader(new FileReader(DATA_FOLDER + AIRPORT_FILE));
			List<String[]> airportsList = reader.readAll();
			reader.close();
			
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
			tx.success();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			tx.close();
		}
	}
	
}
