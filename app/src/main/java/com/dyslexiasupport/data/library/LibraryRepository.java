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

        // --- Bangladesh NCTB Curriculum Class 5 Books ---
        list.add(new ReadingText(
                "nctb_bn_51",
                "এই দেশ এই মানুষ (আমার বাংলা বই - ৫ম শ্রেণি)",
                "সার্থক জনম আমার জন্মেছি এই দেশে। আমরা বাংলাদেশে বাস করি। আমাদের এই দেশে কত বৈচিত্র্য! পাহাড়, নদী, সমতলভূমি, কত না রূপ। আমরা সবাই মিলেমিশে বাস করি। এটিই আমাদের প্রিয় মাতৃভূমি বাংলাদেশ।",
                "🇧🇩",
                1,
                "bn",
                "nctb"
        ));

        list.add(new ReadingText(
                "nctb_bn_52",
                "সংকল্প - কাজী নজরুল ইসলাম (আমার বাংলা বই - ৫ম শ্রেণি)",
                "থাকব নাকো বদ্ধ ঘরে, দেখব এবার জগৎটাকে। কেমন করে ঘুরছে মানুষ যুগান্তরের ঘূর্ণিপাকে। দেশ হতে দেশান্তরে ছুটছে তারা কোন নেশায়, কিসের আশায় করছে তারা বরণ মরণ-যন্ত্রণাকে।",
                "📖",
                2,
                "bn",
                "nctb"
        ));

        list.add(new ReadingText(
                "nctb_bn_53",
                "সুন্দরবনের প্রাণী (আমার বাংলা বই - ৫ম শ্রেণি)",
                "বাংলাদেশের দক্ষিণে সুন্দরবন। এটি পৃথিবীর বৃহত্তম ম্যানগ্রোভ বন। এখানে রয়্যাল বেঙ্গল টাইগার, হরিণ, বানর ও নানা প্রজাতির পাখি বাস করে। সুন্দরবনের প্রাকৃতিক সৌন্দর্য অতুলনীয়।",
                "🐅",
                2,
                "bn",
                "nctb"
        ));

        list.add(new ReadingText(
                "nctb_bn_54",
                "হাতি আর শিয়ালের গল্প (আমার বাংলা বই - ৫ম শ্রেণি)",
                "এক বনে বাস করত এক মস্ত বড় হাতি। হাতিটার খুব অহংকার ছিল। সে বনের পশুপাখিদের খুব জ্বালাতন করত। বুদ্ধিমান শিয়াল হাতিকে নদী পার করে নিয়ে গেল কাদা পানিতে। হাতি কাদায় আটকে গেল আর বনের পশুপাখিরা রক্ষা পেল।",
                "🐘",
                3,
                "bn",
                "nctb"
        ));

        list.add(new ReadingText(
                "nctb_en_51",
                "Hello! (English For Today - Class 5)",
                "Sima: Hello! May I introduce myself? I'm Sima. Jessica: Hi! I'm Jessica. Sima: Where are you going, Jessica? Jessica: I'm going to Chittagong. I'm on holiday with my father.",
                "👋",
                1,
                "en",
                "nctb"
        ));

        list.add(new ReadingText(
                "nctb_en_52",
                "See You! (English For Today - Class 5)",
                "Andy: Hello Tamim, how is your grandmother? Tamim: Hi Andy! She's fine, thanks. Andy: Are you going to the book fair? Tamim: Yes, I am going with my cousin. Would you like to come with us?",
                "🙋‍♂️",
                1,
                "en",
                "nctb"
        ));

        list.add(new ReadingText(
                "nctb_en_53",
                "Saikat's Family (English For Today - Class 5)",
                "Saikat Islam lives with his parents in a flat in Bogra. His father Mr. Rasidul Islam is a banker. But in his free time, Mr. Islam writes stories and listens to music. Saikat's mother is Mrs. Monwara Islam. She is a housewife. She loves sewing in her free time.",
                "👨‍👩‍👦",
                2,
                "en",
                "nctb"
        ));

        list.add(new ReadingText(
                "nctb_en_54",
                "Where is the Library? (English For Today - Class 5)",
                "Student: Excuse me, is there a library near here? Teacher: Yes, the library is next to the science building, across from the auditorium. You can read many wonderful books there.",
                "🏫",
                2,
                "en",
                "nctb"
        ));

        // Load custom user scanned & imported texts
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
                        obj.optString("lang", "bn"),
                        obj.optString("category", "custom")
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
                obj.put("category", t.category);
                array.put(obj);
            }
            prefs.edit().putString(KEY_CUSTOM_TEXTS, array.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
