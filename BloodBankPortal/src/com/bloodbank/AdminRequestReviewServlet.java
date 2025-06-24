package com.bloodbank;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.Properties;

public class AdminRequestReviewServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        try {
            // Load DB credentials
            InputStream input = getServletContext().getResourceAsStream("/WEB-INF/db.properties");
            Properties props = new Properties();
            props.load(input);

            String url = props.getProperty("db.url");
            String username = props.getProperty("db.username");
            String password = props.getProperty("db.password");

            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, username, password);

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, email, reason FROM user_requests WHERE status = 'PENDING'");

            out.println("<!DOCTYPE html>");
            out.println("<html><head><title>Admin Request Review</title>");
            out.println("<link rel='icon' href='Blood_transparent.png'>");
            out.println("<style>");
            out.println("body { font-family: Arial; background: #f9f9f9; text-align: center; }");
            out.println("h2 { color: #d73232; }");
            out.println("table { margin: 30px auto; border-collapse: collapse; width: 85%; box-shadow: 0 0 10px #ccc; }");
            out.println("th, td { padding: 12px; border: 1px solid #ddd; }");
            out.println("th { background: #d73232; color: white; }");
            out.println("form { display: inline; }");
            out.println("button.approve { background: #28a745; color: white; padding: 6px 12px; border: none; margin-right: 4px; cursor: pointer; }");
            out.println("button.reject { background: #dc3545; color: white; padding: 6px 12px; border: none; cursor: pointer; }");
            out.println("</style></head><body>");

            out.println("<h2>Pending Access Requests</h2>");
            out.println("<table>");
            out.println("<tr><th>Email</th><th>Reason</th><th>Actions</th></tr>");

            boolean hasRequests = false;
            while (rs.next()) {
                hasRequests = true;
                int id = rs.getInt("id");
                String email = rs.getString("email");
                String reason = rs.getString("reason");

                out.println("<tr>");
                out.println("<td>" + email + "</td>");
                out.println("<td>" + reason + "</td>");
                out.println("<td>");
                out.println("<form method='post' action='approve-request'>");
                out.println("<input type='hidden' name='requestId' value='" + id + "'>");
                out.println("<button type='submit' class='approve'>Approve</button>");
                out.println("</form>");

                out.println("<form method='post' action='reject-request'>");
                out.println("<input type='hidden' name='requestId' value='" + id + "'>");
                out.println("<button type='submit' class='reject'>Reject</button>");
                out.println("</form>");
                out.println("</td>");
                out.println("</tr>");
            }

            if (!hasRequests) {
                out.println("<tr><td colspan='3'>No pending requests.</td></tr>");
            }

            out.println("</table>");
            out.println("</body></html>");

            conn.close();

        } catch (Exception e) {
            out.println("<p style='color:red;'>Error occurred: " + e.getMessage() + "</p>");
            e.printStackTrace(out);
        }
    }
}