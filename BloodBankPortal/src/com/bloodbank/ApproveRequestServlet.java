package com.bloodbank;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.Properties;

public class ApproveRequestServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        int requestId = Integer.parseInt(request.getParameter("requestId"));
        
        try {
            InputStream input = getServletContext().getResourceAsStream("/WEB-INF/db.properties");
            Properties props = new Properties();
            props.load(input);

            String url = props.getProperty("db.url");
            String username = props.getProperty("db.username");
            String password = props.getProperty("db.password");

            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, username, password);

            PreparedStatement ps = conn.prepareStatement("UPDATE user_requests SET status = 'APPROVED' WHERE id = ?");
            ps.setInt(1, requestId);
            ps.executeUpdate();

            conn.close();

            response.sendRedirect("review-requests");  // redirect back to the review page

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}