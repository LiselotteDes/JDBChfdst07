package be.vdab;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
/*
Je roept een stored procedure op met een CallableStatement object.
CallableStatement erft van PreparedStatement
*/
public class Vb_OproepenStoredProcedures {
    private static final String URL = 
            "jdbc:mysql://localhost/tuincentrum?useSSL=false" +
            /*
            Een MySQL gebruiker kan standaard enkel een stored procedure uitvoeren als hij de metadata van alle databases mag lezen.
            Dit is gevaarlijk als een hacker deze gebruiker (en zijn paswoord) ontdekt.
            Een gebruiker kan zonder deze rechten een stored procedure uitvoeren als je in de JDBC URL 
            de parameter noAccessToProcedureBodies op true plaatst.
            */
            "&noAccessToProcedureBodies=true";
    private static final String USER = "cursist";
    private static final String PASSWORD = "cursist";
    private static final String CALL_PLANTEN_MET_EEN_WOORD = 
            "{call PlantenMetEenWoord(?)}";
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("woord: ");
            String woord = scanner.nextLine();
            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                    /* (*)
                    Geeft aan de Connection method prepareCall de naam van een stored procedure mee.
                    - Je tikt {call voor die naam
                    - Je stelt de stored procedure parameter(s) voor met ?
                    - je sluit af met }.
                    Je krijgt een CallableStatement object terug.
                    */
                    CallableStatement statement = connection.prepareCall(CALL_PLANTEN_MET_EEN_WOORD)) {
                /*
                Vult de eerste stored procedure parameter in.
                Concateneert % voor en na het woord dat de gebruiker intikte. 
                Het like onderdeel van het select statement in de stored procedure wordt dus '%bloem%' als de gebruiker bloem intikt.
                */
                statement.setString(1, '%' + woord + '%');
                // Voert de stored procedure uit die je bij (*) vermeldde en krijgt een ResultSet terug.
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        System.out.println(resultSet.getString("naam"));
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
    
}
