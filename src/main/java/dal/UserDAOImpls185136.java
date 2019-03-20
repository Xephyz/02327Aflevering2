package dal;

import dal.dto.IUserDTO;
import dal.dto.UserDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserDAOImpls185136 implements IUserDAO {
    private Connection createConnection() throws SQLException{
        return DriverManager.getConnection("jdbc:mysql://ec2-52-30-211-3.eu-west-1.compute.amazonaws.com/andersm?"
                + "user=andersm&password=8UMlgcvXfNUVwkhAKd8VA");
    }

    @Override
    public IUserDTO getUser(int userId) throws DALException{
        try (Connection c = createConnection()){
            PreparedStatement getUsr = c.prepareStatement(
                    "SELECT * FROM Afl1Users WHERE userID = ?");
            getUsr.setInt(1, userId);
            ResultSet resSet = getUsr.executeQuery();
            IUserDTO user = null;
            if (resSet.next()){
                user = makeUserFromResultSet(resSet);
            }
            return user;
        } catch (SQLException e){
            throw new DALException(e.getMessage());
        }
    }

    @Override
    public List<IUserDTO> getUserList() throws DALException{

        try (Connection c = createConnection()){
            PreparedStatement getUsrLst = c.prepareStatement(
                    "SELECT * FROM Afl1Users");
            ResultSet resSet = getUsrLst.executeQuery();
            List<IUserDTO> users = new ArrayList<>();

            while (resSet.next()){
                IUserDTO user = makeUserFromResultSet(resSet);
                users.add(user);
            }
            return users;
        } catch (SQLException e){
            throw new DALException(e.getMessage());
        }
    }

    @Override
    public void createUser(IUserDTO user) throws DALException{
        try (Connection c = createConnection()){
            PreparedStatement createUser = c.prepareStatement(
                    "INSERT INTO Afl1Users (userID, userName, ini, roles) VALUES (?, ?, ?, ?)");
            String rolesStr = String.join(";", user.getRoles());
            createUser.setInt(1, user.getUserId());
            createUser.setString(2, user.getUserName());
            createUser.setString(3, user.getIni());
            createUser.setString(3, rolesStr);

            createUser.executeUpdate();

        } catch (SQLException e){
            throw new DALException(e.getMessage());
        }
    }

    @Override
    public void updateUser(IUserDTO user) throws DALException{
        try (Connection c = createConnection()){
            PreparedStatement updUser = c.prepareStatement(
                    "UPDATE Afl1Users SET " +
                            "userName = ?, " +
                            "ini = ?, " +
                            "roles = ? " +
                            "WHERE userID = ?");
            String rolesStr = String.join(";", user.getRoles());
            updUser.setString(1, user.getUserName());
            updUser.setString(2, user.getIni());
            updUser.setString(3, rolesStr);
            updUser.setInt(4, user.getUserId());
            updUser.executeUpdate();

        } catch (SQLException e){
            throw new DALException(e.getMessage());
        }
    }

    @Override
    public void deleteUser(int userId) throws DALException{
        try (Connection c = createConnection()){
            PreparedStatement delUsr = c.prepareStatement(
                    "DELETE FROM Afl1Users WHERE userId = ? ");
            delUsr.setInt(1, userId);
            delUsr.executeUpdate();

        } catch (SQLException e){
            throw new DALException(e.getMessage());
        }
    }

    private IUserDTO makeUserFromResultSet(ResultSet resSet) throws SQLException{
        IUserDTO user = new UserDTO();
        user.setUserId(resSet.getInt("userID"));
        user.setUserName(resSet.getString("userName"));
        user.setIni(resSet.getString("ini"));
        String rolesStr = resSet.getString("roles");
        String[] rolesArr = rolesStr.split(";");
        List<String> rolesList = Arrays.asList(rolesArr);
        user.setRoles(rolesList);
        return user;
    }

}
