<%--
  Created by IntelliJ IDEA.
  User: pvasudev
  Date: 4/1/20
  Time: 4:49 PM
  This file displays all relevant dashboard analytics. Generation of bar charts on JSP
  were taken from https://canvasjs.com/jsp-charts/bar-chart/
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="org.json.JSONException"%>
<%@ page import="org.json.JSONObject"%>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>

<%
  Gson gsonObj = new Gson();
  Map<Object,Object> map = null;

  List<Map<Object,Object>> list = new ArrayList<Map<Object,Object>>();
  int count =0;
  for (String key : ((HashMap<String,Integer>)request.getAttribute("popCountMap")).keySet()) {
      count++;
      int val = ((HashMap<String,Integer>)request.getAttribute("popCountMap")).get(key);
      map = new HashMap<Object,Object>(); map.put("label", key); map.put("y", val); list.add(map);

      if(count==10) { break;}
  }

  List<Map<Object,Object>> list2 = new ArrayList<Map<Object,Object>>();
  int count2 =0;
  for (String key : ((HashMap<String,Integer>)request.getAttribute("timeMap")).keySet()) {
    count2++;
    int val = ((HashMap<String,Integer>)request.getAttribute("timeMap")).get(key);
    map = new HashMap<Object,Object>(); map.put("label", key); map.put("y", val); list2.add(map);

    if(count2==10) { break;}
  }

  List<Map<Object,Object>> list3 = new ArrayList<Map<Object,Object>>();
  int count3 =0;
  for (String key : ((HashMap<String,Integer>)request.getAttribute("deviceMap")).keySet()) {

    int val = ((HashMap<String,Integer>)request.getAttribute("deviceMap")).get(key);
    if(!key.contains("Laptop")) {
      count3++;
      map = new HashMap<Object, Object>();map.put("label", key);map.put("y", val);list3.add(map);
    }
    if(count3==5) { break;}
  }

  String dataPoints = gsonObj.toJson(list);
  String dataPoints2 = gsonObj.toJson(list2);
  String dataPoints3 = gsonObj.toJson(list3);
%>
<!DOCTYPE HTML>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <script type="text/javascript">
    window.onload = function() {

      var chart = new CanvasJS.Chart("chartContainer", {
        animationEnabled: true,
        exportEnabled: true,
        title: {
          text: "Top 10 Popular Search Words"
        },
        axisX: {
          title: "Food Item"
        },
        axisY: {
          title: "Number of Searches"
        },
        data: [{
          type: "column", //change type to bar, line, area, pie, etc
          //indexLabel: "{y}", //Shows y value on all Data Points
          indexLabelFontColor: "#5A5757",
          indexLabelPlacement: "outside",
          dataPoints: <%out.print(dataPoints);%>
        }]
      });

     var chart2 = new CanvasJS.Chart("chartContainer2", {
       animationEnabled: true,
       exportEnabled: true,
       title: {
         text: "Top 10 Popular Search Words with Lowest Average Latency"
       },
       axisX: {
         title: "Food Item"
       },
       axisY: {
         title: "Average Latency (ms)"
       },
       data: [{
         type: "column", //change type to bar, line, area, pie, etc
         //indexLabel: "{y}", //Shows y value on all Data Points
         indexLabelFontColor: "#5A5757",
         indexLabelPlacement: "outside",
         dataPoints: <%out.print(dataPoints2);%>
       }]
      });

      var chart3 = new CanvasJS.Chart("chartContainer3", {
        animationEnabled: true,
        exportEnabled: true,
        title: {
          text: "Top 5 Android Devices"
        },
        axisX: {
          title: "Device"
        },
        axisY: {
          title: "Number of Searches"
        },
        data: [{
          type: "bar",
          indexLabel: "{y}",
          indexLabelFontColor: "#444",
          indexLabelPlacement: "inside",
          dataPoints: <%out.print(dataPoints3);%>
        }]
      });

      chart.render();
      chart2.render();
      chart3.render();

    }
  </script>
</head>
<body>
<h1>Food Search - Dashboard Analytics</h1>
<div id="chartContainer" style="height: 370px; width: 70%;"></div>
<div id="chartContainer2" style="height: 370px; width: 70%;"></div>
<div id="chartContainer3" style="height: 370px; width: 70%;"></div>
<script src="https://canvasjs.com/assets/script/canvasjs.min.js"></script>
<style>
  table, th, td {
    padding: 10px;
    border: 1px solid black;
    border-collapse: collapse;
  }
</style>
<h2>Recorded Logs</h2>
<table border="0" cellpadding="1" cellspacing="1">
  <thead>
  <tr>
    <th scope=”col”>Search Term</th>
    <th scope=”col”>Time Execution (ms)</th>
    <th scope=”col”>Device</th>
    <th scope=”col”>Protein (g)</th>
    <th scope=”col”>Energy (kcal)</th>
    <th scope=”col”>Fat (g)</th>
    <th scope=”col”>Carbs (g)</th>
    <th scope=”col”>Fiber (g)</th>
  </tr>
  </thead>
  <tbody>
<% for (int i=0; i< ((ArrayList<JSONObject>)request.getAttribute("logs")).size(); i++) {
      JSONObject obj = ((ArrayList<JSONObject>)request.getAttribute("logs")).get(i);%>
  <tr>
    <% try { %>
    <th scope="row"><%=obj.getString("searchTerm")%></th>
    <td><%=obj.getString("time")%></td>
    <td><%=obj.getString("device")%></td>
    <td><%=obj.getString("protein")%></td>
    <td><%=obj.getString("energy")%></td>
    <td><%=obj.getString("fat")%></td>
    <td><%=obj.getString("carbs")%></td>
    <td><%=obj.getString("fiber")%></td>
    <% } catch (JSONException e){
      e.printStackTrace();
    } %>
  </tr>
<%} %>
  </tbody>
</table>
</body>
</html>
