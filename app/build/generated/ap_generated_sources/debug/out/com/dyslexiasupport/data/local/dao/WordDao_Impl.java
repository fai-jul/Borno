package com.dyslexiasupport.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.dyslexiasupport.data.local.entity.WordEntity;
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
public final class WordDao_Impl implements WordDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<WordEntity> __insertionAdapterOfWordEntity;

  private final EntityDeletionOrUpdateAdapter<WordEntity> __deletionAdapterOfWordEntity;

  private final EntityDeletionOrUpdateAdapter<WordEntity> __updateAdapterOfWordEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteByIdSync;

  public WordDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfWordEntity = new EntityInsertionAdapter<WordEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `words` (`id`,`word`,`definition`,`language`,`syllables`,`isSaved`,`createdAt`,`ownerId`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final WordEntity entity) {
        statement.bindLong(1, entity.id);
        if (entity.word == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.word);
        }
        if (entity.definition == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.definition);
        }
        if (entity.language == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.language);
        }
        if (entity.syllables == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.syllables);
        }
        final int _tmp = entity.isSaved ? 1 : 0;
        statement.bindLong(6, _tmp);
        statement.bindLong(7, entity.createdAt);
        if (entity.ownerId == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.ownerId);
        }
      }
    };
    this.__deletionAdapterOfWordEntity = new EntityDeletionOrUpdateAdapter<WordEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `words` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final WordEntity entity) {
        statement.bindLong(1, entity.id);
      }
    };
    this.__updateAdapterOfWordEntity = new EntityDeletionOrUpdateAdapter<WordEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `words` SET `id` = ?,`word` = ?,`definition` = ?,`language` = ?,`syllables` = ?,`isSaved` = ?,`createdAt` = ?,`ownerId` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final WordEntity entity) {
        statement.bindLong(1, entity.id);
        if (entity.word == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.word);
        }
        if (entity.definition == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.definition);
        }
        if (entity.language == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.language);
        }
        if (entity.syllables == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.syllables);
        }
        final int _tmp = entity.isSaved ? 1 : 0;
        statement.bindLong(6, _tmp);
        statement.bindLong(7, entity.createdAt);
        if (entity.ownerId == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.ownerId);
        }
        statement.bindLong(9, entity.id);
      }
    };
    this.__preparedStmtOfDeleteByIdSync = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM words WHERE id = ? AND ownerId = ?";
        return _query;
      }
    };
  }

  @Override
  public long insert(final WordEntity word) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfWordEntity.insertAndReturnId(word);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void insertAll(final List<WordEntity> words) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfWordEntity.insert(words);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final WordEntity word) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfWordEntity.handle(word);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final WordEntity word) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfWordEntity.handle(word);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public int deleteByIdSync(final long id, final String ownerId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteByIdSync.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, id);
    _argIndex = 2;
    if (ownerId == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, ownerId);
    }
    try {
      __db.beginTransaction();
      try {
        final int _result = _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
        return _result;
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteByIdSync.release(_stmt);
    }
  }

  @Override
  public LiveData<List<WordEntity>> getAllWords(final String ownerId) {
    final String _sql = "SELECT * FROM words WHERE ownerId = ? OR ownerId = 'shared' ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (ownerId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, ownerId);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"words"}, false, new Callable<List<WordEntity>>() {
      @Override
      @Nullable
      public List<WordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfWord = CursorUtil.getColumnIndexOrThrow(_cursor, "word");
          final int _cursorIndexOfDefinition = CursorUtil.getColumnIndexOrThrow(_cursor, "definition");
          final int _cursorIndexOfLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "language");
          final int _cursorIndexOfSyllables = CursorUtil.getColumnIndexOrThrow(_cursor, "syllables");
          final int _cursorIndexOfIsSaved = CursorUtil.getColumnIndexOrThrow(_cursor, "isSaved");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfOwnerId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerId");
          final List<WordEntity> _result = new ArrayList<WordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WordEntity _item;
            final String _tmpWord;
            if (_cursor.isNull(_cursorIndexOfWord)) {
              _tmpWord = null;
            } else {
              _tmpWord = _cursor.getString(_cursorIndexOfWord);
            }
            final String _tmpDefinition;
            if (_cursor.isNull(_cursorIndexOfDefinition)) {
              _tmpDefinition = null;
            } else {
              _tmpDefinition = _cursor.getString(_cursorIndexOfDefinition);
            }
            final String _tmpLanguage;
            if (_cursor.isNull(_cursorIndexOfLanguage)) {
              _tmpLanguage = null;
            } else {
              _tmpLanguage = _cursor.getString(_cursorIndexOfLanguage);
            }
            final String _tmpSyllables;
            if (_cursor.isNull(_cursorIndexOfSyllables)) {
              _tmpSyllables = null;
            } else {
              _tmpSyllables = _cursor.getString(_cursorIndexOfSyllables);
            }
            final boolean _tmpIsSaved;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsSaved);
            _tmpIsSaved = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new WordEntity(_tmpWord,_tmpDefinition,_tmpLanguage,_tmpSyllables,_tmpIsSaved,_tmpCreatedAt);
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
  public LiveData<List<WordEntity>> getSavedWords(final String ownerId) {
    final String _sql = "SELECT * FROM words WHERE isSaved = 1 AND (ownerId = ? OR ownerId = 'shared') ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (ownerId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, ownerId);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"words"}, false, new Callable<List<WordEntity>>() {
      @Override
      @Nullable
      public List<WordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfWord = CursorUtil.getColumnIndexOrThrow(_cursor, "word");
          final int _cursorIndexOfDefinition = CursorUtil.getColumnIndexOrThrow(_cursor, "definition");
          final int _cursorIndexOfLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "language");
          final int _cursorIndexOfSyllables = CursorUtil.getColumnIndexOrThrow(_cursor, "syllables");
          final int _cursorIndexOfIsSaved = CursorUtil.getColumnIndexOrThrow(_cursor, "isSaved");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfOwnerId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerId");
          final List<WordEntity> _result = new ArrayList<WordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WordEntity _item;
            final String _tmpWord;
            if (_cursor.isNull(_cursorIndexOfWord)) {
              _tmpWord = null;
            } else {
              _tmpWord = _cursor.getString(_cursorIndexOfWord);
            }
            final String _tmpDefinition;
            if (_cursor.isNull(_cursorIndexOfDefinition)) {
              _tmpDefinition = null;
            } else {
              _tmpDefinition = _cursor.getString(_cursorIndexOfDefinition);
            }
            final String _tmpLanguage;
            if (_cursor.isNull(_cursorIndexOfLanguage)) {
              _tmpLanguage = null;
            } else {
              _tmpLanguage = _cursor.getString(_cursorIndexOfLanguage);
            }
            final String _tmpSyllables;
            if (_cursor.isNull(_cursorIndexOfSyllables)) {
              _tmpSyllables = null;
            } else {
              _tmpSyllables = _cursor.getString(_cursorIndexOfSyllables);
            }
            final boolean _tmpIsSaved;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsSaved);
            _tmpIsSaved = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new WordEntity(_tmpWord,_tmpDefinition,_tmpLanguage,_tmpSyllables,_tmpIsSaved,_tmpCreatedAt);
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
  public LiveData<List<WordEntity>> searchWords(final String query, final String ownerId) {
    final String _sql = "SELECT * FROM words WHERE (ownerId = ? OR ownerId = 'shared') AND (word LIKE '%' || ? || '%' OR definition LIKE '%' || ? || '%') ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    if (ownerId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, ownerId);
    }
    _argIndex = 2;
    if (query == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, query);
    }
    _argIndex = 3;
    if (query == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, query);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"words"}, false, new Callable<List<WordEntity>>() {
      @Override
      @Nullable
      public List<WordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfWord = CursorUtil.getColumnIndexOrThrow(_cursor, "word");
          final int _cursorIndexOfDefinition = CursorUtil.getColumnIndexOrThrow(_cursor, "definition");
          final int _cursorIndexOfLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "language");
          final int _cursorIndexOfSyllables = CursorUtil.getColumnIndexOrThrow(_cursor, "syllables");
          final int _cursorIndexOfIsSaved = CursorUtil.getColumnIndexOrThrow(_cursor, "isSaved");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfOwnerId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerId");
          final List<WordEntity> _result = new ArrayList<WordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WordEntity _item;
            final String _tmpWord;
            if (_cursor.isNull(_cursorIndexOfWord)) {
              _tmpWord = null;
            } else {
              _tmpWord = _cursor.getString(_cursorIndexOfWord);
            }
            final String _tmpDefinition;
            if (_cursor.isNull(_cursorIndexOfDefinition)) {
              _tmpDefinition = null;
            } else {
              _tmpDefinition = _cursor.getString(_cursorIndexOfDefinition);
            }
            final String _tmpLanguage;
            if (_cursor.isNull(_cursorIndexOfLanguage)) {
              _tmpLanguage = null;
            } else {
              _tmpLanguage = _cursor.getString(_cursorIndexOfLanguage);
            }
            final String _tmpSyllables;
            if (_cursor.isNull(_cursorIndexOfSyllables)) {
              _tmpSyllables = null;
            } else {
              _tmpSyllables = _cursor.getString(_cursorIndexOfSyllables);
            }
            final boolean _tmpIsSaved;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsSaved);
            _tmpIsSaved = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new WordEntity(_tmpWord,_tmpDefinition,_tmpLanguage,_tmpSyllables,_tmpIsSaved,_tmpCreatedAt);
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
  public int getWordCount(final String ownerId) {
    final String _sql = "SELECT COUNT(*) FROM words WHERE ownerId = ? OR ownerId = 'shared'";
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

  @Override
  public List<WordEntity> getAllWordsSync(final String ownerId) {
    final String _sql = "SELECT * FROM words WHERE ownerId = ? ORDER BY id DESC";
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
      final int _cursorIndexOfWord = CursorUtil.getColumnIndexOrThrow(_cursor, "word");
      final int _cursorIndexOfDefinition = CursorUtil.getColumnIndexOrThrow(_cursor, "definition");
      final int _cursorIndexOfLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "language");
      final int _cursorIndexOfSyllables = CursorUtil.getColumnIndexOrThrow(_cursor, "syllables");
      final int _cursorIndexOfIsSaved = CursorUtil.getColumnIndexOrThrow(_cursor, "isSaved");
      final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
      final int _cursorIndexOfOwnerId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerId");
      final List<WordEntity> _result = new ArrayList<WordEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final WordEntity _item;
        final String _tmpWord;
        if (_cursor.isNull(_cursorIndexOfWord)) {
          _tmpWord = null;
        } else {
          _tmpWord = _cursor.getString(_cursorIndexOfWord);
        }
        final String _tmpDefinition;
        if (_cursor.isNull(_cursorIndexOfDefinition)) {
          _tmpDefinition = null;
        } else {
          _tmpDefinition = _cursor.getString(_cursorIndexOfDefinition);
        }
        final String _tmpLanguage;
        if (_cursor.isNull(_cursorIndexOfLanguage)) {
          _tmpLanguage = null;
        } else {
          _tmpLanguage = _cursor.getString(_cursorIndexOfLanguage);
        }
        final String _tmpSyllables;
        if (_cursor.isNull(_cursorIndexOfSyllables)) {
          _tmpSyllables = null;
        } else {
          _tmpSyllables = _cursor.getString(_cursorIndexOfSyllables);
        }
        final boolean _tmpIsSaved;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsSaved);
        _tmpIsSaved = _tmp != 0;
        final long _tmpCreatedAt;
        _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
        _item = new WordEntity(_tmpWord,_tmpDefinition,_tmpLanguage,_tmpSyllables,_tmpIsSaved,_tmpCreatedAt);
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

  @Override
  public WordEntity getWordByIdSync(final long id, final String ownerId) {
    final String _sql = "SELECT * FROM words WHERE id = ? AND (ownerId = ? OR ownerId = 'shared') LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    _argIndex = 2;
    if (ownerId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, ownerId);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfWord = CursorUtil.getColumnIndexOrThrow(_cursor, "word");
      final int _cursorIndexOfDefinition = CursorUtil.getColumnIndexOrThrow(_cursor, "definition");
      final int _cursorIndexOfLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "language");
      final int _cursorIndexOfSyllables = CursorUtil.getColumnIndexOrThrow(_cursor, "syllables");
      final int _cursorIndexOfIsSaved = CursorUtil.getColumnIndexOrThrow(_cursor, "isSaved");
      final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
      final int _cursorIndexOfOwnerId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerId");
      final WordEntity _result;
      if (_cursor.moveToFirst()) {
        final String _tmpWord;
        if (_cursor.isNull(_cursorIndexOfWord)) {
          _tmpWord = null;
        } else {
          _tmpWord = _cursor.getString(_cursorIndexOfWord);
        }
        final String _tmpDefinition;
        if (_cursor.isNull(_cursorIndexOfDefinition)) {
          _tmpDefinition = null;
        } else {
          _tmpDefinition = _cursor.getString(_cursorIndexOfDefinition);
        }
        final String _tmpLanguage;
        if (_cursor.isNull(_cursorIndexOfLanguage)) {
          _tmpLanguage = null;
        } else {
          _tmpLanguage = _cursor.getString(_cursorIndexOfLanguage);
        }
        final String _tmpSyllables;
        if (_cursor.isNull(_cursorIndexOfSyllables)) {
          _tmpSyllables = null;
        } else {
          _tmpSyllables = _cursor.getString(_cursorIndexOfSyllables);
        }
        final boolean _tmpIsSaved;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsSaved);
        _tmpIsSaved = _tmp != 0;
        final long _tmpCreatedAt;
        _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
        _result = new WordEntity(_tmpWord,_tmpDefinition,_tmpLanguage,_tmpSyllables,_tmpIsSaved,_tmpCreatedAt);
        _result.id = _cursor.getLong(_cursorIndexOfId);
        if (_cursor.isNull(_cursorIndexOfOwnerId)) {
          _result.ownerId = null;
        } else {
          _result.ownerId = _cursor.getString(_cursorIndexOfOwnerId);
        }
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public WordEntity getRandomWordSync(final String ownerId) {
    final String _sql = "SELECT * FROM words WHERE ownerId = ? OR ownerId = 'shared' ORDER BY RANDOM() LIMIT 1";
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
      final int _cursorIndexOfWord = CursorUtil.getColumnIndexOrThrow(_cursor, "word");
      final int _cursorIndexOfDefinition = CursorUtil.getColumnIndexOrThrow(_cursor, "definition");
      final int _cursorIndexOfLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "language");
      final int _cursorIndexOfSyllables = CursorUtil.getColumnIndexOrThrow(_cursor, "syllables");
      final int _cursorIndexOfIsSaved = CursorUtil.getColumnIndexOrThrow(_cursor, "isSaved");
      final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
      final int _cursorIndexOfOwnerId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerId");
      final WordEntity _result;
      if (_cursor.moveToFirst()) {
        final String _tmpWord;
        if (_cursor.isNull(_cursorIndexOfWord)) {
          _tmpWord = null;
        } else {
          _tmpWord = _cursor.getString(_cursorIndexOfWord);
        }
        final String _tmpDefinition;
        if (_cursor.isNull(_cursorIndexOfDefinition)) {
          _tmpDefinition = null;
        } else {
          _tmpDefinition = _cursor.getString(_cursorIndexOfDefinition);
        }
        final String _tmpLanguage;
        if (_cursor.isNull(_cursorIndexOfLanguage)) {
          _tmpLanguage = null;
        } else {
          _tmpLanguage = _cursor.getString(_cursorIndexOfLanguage);
        }
        final String _tmpSyllables;
        if (_cursor.isNull(_cursorIndexOfSyllables)) {
          _tmpSyllables = null;
        } else {
          _tmpSyllables = _cursor.getString(_cursorIndexOfSyllables);
        }
        final boolean _tmpIsSaved;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsSaved);
        _tmpIsSaved = _tmp != 0;
        final long _tmpCreatedAt;
        _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
        _result = new WordEntity(_tmpWord,_tmpDefinition,_tmpLanguage,_tmpSyllables,_tmpIsSaved,_tmpCreatedAt);
        _result.id = _cursor.getLong(_cursorIndexOfId);
        if (_cursor.isNull(_cursorIndexOfOwnerId)) {
          _result.ownerId = null;
        } else {
          _result.ownerId = _cursor.getString(_cursorIndexOfOwnerId);
        }
      } else {
        _result = null;
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
