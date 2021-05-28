package ori.project.shootingmaster;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class ScoresAdapter extends ArrayAdapter<Score> {

    Context context;
    List<Score> scores;

    public ScoresAdapter(@NonNull Context context, List<Score> scores) {
        super(context, R.layout.score, scores);
        this.context = context;
        this.scores = scores;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.score, parent, false);

        TextView userName = (TextView) view.findViewById(R.id.userName);
        TextView score = (TextView) view.findViewById(R.id.score);

        Score temp = scores.get(position);

        userName.setText(temp.getUserName());
        score.setText(String.valueOf(temp.getScore()));

        if (position == 0) {
            userName.setTextColor(Color.RED);
            score.setTextColor(Color.RED);
        }

        return view;
    }
}
