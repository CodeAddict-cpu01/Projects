package com.bloodbank;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

public class CenterRouteServlet extends HttpServlet {

    // Graph node representation (adjacency list)
    static Map<String, List<Route>> graph = new HashMap<>();

    static class Route {
        String to;
        int distance;
        Route(String to, int distance) {
            this.to = to;
            this.distance = distance;
        }
    }

    static class Node implements Comparable<Node> {
        String center;
        int cost;
        Node(String center, int cost) {
            this.center = center;
            this.cost = cost;
        }
        public int compareTo(Node other) {
            return this.cost - other.cost;
        }
    }

    @Override
    public void init() throws ServletException {
        // Sample distances between centers
        graph.put("Dehradun Railway Station", Arrays.asList(
            new Route("Doon Hospital Dehradun", 5),
            new Route("Max Super Specialty Dehradun", 8),
            new Route("AIIMS Rishikesh", 42)
        ));
        graph.put("Doon Hospital Dehradun", Arrays.asList(
            new Route("Max Super Specialty Dehradun", 3),
            new Route("Synergy Hospital", 4)
        ));
        graph.put("Max Super Specialty Dehradun", Arrays.asList(
            new Route("Synergy Hospital", 2),
            new Route("Military Hospital", 6)
        ));
        // Add remaining connections as needed...
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String destination = request.getParameter("center");
        if (destination != null) {
            destination = destination.trim();
        }

        String source = "Dehradun Railway Station";

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        Map<String, Integer> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        PriorityQueue<Node> pq = new PriorityQueue<>();

        for (String node : graph.keySet()) {
            dist.put(node, Integer.MAX_VALUE);
        }
        dist.put(source, 0);
        pq.offer(new Node(source, 0));

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            for (Route route : graph.getOrDefault(current.center, new ArrayList<>())) {
                int newCost = current.cost + route.distance;
                if (newCost < dist.getOrDefault(route.to, Integer.MAX_VALUE)) {
                    dist.put(route.to, newCost);
                    prev.put(route.to, current.center);
                    pq.offer(new Node(route.to, newCost));
                }
            }
        }

        // Validate destination
        if (destination == null || !dist.containsKey(destination) || dist.get(destination) == null || dist.get(destination) == Integer.MAX_VALUE) {
            out.println("<html><head><title>Error</title></head><body>");
            out.println("<h2>Error: Destination not found or not reachable.</h2>");
            out.println("<a href='index.html'>Back to Home</a>");
            out.println("</body></html>");
            return;
        }

        // Build path
        List<String> path = new ArrayList<>();
        for (String at = destination; at != null; at = prev.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);

        // Output result
        Integer distance = dist.get(destination);
        out.println("<html><head><title>Shortest Route</title></head><body>");
        out.println("<h1>Shortest Route to " + destination + "</h1>");
        out.println("<h2>Distance: " + distance + " km</h2>");
        out.println("<h2>Route: " + String.join(" &rarr; ", path) + "</h2>");
        out.println("<a href='index.html'>Back to Home</a>");
        out.println("</body></html>");
    }
}
