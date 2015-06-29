package us.foc.transcranial.dcs.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.List;


/**
 * Extends this class for each Android project and implement getEntities() e.g. call it
 * MyDatabaseHelper
 */
public abstract class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    public DatabaseHelper(Context context, String databaseName, int databaseVersion) {
        super(context, databaseName, null, databaseVersion);
    }

    public static DatabaseHelper getHelper(Context context, Class<? extends DatabaseHelper> helperClass) {
        return OpenHelperManager.getHelper(context, helperClass);
    }

    /**
     * This is called when the database is first created. Usually you should call createTable
     * statements here to create the tables that will store your data.
     */
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            List<Class<?>> entities = getEntities();

            if (entities != null) {
                for (Class<?> entity : entities) {
                    TableUtils.createTable(connectionSource, entity);
                }
            }
        }
        catch (SQLException e) {
            Log.e("DatabaseHelper", "Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Return a list of entity objects mapping to tables in the database
     *
     * @return
     */
    protected abstract List<Class<?>> getEntities();

    @Override
    public void onUpgrade(SQLiteDatabase database,
                          ConnectionSource connection,
                          int oldVersion,
                          int newVersion) {
    }
}
