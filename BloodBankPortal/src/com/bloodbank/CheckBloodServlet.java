package com.bloodbank;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.*;

public class CheckBloodServlet extends HttpServlet {

    // Define all valid blood groups in a fixed order
    private static final String[] BLOOD_TYPES = {
        "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
    };

    private Connection connect() throws Exception {
        Properties props = new Properties();
        InputStream input = getServletContext().getResourceAsStream("/WEB-INF/db.properties");
        props.load(input);
        String url = props.getProperty("db.url");
        String username = props.getProperty("db.username");
        String password = props.getProperty("db.password");
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(url, username, password);
    }

    // Binary Search (sorted list)
    private int binarySearch(List<String> bloodList, String target) {
        int left = 0, right = bloodList.size() - 1;
        while (left <= right) {
            int mid = (left + right) / 2;
            int cmp = bloodList.get(mid).compareToIgnoreCase(target);
            if (cmp == 0) return mid;
            else if (cmp < 0) left = mid + 1;
            else right = mid - 1;
        }
        return -1;
    }

    // Proper Count Sort using fixed-size array
    private Map<String, Integer> countBloodUnits(List<String> bloodGroups) {
        int[] counts = new int[BLOOD_TYPES.length];

        for (String bg : bloodGroups) {
            int index = getBloodGroupIndex(bg);
            if (index != -1) {
                counts[index]++;
            }
        }

        Map<String, Integer> freq = new HashMap<>();
        for (int i = 0; i < BLOOD_TYPES.length; i++) {
            freq.put(BLOOD_TYPES[i], counts[i]);
        }

        return freq;
    }

    // Helper: get index of a blood group
    private int getBloodGroupIndex(String bg) {
        for (int i = 0; i < BLOOD_TYPES.length; i++) {
            if (BLOOD_TYPES[i].equalsIgnoreCase(bg)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String searchGroup = request.getParameter("bloodGroup").toUpperCase();
        List<String> bloodGroups = new ArrayList<>();

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        try (Connection con = connect()) {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT blood_group FROM donors");

            while (rs.next()) {
                bloodGroups.add(rs.getString("blood_group").toUpperCase());
            }

            Collections.sort(bloodGroups); // Required for Binary Search

            int foundIndex = binarySearch(bloodGroups, searchGroup);
            Map<String, Integer> freqMap = countBloodUnits(bloodGroups);

            out.println("<!DOCTYPE html>");
            out.println("<html lang='en'>");
            out.println("<head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<title>Blood Check Result</title>");
            out.println("<link rel='icon' href='Blood_transparent.png'>");
            out.println("<h1>Blood Availability Result</h1>");

            if (foundIndex != -1) {
                int count = freqMap.getOrDefault(searchGroup, 0);
                out.println("<h2><strong>Blood Group:</strong> " + searchGroup + "</h2>");
                out.println("<h2><strong>Available Units:</strong> " + count + "</h2>");
            } else {
                out.println("<h2 style='color:red;'>Blood group " + searchGroup + " is currently unavailable.</h2>");
            }

            out.println("<br><a href='request-blood.html'>← Back to Request Form</a>");
            out.println("</body></html>");

        } catch (Exception e) {
            e.printStackTrace(out);
        }
    }
}
