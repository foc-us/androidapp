package us.foc.transcranial.dcs.db;

import android.content.Context;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import us.foc.transcranial.dcs.model.ProgramEntity;

/**
 * DAO for creating/reading/updating/deleting programs
 */
public class ProgramEntityDao extends AbstractDao<ProgramEntity, String> {

    public ProgramEntityDao(Context context) {
        super(context);
    }

    @Override
    protected Dao<ProgramEntity, String> getDAO(DatabaseHelper helper)
            throws SQLException {
        return helper.getDao(ProgramEntity.class);
    }

    @Override
    protected Class<? extends DatabaseHelper> getHelperClass() {
        return FocusDatabaseHelper.class;
    }
}
