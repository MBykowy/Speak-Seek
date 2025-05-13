package com.example.a404.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a404.R;
import com.example.a404.data.model.Course;
import com.example.a404.ui.home.WordGameActivity;
import com.example.a404.ui.words.WordsActivity;

import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {
    private List<Course> courses;
    private Context context;

    public CourseAdapter(Context context, List<Course> courses) {
        this.context = context;
        this.courses = courses;
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
        holder.textWordCount.setText(course.getWords().size() + " words");

        holder.wordsInCourse.setOnClickListener(v -> {
            Intent intent = new Intent(context, WordsActivity.class);
            intent.putExtra("COURSE_ID", course.getId());
            context.startActivity(intent);
        });

        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, WordGameActivity.class);
            intent.putExtra("COURSE_ID", course.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView wordsInCourse;
        TextView textCourseName;
        TextView textCourseDescription;
        TextView textWordCount;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            wordsInCourse = itemView.findViewById(R.id.words_icon);
            textCourseName = itemView.findViewById(R.id.text_course_name);
            textCourseDescription = itemView.findViewById(R.id.text_course_description);
            textWordCount = itemView.findViewById(R.id.text_word_count);
        }
    }
}