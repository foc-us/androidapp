package us.foc.transcranial.dcs.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import us.foc.transcranial.dcs.model.LazyProgramProvider;
import us.foc.transcranial.dcs.model.ProgramEntity;

/**
 * Performs initial setup/upgrade for an Ormlite DB.
 */
public class FocusDatabaseHelper extends DatabaseHelper {

    private static final String DATABASE_NAME = "focus.db";
    private static final int DATABASE_VERSION = 1;
    private final Context context;

    public FocusDatabaseHelper(Context context) {

        super(context, DATABASE_NAME, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    /**
     * Return the list of entities which require tables to be created in the database.
     */
    @Override
    protected List<Class<?>> getEntities() {

        ArrayList<Class<?>> entityList = new ArrayList<>();

        entityList.add(ProgramEntity.class);

        return entityList;
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        super.onCreate(db, connectionSource);

        ProgramEntityDao programEntityDao = new ProgramEntityDao(context);

        try {
            programEntityDao.add(LazyProgramProvider.getGamerProgram(context));
            programEntityDao.add(LazyProgramProvider.getEnduroProgram(context));
            programEntityDao.add(LazyProgramProvider.getWaveProgram(context));
            programEntityDao.add(LazyProgramProvider.getPulseProgram(context));
            programEntityDao.add(LazyProgramProvider.getNoiseProgram(context));
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
