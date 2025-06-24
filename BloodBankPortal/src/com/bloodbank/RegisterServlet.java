package com.bloodbank;

import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class RegisterServlet extends HttpServlet {

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
        String fullName = request.getParameter("fullName");
        String age = request.getParameter("age");
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");
        String bloodGroup = request.getParameter("bloodGroup");

        LocalDate today = LocalDate.now();

        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try (Connection con = connect()) {
            String sql = "INSERT INTO donors (full_name, age, phone, email, blood_group,date_of_registration, approved) VALUES (?, ?, ?, ?, ?, ?, false)";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, fullName);
            stmt.setInt(2, Integer.parseInt(age));
            stmt.setString(3, phone);
            stmt.setString(4, email);
            stmt.setString(5, bloodGroup);
            stmt.setDate(6, java.sql.Date.valueOf(today));

            int result = stmt.executeUpdate();

            if (result > 0) {
                out.println("<h1>Thank you for registering, " + fullName + " 🩸</h1>");
            } else {
                out.println("<h1>Registration failed. Please try again.</h1>");
            }

        } catch (Exception e) {
            e.printStackTrace(out);
        }
    }
}
