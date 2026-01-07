package com.budget.dao;

import com.budget.db.DatabaseService;
import com.budget.modules.finance.domain.Purse;
import com.budget.modules.finance.domain.PurseType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PurseDAO {

    public List<Purse> getAllPurses() {
        List<Purse> list = new ArrayList<>();
        String sql = "SELECT * FROM purses ORDER BY id";
        try (Connection conn = DatabaseService.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Purse(
                        rs.getInt("id"),
                        rs.getString("name"),
                        PurseType.valueOf(rs.getString("type")),
                        rs.getDouble("allocated_amount"),
                        rs.getDouble("spent_amount"),
                        rs.getBoolean("is_locked"),
                        rs.getDouble("buffer_allowance")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}