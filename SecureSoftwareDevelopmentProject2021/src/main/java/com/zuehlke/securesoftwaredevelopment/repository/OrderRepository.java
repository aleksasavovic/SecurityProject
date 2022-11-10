package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;
import com.zuehlke.securesoftwaredevelopment.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OrderRepository {

    private DataSource dataSource;
    private static final Logger LOG = LoggerFactory.getLogger(CustomerRepository.class);
    public OrderRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    public List<Food> getMenu(int id) {
        List<Food> menu = new ArrayList<>();
        String sqlQuery = "SELECT id, name FROM food WHERE restaurantId=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery))
        {
            ps.setInt(1,id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                menu.add(createFood(rs));
            }

        } catch (SQLException e) {
            LOG.warn(e.getMessage());
        }

        return menu;
    }

    private Food createFood(ResultSet rs) throws SQLException {
        int id = rs.getInt(1);
        String name = rs.getString(2);
        return new Food(id, name);
    }

    public void insertNewOrder(NewOrder newOrder, int userId) {
        LocalDate date = LocalDate.now();
        String sqlQuery = "INSERT INTO delivery (isDone, userId, restaurantId, addressId, date, comment)" +
                "values (FALSE, ?,?,?,?,?)";
        try {
            Connection connection = dataSource.getConnection();
            PreparedStatement ps =connection.prepareStatement(sqlQuery);
            String strDate=date.getYear()+"-"+date.getMonthValue()+"-"+date.getDayOfMonth();
            ps.setInt(1,userId);
            ps.setInt(2,newOrder.getRestaurantId());
            ps.setInt(3,newOrder.getAddress());
            ps.setString(4,strDate);
            ps.setString(5,newOrder.getComment());
            ps.executeUpdate();
            sqlQuery = "SELECT MAX(id) FROM delivery";
            ps=connection.prepareStatement(sqlQuery);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                int deliveryId = rs.getInt(1);
                sqlQuery = "INSERT INTO delivery_item (amount, foodId, deliveryId)" +
                        "values";
                for (int i = 0; i < newOrder.getItems().length; i++) {
                    FoodItem item = newOrder.getItems()[i];
                    String deliveryItem = "";
                    if (i > 0) {
                        deliveryItem = ",";
                    }
                    deliveryItem += "(" + item.getAmount() + ", " + item.getFoodId() + ", " + deliveryId + ")";
                    sqlQuery += deliveryItem;
                }
               // System.out.println(sqlQuery);
                ps =connection.prepareStatement(sqlQuery);
                ps.executeUpdate();
                AuditLogger.getAuditLogger(OrderRepository.class).audit("Kreirana je   porudzbina  pod rednim brojem  '"+deliveryId+"'");
            }

        } catch (SQLException e) {
            LOG.warn(e.getMessage());
        }


    }

    public Object getAddresses(int userId) {
        List<Address> addresses = new ArrayList<>();
        String sqlQuery = "SELECT id, name FROM address WHERE userId=?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(sqlQuery))
        {
            ps.setInt(1,userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                addresses.add(createAddress(rs));
            }

        } catch (SQLException e) {
            LOG.warn(e.getMessage());
        }
        return addresses;
    }

    private Address createAddress(ResultSet rs) throws SQLException {
        int id = rs.getInt(1);
        String name = rs.getString(2);
        return new Address(id, name);

    }
}
