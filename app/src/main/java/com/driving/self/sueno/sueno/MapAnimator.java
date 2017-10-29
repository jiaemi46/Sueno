package com.driving.self.sueno.sueno;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.SeekBar;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;

import java.util.ArrayList;
import java.util.List;


//public class MapAnimator {
//    private MapAnimator mapAnimator = new MapAnimator();
//    //private static MapAnimator mapAnimator;
//    private AnimatorSet RunAnimatorSet;
//
//    private Polyline backgroundPolyline;
//    private Polyline foregroundPolyline;
//    private PolylineOptions optionsForeground;
//    int counter = 0;
//
//    private MapAnimator(){
//
//    }
//
////    public static MapAnimator getInstance(){
////        if(mapAnimator == null) mapAnimator = new MapAnimator();
////        return mapAnimator;
////    }
//
//    public void stopAnimator(){
//        RunAnimatorSet.removeAllListeners();
//        RunAnimatorSet.cancel();
//    }
//
//    public void pauseAnimator(){
//        RunAnimatorSet.removeAllListeners();
//        RunAnimatorSet.cancel();
//    }
//
//
//    public void animateRoute(GoogleMap googleMap, List<LatLng> drivingRoute) {
//        if (RunAnimatorSet == null){
//            RunAnimatorSet = new AnimatorSet();
//        } else {
//            RunAnimatorSet.removeAllListeners();
//            RunAnimatorSet.end();
//            RunAnimatorSet.cancel();
//
//            RunAnimatorSet = new AnimatorSet();
//        }
//
//        //Reset the polylines
//        if (foregroundPolyline != null) foregroundPolyline.remove();
//        if (backgroundPolyline != null) backgroundPolyline.remove();
//
//        PolylineOptions optionsBackground = new PolylineOptions().add(drivingRoute.get(0)).color(Color.RED).width(5);
//        backgroundPolyline = googleMap.addPolyline(optionsBackground);
//
//        optionsForeground = new PolylineOptions().add(drivingRoute.get(0)).color(Color.RED).width(5);
//        foregroundPolyline = googleMap.addPolyline(optionsForeground);
//
//        final ValueAnimator percentageCompletion = ValueAnimator.ofInt(0, 100);
//        percentageCompletion.setDuration(2000);
//        percentageCompletion.setInterpolator(new DecelerateInterpolator());
//        percentageCompletion.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                List<LatLng> foregroundPoints = backgroundPolyline.getPoints();
//
//                int percentageValue = (int) animation.getAnimatedValue();
//                int pointcount = foregroundPoints.size();
//                int countTobeRemoved = (int) (pointcount * (percentageValue / 100.0f));
//                List<LatLng> subListTobeRemoved = foregroundPoints.subList(0, countTobeRemoved);
//                subListTobeRemoved.clear();
//
//                foregroundPolyline.setPoints(foregroundPoints);
//            }
//        });
//        percentageCompletion.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                foregroundPolyline.setColor(Color.RED);
//                foregroundPolyline.setPoints(backgroundPolyline.getPoints());
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//        });
//
//
//        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), Color.RED, Color.RED);
//        colorAnimation.setInterpolator(new AccelerateInterpolator());
//        colorAnimation.setDuration(1200); // milliseconds
//
//        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animator) {
//                foregroundPolyline.setColor((int) animator.getAnimatedValue());
//            }
//
//        });
//
//        ObjectAnimator foregroundRouteAnimator = ObjectAnimator.ofObject(this, "routeIncreaseForward", new RouteEvaluator(), drivingRoute.toArray());
//        foregroundRouteAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
//        foregroundRouteAnimator.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                backgroundPolyline.setPoints(foregroundPolyline.getPoints());
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//        });
//
//        //------Value to adjust the speed of the polyline
//        foregroundRouteAnimator.setDuration(16000);
////        foregroundRouteAnimator.start();
//
//        RunAnimatorSet.playSequentially(foregroundRouteAnimator,
//                percentageCompletion);
//        RunAnimatorSet.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                //secondLoopRunAnimSet.start();
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//        });
//
//        RunAnimatorSet.start();
//    }
//
//    /**
//     * This will be invoked by the ObjectAnimator multiple times. Mostly every 16ms.
//     **/
//    public void setRouteIncreaseForward(LatLng endLatLng) {
//        List<LatLng> foregroundPoints = foregroundPolyline.getPoints();
//        foregroundPoints.add(endLatLng);
//        foregroundPolyline.setPoints(foregroundPoints);
//
//        counter++;
//        if (counter == 10){
//            //MapsActivity.getInstance().movingMarker(endLatLng);
//            Log.i("New", "endLaLng value: " + endLatLng);
//            counter = 0;
//        }
//    }
//}

