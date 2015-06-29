package us.foc.transcranial.dcs.db;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * DAO base class.
 *
 * @param T the object type being persisted
 * @param V the primary key type
 */
public abstract class AbstractDao<T, V> {

    protected final Context context;
    private Dao<T, V> dao;

    public AbstractDao(Context context) {
        this.context = context.getApplicationContext();
    }

    public void addAll(final List<T> entries) throws SQLException {
        // run as batch so success\fail as a batch
        runBatchTransaction(new Callable<Void>() {
            public Void call() throws Exception {
                for (T entry : entries) {
                    dao.createIfNotExists(entry);
                }
                return null;
            }
        });
    }

    public T add(T entry) throws SQLException {
        Dao<T, V> dao = getDAO();
        return dao.createIfNotExists(entry);
    }

    public void update(T entry) throws SQLException {
        Dao<T, V> dao = getDAO();
        dao.update(entry);
    }

    public void delete(T entry) throws SQLException {
        Dao<T, V> dao = getDAO();
        dao.delete(entry);
    }

    // Clear the table.
    public void deleteAll() throws SQLException {
        // run as batch so success\fail as a batch
        runBatchTransaction(new Callable<Void>() {
            public Void call() throws Exception {

                for (T entry : dao.queryForAll()) {
                    delete(entry);
                }

                return null;
            }
        });
    }

    public T get(V id) throws SQLException {
        Dao<T, V> dao = getDAO();
        return dao.queryForId(id);
    }

    public List<T> getAll() throws SQLException {
        Dao<T, V> dao = getDAO();
        return dao.queryForAll();
    }

    public T getFirstEntry() throws SQLException {
        Dao<T, V> dao = getDAO();

        QueryBuilder<T, V> builder = dao.queryBuilder();
        return builder.queryForFirst();
    }

    public long getCount() throws SQLException {
        Dao<T, V> dao = getDAO();
        return dao.countOf();
    }


    /**
     * Run SQL operations in a transaction - only commits after all operations have successfully run
     * Use this for running multiple operations on a single DAO or operations across multiple DOAs
     *
     * @param callable
     * @throws java.sql.SQLException
     */
    public void runBatchTransaction(Callable<Void> callable) throws SQLException {
        Dao<T, V> dao = getDAO();

        try {
            // run as batch so success\fail as a batch
            dao.callBatchTasks(callable);
        }
        catch (Exception e) {
            throw new SQLException(e);
        }
    }

    protected synchronized Dao<T, V> getDAO() throws SQLException {
        if (dao == null) {
            DatabaseHelper helper = DatabaseHelper.getHelper(context, getHelperClass());
            dao = getDAO(helper);
        }
        return dao;
    }

    /**
     * Get the DAO from the open helper - call helper.getDao(<YOUR_ENTITY>.class);
     *
     * @param helper
     * @return
     * @throws java.sql.SQLException
     */
    protected abstract Dao<T, V> getDAO(DatabaseHelper helper) throws SQLException;


    /**
     * Return the project open helper extended from DatabaseHelper e.g. MyDatabaseHelper.class
     *
     * @return
     */
    protected abstract Class<? extends DatabaseHelper> getHelperClass();
}
