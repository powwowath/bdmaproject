/**
 * Create initial Graph
 *
 * @author Gerard
 */
package edu.upc.bdma.project;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import edu.upc.bdma.project.beans.Airport;
import edu.upc.bdma.project.beans.City;
import edu.upc.bdma.project.utils.DistanceCalculator;
import org.apache.commons.io.FileUtils;
import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class PopulateGraph {
    // Constants
    private static final String DATA_FOLDER = "D:/BigDataManagementAnalytics/Projecte/data/"; // Modify
    private static final String GRAPH_DB_DATA_FILE = "graph.db";
    private static final int NEAR_RADIUS = 50000; // Meters

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

            HashMap<String, Airport> airportMap = pg.getAirports(mongoDatabase);

            HashMap<String, City> cityMap = pg.getCities(airportMap);
            cityMap = pg.updateCityPct(airportMap, cityMap);

            pg.generateBaseGraph(graph, airportMap, cityMap);

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
     * Get Airports HashMap
     */
    private static HashMap<String, Airport> getAirports(MongoDatabase db) {
System.out.println("getAirports");
        MongoCollection<Document> airportsCollection = db.getCollection("airports");
        MongoCollection<Document> routesCollection = db.getCollection("routes");
        MongoCollection<Document> poisCollection = db.getCollection("pois");
        FindIterable<Document> find = airportsCollection.find();
        MongoCursor<Document> cursor = find.iterator();

        HashMap<String, Airport> airports = new HashMap<String, Airport>();

        try {
            while (cursor.hasNext()) {
                Document airportDocument = cursor.next();
                // Few small/regional airports don't have region value
                if(!airportDocument.get("tz_database_timezone").equals("\\N")) {

                    Airport airport = new Airport();
                    airport.setId(Long.parseLong(airportDocument.get("id").toString()));
                    airport.setName(airportDocument.get("name").toString());
                    airport.setCity(airportDocument.get("city").toString());
                    airport.setCountry(airportDocument.get("country").toString());
                    airport.setRegion(airportDocument.get("tz_database_timezone").toString().substring(0, airportDocument.get("tz_database_timezone").toString().indexOf('/')));
                    airport.setCode(airportDocument.get("iata").toString());
                    airport.setLon(Double.parseDouble(airportDocument.get("lon").toString()));
                    airport.setLat(Double.parseDouble(airportDocument.get("lat").toString()));

                    List<String> categories = Arrays.asList("7317"); // Cultural - Museums
                    long nCultural = findNumberPOIS(poisCollection, categories, airport.getLon(), airport.getLat(), NEAR_RADIUS);

                    categories = Arrays.asList("9357"); // Beach
                    long nBeach = findNumberPOIS(poisCollection, categories, airport.getLon(), airport.getLat(), NEAR_RADIUS);

                    categories = Arrays.asList("9935"); // Mountain
                    long nMountain = findNumberPOIS(poisCollection, categories, airport.getLon(), airport.getLat(), NEAR_RADIUS);

                    categories = Arrays.asList("7376"); // Tourist
                    long nTourist = findNumberPOIS(poisCollection, categories, airport.getLon(), airport.getLat(), NEAR_RADIUS);

                    categories = Arrays.asList("9379"); // Nightlife
                    long nNightlife = findNumberPOIS(poisCollection, categories, airport.getLon(), airport.getLat(), NEAR_RADIUS);

                    airport.setnCultural(nCultural);
                    airport.setnBeach(nBeach);
                    airport.setnMountain(nMountain);
                    airport.setnTourist(nTourist);
                    airport.setnNightlife(nNightlife);

                    // Routes
                    BasicDBObject query = new BasicDBObject("source", airport.getCode());
                    FindIterable<Document> routesFind = routesCollection.find(query);
                    MongoCursor<Document> routesCursor = routesFind.iterator();
                    Set<String> routes = new HashSet<String>();

                    while (routesCursor.hasNext()) {
                        Document routeDocument = routesCursor.next();
                        routes.add(routeDocument.get("dest").toString());
                    }
                    airport.setRoutes(routes);

                    airports.put(airport.getCode(), airport);
                }
            }
        } finally {
            cursor.close();
        }

        return airports;
    }

    /**
     * Get Cities HashMap
     */
    private static HashMap<String, City> getCities(HashMap<String, Airport> airportMap) {
System.out.println("getCities");
        HashMap<String, City> cities = new HashMap<String, City>();

        Iterator it = airportMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Airport airport = (Airport)pair.getValue();

            City city = cities.get(airport.getCity());
            List<String> airportsCodes;
            if (city == null) {
                city = new City();
                city.setName(airport.getCity());
                city.setCountry(airport.getCountry());
                city.setAirportCodes(new String[]{airport.getCode()});
            } else {
                airportsCodes = new ArrayList<String>(Arrays.asList(city.getAirportCodes()));
                airportsCodes.add(airport.getCode());
                city.setAirportCodes(airportsCodes.toArray(new String[airportsCodes.size()]));
            }
            cities.put(city.getName()+"_"+city.getCountry(), city);
        }

        return cities;
    }

    /**
     * Add poi numbers and update percentage
     * @param airportMap
     * @param cityMap
     * @return
     */
    private static HashMap<String, City> updateCityPct(HashMap<String, Airport> airportMap, HashMap<String, City> cityMap){
System.out.println("updateCityPct");
        Iterator it = airportMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Airport airport = (Airport)pair.getValue();

            City city = cityMap.get(airport.getCity()+"_"+airport.getCountry());
            city.setnCultural(city.getnCultural() + airport.getnCultural());
            city.setnBeach(city.getnBeach() + airport.getnBeach());
            city.setnMountain(city.getnMountain() + airport.getnMountain());
            city.setnTourist(city.getnTourist() + airport.getnTourist());
            city.setnNightlife(city.getnNightlife() + airport.getnNightlife());

            // TODO: update pct
            long total = 0;
            total = city.getnCultural();
            total += city.getnBeach();
            total += city.getnMountain();
            total += city.getnTourist();
            total += city.getnNightlife();

            if (total > 0) {
                city.setPctCultural(Math.round((city.getnCultural()*100) / total));
                city.setPctBeach(Math.round(city.getnBeach()*100) / total);
                city.setPctMountain(Math.round(city.getnMountain()*100) / total);
                city.setPctTourist(Math.round(city.getnTourist()*100) / total);
                city.setPctNightlife(Math.round(city.getnNightlife()*100) / total);

                long denom = city.getnNightlife() + city.getnBeach() + city.getnMountain();
                if (denom > 0){
                    city.setPctRelax(1 - (city.getnNightlife() / denom));
                }

                city.setPctYoung(city.getPctBeach()+city.getPctNightlife());
            }
            cityMap.put(airport.getCity()+"_"+airport.getCountry(), city);
        }

        return cityMap;
    }


    /**
     * Generate Neo4J Graph
     * @param graph
     * @param airportMap
     * @param cityMap
     */
    private static void generateBaseGraph(GraphDatabaseService graph, HashMap<String, Airport> airportMap, HashMap<String, City> cityMap){
        HashMap<String, Node> airportNodes = new HashMap<String, Node>();
        HashMap<String, Node> cityNodes = new HashMap<String, Node>();

        Iterator it = cityMap.entrySet().iterator();
        City city;

        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            city = (City) pair.getValue();
            Node node = graph.createNode(NodeType.City);
            node.setProperty("id", city.getId());
            node.setProperty("name", city.getName());
            node.setProperty("country", city.getCountry());
            node.setProperty("nCultural", city.getnCultural());
            node.setProperty("nBeach", city.getnBeach());
            node.setProperty("nMountain", city.getnMountain());
            node.setProperty("nTourist", city.getnTourist());
            node.setProperty("nNightlife", city.getnNightlife());
            node.setProperty("pctCultural", city.getPctCultural());
            node.setProperty("pctBeach", city.getPctBeach());
            node.setProperty("pctMountain", city.getPctMountain());
            node.setProperty("pctTourist", city.getPctTourist());
            node.setProperty("pctNightlife", city.getPctNightlife());
            node.setProperty("pctYoung", city.getPctYoung());
            node.setProperty("pctRelax", city.getPctRelax());
            node.setProperty("cost", city.getCost());
            node.setProperty("promo", city.getPromo());

            cityNodes.put(city.getName()+"_"+city.getCountry(), node);
        }


        it = airportMap.entrySet().iterator();
        Airport airport;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            airport = (Airport)pair.getValue();
            Node node = graph.createNode(NodeType.Airport);
            node.setProperty("id", airport.getId());
            node.setProperty("name", airport.getName());
            node.setProperty("city", airport.getCity());
            node.setProperty("country", airport.getCountry());
            node.setProperty("region", airport.getRegion());
            node.setProperty("code", airport.getCode());
            node.setProperty("lon", airport.getLon());
            node.setProperty("lat", airport.getLat());

            node.createRelationshipTo(cityNodes.get(airport.getCity()+"_"+airport.getCountry()), RelationTypes.belongs);

            airportNodes.put(airport.getCode(), node);
        }

        // Create routes
        it = airportMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            airport = (Airport) pair.getValue();

            Node node = airportNodes.get(airport.getCode());
            for (String s : airport.getRoutes()) {
                Node nodeDest = airportNodes.get(s);
                // To avoid relationships with non inserted airports
                if (nodeDest != null) {
                    Relationship rel = node.createRelationshipTo(nodeDest, RelationTypes.flights);
                    rel.setProperty("distance", DistanceCalculator.distance(Double.parseDouble(node.getProperty("lat").toString()), Double.parseDouble(node.getProperty("lon").toString()), Double.parseDouble(nodeDest.getProperty("lat").toString()), Double.parseDouble(nodeDest.getProperty("lon").toString()), "K"));

                }
            }
        }
    }

    /**
     * Find number of pois of given categories near lat,lon
     * @param categories
     * @param lon
     * @param lat
     * @param radius in Meters
     * @return number of POIS
     */
    private static long findNumberPOIS(MongoCollection<Document> collection, List<String> categories, Double lon, Double lat, int radius){
        BasicDBObject geometryFilter = new BasicDBObject("$geometry", "type");
        geometryFilter.append("coordinates", new double[] { lon, lat });

        BasicDBObject spatialFilter = new BasicDBObject("$nearSphere", geometryFilter);
        spatialFilter.append("$maxDistance", NEAR_RADIUS);

        BasicDBObject query = new BasicDBObject("coords.coordinates", spatialFilter);
        query.append("feattyp", Integer.parseInt(categories.get(0)));

        return collection.count(query);
    }







    /**
     * Populate Graph
     */
    private static void populate(MongoDatabase db, GraphDatabaseService graph) {
        MongoCollection<Document> airportsCollection = db.getCollection("airports");
        MongoCollection<Document> poisCollection = db.getCollection("pois");

        // Neo4J
        HashMap<String, Node> airportNodes = new HashMap<String, Node>();
        HashMap<String, Node> cityNodes = new HashMap<String, Node>();
        HashMap<String, String> cityAirports = new HashMap<String, String>();

        // Mongo
        FindIterable<Document> find = airportsCollection.find();
        MongoCursor<Document> cursor = find.iterator();
        try {
            while (cursor.hasNext()) {
                Document airportDocument = cursor.next();

                // Few small/regional airports don't have region value
                if(!airportDocument.get("tz_database_timezone").equals("\\N")){
                    Node node = null;
                    node = graph.createNode(NodeType.Airport);
                    node.setProperty("id", airportDocument.get("id"));
                    node.setProperty("name", airportDocument.get("name"));
                    node.setProperty("city", airportDocument.get("city"));
                    node.setProperty("country", airportDocument.get("country"));
                    node.setProperty("region", airportDocument.get("tz_database_timezone").toString().substring(0, airportDocument.get("tz_database_timezone").toString().indexOf('/')));
                    node.setProperty("code", airportDocument.get("iata"));
                    node.setProperty("lon", airportDocument.get("lon"));
                    node.setProperty("lat", airportDocument.get("lat"));
                    // Cultural - Museums
/*                    List<String> categories = Arrays.asList("7317");
                    long nCultural = findNumberPOIS(poisCollection, categories, airportDocument.get("lon").toString(), airportDocument.get("lat").toString(), NEAR_RADIUS);
                    // Beach
                    categories = Arrays.asList("9357");
                    long nBeach = findNumberPOIS(poisCollection, categories, airportDocument.get("lon").toString(), airportDocument.get("lat").toString(), NEAR_RADIUS);
                    // Mountain
                    categories = Arrays.asList("9935");
                    long nMountain = findNumberPOIS(poisCollection, categories, airportDocument.get("lon").toString(), airportDocument.get("lat").toString(), NEAR_RADIUS);
                    // Tourist
                    categories = Arrays.asList("7376");
                    long nTourist = findNumberPOIS(poisCollection, categories, airportDocument.get("lon").toString(), airportDocument.get("lat").toString(), NEAR_RADIUS);
                    // Nightlife
                    categories = Arrays.asList("9379");
                    long nNightlife = findNumberPOIS(poisCollection, categories, airportDocument.get("lon").toString(), airportDocument.get("lat").toString(), NEAR_RADIUS);
                    node.setProperty("nCultural", nCultural);
                    node.setProperty("nBeach", nBeach);
                    node.setProperty("nMountain", nMountain);
                    node.setProperty("nTourist", nTourist);
                    node.setProperty("nNightlife", nNightlife);
*/
                    // Kep relationship between cities and airports
                    cityAirports.put(airportDocument.get("city").toString(), airportDocument.get("id").toString());

                    // Keep Airport Node
                    airportNodes.put(airportDocument.get("id").toString(), node);
                }
            }

            // Insert cities
            cityNodes = insertCities (db, graph, cityAirports);

            // Update city percentages
//            cityNodes = updateCityPercentages (db, graph, cityAirports);

        } finally {
            cursor.close();
        }

    }


    /**
     * Insert Cities
     * @param db
     * @param graph
     * @param cityAirports
     * @return
     */
    private static HashMap<String, Node> insertCities(MongoDatabase db, GraphDatabaseService graph, HashMap<String, String> cityAirports) {
        HashMap<String, Node> cityNodes = new HashMap<String, Node>();
        Iterator it = cityAirports.entrySet().iterator();

        int i = 1; // auto-generated ID
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Node node = null;
            node = graph.createNode(NodeType.City);
            node.setProperty("id", i);
            node.setProperty("name", pair.getKey());
            node.setProperty("pct_cult", 0);
            node.setProperty("pct_beach", 0);
            node.setProperty("pct_mount", 0);
            node.setProperty("pct_tourist", 0);
            node.setProperty("cost", "regular");
            node.setProperty("promo", 0);

            // TODO: crear relació amb Aiport - a posteriori Neo4j
            // TODO: cost de vida - a posteriori Neo4j
            // TODO: percentatges - a posteriori Neo4j
            // TODO: promo - a posteriori Neo4j
            cityNodes.put(pair.getKey().toString(), node);
            i++;
        }

        return cityNodes;
    }


}
