package com.dyslexiasupport.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.dyslexiasupport.data.local.entity.ExerciseResultEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ExerciseResultDao_Impl implements ExerciseResultDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ExerciseResultEntity> __insertionAdapterOfExerciseResultEntity;

  public ExerciseResultDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfExerciseResultEntity = new EntityInsertionAdapter<ExerciseResultEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `exercise_results` (`id`,`exerciseType`,`score`,`totalQuestions`,`timestamp`,`ownerId`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final ExerciseResultEntity entity) {
        statement.bindLong(1, entity.id);
        if (entity.exerciseType == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.exerciseType);
        }
        statement.bindLong(3, entity.score);
        statement.bindLong(4, entity.totalQuestions);
        statement.bindLong(5, entity.timestamp);
        if (entity.ownerId == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.ownerId);
        }
      }
    };
  }

  @Override
  public long insert(final ExerciseResultEntity result) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfExerciseResultEntity.insertAndReturnId(result);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public LiveData<List<ExerciseResultEntity>> getAllResults(final String ownerId) {
    final String _sql = "SELECT * FROM exercise_results WHERE ownerId = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (ownerId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, ownerId);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"exercise_results"}, false, new Callable<List<ExerciseResultEntity>>() {
      @Override
      @Nullable
      public List<ExerciseResultEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfExerciseType = CursorUtil.getColumnIndexOrThrow(_cursor, "exerciseType");
          final int _cursorIndexOfScore = CursorUtil.getColumnIndexOrThrow(_cursor, "score");
          final int _cursorIndexOfTotalQuestions = CursorUtil.getColumnIndexOrThrow(_cursor, "totalQuestions");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfOwnerId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerId");
          final List<ExerciseResultEntity> _result = new ArrayList<ExerciseResultEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExerciseResultEntity _item;
            final String _tmpExerciseType;
            if (_cursor.isNull(_cursorIndexOfExerciseType)) {
              _tmpExerciseType = null;
            } else {
              _tmpExerciseType = _cursor.getString(_cursorIndexOfExerciseType);
            }
            final int _tmpScore;
            _tmpScore = _cursor.getInt(_cursorIndexOfScore);
            final int _tmpTotalQuestions;
            _tmpTotalQuestions = _cursor.getInt(_cursorIndexOfTotalQuestions);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            _item = new ExerciseResultEntity(_tmpExerciseType,_tmpScore,_tmpTotalQuestions,_tmpTimestamp);
            _item.id = _cursor.getLong(_cursorIndexOfId);
            if (_cursor.isNull(_cursorIndexOfOwnerId)) {
              _item.ownerId = null;
            } else {
              _item.ownerId = _cursor.getString(_cursorIndexOfOwnerId);
            }
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public List<ExerciseResultEntity> getAllResultsSync(final String ownerId) {
    final String _sql = "SELECT * FROM exercise_results WHERE ownerId = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (ownerId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, ownerId);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfExerciseType = CursorUtil.getColumnIndexOrThrow(_cursor, "exerciseType");
      final int _cursorIndexOfScore = CursorUtil.getColumnIndexOrThrow(_cursor, "score");
      final int _cursorIndexOfTotalQuestions = CursorUtil.getColumnIndexOrThrow(_cursor, "totalQuestions");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfOwnerId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerId");
      final List<ExerciseResultEntity> _result = new ArrayList<ExerciseResultEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final ExerciseResultEntity _item;
        final String _tmpExerciseType;
        if (_cursor.isNull(_cursorIndexOfExerciseType)) {
          _tmpExerciseType = null;
        } else {
          _tmpExerciseType = _cursor.getString(_cursorIndexOfExerciseType);
        }
        final int _tmpScore;
        _tmpScore = _cursor.getInt(_cursorIndexOfScore);
        final int _tmpTotalQuestions;
        _tmpTotalQuestions = _cursor.getInt(_cursorIndexOfTotalQuestions);
        final long _tmpTimestamp;
        _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        _item = new ExerciseResultEntity(_tmpExerciseType,_tmpScore,_tmpTotalQuestions,_tmpTimestamp);
        _item.id = _cursor.getLong(_cursorIndexOfId);
        if (_cursor.isNull(_cursorIndexOfOwnerId)) {
          _item.ownerId = null;
        } else {
          _item.ownerId = _cursor.getString(_cursorIndexOfOwnerId);
        }
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
