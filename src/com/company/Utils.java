package com.company;

import com.company.Forms.ClientForm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.Properties;

public class Utils {
    public static Connection sysConnection;

    public static void createUser(Connection sysConnection, String userName, String password) {
        try {
            Statement statement = sysConnection.createStatement();
            statement.execute("Grant connect, resource TO " + userName + " IDENTIFIED BY " + password);
            statement.execute("Grant aq_user_role TO " + userName);
            statement.execute("Grant execute ON sys.dbms_aqadm TO " + userName);
            statement.execute("Grant execute ON sys.dbms_aq TO " + userName);
            statement.execute("Grant execute ON sys.dbms_aqin TO " + userName);
            statement.execute("Grant execute ON sys.dbms_aqjms TO " + userName);
            System.out.println("User " + userName + " has been created");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection connectAsSys(String password) throws SQLException {
        Locale.setDefault(Locale.ENGLISH);
        String url = "jdbc:oracle:thin:@localhost:1521:XE";
        Properties props = new Properties();
        props.put("user", "sys");
        props.put("password", password);
        props.put("internal_logon", "sysdba");
        return DriverManager.getConnection(url, props);
    }

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        Locale.setDefault(Locale.ENGLISH);
        String hostname = "localhost";
        String oracle_sid = "xe";
        int portno = 1521;
        String userName = "jmsuser";
        String password = "jmsuser";
        String driver = "thin";
//        QueueConnectionFactory QFac = null;
//        QueueConnection QCon = null;
//        ConnectionFactory QFac = null;
        Connection QCon = null;
            // get connection factory , not going through JNDI here
//            QFac = AQjmsFactory.getQueueConnectionFactory(hostname, oracle_sid, portno, driver);
//            Class.forName("oracle.jdbc.OracleDriver");
        String url = "jdbc:oracle:thin:@localhost:1521:XE";
        Properties props = new Properties();
        props.put("user", "sys");
        props.put("password", "123");
        props.put("internal_logon", "sysdba");

        java.sql.Connection connection = DriverManager.getConnection(url, props);
        Statement statement = connection.createStatement();
        statement.execute("Grant connect, resource TO jmsuser IDENTIFIED BY jmsuser");
        statement.execute("Grant aq_user_role TO jmsuser");
        statement.execute("Grant execute ON sys.dbms_aqadm TO jmsuser");
        statement.execute("Grant execute ON sys.dbms_aq TO jmsuser");
        statement.execute("Grant execute ON sys.dbms_aqin TO jmsuser");
        statement.execute("Grant execute ON sys.dbms_aqjms TO jmsuser");
            //QFac = AQjmsFactory.getConnectionFactory(url, props);

            // create connection
//            QCon = QFac.createQueueConnection(userName, password);
            //QCon = QFac.createConnection();
        return QCon;
    }


//    public static void main(String[] args) {
//        sysConnection = connectAsSys("123");
//        createUser(sysConnection, "jmsuser", "jmsuser");

//        AQjmsSession session = (AQjmsSession) connection.createSession(true, Session.CLIENT_ACKNOWLEDGE);
        //session.grantSystemPrivilege("MANAGE_ANY", "jmsuser", false);
        //connection.start();
//    }
}
