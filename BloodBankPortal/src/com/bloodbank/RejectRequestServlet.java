package com.bloodbank;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.Properties;

public class RejectRequestServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int requestId = Integer.parseInt(request.getParameter("requestId"));
        Connection conn = null;

        try {
            InputStream input = getServletContext().getResourceAsStream("/WEB-INF/db.properties");
            Properties props = new Properties();
            props.load(input);

            String url = props.getProperty("db.url");
            String username = props.getProperty("db.username");
            String password = props.getProperty("db.password");

            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, username, password);

            PreparedStatement ps = conn.prepareStatement("UPDATE user_requests SET status = 'REJECTED' WHERE id = ?");
            ps.setInt(1, requestId);
            int updated = ps.executeUpdate();

            System.out.println("Request ID " + requestId + " rejected. Rows affected: " + updated);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException ignored) {}
            response.sendRedirect("review-requests");
        }
    }
}
