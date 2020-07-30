package edu.cmu.pvasudev;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.json.JSONObject;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Author: Pooja Vasudevan
 * Last Modified: Apr 5, 2020
 *
 * This program is a TomEE web service that takes a user search string and accesses the business logic of the model class to retrieve
 * relevant nutritional information related to the search term. It returns the extracted output from the API as a JSON formatted
 * string that gets sent back to the android app. It also stores the relevant information into MongoDB, of which can be processed and
 * extracted to display dashboard analytics on the /getDashboard page. Dashboard analytics include top 10 search words, top 10 search words with lowest average
 * latency, and top 5 android devices.
 **/
@WebServlet(name = "FoodServlet",
        urlPatterns = {"/getFood","/getDashboard"})
public class FoodServlet extends HttpServlet {

    FoodModel ipm = null;  // The "business model" for this app
    MongoCollection<Document> collection = null;
    // Initiate this servlet by instantiating the model that it will use as well as making the connection to MongoDB.
    @Override
    public void init() {
        ipm = new FoodModel();
        MongoClientURI uri = new MongoClientURI("mongodb+srv://poojav:database90@cluster0-9iwgn.mongodb.net/test?retryWrites=true&w=majority");
        MongoClient mongoClient = new MongoClient(uri);
        MongoDatabase database = mongoClient.getDatabase("test");

        collection = database.getCollection("foodItemsList");
    }

    /*
     * This doGet() method takes the user's search and implements the searchFood() method from FoodModel to retrieve
     * relevant nutritional information to write out to the server as a JSON formatted String. It also implements the logic from
     * the model class to store and extract data from MongoDB dependent on the relevant dashboard analytics to /getDashboard page.
     * It writes three important types of analytics to results.jsp : top 10 search terms, top 10 search terms with lowest execution time,
     * and top 5 android devices.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String search = request.getParameter("searchWord");
        if(search!=null){
            long start = System.currentTimeMillis();
           String result = ipm.searchFood(search);
           if(result.length()!=0) {
               long end = System.currentTimeMillis();
               long totalTime = end - start;
               String ua = request.getHeader("User-Agent");
               ipm.storeData(search, result, totalTime, ua, collection);
               response.getWriter().println(result);
           }
           else{
               response.getWriter().println("No results exist");
           }

        }



        if(request.getServletPath().equals("/getDashboard")) {//display dashboard information to results.jsp
            ArrayList<JSONObject>  logsList = ipm.getLogs(collection);
            HashMap<String,Integer> countMap = ipm.getSearchCount(collection);
            HashMap<String,Integer> sortedCountMap = ipm.sortByValue(countMap);
            HashMap<String,Integer> timeMap = ipm.getAverageTime(countMap,collection);
            HashMap<String,Integer> sortedTimeMap = ipm.sortByValueAsc(timeMap);
            HashMap<String,Integer> deviceMap = ipm.getDeviceCount(collection);
            HashMap<String,Integer> sortedDeviceMap = ipm.sortByValue(deviceMap);


            request.setAttribute("logs", logsList);
            request.setAttribute("popCountMap",sortedCountMap);
            request.setAttribute("timeMap",sortedTimeMap);
            request.setAttribute("deviceMap",sortedDeviceMap);

            String nextView = "result.jsp";
            RequestDispatcher view = request.getRequestDispatcher(nextView);
            view.forward(request, response);

        }



    }
}
