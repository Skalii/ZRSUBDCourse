package sample;

import java.sql.*;

/**
 * Created by Дмитрий on 11.06.2017.
 */
public class DBConnection {
    private String dataBasePath = "src\\DB.fdb?charSet=windows-1251",
            url = "jdbc:firebirdsql:local:" + dataBasePath,
            user = "SYSDBA",
            password = "masterkey";
    public Connection connection;
    private Statement statement;
    private ResultSet resultSet;

    public void connection() {
        try {
            Class.forName("org.firebirdsql.jdbc.FBDriver").newInstance();
            connection = DriverManager.getConnection(url, user, password);
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public String[][] createSQL(String sql) {
        try {
            resultSet = statement.executeQuery(sql);
            int col = resultSet.getMetaData().getColumnCount();
            resultSet.last();

            int row = resultSet.getRow();
            resultSet.first();
            String[][] array = new String[row + 1][col];
            int j = 0;

            for (int i = 1; i <= col; i++)
                array[j][i - 1] = resultSet.getMetaData().getColumnName(i);

            resultSet.beforeFirst();

            while (resultSet.next()) {
                j++;
                for (int i = 1; i < col + 1; i++) {
                    String s = resultSet.getString(i);
                    if (s != null)
                        array[j][i - 1] = s;
                }
            }
            statement.closeOnCompletion();
            return array;
        } catch (SQLException e) {
            e.printStackTrace();
            return new String[0][0];
        }
    }
}