package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;
import com.zuehlke.securesoftwaredevelopment.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import javax.xml.transform.Result;
import java.sql.*;

@Repository
public class UserRepository {

    private static final Logger LOG = LoggerFactory.getLogger(UserRepository.class);

    private DataSource dataSource;

    public UserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public User findUser(String username) {
        String query = "SELECT id, username, password FROM users WHERE username=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(query))
        {
            ps.setString(1,username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(1);
                String username1 = rs.getString(2);
                String password = rs.getString(3);
                return new User(id, username1, password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean validCredentials(String username, String password) {
        String query = "SELECT username FROM users WHERE username=? AND password=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(query))
        {
            ps.setString(1,username);
            ps.setString(2,password);
            ResultSet rs =ps.executeQuery();
            Boolean success = rs.next();
            if(success)
                AuditLogger.getAuditLogger(UserRepository.class).audit("Logovanje uspelo za korisnika'" + username+"'");
            else
                AuditLogger.getAuditLogger(UserRepository.class).audit("Logovanje neuspesno za korisnika '" + username+"'");
            return success;
        } catch (SQLException e) {
            LOG.warn(e.getMessage());
        }
        return false;
    }

    public void delete(int userId) {
        String query = "DELETE FROM users WHERE id =?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1,userId);
            ps.executeUpdate();
            AuditLogger.getAuditLogger(UserRepository.class).audit("Logovanje je uspesno obrisao korisnika '" +userId +"'");
        } catch (SQLException e) {
            LOG.warn(e.getMessage());
        }
    }
}
