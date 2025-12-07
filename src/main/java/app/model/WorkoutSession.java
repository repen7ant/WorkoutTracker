package app.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

@DatabaseTable(tableName = "workout_sessions")
public class WorkoutSession {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(dataType = DataType.DATE_STRING)
    private Date date;

    @DatabaseField
    private double bodyweight;

    public WorkoutSession() {}

    public WorkoutSession(Date date, double bodyweight) {
        this.date = date;
        this.bodyweight = bodyweight;
    }

    public int getId() { return id; }
    public Date getDate() { return date; }
    public double getBodyweight() { return bodyweight; }
}
