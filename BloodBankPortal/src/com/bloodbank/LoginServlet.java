package com.bloodbank;

import java.io.*;
import java.sql.*;
import java.util.Properties;
import javax.servlet.*;
import javax.servlet.http.*;

public class LoginServlet extends HttpServlet {

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
        // Match these parameter names with your login.html form inputs
        String email = request.getParameter("loginEmail");
        String password = request.getParameter("loginPassword");

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        try (Connection con = connect()) {
            String sql = "SELECT * FROM register WHERE email = ? AND password = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                HttpSession session = request.getSession();
                session.setAttribute("user", rs.getString("full_name"));
                response.sendRedirect("index.html"); 
            } else {
                out.println("<h1>Invalid email or password. Please try again.</h1>");
                out.println("<a href='login.html'>Back to Login</a>");
            }
        } catch (Exception e) {
            e.printStackTrace(out);
        }
    }
}
