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
import com.dyslexiasupport.data.local.entity.ReadingSessionEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
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
public final class ReadingSessionDao_Impl implements ReadingSessionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ReadingSessionEntity> __insertionAdapterOfReadingSessionEntity;

  public ReadingSessionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfReadingSessionEntity = new EntityInsertionAdapter<ReadingSessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `reading_sessions` (`id`,`textId`,`textTitle`,`durationSeconds`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final ReadingSessionEntity entity) {
        statement.bindLong(1, entity.id);
        if (entity.textId == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.textId);
        }
        if (entity.textTitle == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.textTitle);
        }
        statement.bindLong(4, entity.durationSeconds);
        statement.bindLong(5, entity.timestamp);
      }
    };
  }

  @Override
  public long insert(final ReadingSessionEntity session) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfReadingSessionEntity.insertAndReturnId(session);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public LiveData<List<ReadingSessionEntity>> getAllSessions() {
    final String _sql = "SELECT * FROM reading_sessions ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"reading_sessions"}, false, new Callable<List<ReadingSessionEntity>>() {
      @Override
      @Nullable
      public List<ReadingSessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTextId = CursorUtil.getColumnIndexOrThrow(_cursor, "textId");
          final int _cursorIndexOfTextTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "textTitle");
          final int _cursorIndexOfDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "durationSeconds");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final List<ReadingSessionEntity> _result = new ArrayList<ReadingSessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReadingSessionEntity _item;
            final String _tmpTextId;
            if (_cursor.isNull(_cursorIndexOfTextId)) {
              _tmpTextId = null;
            } else {
              _tmpTextId = _cursor.getString(_cursorIndexOfTextId);
            }
            final String _tmpTextTitle;
            if (_cursor.isNull(_cursorIndexOfTextTitle)) {
              _tmpTextTitle = null;
            } else {
              _tmpTextTitle = _cursor.getString(_cursorIndexOfTextTitle);
            }
            final long _tmpDurationSeconds;
            _tmpDurationSeconds = _cursor.getLong(_cursorIndexOfDurationSeconds);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            _item = new ReadingSessionEntity(_tmpTextId,_tmpTextTitle,_tmpDurationSeconds,_tmpTimestamp);
            _item.id = _cursor.getLong(_cursorIndexOfId);
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
  public LiveData<Long> getTotalReadingTimeSeconds() {
    final String _sql = "SELECT SUM(durationSeconds) FROM reading_sessions";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"reading_sessions"}, false, new Callable<Long>() {
      @Override
      @Nullable
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final Long _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(0);
            }
            _result = _tmp;
          } else {
            _result = null;
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
  public List<ReadingSessionEntity> getAllSessionsSync() {
    final String _sql = "SELECT * FROM reading_sessions ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfTextId = CursorUtil.getColumnIndexOrThrow(_cursor, "textId");
      final int _cursorIndexOfTextTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "textTitle");
      final int _cursorIndexOfDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "durationSeconds");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final List<ReadingSessionEntity> _result = new ArrayList<ReadingSessionEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final ReadingSessionEntity _item;
        final String _tmpTextId;
        if (_cursor.isNull(_cursorIndexOfTextId)) {
          _tmpTextId = null;
        } else {
          _tmpTextId = _cursor.getString(_cursorIndexOfTextId);
        }
        final String _tmpTextTitle;
        if (_cursor.isNull(_cursorIndexOfTextTitle)) {
          _tmpTextTitle = null;
        } else {
          _tmpTextTitle = _cursor.getString(_cursorIndexOfTextTitle);
        }
        final long _tmpDurationSeconds;
        _tmpDurationSeconds = _cursor.getLong(_cursorIndexOfDurationSeconds);
        final long _tmpTimestamp;
        _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        _item = new ReadingSessionEntity(_tmpTextId,_tmpTextTitle,_tmpDurationSeconds,_tmpTimestamp);
        _item.id = _cursor.getLong(_cursorIndexOfId);
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
