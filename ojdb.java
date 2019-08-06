/**
 * Created by Matthew Pattermann on 11/18/2018.
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.*;
import java.lang.*;
import java.math.*;

public class Project3 {
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = ""; //removed: must use your own

    //  Database credentials
    static final String USER = ""; //enter username for jdb
    static final String PASS = ""; //enter password for jdb

    static String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";


    public static String encrypt(String text,String key){


        int len = text.length();

        String subkey = key + text;
        subkey = subkey.substring(0,subkey.length());

        String sb = "";
        for(int x=0;x<len;x++){
            int get1 = alphabet.indexOf(text.charAt(x));
            int get2 = alphabet.indexOf(subkey.charAt(x));

            int total = (get1 + get2)%26;
            if (total < 0) {
                total = 26 + total;
            }

            sb += alphabet.charAt(total);
        }

        return sb;
    }

    public static String decrypt(String text,String key){
        int len = text.length();

        String subkey = key + text;
        subkey = subkey.substring(0,subkey.length());

        String sb = "";
        for(int x=0;x<len;x++){
            int get1 = alphabet.indexOf(text.charAt(x));
            int get2 = alphabet.indexOf(subkey.charAt(x));

            int total = (get1 - get2)%26;
            if (total < 0) {
                total = 26 + total;
            }

            sb += alphabet.charAt(total);
        }

        return sb;
    }

    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        String curr_user = null;
        try{
            FileWriter writer = new FileWriter("output.txt");
            Class.forName("oracle.jdbc.OracleDriver");
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            ResultSet rs = null;
            //loop through input txt
            try (BufferedReader br = new BufferedReader(new FileReader("input.txt"))) {
                int l = 1;
                String line;
                while ((line = br.readLine()) != null) {
                    writer.write(l + ": " + line + "\n");
                    stmt = conn.createStatement();
                    Statement stmt2 = conn.createStatement();
                    // check for login
                    if (line.split(" ",2)[0].equals("LOGIN")) {
                        //do login stuff WORKS /////////////////////////////////////////////////////////////////////////

                        String sql;
                        sql = "SELECT Username FROM USERS WHERE Username = '" + line.split(" ",3)[1] + "' AND Password = '" + line.split(" ",3)[2] + "'";
                        rs = stmt.executeQuery(sql);
                        if(rs.next()) {
                            writer.write("Login successful\n");
                            curr_user = rs.getString("Username");

                        } else {
                            writer.write("Invalid login\n");
                        }
                    } else if (line.split(" ",2)[0].equals("CREATE")) {
                        //check for role or user
                        if (line.split(" ",3)[1].equals("ROLE")) {
                            //create role stuff ////////////////////////////////////////////////////////////////////////
                            //check for admin role of curr_user
                            String sql = "SELECT UsersRoles.RoleId FROM Users JOIN UsersRoles ON Users.UserId = UsersRoles.UserId WHERE Username = '" + curr_user + "'";
                            rs = stmt.executeQuery(sql);
                            if(rs.next()) {
                                if (rs.getString("RoleId").equals("1")) {
                                    //you are an admin
                                    int new_row_num = 0;
                                    sql = "SELECT COUNT(RoleId) FROM Roles";
                                    rs = stmt.executeQuery(sql);
                                    if(rs.next()) {
                                        new_row_num = rs.getInt("COUNT(RoleId)");
                                    }
                                    new_row_num++;

                                    sql = "INSERT INTO Roles VALUES (" + new_row_num + ", '" + line.split(" ",4)[2] + "', '" + line.split(" ",4)[3] + "')";
                                    rs = stmt.executeQuery(sql);
                                    writer.write("Role created successfully\n");
                                }
                            } else {
                                //not and admin
                                writer.write("Authorization failure\n");
                            }



                        } else if (line.split(" ",3)[1].equals("USER")) {
                            //create user stuff ////////////////////////////////////////////////////////////////////////
                            //check for admin role of curr_user
                            String sql = "SELECT UsersRoles.RoleId FROM Users JOIN UsersRoles ON Users.UserId = UsersRoles.UserId WHERE Username = '" + curr_user + "'";
                            rs = stmt.executeQuery(sql);
                            if(rs.next()) {
                                if (rs.getString("RoleId").equals("1")) {
                                    //you are an admin
                                    int new_row_num = 0;
                                    sql = "SELECT COUNT(UserId) FROM Users";
                                    rs = stmt.executeQuery(sql);
                                    if(rs.next()) {
                                        new_row_num = rs.getInt("COUNT(UserId)");
                                    }
                                    new_row_num++;

                                    sql = "INSERT INTO Users VALUES (" + new_row_num + ", '" + line.split(" ",4)[2] + "', '" + line.split(" ",4)[3] + "')";
                                    rs = stmt.executeQuery(sql);
                                    writer.write("User created successfully\n");
                                }
                            } else {
                                //not and admin
                                writer.write("Authorization failure\n");
                            }


                        } else {
                            //something went wrong in create command
                            System.out.println("Some invalid command");
                        }

                    } else if (line.split(" ",2)[0].equals("GRANT")) {
                        //check for role or user
                        if (line.split(" ",3)[1].equals("ROLE")) {
                            //grant role stuff /////////////////////////////////////////////////////////////////////////
                            //check for admin
                            String sql = "SELECT UsersRoles.RoleId FROM Users JOIN UsersRoles ON Users.UserId = UsersRoles.UserId WHERE Username = '" + curr_user + "'";
                            rs = stmt.executeQuery(sql);
                            if(rs.next()) {
                                if (rs.getString("RoleId").equals("1")) {
                                    //you are an admin
                                    //get userID
                                    int userID = -1;
                                    sql = "SELECT UserId FROM Users WHERE Username = '" + line.split(" ",4)[2] + "'";
                                    rs = stmt.executeQuery(sql);
                                    if (rs.next()) {
                                        userID = rs.getInt("UserId");
                                    }
                                    //get roleID
                                    int roleID = -1;
                                    sql = "SELECT RoleId FROM Roles WHERE RoleName = '" + line.split(" ",4)[3] + "'";
                                    rs = stmt.executeQuery(sql);
                                    if (rs.next()) {
                                        roleID = rs.getInt("RoleId");
                                    }

                                    sql = "INSERT INTO UsersRoles VALUES (" + userID + ", " + roleID + ")";
                                    rs = stmt.executeQuery(sql);
                                    writer.write("Role assigned successfully\n");
                                }
                            } else {
                                //not and admin
                                writer.write("Authorization failure\n");
                            }

                        } else if (line.split(" ",3)[1].equals("PRIVILEGE")) {
                            //grant privelege stuff ////////////////////////////////////////////////////////////////////
                            //check for admin
                            String sql = "SELECT UsersRoles.RoleId FROM Users JOIN UsersRoles ON Users.UserId = UsersRoles.UserId WHERE Username = '" + curr_user + "'";
                            rs = stmt.executeQuery(sql);
                            if(rs.next()) {
                                if (rs.getString("RoleId").equals("1")) {
                                    //you are an admin
                                    //get roleId
                                    int roleID = -1;
                                    sql = "SELECT RoleId FROM Roles WHERE RoleName = '" + line.split(" ",7)[4] + "'";
                                    rs = stmt.executeQuery(sql);
                                    if (rs.next()) {
                                        roleID = rs.getInt("RoleId");
                                    }

                                    //get privID
                                    int privID = -1;
                                    sql = "SELECT PrivId FROM Privileges WHERE PrivName = '" + line.split(" ",7)[2] + "'";
                                    rs = stmt.executeQuery(sql);
                                    if (rs.next()) {
                                        privID = rs.getInt("PrivId");
                                    }

                                    sql = "INSERT INTO RolesPrivileges VALUES (" + roleID + ", " + privID + ", '" + line.split(" ",7)[6] + "')";
                                    rs = stmt.executeQuery(sql);
                                    writer.write("Privilege granted successfully\n");

                                }
                            }
                        } else {
                            //something went wrong in create command
                            writer.write("Authorization failure\n");
                        }

                    } else if (line.split(" ",2)[0].equals("REVOKE")) {
                        //revoke privelege stuff ///////////////////////////////////////////////////////////////////////
                        //check for admin
                        String sql = "SELECT UsersRoles.RoleId FROM Users JOIN UsersRoles ON Users.UserId = UsersRoles.UserId WHERE Username = '" + curr_user + "'";
                        rs = stmt.executeQuery(sql);
                        if(rs.next()) {
                            if (rs.getString("RoleId").equals("1")) {
                                //you are an admin
                                //get roleId
                                int roleID = -1;
                                sql = "SELECT RoleId FROM Roles WHERE RoleName = '" + line.split(" ",7)[4] + "'";
                                rs = stmt.executeQuery(sql);
                                if (rs.next()) {
                                    roleID = rs.getInt("RoleId");
                                }

                                //get privID
                                int privID = -1;
                                sql = "SELECT PrivId FROM Privileges WHERE PrivName = '" + line.split(" ",7)[2] + "'";
                                rs = stmt.executeQuery(sql);
                                if (rs.next()) {
                                    privID = rs.getInt("PrivId");
                                }

                                sql = "DELETE FROM RolesPrivileges WHERE RoleId = '" + roleID + "' AND PrivID = '" + privID + "' AND TableName = '" + line.split(" ",7)[6] + "'";
                                rs = stmt.executeQuery(sql);
                                writer.write("Privilege revoked successfully\n");

                            }
                        } else {
                            writer.write("Authorization failure");
                        }


                    } else if (line.split(" ",2)[0].equals("INSERT")) {
                        //insert into stuff ////////////////////////////////////////////////////////////////////////////
                        //check if user has insert roles on the table
                        String sql;
                        int userID = -1;
                        sql = "SELECT UserId FROM Users WHERE Username = '" + curr_user + "'";
                        rs = stmt.executeQuery(sql);
                        if (rs.next()) {
                            userID = rs.getInt("UserId");
                        }

                        //get privID for INSERT
                        int privID = -1;
                        sql = "SELECT PrivId FROM Privileges WHERE PrivName = 'INSERT'";
                        rs = stmt.executeQuery(sql);
                        if (rs.next()) {
                            privID = rs.getInt("PrivId");
                        }

                        sql = "SELECT RoleId FROM UsersRoles WHERE UserId = '" + userID + "'";
                        rs = stmt.executeQuery(sql);
                        boolean tester = false;
                        while (rs.next()) {

                            int roleID = rs.getInt("RoleId");
                            sql = "SELECT RoleId FROM RolesPrivileges WHERE RoleId = " + roleID + " AND PrivId = " + privID + " AND TableName = '" + line.split(" ",4)[2] + "'";
                            ResultSet rs2 = stmt2.executeQuery(sql);
                            if (rs2.next()) {
                                tester = true;

                                //get columnNo
                                String temp = line.substring(line.lastIndexOf(") ") + 2);
                                int columnNo = Integer.parseInt(temp.split(" ", 3)[1]);
                                //get ownerRole
                                String ownerRoleS = (temp.split(" ", 3)[2]);
                                //System.out.println(ownerRoleS);
                                //get id of ownerrole
                                sql = "SELECT RoleId FROM Roles WHERE RoleName = '" + ownerRoleS + "'";
                                rs2 = stmt2.executeQuery(sql);
                                int ownerRole = 0;
                                if (rs2.next()) {
                                    ownerRole = rs2.getInt("RoleId");
                                }

                                //get array of values without parentheses
                                String values = line.substring(line.indexOf("(")+1,line.indexOf(")"));
                                ResultSet rs7;
                                if (columnNo > 0) {
                                    //get encryption key from roles table
                                    sql = "SELECT EncryptionKey FROM Roles WHERE RoleName = '" + line.substring(line.lastIndexOf(' ') + 1) + "'"; //get rolemanager
                                    rs7 = stmt2.executeQuery(sql);

                                    if (rs7.next()) {
                                        String encKey = rs7.getString("EncryptionKey");
                                        //encrypt value

                                        String trimmedValue = values.split(", ", 10)[columnNo - 1].substring(1, values.split(", ", 10)[columnNo - 1].length()-1);
                                        //System.out.println("trimmed value: " + trimmedValue); //////////////COMMENT
                                        String encrypted = encrypt(trimmedValue, encKey);
                                        //System.out.println("encrypted value: " + encrypted);
                                        //place tuple
                                        sql = "INSERT INTO " + line.split(" ",4)[2] + " VALUES (";

                                        for (int a = 0; a < columnNo - 1; a++) {
                                             sql = sql.concat(values.split(", ", columnNo)[a] + ", ");
                                        }
                                        sql = sql.concat("'" + encrypted + "', ");
                                        if (columnNo >= values.split(", ", columnNo+1).length) {
                                            sql = sql.concat(columnNo + ", " + ownerRole + ")");
                                        } else {
                                            sql = sql.concat(values.split(", ", columnNo+1)[columnNo] + ", " + columnNo + ", " + ownerRole + ")");
                                        }

                                        writer.write("Row inserted successfully\n");
                                        rs7 = stmt2.executeQuery(sql);
                                    }
                                } else {
                                    sql = "INSERT INTO " + line.split(" ",4)[2] + " VALUES " + values + ", " + columnNo + ", " + ownerRole;
                                    rs7 = stmt.executeQuery(sql);
                                    writer.write("Row inserted successfully\n");
                                }
                                break;
                            }
                            //rs2.close();
                        }
                        if (!tester) {
                            writer.write("Authorization failure\n");
                        }


                    } else if (line.split(" ",2)[0].equals("SELECT")) {
                        //select stuff /////////////////////////////////////////////////////////////////////////////////
                        //get userId
                        String sql;
                        int userID = -1;
                        sql = "SELECT UserId FROM Users WHERE Username = '" + curr_user + "'";
                        rs = stmt.executeQuery(sql);
                        if (rs.next()) {
                            userID = rs.getInt("UserId");
                        }
                        //get privID for SELECT
                        int privID = -1;
                        sql = "SELECT PrivId FROM Privileges WHERE PrivName = 'SELECT'";
                        rs = stmt.executeQuery(sql);
                        if (rs.next()) {
                            privID = rs.getInt("PrivId");
                        }


                        //get roleID and check for priveleges
                        sql = "SELECT RoleId FROM UsersRoles WHERE UserId = " + userID;
                        ResultSet rsboi = stmt.executeQuery(sql);
                        if (rsboi.next()) {
                            int roleId = rsboi.getInt("RoleId");
                            sql = "SELECT PrivId FROM RolesPrivileges WHERE TableName = '" + line.split(" ",4)[3] + "' AND PrivId = " + privID + " AND RoleId = " + roleId;
                            ResultSet rs2 = stmt.executeQuery(sql);
                            if (rs2.next()) {


                                //we can run the select code
                                sql = "SELECT * FROM " + line.split(" ",4)[3];
                                String table = line.split(" ",4)[3];

                                rs2 = stmt.executeQuery(sql);
                                if (table.equals("Customers")) {

                                    writer.write("CUSTOMERID, FIRSTNAME, LASTNAME, ADDRESS\n");

                                    while (rs2.next()) {

                                        int id = rs2.getInt("CustomerId");
                                        String fname = rs2.getString("FirstName");
                                        String lname = rs2.getString("LastName");
                                        String address = rs2.getString("Address");
                                        int ownerRole = rs2.getInt("OwnerRole");
                                        int columnNo = rs2.getInt("EncryptedColumn");

                                        if (columnNo > 0) {
                                            sql = "SELECT EncryptionKey FROM Roles WHERE RoleId = " + roleId;

                                            ResultSet rs3 = stmt2.executeQuery(sql);

                                            if (rs3.next()) {

                                                String enckey = rs3.getString("EncryptionKey");

                                                sql = "SELECT RoleId FROM UsersRoles WHERE RoleId = " + ownerRole + " AND UserId = " + userID;
                                                rs3 = stmt2.executeQuery(sql);

                                                if (rs3.next()) {
                                                    if (columnNo == 2) {
                                                        fname = decrypt(fname, enckey);
                                                    }
                                                    if (columnNo == 3) {
                                                        lname = decrypt(lname, enckey);
                                                    }
                                                    if (columnNo == 4) {
                                                        address = decrypt(address, enckey);
                                                    }
                                                    writer.write(id + ", " + fname + ", " + lname + ", " + address + "\n");
                                                }
                                            }

                                        } else {
                                            writer.write(id + ", " + fname + ", " + lname + ", " + address + "\n");
                                        }


                                    }

                                } else if (table.equals("Companies")) {
                                    writer.write("COMPANYID, COMPANYNAME ADDRESS STATE\n");
                                    while (rs2.next()) {

                                        int id = rs2.getInt("CompanyId");
                                        String cname = rs2.getString("CompanyName");
                                        String address = rs2.getString("Address");
                                        String state = rs2.getString("State");
                                        int ownerRole = rs2.getInt("OwnerRole");
                                        int columnNo = rs2.getInt("EncryptedColumn");

                                        if (columnNo > 0) {
                                            sql = "SELECT EncryptionKey FROM Roles WHERE RoleId = " + roleId;

                                            ResultSet rs3 = stmt2.executeQuery(sql);

                                            if (rs3.next()) {

                                                String enckey = rs3.getString("EncryptionKey");

                                                sql = "SELECT RoleId FROM UsersRoles WHERE RoleId = " + ownerRole + " AND UserId = " + userID;
                                                rs3 = stmt2.executeQuery(sql);

                                                if (rs3.next()) {
                                                    if (columnNo == 2) {
                                                        cname = decrypt(cname, enckey);
                                                    }
                                                    if (columnNo == 3) {
                                                        address = decrypt(address, enckey);
                                                    }
                                                    if (columnNo == 4) {
                                                        state = decrypt(state, enckey);
                                                    }
                                                    writer.write(id + ", " + cname + ", " + address + ", " + state + "\n");
                                                }
                                            }
                                        } else {
                                            writer.write(id + ", " + cname + ", " + address + ", " + state + "\n");
                                        }
                                    }

                                } else if (table.equals("Retailers")) {
                                    writer.write("RETAILERID, RETAILERNAME, ADDRESS\n");
                                    while (rs2.next()) {
                                        int id = rs2.getInt("RetailerId");
                                        String rname = rs2.getString("RetilaerName");
                                        String address = rs2.getString("Address");
                                        int ownerRole = rs2.getInt("OwnerRole");
                                        int columnNo = rs2.getInt("EncryptedColumn");

                                        if (columnNo > 0) {
                                            sql = "SELECT EncryptionKey FROM Roles WHERE RoleId = " + roleId;

                                            ResultSet rs3 = stmt2.executeQuery(sql);

                                            if (rs3.next()) {

                                                String enckey = rs3.getString("EncryptionKey");

                                                sql = "SELECT RoleId FROM UsersRoles WHERE RoleId = " + ownerRole + " AND UserId = " + userID;
                                                rs3 = stmt2.executeQuery(sql);
                                                if (rs3.next()) {
                                                    if (columnNo == 2) {
                                                        rname = decrypt(rname, enckey);
                                                    }
                                                    if (columnNo == 3) {
                                                        address = decrypt(address, enckey);
                                                    }

                                                    writer.write(id + ", " + rname + ", " + address + "\n");
                                                }
                                            }

                                        } else {
                                            writer.write(id + ", " + rname + ", " + address + "\n");
                                        }
                                    }

                                } else if (table.equals("Products")) {
                                    writer.write("PRODUCTID, PRODUCTNAME, CATEGORY, COMPANYID, EXFACTORYPRICE\n");

                                    while (rs2.next()) {

                                        int id = rs2.getInt("ProductId");
                                        String pname = rs2.getString("ProductName");
                                        String category = rs2.getString("Category");
                                        int cid = rs2.getInt("CompanyId");
                                        double price = rs2.getDouble("ExFactoryPrice");
                                        int ownerRole = rs2.getInt("OwnerRole");
                                        int columnNo = rs2.getInt("EncryptedColumn");

                                        if (columnNo > 0) {
                                            sql = "SELECT EncryptionKey FROM Roles WHERE RoleId = " + roleId;

                                            ResultSet rs3 = stmt2.executeQuery(sql);


                                            if (rs3.next()) {

                                                String enckey = rs3.getString("EncryptionKey");

                                                sql = "SELECT RoleId FROM UsersRoles WHERE RoleId = " + ownerRole + " AND UserId = " + userID;
                                                rs3 = stmt2.executeQuery(sql);
                                                if (rs3.next() || true) {
                                                    //System.out.println(columnNo);
                                                    if (columnNo == 2) {
                                                        pname = decrypt(pname, enckey);
                                                    }
                                                    if (columnNo == 3) {
                                                        category = decrypt(category, enckey);
                                                    }


                                                    writer.write(id + ", " + pname + ", " + category + ", " + cid + ", " + price + "\n");
                                                }
                                            }

                                        } else {
                                            writer.write(id + ", " + pname + ", " + category + ", " + cid + ", " + price + "\n");
                                        }
                                    }

                                } else if (table.equals("RetailerInventories")) {
                                    writer.write("PRODUCTID, RETAILERID, TOTALSTOCK\n");
                                    while (rs2.next()) {
                                        int pid = rs2.getInt("ProductId");
                                        int rid = rs2.getInt("RetailerId");
                                        int stock = rs2.getInt("TotalStock");
                                        int ownerRole = rs2.getInt("OwnerRole");
                                        int columnNo = rs2.getInt("EncryptedColumn");

                                        if (columnNo > 0) {
                                            sql = "SELECT EncryptionKey FROM Roles WHERE RoleId = " + roleId;

                                            ResultSet rs3 = stmt2.executeQuery(sql);

                                            if (rs3.next()) {

                                                String enckey = rs3.getString("EncryptionKey");

                                                sql = "SELECT RoleId FROM UsersRoles WHERE RoleId = " + ownerRole + " AND UserId = " + userID;
                                                rs3 = stmt2.executeQuery(sql);
                                                if (rs3.next()) {

                                                    writer.write(pid + ", " + rid + ", " + stock + "\n");
                                                }
                                            }

                                        } else {
                                            writer.write(pid + ", " + rid + ", " + stock + "\n");
                                        }
                                    }

                                } else if (table.equals("Orders")) {
                                    writer.write("ORDERID, CUSTOMERID, RETAILERID, PRODUCTID, COUNT, UNITPRICE, ORDERDATE, STATUS\n");
                                    while (rs2.next()) {
                                        int id = rs2.getInt("CompanyId");
                                        int cid = rs2.getInt("CustomerId");
                                        int rid = rs2.getInt("RetailerId");
                                        int pid = rs2.getInt("ProductId");
                                        int count = rs2.getInt("Count");
                                        int price = rs2.getInt("UnitPrice");
                                        String dte = rs2.getString("OrderDate");
                                        String status = rs2.getString("Status");
                                        int ownerRole = rs2.getInt("OwnerRole");
                                        int columnNo = rs2.getInt("EncryptedColumn");

                                        if (columnNo > 0) {
                                            sql = "SELECT EncryptionKey FROM Roles WHERE RoleId = " + roleId;

                                            ResultSet rs3 = stmt2.executeQuery(sql);

                                            if (rs3.next()) {

                                                String enckey = rs3.getString("EncryptionKey");

                                                sql = "SELECT RoleId FROM UsersRoles WHERE RoleId = " + ownerRole + " AND UserId = " + userID;
                                                rs3 = stmt2.executeQuery(sql);
                                                if (rs3.next()) {

                                                    if (columnNo == 7) {
                                                        dte = decrypt(dte, enckey);
                                                    }
                                                    if (columnNo == 8) {
                                                        status = decrypt(status, enckey);
                                                    }

                                                    writer.write(id + ", " + cid + ", " + rid + ", " + pid + ", " + count + ", " + price + ", " + dte + ", " + status + "\n");
                                                }
                                            }

                                        } else {
                                            writer.write(id + ", " + cid + ", " + rid + ", " + pid + ", " + count + ", " + price + ", " + dte + ", " + status + "\n");
                                        }
                                    }
                                } else {
                                    writer.write("Authorization failure\n");
                                }

                            }
                        }

                    } else if (line.split(" ",2)[0].equals("QUIT")) {
                        //quit stuff
                        break;

                    } else {
                        System.out.println("Some invalid command");
                    }
                    l++;
                    writer.write("\n");

                }
                //writer.close();
            }
            writer.close();
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            //Handle errors for JDBC
            se.printStackTrace();
        }catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
        }finally {
            //finally block used to close resources
            try {
                if (stmt != null) {
                    stmt.close();
                }

            } catch (SQLException se2) {
            }// nothing we can do
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }

        }
        //System.out.println("Adios!");
    }
}
