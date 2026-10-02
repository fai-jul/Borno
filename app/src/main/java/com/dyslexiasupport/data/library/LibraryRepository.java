package com.dyslexiasupport.data.library;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.dyslexiasupport.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for loading reading texts, including user-scanned and custom saved texts.
 */
public class LibraryRepository {

    private static final String PREF_NAME = "custom_library_prefs";
    private static final String KEY_CUSTOM_TEXTS = "custom_texts";

    private final Context context;
    private final SharedPreferences prefs;

    public LibraryRepository(@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @NonNull
    public List<ReadingText> loadAll() {
        List<ReadingText> list = new ArrayList<>();

        String storyBn = context.getString(R.string.sample_story_bn);
        String storyEn = context.getString(R.string.sample_story_en);

        list.add(new ReadingText(
                "bn_1",
                "পাখি ও খরগোশ",
                storyBn,
                "🐦",
                1,
                "bn"
        ));

        list.add(new ReadingText(
                "en_1",
                "The Bird and the Rabbit",
                storyEn,
                "🐰",
                1,
                "en"
        ));

        list.add(new ReadingText(
                "bn_2",
                "নদীর তীরে একাদশী",
                "এক ছিল নদী। নদীর ধারে বাস করত একটি ছোট্ট শিয়াল। সে প্রতিদিন সকালে নদী পার হয়ে ফল সংগ্রহ করতে যেত। একদিন নদী খুব গভীর ছিল। শিয়াল ভাবল, সে কীভাবে পার হবে? তখন একটি কচ্ছপ আসল এবং বলল, “আমার পিঠে উঠে বসো।” শিয়াল কচ্ছপের পিঠে উঠে নদী পার হলো।",
                "🦊",
                2,
                "bn"
        ));

        list.add(new ReadingText(
                "en_2",
                "The Fox and the Turtle",
                "Once upon a time, a clever little fox lived near a wide river. Every morning, the fox crossed the river to gather fresh berries. One day, the water was very deep. A friendly turtle swam over and said, “Hop on my shell!” The fox rode safely across the water.",
                "🐢",
                2,
                "en"
        ));

        list.add(new ReadingText(
                "bn_3",
                "মহাকাশের তারা",
                "রাতের আকাশে হাজার হাজার তারা মিটিমিটি করে জ্বলে। তারাগুলো আসলে অনেক দূরের জ্বলন্ত গ্যাসের পিণ্ড। বৈজ্ঞানিকরা টেলিস্কোপ দিয়ে তারা পর্যবেক্ষণ করেন। আমরা যখন রাতের দিকে তাকাই, মনে হয় যেন এক জাদুকরী পৃথিবী দেখছি।",
                "⭐",
                3,
                "bn"
        ));

        list.add(new ReadingText(
                "en_3",
                "Stars in the Sky",
                "On a clear night, thousands of stars twinkle high up in the dark sky. Stars are huge glowing balls of gas far away in space. Astronomers use powerful telescopes to study them and learn about the universe.",
                "🌟",
                3,
                "en"
        ));

        // Load custom user scanned texts
        list.addAll(0, loadCustomTexts());

        return list;
    }

    public void addCustomText(@NonNull ReadingText text) {
        List<ReadingText> current = loadCustomTexts();
        current.add(0, text);
        saveCustomTexts(current);
    }

    private List<ReadingText> loadCustomTexts() {
        List<ReadingText> list = new ArrayList<>();
        String jsonStr = prefs.getString(KEY_CUSTOM_TEXTS, null);
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            return list;
        }
        try {
            JSONArray array = new JSONArray(jsonStr);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                list.add(new ReadingText(
                        obj.getString("id"),
                        obj.getString("title"),
                        obj.getString("body"),
                        obj.optString("emoji", "📄"),
                        obj.optInt("level", 1),
                        obj.optString("lang", "bn")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private void saveCustomTexts(List<ReadingText> list) {
        try {
            JSONArray array = new JSONArray();
            for (ReadingText t : list) {
                JSONObject obj = new JSONObject();
                obj.put("id", t.id);
                obj.put("title", t.title);
                obj.put("body", t.body);
                obj.put("emoji", t.emoji);
                obj.put("level", t.level);
                obj.put("lang", t.lang);
                array.put(obj);
            }
            prefs.edit().putString(KEY_CUSTOM_TEXTS, array.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
