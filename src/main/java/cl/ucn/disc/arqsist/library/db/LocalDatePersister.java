package cl.ucn.disc.arqsist.library.db;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.BaseDataType;
import com.j256.ormlite.support.DatabaseResults;

import java.sql.SQLException;
import java.time.LocalDate;

public class LocalDatePersister extends BaseDataType {

    private static final LocalDatePersister SINGLETON = new LocalDatePersister();

    private LocalDatePersister() {
        super(SqlType.STRING, new Class<?>[] { LocalDatePersister.class });
    }

    public static LocalDatePersister getSingleton() {
        return SINGLETON;
    }

    @Override
    public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {

        return defaultStr;
    }

    @Override
    public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {

        return results.getString(columnPos);
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
        if (sqlArg == null) {
            return null;
        }
        String val = (String) sqlArg;
        return LocalDate.parse(val);
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) throws SQLException {
        if (javaObject == null) {
            return null;
        }
        return javaObject.toString();
    }
}
