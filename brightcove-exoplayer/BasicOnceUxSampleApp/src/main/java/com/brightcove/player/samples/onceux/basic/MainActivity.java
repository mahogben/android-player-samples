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

    private String accountID = "5567732451001";
    private String videoID  =  "5676756058001";
    private String ad_config_id = "0065f90a-19bf-40a1-acde-082d65166c31";
    private String policyKey = "BCpkADawqM3PIXlPKFoF3J6ygbV02JWu-scu5IJJzITxhaCpMMi_XFonqY8ye1Qv4PEnM63tEkvl7hGt11CzpLY3ZA_cJ9bAyiWfvczcquhcN2-r27NhPwlCC3CQGLqHp7eytiD4Kbko7sN9";


    // Private instance variables

    // The OnceUX plugin VMAP data URL, which tells the plugin when to
    // send tracking beacons, when to hide the player controls and
    // what the click through URL for the ads shoud be.  The VMAP data
    // will also identify what the companion ad should be and what
    // it's click through URL is.
    private String onceUxAdDataUrl = "http://once.unicornmedia.com/now/ads/vmap/od/auto/c501c3ee-7f1c-4020-aa6d-0b1ef0bbd4a9/202ef8bb-0d9d-4f6f-bd18-f45aa3010fe6/8a146f45-9fac-462e-a111-de60ec96198b/content.once";

    private OnceUxComponent plugin;
    public OnceUxComponent getOnceUxPlugin() {
        return plugin;
    }

    private EventEmitter eventEmitter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // When extending the BrightcovePlayer, we must assign brightcoveVideoView before
        // entering the superclass.  This allows for some stock video player lifecycle
        // management.
        setContentView(R.layout.onceux_activity_main);
        brightcoveVideoView = (BrightcoveExoPlayerVideoView) findViewById(R.id.brightcove_video_view);
        brightcoveVideoView.getAnalytics().setAccount("5420904993001");
        super.onCreate(savedInstanceState);

        eventEmitter = brightcoveVideoView.getEventEmitter();

        // Setup the event handlers for the OnceUX plugin, set the companion ad container,
        // register the VMAP data URL inside the plugin and start the video.  The plugin will
        // detect that the video has been started and pause it until the ad data is ready or an
        // error condition is detected.  On either event the plugin will continue playing the
        // video.
        registerEventHandlers();


        plugin = new OnceUxComponent(this, brightcoveVideoView);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put("ad_config_id", ad_config_id);

        plugin = new OnceUxComponent(this, brightcoveVideoView);

        Catalog catalog = new Catalog(eventEmitter, accountID, policyKey);

        catalog.findVideoByID(videoID, null, parameters, new VideoListener() {
            @Override
            public void onVideo(Video video) {
                try {
                    plugin.processVideo(video);
                } catch(NoSourceFoundException e) {

                }
            }
        });
   }

    // Private instance methods

    /**
     * Procedural abstraction used to setup event handlers for the OnceUX plugin.
     */
    private void registerEventHandlers() {
        // Handle the case where the ad data URL has not been supplied to the plugin.
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


/*
SOME USEFUL METHODS

    Adding params to a VMAP URL:
    public static void addVMAPQueryParams(Video video, String vmapQuery){
        Map<DeliveryType,SourceCollection> map = video.getSourceCollections();
        for (SourceCollection sourceCollection : map.values()) {
            for (Source s : sourceCollection.getSources()) {
                String full = (s.getProperties().get(Source.Fields.VMAP)).toString().concat(vmapQuery);
                s.getProperties().put(Source.Fields.VMAP, full);
            }
        }
    }
 */