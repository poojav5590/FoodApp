package edu.cmu.pvasudev;



import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Author: Pooja Vasudevan
 * Last Modified: Apr 5, 2020
 *
 * This program is the model class in the MVC, which implements all necessary business logics. The implementation includes taking the user search and
 * making a GET request to API to retrieve nutrient facts. It also takes the information from the request and reply and stores into MongoDB.
 * The information is further extracted from MongoDB documents for dashboard analytics.
 **/
public class FoodModel {

/**
 *This function takes user's search string and makes a GET request to Edamam Food Database API, parses the JSON request, and
 * returns a JSON formatted string of nutrient facts.
 **/
    public String searchFood(String searchTerm) {
        String finalResult="";
        try {


            URL url = new URL("https://api.edamam.com/api/food-database/parser?ingr="+searchTerm+"&app_id=a6ec1a32&app_key=adbce3f99105b170f0a3cabcb2bb3848");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }


            String inline="";

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            while ((output = br.readLine()) != null) {
                inline+=output;
            }

            JSONObject obj = new JSONObject(inline);
            JSONArray arr = obj.getJSONArray("parsed");
            if(arr.length()!=0) {
                JSONObject nutrientObj = arr.getJSONObject(0).getJSONObject("food").getJSONObject("nutrients");


                finalResult = nutrientObj.toString();
            }



            conn.disconnect();



        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return finalResult;

       }

    /**
     *This function takes information from request/reply of Android app and stores into MongoDB document, which is part of a collection.
     @param searchTerm user's search string
     @param output JSON string output from API
     @param time total time execution (ms) to receive response from API
     @param ua string user-agent header from device
     @param collection MongoDB collection to access documents and insert one
     @void
     **/
       public void storeData(String searchTerm, String output, long time, String ua, MongoCollection<Document> collection){

           try {
               JSONObject nutrientObj = new JSONObject(output);
               long protein = nutrientObj.getLong("PROCNT");
               long energy = nutrientObj.getLong("ENERC_KCAL");
               long fat = nutrientObj.getLong("FAT");
               long carbs = nutrientObj.getLong("CHOCDF");
               long fiber = nutrientObj.getLong("FIBTG");

               // Build/OSM1.180201.007 = Nexus 5 API 27
               // Build/OSM1.180201.031 = Nexus 6 API 27
               //.001 = Pixel 2 API 29
               //.002 = Nexus 5X API 29
               //.117 = Galaxy Nexus API 28
               if(ua.contains("Mozilla")){
                   ua = "Windows/Mac Laptop";
               } else if(ua.contains(".007")){
                   ua = "Nexus 5 API 27";
               } else if(ua.contains(".031")){
                   ua = "Nexus 6 API 27";
               }else if(ua.contains(".001")){
                   ua="Pixel 2 API 29";
               } else if(ua.contains(".002")){
                   ua="Nexus 5X API 29";
               } else if(ua.contains(".117")){
                   ua="Galaxy Nexus API 28";
               }

           Document doc = new Document("name", "MongoDB")
                   .append("type","database")
                   .append("searchTerm",searchTerm)
                   .append("time",String.valueOf(time))
                   .append("protein",String.valueOf(protein))
                   .append("energy",String.valueOf(energy))
                   .append("fat",String.valueOf(fat))
                   .append("carbs",String.valueOf(carbs))
                   .append("fiber",String.valueOf(fiber))
                   .append("device",ua);


           collection.insertOne(doc);





           } catch (JSONException e) {
               e.printStackTrace();
           }
       }
     /**
     *This function iterates through all documents of MongoDB and returns the search counts for all search words and returns a hashmap
     **/
       public HashMap<String,Integer> getSearchCount(MongoCollection<Document> collection){


           HashMap<String,Integer> map = new HashMap<String,Integer>();

           MongoCursor<Document> cursor = collection.find().iterator();
           while (cursor.hasNext()) {
               try{
               String r = cursor.next().toJson();
               //System.out.println(r);
               JSONObject obj = new JSONObject(r);
                String key = obj.getString("searchTerm");
                   if(!map.containsKey(key)){
                       map.put(key,1);
                   }
                   else{
                       map.put(key,map.get(key)+1);
                   }

               } catch (JSONException e) {
                   e.printStackTrace();
               }


           }

           return map;

       }
        /**
         * This function iterates through all documents of MongoDB and returns the average execution time (ms) for all search words
         * and returns it in a hashmap.
        **/
       public HashMap<String,Integer> getAverageTime(HashMap<String,Integer> map, MongoCollection<Document> collection){


           HashMap<String,Integer> finalMap = new HashMap<String,Integer>();

           MongoCursor<Document> cursor = collection.find().iterator();
           while (cursor.hasNext()) {
               try{
                   String r = cursor.next().toJson();
                   JSONObject obj = new JSONObject(r);
                   String key = obj.getString("searchTerm");
                   int t = Integer.parseInt(obj.getString("time"));
                   if(!finalMap.containsKey(key)){

                       finalMap.put(key,t);
                   }
                   else{
                       finalMap.put(key,finalMap.get(key)+t);
                   }

               } catch (JSONException e) {
                   e.printStackTrace();
               }


           }


           for(String k: finalMap.keySet()){
               finalMap.put(k, finalMap.get(k)/map.get(k));
           }

           return finalMap;

       }
     /**
     * This function iterates through all documents of MongoDB and returns the number of searches conducted across different Android devices
     * and returns it in a hashmap.
     **/
       public HashMap<String, Integer> getDeviceCount(MongoCollection<Document> collection){

           HashMap<String,Integer> map = new HashMap<String,Integer>();

           MongoCursor<Document> cursor = collection.find().iterator();
           while (cursor.hasNext()) {
               try{
                   String r = cursor.next().toJson();
                   JSONObject obj = new JSONObject(r);

                   if(obj.has("device")) {
                       String key = obj.getString("device");
                       if (!map.containsKey(key)) {
                           map.put(key, 1);
                       } else {
                           map.put(key, map.get(key) + 1);
                       }

                   }

               } catch (JSONException e) {
                   e.printStackTrace();
               }


           }

           return map;

       }
    /**
     * This function iterates through all documents of MongoDB, extracts all necessary information, and returns it into an ArrayList of
     * JSONObjects. This is to display "logs" into a formatted table on dashboard.
     **/
    public ArrayList<JSONObject> getLogs(MongoCollection<Document> collection) {


        ArrayList<JSONObject> list = new ArrayList<JSONObject>();

        MongoCursor<Document> cursor = collection.find().iterator();
        while (cursor.hasNext()) {
            try {
                String r = cursor.next().toJson();
                JSONObject obj = new JSONObject(r);
                list.add(obj);


            } catch (JSONException e) {
                e.printStackTrace();
            }



        }

        return list;
    }

    /**
     * This function is a helper method that sorts a hashmap by its value in descending order. The sample code was borrowed from geeksforgeeks.
     **/
    public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> hm)
    {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Integer> > list =
                new LinkedList<Map.Entry<String, Integer> >(hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Integer> >() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2)
            {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    /**
     * This function is a helper method that sorts a hashmap by its value in ascending order. The sample code was borrowed from geeksforgeeks.
     **/
    public static HashMap<String, Integer> sortByValueAsc(HashMap<String, Integer> hm)
    {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Integer> > list =
                new LinkedList<Map.Entry<String, Integer> >(hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Integer> >() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2)
            {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

}
