// Ścieżka: app/java/com/example/a404/ui/adapters/CourseAdapter.java
package com.example.a404.ui.adapters;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale; // Dodane dla formatowania tekstu

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private List<Course> courses = new ArrayList<>(); // Inicjalizuj od razu
    private final OnCourseClickListener courseClickListener; // Interfejs do obsługi kliknięć

    // Interfejs do obsługi kliknięć
    public interface OnCourseClickListener {
        void onCourseClicked(Course course); // Kliknięcie na cały element
        void onWordsIconClicked(Course course); // Kliknięcie na ikonę słówek
    }

    public CourseAdapter(List<Course> initialCourses, OnCourseClickListener listener) {
        if (initialCourses != null) {
            this.courses.addAll(initialCourses);
        }
        this.courseClickListener = listener;
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
        holder.bind(course, courseClickListener);
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    // Metoda do aktualizacji danych w adapterze
    public void updateCourses(List<Course> newCourses) {
        this.courses.clear();
        if (newCourses != null) {
            this.courses.addAll(newCourses);
        }
        notifyDataSetChanged(); // Dla prostoty. W przyszłości rozważ DiffUtil.
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView wordsInCourseIcon; // Zmieniono nazwę dla jasności
        TextView textCourseName;
        TextView textCourseDescription;
        TextView textWordCount;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            wordsInCourseIcon = itemView.findViewById(R.id.words_icon); // Upewnij się, że ID jest poprawne
            textCourseName = itemView.findViewById(R.id.text_course_name);
            textCourseDescription = itemView.findViewById(R.id.text_course_description);
            textWordCount = itemView.findViewById(R.id.text_word_count);
        }

        public void bind(final Course course, final OnCourseClickListener listener) {
            textCourseName.setText(course.getName());
            textCourseDescription.setText(course.getDescription());

            // Bezpieczne sprawdzanie listy słówek przed wywołaniem size()
            int wordCount = (course.getWords() != null) ? course.getWords().size() : 0;
            textWordCount.setText(String.format(Locale.getDefault(), "%d słów", wordCount)); // Użyj string resource w przyszłości

            // Ustawienie listenerów kliknięć przekazując obiekt Course
            wordsInCourseIcon.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onWordsIconClicked(course);
                }
            });

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCourseClicked(course);
                }
            });

            // Możesz też ustawić listener na itemView, jeśli chcesz, aby cały element był klikalny
            // itemView.setOnClickListener(v -> {
            //    if (listener != null) {
            //        listener.onCourseClicked(course);
            //    }
            // });
        }
    }
}