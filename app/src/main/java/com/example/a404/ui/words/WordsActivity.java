package com.example.a404.ui.words;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a404.data.dao.CourseDao;
import com.example.a404.data.model.Course;
import com.example.a404.data.model.WordDbHelper;
import com.example.a404.ui.adapters.WordAdapter;
import com.example.a404.R;


public class WordsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private WordAdapter adapter;
    private WordDbHelper dbHelper;
    private CourseDao courseDao;
    private TextView textCourseName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words);

        dbHelper = new WordDbHelper(this);
        courseDao = new CourseDao(dbHelper);

        textCourseName = findViewById(R.id.text_course_name);

        recyclerView = findViewById(R.id.recycler_view_words);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        long courseId = getIntent().getLongExtra("COURSE_ID", -1);
        if (courseId != -1) {
            loadWords(courseId);
        }
    }

    private void loadWords(long courseId) {
        Course course = courseDao.getCourseById(courseId);
        if (course != null) {
            textCourseName.setText(course.getName());
            adapter = new WordAdapter(course.getWords());
            recyclerView.setAdapter(adapter);
        }
    }
}
