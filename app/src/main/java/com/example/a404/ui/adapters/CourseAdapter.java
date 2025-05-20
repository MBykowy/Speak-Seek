package com.example.a404.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a404.R;
import com.example.a404.data.model.Course;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {
    private List<Course> courses;
    private final OnCourseClickListener listener;

    public CourseAdapter(List<Course> courses, OnCourseClickListener listener) {
        this.courses = courses;
        this.listener = listener;
    }

    public interface OnCourseClickListener {
        void onCourseClicked(Course course);
        void onWordsIconClicked(Course course);
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.textCourseName.setText(course.getName());
        holder.textCourseDescription.setText(course.getDescription());

        // Wyświetl liczbę słów w kursie
        int wordCount = course.getWords() != null ? course.getWords().size() : 0;
        String wordCountText = wordCount + " " + getWordForm(wordCount);
        holder.textWordCount.setText(wordCountText);

        // Ustaw obsługę zdarzeń
        holder.buttonStartCourse.setOnClickListener(v -> listener.onCourseClicked(course));
        holder.imageWords.setOnClickListener(v -> listener.onWordsIconClicked(course));
        holder.itemView.setOnClickListener(v -> listener.onCourseClicked(course));
    }

    private String getWordForm(int count) {
        if (count == 1) return "słowo";
        if (count % 10 >= 2 && count % 10 <= 4 && (count % 100 < 10 || count % 100 >= 20)) return "słowa";
        return "słów";
    }

    @Override
    public int getItemCount() {
        return courses != null ? courses.size() : 0;
    }

    public void updateCourses(List<Course> newCourses) {
        this.courses = newCourses;
        notifyDataSetChanged();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView textCourseName;
        TextView textCourseDescription;
        TextView textWordCount;
        MaterialButton buttonStartCourse;
        ImageView imageWords;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            textCourseName = itemView.findViewById(R.id.text_course_name);
            textCourseDescription = itemView.findViewById(R.id.text_course_description);
            textWordCount = itemView.findViewById(R.id.text_word_count);
            buttonStartCourse = itemView.findViewById(R.id.button_start_course);
            imageWords = itemView.findViewById(R.id.image_words);
        }
    }
}