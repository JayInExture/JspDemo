import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/FormServlet")
public class FormServlet extends HttpServlet {
    private Connection connection;
    private static final Logger Log = LogManager.getLogger(FormServlet.class);

    public void init() throws ServletException {
        super.init();
        DatabaseInitializer initializer = DatabaseInitializer.getInstance();
        connection = initializer.getConnection();
    }

    public void destroy() {
        super.destroy();
        if (connection != null) {
            try {
                connection.close();
                Log.info("Database connection closed successfully.");
            } catch (SQLException e) {
                Log.error("Error closing database connection: " + e.getMessage(), e);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        displayUserData(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, IOException {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String phoneNumber = request.getParameter("phoneNumber");
        String alphaExp = "^[a-zA-Z ]*$";
        if (!firstName.matches(alphaExp)) {
            request.setAttribute("errorMessage", "First name must contain only alphabetic characters.");
            displayUserData(request, response);
            return;
        }

        if (!lastName.matches(alphaExp)) {
            request.setAttribute("errorMessage", "Last name must contain only alphabetic characters.");
            displayUserData(request, response);
            return;
        }

        if (connection != null) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO jsptable (first_name, last_name, phone_number) VALUES (?, ?, ?)")) {
                statement.setString(1, firstName);
                statement.setString(2, lastName);
                statement.setString(3, phoneNumber);
                statement.executeUpdate();

                request.setAttribute("successMessage","Data Added Successfully!!");
                displayUserData(request, response);
            } catch (SQLException e) {
                request.setAttribute("errorMessage", e.getMessage());
                displayUserData(request, response);
            }
        } else {
            Log.error("Database connection is null");
        }

        response.sendRedirect(request.getContextPath() + "/");
    }

    private void displayUserData(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<User> userData = new ArrayList<>();
        try {
            String searchQuery = request.getParameter("searchQuery");
            String sortOrder = request.getParameter("sortOrder");

            // Get current page number from request parameter, default to 1
            int currentPage = 1;
            String pageParam = request.getParameter("page");
            if (pageParam != null && !pageParam.isEmpty()) {
                currentPage = Integer.parseInt(pageParam);
            }

            // Get number of rows per page from request parameter, default to 3
            int rowsPerPage = 3;
            String rowsPerPageParam = request.getParameter("rowsPerPage");
            if (rowsPerPageParam != null && !rowsPerPageParam.isEmpty()) {
                rowsPerPage = Integer.parseInt(rowsPerPageParam);
            }

            // Calculate offset for pagination
            int offset = (currentPage - 1) * rowsPerPage;

            // Construct SQL query to retrieve data for the current page
            String sqlQuery = "SELECT * FROM jsptable";
            if (searchQuery != null && !searchQuery.isEmpty()) {
                sqlQuery += " WHERE first_name LIKE '%" + searchQuery + "%' OR last_name LIKE '%" + searchQuery + "%' OR phone_number LIKE '%" + searchQuery + "%'";
            }
            if (sortOrder != null && !sortOrder.isEmpty()) {
                sqlQuery += " ORDER BY first_name " + sortOrder; // Sort by first name with selected order
            }
            sqlQuery += " LIMIT ?, ?";

            // Prepare statement and execute query
            try (PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
                statement.setInt(1, offset);
                statement.setInt(2, rowsPerPage);
                ResultSet rs = statement.executeQuery();

                // Populate user data list
                while (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setFirstName(rs.getString("first_name"));
                    user.setLastName(rs.getString("last_name"));
                    user.setPhoneNumber(rs.getInt("phone_number"));
                    userData.add(user);
                }
            }

            // Calculate total number of pages
            int totalCount = getTotalRowCount(searchQuery); // Implement this method to get the total count of records based on search query
            int totalPages = (int) Math.ceil((double) totalCount / rowsPerPage);

            // Set necessary attributes in the request
            request.setAttribute("userData", userData);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("currentPage", currentPage);
            request.setAttribute("rowsPerPage", rowsPerPage);

            // Forward the request to the JSP page
            RequestDispatcher dispatcher = request.getRequestDispatcher("/index.jsp");
            dispatcher.forward(request, response);
        } catch (SQLException e) {
            Log.error("Exception occurred while fetching user data", e);
        }
    }

    private int getTotalRowCount(String searchQuery) throws SQLException {
        int totalCount = 0;
        try (Statement statement = connection.createStatement()) {
            String countQuery = "SELECT COUNT(*) AS total FROM jsptable";
            if (searchQuery != null && !searchQuery.isEmpty()) {
                countQuery += " WHERE first_name LIKE '%" + searchQuery + "%' OR last_name LIKE '%" + searchQuery + "%' OR phone_number LIKE '%" + searchQuery + "%'";
            }
            ResultSet rs = statement.executeQuery(countQuery);
            if (rs.next()) {
                totalCount = rs.getInt("total");
            }
        }
        return totalCount;
    }
}
