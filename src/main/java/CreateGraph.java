/**
 * Create initial Graph
 * 
 * @author Gerard
 */

import java.io.File;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;


public class CreateGraph {

	private final static File DB = new File("/home/bdma09/data/neo4j/graph.db");
	
	private static Random random = new Random();

	enum NodeType implements Label {
		 City, Poi
	}
	
	enum RelationTypes implements RelationshipType {
	     flights, has
	}

}
