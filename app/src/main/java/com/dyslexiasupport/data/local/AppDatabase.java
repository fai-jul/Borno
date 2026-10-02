package com.dyslexiasupport.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.dyslexiasupport.data.local.dao.DocumentDao;
import com.dyslexiasupport.data.local.dao.ExerciseResultDao;
import com.dyslexiasupport.data.local.dao.NoteDao;
import com.dyslexiasupport.data.local.dao.ReadingSessionDao;
import com.dyslexiasupport.data.local.dao.SentenceDao;
import com.dyslexiasupport.data.local.dao.WordDao;
import com.dyslexiasupport.data.local.entity.DocumentEntity;
import com.dyslexiasupport.data.local.entity.ExerciseResultEntity;
import com.dyslexiasupport.data.local.entity.NoteEntity;
import com.dyslexiasupport.data.local.entity.ReadingSessionEntity;
import com.dyslexiasupport.data.local.entity.SentenceEntity;
import com.dyslexiasupport.data.local.entity.WordEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Main Room database instance.
 * Version 2 adds the "documents" table (scanned texts). The migration keeps all existing data.
 */
@Database(entities = {
        WordEntity.class,
        SentenceEntity.class,
        NoteEntity.class,
        ReadingSessionEntity.class,
        ExerciseResultEntity.class,
        DocumentEntity.class
}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "dyslexia_database";
    private static volatile AppDatabase INSTANCE;

    public abstract WordDao wordDao();
    public abstract SentenceDao sentenceDao();
    public abstract NoteDao noteDao();
    public abstract ReadingSessionDao readingSessionDao();
    public abstract ExerciseResultDao exerciseResultDao();
    public abstract DocumentDao documentDao();

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            // Must match exactly what Room expects for DocumentEntity.
            db.execSQL("CREATE TABLE IF NOT EXISTS `documents` ("
                    + "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "`title` TEXT NOT NULL, "
                    + "`body` TEXT NOT NULL, "
                    + "`lang` TEXT NOT NULL, "
                    + "`createdAt` INTEGER NOT NULL)");
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME
                            )
                            .addMigrations(MIGRATION_1_2)
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    Executors.newSingleThreadExecutor().execute(() -> prepopulate(getInstance(context)));
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static void prepopulate(AppDatabase db) {
        long now = System.currentTimeMillis();

        List<WordEntity> words = new ArrayList<>();
        words.add(new WordEntity("পাখি", "ছোট ডানাওয়ালা প্রাণী, আকাশে ওড়ে", "bn", "পা  ·  খি", true, now));
        words.add(new WordEntity("খরগোশ", "লম্বা কান ও তুলতুলে পশমওয়ালা প্রাণী", "bn", "খর  ·  গো  ·  শ", true, now));
        words.add(new WordEntity("বন্ধু", "যার সাথে খেলা ও গল্প করা যায়", "bn", "ব  ·  ন্ধু (ব + ন + ধ)", true, now));
        words.add(new WordEntity("Rabbit", "A small mammal with long ears and soft fur", "en", "Rab  ·  bit", true, now));
        words.add(new WordEntity("Bird", "A creature with feathers and wings", "en", "Bird", false, now));
        words.add(new WordEntity("Friend", "A person who you know well and like", "en", "Friend", true, now));
        db.wordDao().insertAll(words);

        List<SentenceEntity> sentences = new ArrayList<>();
        sentences.add(new SentenceEntity("পাখিটি গাছে বসে গান গায়।", "The bird sits on the tree and sings.", "bn", now));
        sentences.add(new SentenceEntity("The rabbit eats green grass.", "খরগোশটি সবুজ ঘাস খায়।", "en", now));
        db.sentenceDao().insertAll(sentences);

        List<NoteEntity> notes = new ArrayList<>();
        notes.add(new NoteEntity("আজকের পড়া", "আজকে পাখি ও খরগোশের গল্পটা খুব সুন্দর লেগেছে।", now, now));
        db.noteDao().insertAll(notes);
    }
}
