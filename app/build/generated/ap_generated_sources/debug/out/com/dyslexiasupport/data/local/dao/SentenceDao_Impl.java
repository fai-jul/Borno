package com.dyslexiasupport.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.dyslexiasupport.data.local.entity.SentenceEntity;
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
public final class SentenceDao_Impl implements SentenceDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SentenceEntity> __insertionAdapterOfSentenceEntity;

  private final EntityDeletionOrUpdateAdapter<SentenceEntity> __deletionAdapterOfSentenceEntity;

  public SentenceDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSentenceEntity = new EntityInsertionAdapter<SentenceEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `sentences` (`id`,`sentence`,`translation`,`language`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final SentenceEntity entity) {
        statement.bindLong(1, entity.id);
        if (entity.sentence == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.sentence);
        }
        if (entity.translation == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.translation);
        }
        if (entity.language == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.language);
        }
        statement.bindLong(5, entity.createdAt);
      }
    };
    this.__deletionAdapterOfSentenceEntity = new EntityDeletionOrUpdateAdapter<SentenceEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `sentences` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final SentenceEntity entity) {
        statement.bindLong(1, entity.id);
      }
    };
  }

  @Override
  public long insert(final SentenceEntity sentence) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfSentenceEntity.insertAndReturnId(sentence);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void insertAll(final List<SentenceEntity> sentences) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfSentenceEntity.insert(sentences);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final SentenceEntity sentence) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfSentenceEntity.handle(sentence);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public LiveData<List<SentenceEntity>> getAllSentences() {
    final String _sql = "SELECT * FROM sentences ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"sentences"}, false, new Callable<List<SentenceEntity>>() {
      @Override
      @Nullable
      public List<SentenceEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSentence = CursorUtil.getColumnIndexOrThrow(_cursor, "sentence");
          final int _cursorIndexOfTranslation = CursorUtil.getColumnIndexOrThrow(_cursor, "translation");
          final int _cursorIndexOfLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "language");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<SentenceEntity> _result = new ArrayList<SentenceEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SentenceEntity _item;
            final String _tmpSentence;
            if (_cursor.isNull(_cursorIndexOfSentence)) {
              _tmpSentence = null;
            } else {
              _tmpSentence = _cursor.getString(_cursorIndexOfSentence);
            }
            final String _tmpTranslation;
            if (_cursor.isNull(_cursorIndexOfTranslation)) {
              _tmpTranslation = null;
            } else {
              _tmpTranslation = _cursor.getString(_cursorIndexOfTranslation);
            }
            final String _tmpLanguage;
            if (_cursor.isNull(_cursorIndexOfLanguage)) {
              _tmpLanguage = null;
            } else {
              _tmpLanguage = _cursor.getString(_cursorIndexOfLanguage);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new SentenceEntity(_tmpSentence,_tmpTranslation,_tmpLanguage,_tmpCreatedAt);
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
  public int getSentenceCount() {
    final String _sql = "SELECT COUNT(*) FROM sentences";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _result;
      if (_cursor.moveToFirst()) {
        _result = _cursor.getInt(0);
      } else {
        _result = 0;
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
