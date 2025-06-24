package com.bloodbank;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class ViewDonorsServlet extends HttpServlet {

    private static final Map<String, Integer> bloodOrder = new HashMap<>();
    static {
        bloodOrder.put("A+", 1); bloodOrder.put("A-", 2);
        bloodOrder.put("B+", 3); bloodOrder.put("B-", 4);
        bloodOrder.put("AB+", 5); bloodOrder.put("AB-", 6);
        bloodOrder.put("O+", 7); bloodOrder.put("O-", 8);
    }

    private Connection connect() throws Exception {
        Properties props = new Properties();
        InputStream input = getServletContext().getResourceAsStream("/WEB-INF/db.properties");
        props.load(input);
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(
            props.getProperty("db.url"),
            props.getProperty("db.username"),
            props.getProperty("db.password")
        );
    }

    private static class Donor {
        String name, phone, email, bloodGroup, registrationDate;
        int age;
        Donor(String name, int age, String phone, String email, String bloodGroup, String registrationDate) {
            this.name = name; this.age = age; this.phone = phone;
            this.email = email; this.bloodGroup = bloodGroup;
            this.registrationDate = registrationDate;
        }
    }

    private List<Donor> mergeSort(List<Donor> donors) {
        if (donors.size() <= 1) return donors;
        int mid = donors.size() / 2;
        List<Donor> left = mergeSort(donors.subList(0, mid));
        List<Donor> right = mergeSort(donors.subList(mid, donors.size()));
        return merge(left, right);
    }

    private List<Donor> merge(List<Donor> left, List<Donor> right) {
        List<Donor> result = new ArrayList<>();
        int i = 0, j = 0;
        while (i < left.size() && j < right.size()) {
            int l = bloodOrder.getOrDefault(left.get(i).bloodGroup.toUpperCase(), Integer.MAX_VALUE);
            int r = bloodOrder.getOrDefault(right.get(j).bloodGroup.toUpperCase(), Integer.MAX_VALUE);
            result.add(l <= r ? left.get(i++) : right.get(j++));
        }
        while (i < left.size()) result.add(left.get(i++));
        while (j < right.size()) result.add(right.get(j++));
        return result;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String adminId = request.getParameter("admin_id");
        String email = request.getParameter("email");
        String donorName = request.getParameter("donorName");
        String donorPhone = request.getParameter("donorPhone");

        try (Connection con = connect()) {

            // ✅ Admin access (view all)
            if (adminId != null && !adminId.isEmpty()) {
                PreparedStatement adminStmt = con.prepareStatement("SELECT * FROM admins WHERE admin_id = ?");
                adminStmt.setString(1, adminId);
                ResultSet adminRs = adminStmt.executeQuery();
                if (adminRs.next()) {
                    doGet(request, response); return;
                }
            }

            // ✅ Validate if user email is approved
            PreparedStatement checkEmail = con.prepareStatement("SELECT status FROM user_requests WHERE email = ?");
            checkEmail.setString(1, email);
            ResultSet userRs = checkEmail.executeQuery();

            if (userRs.next() && "APPROVED".equalsIgnoreCase(userRs.getString("status"))) {
                showSpecificDonor(donorName, donorPhone, response);
            } else {
                response.setContentType("text/html");
                PrintWriter out = response.getWriter();
                out.println("<link rel='icon' type='image/png' href='images/Blood_transparent.png'>");
                out.println("<html><body style='font-family:sans-serif;text-align:center;'>");
                out.println("<h2 style='color:red;'>Access Denied: Your email is not approved to view donor details.</h2>");
                out.println("<a href='request-donor-access.html'>← Go Back</a>");
                out.println("</body></html>");
            }

        } catch (Exception e) {
            e.printStackTrace(response.getWriter());
        }
    }

    private void showSpecificDonor(String nameQuery, String phoneQuery, HttpServletResponse response) throws IOException {
        List<Donor> result = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        try (Connection con = connect()) {
            PreparedStatement stmt = con.prepareStatement(
                "SELECT Full_Name, Age, Phone, Email, Blood_Group, date_of_registration FROM donors WHERE Full_Name = ? AND Phone = ?"
            );
            stmt.setString(1, nameQuery);
            stmt.setString(2, phoneQuery);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                result.add(new Donor(
                    rs.getString("Full_Name"),
                    rs.getInt("Age"),
                    rs.getString("Phone"),
                    rs.getString("Email"),
                    rs.getString("Blood_Group"),
                    rs.getDate("date_of_registration") != null ? formatter.format(rs.getDate("date_of_registration")) : "N/A"
                ));
            }

            out.println("<html><head><title>Matched Donor</title>");
            out.println("<link rel='icon' type='image/png' href='images/Blood_transparent.png'>");
            out.println(phoneQuery);
            out.println("<style>body{font-family:sans-serif;background:#fdf6f6;text-align:center;} table{margin:auto;margin-top:20px;border-collapse:collapse;} th,td{padding:10px;border:1px solid #ccc;} th{background:#d73232;color:white;} tr:nth-child(even){background:#f8dede;} a{margin-top:20px;display:inline-block;color:#d73232;text-decoration:none;}</style>");
            out.println("</head><body>");
            if (result.isEmpty()) {
                out.println("<h2>No approved donor found with that name and number.</h2>");
            } else {
                out.println("<h2>Matched Donor</h2><table><tr><th>Name</th><th>Age</th><th>Phone</th><th>Email</th><th>Blood Group</th><th>Registration Date</th></tr>");
                for (Donor d : result) {
                    out.printf("<tr><td>%s</td><td>%d</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>",
                        d.name, d.age, d.phone, d.email, d.bloodGroup, d.registrationDate);
                }
                out.println("</table>");
            }
            out.println("<a href='request-donor-access.html'>← Back</a></body></html>");
        } catch (Exception e) {
            e.printStackTrace(out);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Donor> donorList = new ArrayList<>();
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

        try (Connection con = connect()) {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT Full_Name, Age, Phone, Email, Blood_Group, date_of_registration FROM donors");

            while (rs.next()) {
                donorList.add(new Donor(
                    rs.getString("Full_Name"),
                    rs.getInt("Age"),
                    rs.getString("Phone"),
                    rs.getString("Email"),
                    rs.getString("Blood_Group"),
                    rs.getDate("date_of_registration") != null ? formatter.format(rs.getDate("date_of_registration")) : "N/A"
                ));
            }

            List<Donor> sortedDonors = mergeSort(donorList);

            out.println("<html><head><title>All Donors</title>");
            out.println("<link rel='icon' type='image/png' href='images/Blood_transparent.png'>");
            out.println("<style>body{font-family:Arial;background:#fff5f5;margin:0;padding:0;} h2{text-align:center;color:#b30000;} .container{max-width:1000px;margin:auto;padding:20px;} table{width:100%;margin-top:20px;border-collapse:collapse;box-shadow:0 0 10px rgba(0,0,0,0.1);} th,td{padding:14px;border:1px solid #ccc;text-align:center;} th{background:#d73232;color:white;} tr:nth-child(even){background:#fce8e8;} .back-btn{text-align:center;margin-top:20px;} .back-btn a{background:#d73232;color:white;padding:10px 20px;text-decoration:none;border-radius:5px;} .back-btn a:hover{background:#a60000;}</style>");
            out.println("</head><body><div class='container'>");

            out.println("<div class='back-btn'><a href='index.html'>← Back to Home</a> <a href='review-requests'>→ View Donor List Requests</a></div>");

            out.println("<h2>Donor List (Sorted by Blood Group)</h2>");
            out.println("<table><tr><th>Name</th><th>Age</th><th>Phone</th><th>Email</th><th>Blood Group</th><th>Registration Date</th></tr>");
            for (Donor d : sortedDonors) {
                out.printf("<tr><td>%s</td><td>%d</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>",
                    d.name, d.age, d.phone, d.email, d.bloodGroup, d.registrationDate);
            }

            out.println("</table></div></body></html>");

        } catch (Exception e) {
            e.printStackTrace(out);
        }
    }
}
