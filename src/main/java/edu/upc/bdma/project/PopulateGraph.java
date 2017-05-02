/**
 * Create initial Graph
 *
 * @author Gerard
 */
package edu.upc.bdma.project;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import org.apache.commons.io.FileUtils;
import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;


public class PopulateGraph {
    // Constants
    private static final String DATA_FOLDER = "D:/BigDataManagementAnalytics/Projecte/data/"; // Modify
    private static final String GRAPH_DB_DATA_FILE = "graph.db";

    enum NodeType implements Label { Airport, City }
    enum RelationTypes implements RelationshipType { flights, belongs }


    /*
     * Main
     */
    public static void main(String[] args) {

        PopulateGraph pg = new PopulateGraph();

        // Mongo Connection
        MongoClient client = new MongoClient("192.168.1.109" , 27017 );
        MongoDatabase mongoDatabase = client.getDatabase("bdma");


        // Neo4J connection and TX
        File DB = new File(DATA_FOLDER + GRAPH_DB_DATA_FILE);
        GraphDatabaseService graph = null;
        Transaction tx = null;
        try {
            FileUtils.deleteDirectory(DB);
            graph = new GraphDatabaseFactory().newEmbeddedDatabase(DB);
            tx = graph.beginTx();

            // Insert Airport Nodes
            pg.insertAirports(mongoDatabase, graph);

            // Insert City Nodes

            // Insert Airport Nodes


            tx.success();
        } catch (IOException e) {
            // TODO Auto-gen catch block
            e.printStackTrace();
        } finally {
            tx.close();
            graph.shutdown();
        }

        client.close();
    }


    /**
     * Insert Airports
     *
     * returns <cityName, Aiport Id>
     */
    private static void insertAirports(MongoDatabase db, GraphDatabaseService graph) {
        MongoCollection<Document> airportsCollection = db.getCollection("airports");

        // Neo4J
        HashMap<String, Node> airportNodes = new HashMap<String, Node>();
        HashMap<String, Node> cityNodes = new HashMap<String, Node>();
        HashMap<String, String> cityAirports = new HashMap<String, String>();

        // Mongo
        FindIterable<Document> find = airportsCollection.find();
        MongoCursor<Document> cursor = find.iterator();
        try {
            while (cursor.hasNext()) {
                Document airport = cursor.next();

                // Few small/regional airports don't have region value
                if(!airport.get("tz_database_timezone").equals("\\N")){
                    // Kep relationship between cities and airports
                    cityAirports.put(airport.get("city").toString(), airport.get("id").toString());

                    Node node = null;
                    node = graph.createNode(NodeType.Airport);
                    node.setProperty("id", airport.get("id"));
                    node.setProperty("name", airport.get("name"));
                    node.setProperty("city", airport.get("city"));
                    node.setProperty("country", airport.get("country"));
                    node.setProperty("region", airport.get("tz_database_timezone").toString().substring(0, airport.get("tz_database_timezone").toString().indexOf('/')));
                    node.setProperty("code", airport.get("iata"));
                    node.setProperty("lon", airport.get("lon"));
                    node.setProperty("lat", airport.get("lat"));

                    airportNodes.put(airport.get("iata").toString(), node);
                }
            }

            // Insert cities
            cityNodes = insertCities (db, graph, cityAirports);

        } finally {
            cursor.close();
        }

    }


    /**
     * Insert Cities
     */
    private static HashMap<String, Node> insertCities(MongoDatabase db, GraphDatabaseService graph, HashMap<String, String> cityAirports) {
        HashMap<String, Node> cityNodes = new HashMap<String, Node>();

        Iterator it = cityAirports.entrySet().iterator();
        int i = 1;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Node node = null;
            node = graph.createNode(NodeType.City);
            node.setProperty("id", i);
            node.setProperty("name", pair.getKey());
            node.setProperty("pct_cult", 0);
            node.setProperty("pct_beach", 0);
            node.setProperty("pct_mount", 0);
            node.setProperty("pct_touri", 0);
            node.setProperty("cost", "regular");
            node.setProperty("promo", 0);

            // TODO: crear relació amb Aiport
            // TODO: cost de vida
            // TODO: percentatges
            // TODO: promo
            cityNodes.put(pair.getKey().toString(), node);

            i++;
        }

        return cityNodes;
    }



}
