package app.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "workout_exercises")
public final class WorkoutExercise {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private String name;

    @DatabaseField
    private String setsString;

    @DatabaseField(foreign = true, foreignAutoRefresh = true,
            columnName = "session_id")
    private WorkoutSession session;

    public WorkoutExercise() {}

    public WorkoutExercise(final String name, final String setsString,
                           final WorkoutSession session) {
        this.name = name;
        this.setsString = setsString;
        this.session = session;
    }

    public String getName() {
        return name;
    }

    public String getSetsString() {
        return setsString;
    }

    public WorkoutSession getSession() {
        return session;
    }

    public int getId() {
        return id;
    }
}
