package com.bloodbank;

import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Properties;
import javax.servlet.*;
import javax.servlet.http.*;

public class SignupServlet extends HttpServlet {

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

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String full_Name = request.getParameter("fullName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        try (Connection con = connect()) {
            // Check if email already exists
            String checkSql = "SELECT * FROM register WHERE email = ?";
            PreparedStatement checkStmt = con.prepareStatement(checkSql);
            checkStmt.setString(1, email);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                out.println("<h3>Email already registered. Please login.</h3>");
            } else {
                String insertSql = "INSERT INTO register (full_name, email, password, created_at) VALUES (?, ?, ?, ?)";
                PreparedStatement insertStmt = con.prepareStatement(insertSql);
                insertStmt.setString(1, full_Name);
                insertStmt.setString(2, email);
                insertStmt.setString(3, password);
                insertStmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));

                int result = insertStmt.executeUpdate();

                if (result > 0) {
                    out.println("<h1>Signup successful. You can now <a href='login.html'>login</a>.</h1>");
                } else {
                    out.println("<h1>Signup failed. Please try again.</h1>");
                }
            }
        } catch (Exception e) {
            e.printStackTrace(out);
        }
    }
}
