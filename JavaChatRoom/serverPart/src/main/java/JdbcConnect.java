import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**********
 * This class is used to connect to the database.
 */
public class JdbcConnect {
    private static final String USER_NAME = "root";
    private static final String PASSWORD = "12345678";
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/?user=root";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet rs;
    // construct the class
    public JdbcConnect(){
        try{
            Class.forName(DRIVER);
            System.out.println("DB connected");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    // Get the connection. (redundant but still not edit)
    public Connection getTheConnection(){
        return connection;
    }


   // Get the connection
    public Connection getConnection() {
        try{
            connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD);
            String dbName = "ChatRoom";
            connection.setCatalog(dbName);
        }catch (SQLException e){
            e.printStackTrace();
        }
        return connection;
    }


    // This method is used to update prepared statement, and return whether it is updated successfully.
    public boolean updateByPreparedStatement(String sql, List<Object> params) throws SQLException {
        int resultLineNumber = -1; // if the line is smaller than 0, it means that can't update
        preparedStatement = connection.prepareStatement(sql);
        if(params != null && !params.isEmpty()){
            for(int i = 0; i < params.size(); i++){
                preparedStatement.setObject(i+1, params.get(i));
            }
        }
        resultLineNumber = preparedStatement.executeUpdate();
        return resultLineNumber > 0;

    }


    // This method is used to find the resultSet, return the result as a map.
    public Map<String, Object> findSimpleResult(String sql, List<Object> params) throws SQLException {
        Map<String, Object> map = new HashMap<>();
        preparedStatement = connection.prepareStatement(sql);
        if(params != null && !params.isEmpty()){
            for(int i = 0; i < params.size(); i++){
                preparedStatement.setObject(i+1, params.get(i));
            }
        }
        rs = preparedStatement.executeQuery();
        ResultSetMetaData metaData = rs.getMetaData();
        int col = metaData.getColumnCount();
        while(rs.next()){
            for(int i = 0; i < col; i++){
                String colName = metaData.getColumnLabel(i+1);
                Object colValue = rs.getObject(colName);
                if(colValue == null){
                    colValue = "";
                }
                map.put(colName, colValue);
            }
        }
        return map;
    }


    public static void main(String[] args) {
        JdbcConnect jdbcConnect = new JdbcConnect();
        jdbcConnect.getConnection();
    }
}
