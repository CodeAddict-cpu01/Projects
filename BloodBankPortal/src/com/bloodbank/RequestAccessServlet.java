package com.bloodbank;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.Properties;
import java.util.logging.*;

public class RequestAccessServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(RequestAccessServlet.class.getName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String reason = request.getParameter("reason");

        Connection conn = null;
        PreparedStatement ps = null;
        PrintWriter out = response.getWriter();

        try {
            // Load DB connection properties
            InputStream input = getServletContext().getResourceAsStream("/WEB-INF/db.properties");
            Properties props = new Properties();
            props.load(input);

            String url = props.getProperty("db.url");
            String username = props.getProperty("db.username");
            String password = props.getProperty("db.password");

            // Establish database connection
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, username, password);

            // Prepare the SQL statement
            ps = conn.prepareStatement("INSERT INTO user_requests (email, reason) VALUES (?, ?)");
            ps.setString(1, email);
            ps.setString(2, reason);

            // Execute the update
            int result = ps.executeUpdate();

            // Set content type for response
            response.setContentType("text/html");

            // Send the response with HTML header
            out.println("<!DOCTYPE html>");
            out.println("<html lang='en'>");
            out.println("<head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            out.println("<title>Blood Bank Portal</title>");
            
            // Add the favicon linkc
            out.println("<link rel='icon' type='image/png' href='Blood_transparent.png'>"); // Your favicon location

            // Handle response based on result of database insert
            if (result > 0) {
                out.println("<h1>Request submitted successfully! Please wait for admin approval.</h1>");
            } else {
                out.println("<h1>Failed to submit request. Try again later.</h1>");
            }

            out.println("</body>");
            out.println("</html>");
        } catch (Exception e) {
            // Log the exception
            logger.log(Level.SEVERE, "Error processing request", e);

            // Display a generic error message to the user
            out.println("<h1>❌ An error occurred while processing your request. Please try again later.</h1>");
        } finally {
            // Ensure resources are closed
            try {
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Error closing resources", e);
            }
        }
    }
}
