package edu.duke.adtg.domain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Repository
public class CategoryDAO implements DAOFactory<Category> {
    private final DAOConn conn;
    private static final Logger logger = LoggerFactory.getLogger(CategoryDAO.class);

    @Autowired
    public CategoryDAO(DAOConn conn) {
        this.conn = conn;
    }

    @Override
    public void setConnection(DAOConn conn) {
    }

    @Override
    public Connection getConnection() {
        try {
            return conn.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }



    public List<String> getAllCategories() throws SQLException {
        String sql = "SELECT category FROM adtg.assn_category";
        List<String> categories = new ArrayList<>();
    
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        }
        return categories;
    }

    public String getPenaltyFormula(String category) throws SQLException {
        String sql = "SELECT * FROM adtg.assn_category WHERE category = ?";
        String penaltyFormula = null;
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    penaltyFormula = rs.getString("penalty");
                    return penaltyFormula;
                }
            }
        }
        return penaltyFormula;

    }
    


    // Method to check if a category exists
    public boolean checkCategoryExists(Category category) throws SQLException {
        String sql = "SELECT 1 FROM adtg.assn_category WHERE category = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, category.getName());
            try (ResultSet resultSet = ps.executeQuery()) {
                return resultSet.next();
            }
        }
    }


    // Method to add a category if it doesn't exist
    public void addCategory(Category category) throws SQLException {
        String sql = "INSERT INTO adtg.assn_category (category, penalty) VALUES (?,?)";
        
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getPenaltyFormula());
            ps.executeUpdate();
        }
    }


}
