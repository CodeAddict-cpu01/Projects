package com.bloodbank;

import java.io.*;
import java.sql.*;
import java.util.Properties;
import javax.servlet.*;
import javax.servlet.http.*;

public class AdminApprovalServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        boolean approve = Boolean.parseBoolean(request.getParameter("approve"));

        try {
            // Load DB credentials
            ServletContext context = getServletContext();
            InputStream input = context.getResourceAsStream("/WEB-INF/db.properties");
            Properties props = new Properties();
            props.load(input);

            Connection conn = DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.username"),
                props.getProperty("db.password")
            );

            PreparedStatement ps = conn.prepareStatement("UPDATE donors SET approved = ? WHERE email = ?");
            ps.setBoolean(1, approve);
            ps.setString(2, email);
            ps.executeUpdate();

            response.getWriter().println("User approval status updated.");
        } catch (Exception e) {
            e.printStackTrace(response.getWriter());
        }
    }
}
