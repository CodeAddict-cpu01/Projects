package com.bloodbank;

import java.io.*;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestBloodServlet extends HttpServlet {

    private static class BloodRequest implements Comparable<BloodRequest> {
        String name;
        int age;
        String phone;
        String email;
        String bloodGroup;
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

        // Priority: Emergency > Timestamp (Earlier requests first)
        @Override
        public int compareTo(BloodRequest other) {
            if (this.emergency != other.emergency) {
                return this.emergency ? -1 : 1;
            }
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String name = request.getParameter("name");
        int age = Integer.parseInt(request.getParameter("age"));
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");
        String bloodGroup = request.getParameter("bloodGroup");
        boolean emergency = request.getParameter("emergency") != null;
        Timestamp now = new Timestamp(System.currentTimeMillis());

        BloodRequest newRequest = new BloodRequest(name, age, phone, email, bloodGroup, emergency, now);

        try (Connection con = connect()) {
            String sql = "INSERT INTO blood_requests (name, age, phone, email, blood_group, emergency, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setInt(2, age);
            stmt.setString(3, phone);
            stmt.setString(4, email);
            stmt.setString(5, bloodGroup);
            stmt.setBoolean(6, emergency);
            stmt.setTimestamp(7, now);
            stmt.executeUpdate();

            out.println("<h1>Request submitted successfully!</h1>");
        } catch (Exception e) {
            e.printStackTrace(out);
        }
    }

    // This could be used in another method to retrieve and sort all requests:
    public List<BloodRequest> getSortedRequests(Connection con) throws SQLException {
        List<BloodRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM blood_requests";
        ResultSet rs = con.createStatement().executeQuery(sql);
        while (rs.next()) {
            list.add(new BloodRequest(
                rs.getString("name"),
                rs.getInt("age"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("blood_group"),
                rs.getBoolean("emergency"),
                rs.getTimestamp("timestamp")
            ));
        }
        PriorityQueue<BloodRequest> pq = new PriorityQueue<>(list);
        List<BloodRequest> sorted = new ArrayList<>();
        while (!pq.isEmpty()) sorted.add(pq.poll());
        return sorted;
    }
}

