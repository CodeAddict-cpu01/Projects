package com.bloodbank;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.Properties;

public class VerifyAdminServlet extends HttpServlet {
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        try {
            // Load database credentials
            InputStream input = getServletContext().getResourceAsStream("/WEB-INF/db.properties");
            Properties props = new Properties();
            props.load(input);

            String url = props.getProperty("db.url");
            String username = props.getProperty("db.username");
            String password = props.getProperty("db.password");

            // Load JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, username, password);

            // Read admin ID from request
            String adminIdStr = request.getParameter("adminId");

            if (adminIdStr == null || adminIdStr.trim().isEmpty()) {
                out.println("<h3>Admin ID cannot be empty</h3>");
                return;
            }

            int adminId = Integer.parseInt(adminIdStr.trim());

            // Query to check admin
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM admins WHERE admin_id = ?");
            ps.setInt(1, adminId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Admin found
                response.sendRedirect("view-donors");
            } else {
                out.println("<h1>Invalid Admin ID</h1>");
            }

            conn.close();

        } catch (NumberFormatException nfe) {
            out.println("<h1>Admin ID must be a number</h1>");
        } catch (Exception e) {
            e.printStackTrace(out);
        }
    }
}
