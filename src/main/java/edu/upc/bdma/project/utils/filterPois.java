package edu.upc.bdma.project.utils;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import org.neo4j.graphdb.Node;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Gerard on 10/04/2017.
 */
public class filterPois {
    public static void main(String[] args) {
        try {
            CSVReader reader = null;
            reader = new CSVReader(new FileReader("D:/BigDataManagementAnalytics/Projecte/data/pois.csv"), '|');
            String[] nextLine;

            CSVWriter writer = new CSVWriter(new FileWriter("D:/BigDataManagementAnalytics/Projecte/data/pois_filtered.csv"), '|');
            int i = 0;
            while ((nextLine = reader.readNext()) != null) {
                // Only selected categories
                // 7376 - Important Tourist Attraction
                // 9935 - Mountain Pass
                // 9357 - Beach
                // 7317 - Museum
                // 9379 - Nightlife
                if (nextLine[1].equals("7376") || nextLine[1].equals("9935") || nextLine[1].equals("9357") || nextLine[1].equals("7317") || nextLine[1].equals("9379")) {
                    writer.writeNext(nextLine);
                }

                if (i%100000==0){
                    System.out.println(i);
                }
                i++;
            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
