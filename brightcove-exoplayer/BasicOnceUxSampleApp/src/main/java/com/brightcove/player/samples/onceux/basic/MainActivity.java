package com.brightcove.player.samples.onceux.basic;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.brightcove.player.controller.NoSourceFoundException;
import com.brightcove.player.edge.Catalog;
import com.brightcove.player.edge.VideoListener;
import com.brightcove.player.event.EventEmitter;
import com.brightcove.player.event.EventListener;
import com.brightcove.player.event.EventType;
import com.brightcove.player.event.Event;

import com.brightcove.player.model.Video;
import com.brightcove.player.view.BrightcovePlayer;
import com.brightcove.player.view.BrightcoveExoPlayerVideoView;

import com.brightcove.onceux.OnceUxComponent;
import com.brightcove.onceux.event.OnceUxEventType;

import java.util.HashMap;
import java.util.Map;

/**
 * This app illustrates how to use the Once UX plugin to ensure that:
 *
 * - player controls are hidden during ad playback,
 *
 * - tracking beacons are fired from the client side,
 *
 * - videos are clickable during ad playback and visit the appropriate website,
 *
 * - the companion banner is shown on page switched appropriately as new ads are played
 *
 * @author Paul Michael Reilly
 */
public class MainActivity extends BrightcovePlayer {

    // Private class constants

    private final String TAG = this.getClass().getSimpleName();

    // Private instance variables

    // The OnceUX plugin VMAP data URL, which tells the plugin when to
    // send tracking beacons, when to hide the player controls and
    // what the click through URL for the ads shoud be.  The VMAP data
    // will also identify what the companion ad should be and what
    // it's click through URL is.
    //private String onceUxAdDataUrl = "http://once.unicornmedia.com/now/ads/vmap/od/auto/c501c3ee-7f1c-4020-aa6d-0b1ef0bbd4a9/202ef8bb-0d9d-4f6f-bd18-f45aa3010fe6/8a146f45-9fac-462e-a111-de60ec96198b/content.once";

    private String accountID = "";
    private String videoID  =  "";
    private String ad_config_id = "";
    private String policyKey = "";

    private OnceUxComponent plugin;
    public OnceUxComponent getOnceUxPlugin() {
        return plugin;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // When extending the BrightcovePlayer, we must assign brightcoveVideoView before
        // entering the superclass.  This allows for some stock video player lifecycle
        // management.
        setContentView(R.layout.onceux_activity_main);
        brightcoveVideoView = (BrightcoveExoPlayerVideoView) findViewById(R.id.brightcove_video_view);
        brightcoveVideoView.getAnalytics().setAccount(accountID);
        super.onCreate(savedInstanceState);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("ad_config_id", ad_config_id);

        Catalog catalog = new Catalog(brightcoveVideoView.getEventEmitter(), accountID, policyKey);

        registerEventHandlers();
        plugin = new OnceUxComponent(this, brightcoveVideoView);

        catalog.findVideoByID(videoID, null, parameters, new VideoListener() {
            @Override
            public void onVideo(Video video) {
                try {
                    plugin.processVideo(video);
                } catch (NoSourceFoundException e) {
                    brightcoveVideoView.add(video);
                }
            }
        });
        //For use with ONCE
        //plugin.processVideo(onceUxAdDataUrl);
    }

    // Private instance methods

    /**
     * Procedural abstraction used to setup event handlers for the OnceUX plugin.
     */
    private void registerEventHandlers() {
        // Handle the case where the ad data URL has not been supplied to the plugin.
        EventEmitter eventEmitter = brightcoveVideoView.getEventEmitter();
        eventEmitter.on(OnceUxEventType.NO_AD_DATA_URL, new EventListener() {
            @Override
            public void processEvent(Event event) {
                // Log the event and display a warning message (later)
                Log.e(TAG, event.getType());
                // TODO: throw up a stock Android warning widget.
            }
        });
    }
}
