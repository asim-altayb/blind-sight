package com.example.user.blindsight.navigation;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;

import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Navigation extends ActivityResource {

    public Navigation(Activity activity) {
        super(activity);
        this.way = new LinkedList<Position>();
        this.passed = new LinkedList<Position>();
        this.vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        this.attractions = new LinkedList<Position>();
    }

    public Navigation(Activity activity, Queue<Position> positions) {
        this(activity);
        this.way = positions;
    }

    public Navigation(UpdateableActivity activity, boolean stam) {
        this(activity);
        this.updateable = activity;
        if (!stam) {
            this.way.add(new Position(34.81849653, 32.11324305));
            this.way.add(new Position(34.81800239, 32.11289161));
//            this.way.add(new Position(32.11309088, 34.81792111));
//            this.way.add(new Position(32.11313755, 34.81774711));
//            this.way.add(new Position(32.11340843, 34.81778721));
        } else {
            this.way.add(new Position(30.81829653, 40.11290305));
            this.way.add(new Position(45.81792111, 2.11309088));
            this.way.add(new Position(0.81774711, 90.11313755));
            this.way.add(new Position(60.81778721, 12.11340843));
        }
    }

    public void walk(Position position, float angle) {
        Position next = way.peek();
        if (next == null) {
            updateable.clear();
            stopVibrating();
            return;
        }
        float distance = position.getDistance(next);
        float recommended_angle = position.getAngle(next);


        updateable.clear();
        updateable.print("distance: " + distance + "m \n" + "needed angle: " + recommended_angle + "\n\n");
        updateable.print("next location:\n" + next.latitude + " , " + next.longitude + "\n\n");
        updateable.print("current location:\n" + position.latitude + " , " + position.longitude + "\n\n" +
                "angle: " + angle + " -> ");


        //distance
        if (position.isNear(next)) {
            nextStep();
            updateable.print("next position");
            walk(position, angle);
        } else {
            //angle
            if (Math.abs(recommended_angle - angle) > Position.MIN_ANGLE) {
                vibrate();
            } else {
                stopVibrating();
            }
        }
    }

    public void nextStep() {
        Position position = way.poll();
        if (position != null) {
            passed.add(position);
        }
    }

    public void setAttractions(DirectionsRoute directionsRoute) {
        DirectionsStep[] directionsSteps = directionsRoute.legs[0].steps;
        for (DirectionsStep directionsStep : directionsSteps) {
            attractions.add(new Position(directionsStep.startLocation.lng, directionsStep.startLocation.lat));
        }
    }

    public void setWay(DirectionsRoute directionsRoute) {
        if (directionsRoute != null) {
            way = new LinkedList<Position>();
            DirectionsStep[] directionsSteps = directionsRoute.legs[0].steps;
            for (DirectionsStep directionsStep : directionsSteps) {
                way.add(new Position(directionsStep.startLocation.lng, directionsStep.startLocation.lat));
            }
        } else {

        }
    }

    public void onDestroy() {
        stopVibrating();
    }

    public void vibrate() {
        updateable.print("vibrate");
        if (vibratorFinish < System.currentTimeMillis()) {
            int runningTime = 50000;
            vibratorFinish = System.currentTimeMillis() + runningTime;
            vibrator.vibrate(runningTime);
        }
    }

    public void stopVibrating() {
        updateable.print("quite");
        vibratorFinish = System.currentTimeMillis();
        vibrator.cancel();
    }

    public boolean isNearAttraction(Position currentPosition) {
        for (Position attraction : attractions) {
            if (currentPosition.isNear(attraction)) {
                return true;
            }
        }
        return false;
    }

    private Vibrator vibrator;
    private Queue<Position> way;
    private Queue<Position> passed;
    private List<Position> attractions;
    private long vibratorFinish = 0;
    private Updateable updateable = new EmptyUdateable();

    class EmptyUdateable implements Updateable {
        @Override
        public void update() { }

        @Override
        public void print(String str) { }

        @Override
        public void clear() { }
    };

}
