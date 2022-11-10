package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;
import com.zuehlke.securesoftwaredevelopment.config.Entity;
import com.zuehlke.securesoftwaredevelopment.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CustomerRepository {

    private static final Logger LOG = LoggerFactory.getLogger(CustomerRepository.class);
    private static final AuditLogger auditLogger = AuditLogger.getAuditLogger(CustomerRepository.class);

    private DataSource dataSource;

    public CustomerRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Person createPersonFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt(1);
        String firstName = rs.getString(2);
        String lastName = rs.getString(3);
        String personalNumber = rs.getString(4);
        String address = rs.getString(5);
        return new Person(id, firstName, lastName, personalNumber, address);
    }

    public List<Customer> getCustomers() {
        List<com.zuehlke.securesoftwaredevelopment.domain.Customer> customers = new ArrayList<com.zuehlke.securesoftwaredevelopment.domain.Customer>();
        String query = "SELECT id, username FROM users";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {

            while (rs.next()) {
                customers.add(createCustomer(rs));
            }
        } catch (SQLException e) {
            LOG.warn(e.toString());
        }
        return customers;
    }

    private com.zuehlke.securesoftwaredevelopment.domain.Customer createCustomer(ResultSet rs) throws SQLException {
        return new com.zuehlke.securesoftwaredevelopment.domain.Customer(rs.getInt(1), rs.getString(2));
    }

    public List<Restaurant> getRestaurants() {
        List<Restaurant> restaurants = new ArrayList<Restaurant>();
        String query = "SELECT r.id, r.name, r.address, rt.name  FROM restaurant AS r JOIN restaurant_type AS rt ON r.typeId = rt.id ";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {

            while (rs.next()) {
                restaurants.add(createRestaurant(rs));
            }

        } catch (SQLException e) {
            LOG.warn(e.toString());
        }
        return restaurants;
    }

    private Restaurant createRestaurant(ResultSet rs) throws SQLException {
        int id = rs.getInt(1);
        String name = rs.getString(2);
        String address = rs.getString(3);
        String type = rs.getString(4);

        return new Restaurant(id, name, address, type);
    }


    public Object getRestaurant(String id){
        String query = "SELECT r.id, r.name, r.address, rt.name  FROM restaurant AS r JOIN restaurant_type AS rt ON r.typeId = rt.id WHERE r.id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(query))
        {
            ps.setString(1,id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                LOG.info("Resotran sa rednim brojem '"+id+"' je pregledan");
                return createRestaurant(rs);
            }


        } catch (SQLException e) {
            LOG.warn(e.getMessage());
        }
        return null;
    }
    public Object getRestaurant(String id,int i){
        String query = "SELECT r.id, r.name, r.address, rt.name  FROM restaurant AS r JOIN restaurant_type AS rt ON r.typeId = rt.id WHERE r.id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(query))
        {
            ps.setString(1,id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return createRestaurant(rs);
            }


        } catch (SQLException e) {
            LOG.warn(e.getMessage());
        }
        return null;
    }

    public void deleteRestaurant(int id) {
        String query = "DELETE FROM restaurant WHERE id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(query))
        {

            ps.setInt(1,id);
            ps.executeUpdate();
            AuditLogger.getAuditLogger(CustomerRepository.class).audit("je izbrisao restoran sa rednim brojem '"+id+"'");
        } catch (SQLException e) {
            LOG.warn(e.getMessage());
        }
    }

    public void updateRestaurant(RestaurantUpdate restaurantUpdate) {
        String query = "UPDATE restaurant SET name =?, address=?, typeId =? WHERE id =?";
        Restaurant old = (Restaurant) getRestaurant(String.valueOf(restaurantUpdate.getId()),1);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(query))


        {
            ps.setString(1,restaurantUpdate.getName());
            ps.setString(2,restaurantUpdate.getAddress());
            ps.setInt(3,restaurantUpdate.getRestaurantType());
            ps.setInt(4,restaurantUpdate.getId());
            ps.executeUpdate();
            if(!restaurantUpdate.getName().equals(old.getName()))
            AuditLogger.getAuditLogger(CustomerRepository.class).auditChange(new Entity("restoraunt update",
                    String.valueOf(old.getId()),
                            old.getName(),
                    restaurantUpdate.getName())
                   );
            if(!String.valueOf(restaurantUpdate.getRestaurantType()).equals(old.getRestaurantType()))
                AuditLogger.getAuditLogger(CustomerRepository.class).auditChange(new Entity("restoraunt update",
                        String.valueOf(old.getId()),
                        old.getRestaurantType(),
                        String.valueOf(restaurantUpdate.getRestaurantType()))
                );
            if(!restaurantUpdate.getAddress().equals(old.getAddress()))
                AuditLogger.getAuditLogger(CustomerRepository.class).auditChange(new Entity("restoraunt update",
                        String.valueOf(old.getId()),
                        old.getAddress(),
                        restaurantUpdate.getAddress())
                );

        } catch (SQLException e) {
            LOG.warn(e.getMessage());
        }

    }

    public Customer getCustomer(String id) {
        String sqlQuery = "SELECT id, username, password FROM users WHERE id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery))
        {
            ps.setString(1,id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                LOG.info("Profil musterije sa rednim brojem '"+id+"' je pregledan");
                return createCustomerWithPassword(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Customer getCustomer(String id, int i) {
        String sqlQuery = "SELECT id, username, password FROM users WHERE id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery))
        {
            ps.setString(1,id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return createCustomerWithPassword(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Customer createCustomerWithPassword(ResultSet rs) throws SQLException {
        int id = rs.getInt(1);
        String username = rs.getString(2);
        String password = rs.getString(3);
        return new Customer(id, username, password);
    }


    public void deleteCustomer(String id) {
        String query = "DELETE FROM users WHERE id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(query))
        {
            ps.setString(1,id);
            ps.executeUpdate();
            AuditLogger.getAuditLogger(CustomerRepository.class).audit("je izbrisao korisnika sa rednim brojem '"+id+"'");
        } catch (SQLException e) {
            LOG.warn(e.getMessage());
        }
    }
    // logovati lozinku kao plaintext nije pravo resenje
    // treba je ili sifrovati ili napraviti novu tabelu u bazi gde se cuvaju stare lozinke.
    //logovanje lozinke u ovom primeru je uradjeno samo u demonstracione svrhe
    public void updateCustomer(CustomerUpdate customerUpdate) {
        Customer old = (Customer) getCustomer(String.valueOf(customerUpdate.getId()));
        String query = "UPDATE users SET username =?, password=? WHERE id =?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(query))
        {
            ps.setString(1, customerUpdate.getUsername());
            ps.setString(2,customerUpdate.getPassword());
            ps.setInt(3,customerUpdate.getId());
            ps.executeUpdate();
            if(!customerUpdate.getUsername().equals(old.getUsername()))
                AuditLogger.getAuditLogger(CustomerRepository.class).auditChange(new Entity("customer update",
                        String.valueOf(old.getId()),
                        old.getUsername(),
                        customerUpdate.getUsername())
                );
            if(!String.valueOf(customerUpdate.getPassword()).equals(old.getPassword()))
                AuditLogger.getAuditLogger(CustomerRepository.class).auditChange(new Entity("customer update",
                        String.valueOf(old.getId()),
                        old.getPassword(),
                        String.valueOf(customerUpdate.getPassword()))
                );


        } catch (SQLException e) {
            LOG.warn(e.getMessage());
        }
    }

    public List<Address> getAddresses(String id) {
        String sqlQuery = "SELECT id, name FROM address WHERE userId=?";
        List<Address> addresses = new ArrayList<Address>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery))
        {
            ps.setString(1,id);
            ResultSet rs =ps.executeQuery();
            while (rs.next()) {
                addresses.add(createAddress(rs));
            }

        } catch (SQLException e) {
            LOG.warn(e.getMessage());
        }
        return addresses;
    }

    public Address getAddress(String id){
        String sqlQuery = "SELECT id, name FROM address WHERE id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery))
        {
            ps.setString(1,id);
            ResultSet rs =ps.executeQuery();
            if(rs.next())
                return createAddress(rs);

        } catch (SQLException e) {
            LOG.warn(e.getMessage());
        }
        return null;
    }


    private Address createAddress(ResultSet rs) throws SQLException {
        int id = rs.getInt(1);
        String name = rs.getString(2);
        return new Address(id, name);
    }

    public void deleteCustomerAddress(int id) {
        String query = "DELETE FROM address WHERE id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(query))
        {
            ps.setInt(1,id);
            ps.executeUpdate();
            LOG.warn("Adresa sa rednim brojem '"+id+"' je izbrisana");
        } catch (SQLException e) {
            LOG.warn(e.getMessage());
        }
    }

    public void updateCustomerAddress(Address address) {
        Address old = (Address) getAddress(String.valueOf(address.getId()));
        String query = "UPDATE address SET name =? WHERE id =?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(query))
        {
            ps.setString(1,address.getName());
            ps.setInt(2,address.getId());
            ps.executeUpdate();
            AuditLogger.getAuditLogger(CustomerRepository.class).auditChange(new Entity("address update",
                    String.valueOf(old.getId()),
                    old.getName(),
                    address.getName())
            );
        } catch (SQLException e) {
            LOG.warn(e.getMessage());
        }
    }

    public void putCustomerAddress(NewAddress newAddress) {
        String query = "INSERT INTO address (name, userId) VALUES (?,?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(query))
        {
            ps.setString(1,newAddress.getName());
            ps.setInt(2,newAddress.getUserId());
            ps.executeUpdate();
            LOG.warn("Dodata je nova adresa '"+ newAddress.getName()+"'");
        } catch (SQLException e) {
            LOG.warn(e.getMessage());
        }
    }
}
