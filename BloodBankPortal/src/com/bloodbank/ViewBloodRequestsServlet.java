package com.bloodbank;

import java.io.*;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class ViewBloodRequestsServlet extends HttpServlet {

    private static class BloodRequest implements Comparable<BloodRequest> {
        String name, phone, email, bloodGroup;
        int age;
        boolean emergency;
        Timestamp timestamp;

        BloodRequest(String name, int age, String phone, String email, String bloodGroup, boolean emergency, Timestamp timestamp) {
            this.name = name;
            this.age = age;
            this.phone = phone;
            this.email = email;
            this.bloodGroup = bloodGroup;
            this.emergency = emergency;
            this.timestamp = timestamp;
        }

        @Override
        public int compareTo(BloodRequest other) {
            if (this.emergency != other.emergency) return this.emergency ? -1 : 1;
            return this.timestamp.compareTo(other.timestamp);
        }
    }

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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        PriorityQueue<BloodRequest> pq = new PriorityQueue<>();

        try (Connection con = connect()) {
            String sql = "SELECT * FROM blood_requests";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                pq.add(new BloodRequest(
                    rs.getString("name"),
                    rs.getInt("age"),
                    rs.getString("phone"),
                    rs.getString("email"),
                    rs.getString("blood_group"),
                    rs.getBoolean("emergency"),
                    rs.getTimestamp("timestamp")
                ));
            }

            out.println("<html><head><title>Blood Requests</title></head><body>");
            out.println("<h2>Sorted Blood Requests (Emergency First)</h2>");
            out.println("<table border='1'><tr><th>Name</th><th>Age</th><th>Phone</th><th>Email</th><th>Blood Group</th><th>Emergency</th><th>Timestamp</th></tr>");

            while (!pq.isEmpty()) {
                BloodRequest br = pq.poll();
                out.printf("<tr><td>%s</td><td>%d</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>",
                    br.name, br.age, br.phone, br.email, br.bloodGroup,
                    br.emergency ? "Yes" : "No", br.timestamp.toString());
            }

            out.println("</table></body></html>");

        } catch (Exception e) {
            e.printStackTrace(out);
        }
    }
}

