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
            IUserDTO user = new UserDTO();
            PreparedStatement getUsr = c.prepareStatement(
                    "SELECT * FROM Afl2_Users WHERE user_id = ?");
            getUsr.setInt(1, userId);
            ResultSet userResSet = getUsr.executeQuery();

            PreparedStatement getUsrRols = c.prepareStatement(
                    "SELECT role_name FROM Afl2_Roles INNER JOIN Afl2_UserRoles ON role_id = roles_id WHERE users_id = ?");
            getUsrRols.setInt(1, userId);
            ResultSet roleResSet = getUsrRols.executeQuery();

            if (userResSet.next()){
                user.setUserId(userResSet.getInt("user_id"));
                user.setUserName(userResSet.getString("user_username"));
                user.setIni(userResSet.getString("user_ini"));
            }
            if (roleResSet.next()){
                user.addRole(roleResSet.getString("role_name"));
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
                    "SELECT user_id FROM Afl2_Users");
            ResultSet resSet = getUsrLst.executeQuery();
            List<IUserDTO> users = new ArrayList<>();

            while (resSet.next()){
                IUserDTO user = getUser(resSet.getInt("user_id"));
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
                    "INSERT INTO Afl2_Users (user_id, user_username, user_ini) VALUES (?, ?, ?)");
            createUser.setInt(1, user.getUserId());
            createUser.setString(2, user.getUserName());
            createUser.setString(3, user.getIni());
            createUser.executeUpdate();

            PreparedStatement delOldRoles = c.prepareStatement(
                    "DELETE FROM Afl2_UserRoles WHERE users_id = ?");
            delOldRoles.setInt(1, user.getUserId());
            delOldRoles.executeUpdate();

            for (String s : user.getRoles()){
                PreparedStatement role = c.prepareStatement(
                        "SELECT role_id FROM Afl2_Roles WHERE role_name = ? LIMIT 1;");
                role.setString(1, s);
                ResultSet roleSet = role.executeQuery();
                if (roleSet.next()){
                    assignRole(user, roleSet, c);
                } else {
                    PreparedStatement newRole = c.prepareStatement(
                            "INSERT INTO Afl2_Roles (role_name) VALUES (?)");
                    newRole.setString(1, s);
                    newRole.executeUpdate();

                    PreparedStatement getRoleId = c.prepareStatement("SELECT role_id FROM Afl2_Roles WHERE role_name = ?");
                    getRoleId.setString(1, s);
                    ResultSet roleId = getRoleId.executeQuery();
                    if (roleId.next()){
                        assignRole(user, roleId, c);
                    }
                }
            }

        } catch (SQLException e){
            throw new DALException(e.getMessage());
        }
    }

    private void assignRole(IUserDTO user, ResultSet resSet, Connection c) throws SQLException{
        PreparedStatement assignRole = c.prepareStatement(
                "INSERT INTO Afl2_UserRoles (users_id, roles_id) VALUES (?, ?)");
        assignRole.setInt(1, user.getUserId());
        assignRole.setInt(2, resSet.getInt(1));
        assignRole.executeUpdate();
    }


    @Override
    public void updateUser(IUserDTO user) throws DALException{
        try (Connection c = createConnection()){
            deleteUser(user.getUserId());
            createUser(user);

        } catch (SQLException e){
            throw new DALException(e.getMessage());
        }
    }

    @Override
    public void deleteUser(int userId) throws DALException{
        try (Connection c = createConnection()){
            PreparedStatement delUsrRols = c.prepareStatement(
                    "DELETE FROM Afl2_UserRoles WHERE users_id = ?");
            PreparedStatement delUsr = c.prepareStatement(
                    "DELETE FROM Afl2_Users WHERE user_id = ? ");
            delUsrRols.setInt(1, userId);
            delUsr.setInt(1, userId);
            delUsrRols.executeUpdate();
            delUsr.executeUpdate();

        } catch (SQLException e){
            throw new DALException(e.getMessage());
        }
    }
}
