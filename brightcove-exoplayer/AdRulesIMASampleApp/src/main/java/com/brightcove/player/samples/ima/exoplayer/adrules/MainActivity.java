package com.brightcove.player.samples.ima.exoplayer.adrules;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;

import com.brightcove.ima.GoogleIMAComponent;
import com.brightcove.ima.GoogleIMAEventType;
import com.brightcove.player.edge.Catalog;
import com.brightcove.player.edge.VideoListener;
import com.brightcove.player.event.Event;
import com.brightcove.player.event.EventEmitter;
import com.brightcove.player.event.EventListener;
import com.brightcove.player.event.EventType;
import com.brightcove.player.model.VideoFields;
import com.brightcove.player.mediacontroller.BrightcoveMediaController;
import com.brightcove.player.mediacontroller.BrightcoveSeekBar;
import com.brightcove.player.model.Video;
import com.brightcove.player.view.BaseVideoView;
import com.brightcove.player.util.StringUtil;
import com.brightcove.player.view.BrightcoveExoPlayerVideoView;
import com.brightcove.player.view.BrightcovePlayer;
import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.AdsRequest;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This app illustrates how to use the Google IMA plugin with Ad Rules (aka VMAP)
 * with the Brightcove Player for Android.
 *
 * Note: Video cue points are not used with IMA Ad Rules. The AdCuePoints referenced
 * in the setupAdMarkers method below are Google IMA objects.
 *
 * @author Paul Matthew Reilly (original code)
 * @author Paul Michael Reilly (added explanatory comments)
 */
public class MainActivity extends BrightcovePlayer {

    private final String TAG = this.getClass().getSimpleName();

    private EventEmitter eventEmitter;
    private GoogleIMAComponent googleIMAComponent;
    private String adRulesURL = "http://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=%2F15018773%2Feverything2&ciu_szs=300x250%2C468x60%2C728x90&impl=s&gdfp_req=1&env=vp&output=xml_vast2&unviewed_position_start=1&url=dummy&correlator=[timestamp]&cmsid=133&vid=10XWSh7W4so&ad_rule=1";

    private String QMIAds = "https://pubads.g.doubleclick.net/gampad/live/ads?sz=640x480&iu=%2F7548%2FGTVA_TVA_TVARATTRAPAGE_FR_WEB&gdfp_req=1&env=vp&output=xml_vast3&ad_rule=1&cmsid=2457919&vid=5735137400001&pp=tvar&hl=fr&unviewed_position_start=1&url=tv.accedo.ott.flow.tva.quebecor&description_url=%7Bwindow.location.href%7D&correlator=[timestamp]&cust_params=video_duration%3D5945774%26plID%3D%7Bplayer.id%7D%26ttID%3D5735137400001%26adrequestorigin%3DAppGrid&ms=4pcbjfTck7xkDK0X-vwxDeDquHsggLqYU9RNvfBIT_LgVREmiAHwqkz-_dLfS8DonfwkJM_nRJlgrv2HCCH2s6cRza0KxFBVoHrExztJUB7w-nNNonz8aC_9fe6zI15NXMl1xIh8hgzerZfYJ7uea7Vz7tq7l_xERWBmgBZk9ece4xtsBeCBOud1Fj5Q-9iqi8ETqQzHBuH83HpOx5WycKPLx5QxUGyNjFrdrJbGue8VG6zeVYFoQMPqSk-ZkqL5I7f9Q5vuN5LZrdg0Hs1oKTeSQrrOWlSHM_h28PQyun2sUUXN_v4-R5MZ5Ybkbvl2zI2JdNViy0bl5HDg5_poZQ&sdkv=h.3.45.0%2Fn.android.3.5.2%2Ftv.accedo.ott.flow.tva.quebecor&sdki=405&scor=2813831996324712&js=ima-android.3.5.2&msid=tv.accedo.ott.flow.tva.quebecor&an=tv.accedo.ott.flow.tva.quebecor&mv=80892400.com.android.vending&u_so=l&osd=2&frm=0&sdr=1&is_amp=0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // When extending the BrightcovePlayer, we must assign the BrightcoveExoPlayerVideoView before
        // entering the superclass. This allows for some stock video player lifecycle
        // management.
        setContentView(R.layout.ima_activity_main);
        brightcoveVideoView = (BrightcoveExoPlayerVideoView) findViewById(R.id.brightcove_video_view);

        // *** This method call is optional *** //
        setupAdMarkers(brightcoveVideoView);

        super.onCreate(savedInstanceState);
        eventEmitter = brightcoveVideoView.getEventEmitter();

        // Use a procedural abstraction to setup the Google IMA SDK via the plugin.
        setupGoogleIMA();

        Catalog catalog = new Catalog(eventEmitter, "5567732451001", "BCpkADawqM3aVEOsSraCT-9rth_21TtVmTLmpg-T5JXcNLciCmzMM4LpRy7RqAToUeIj6NMWLk5o2M645WcxlDvLRVH4tM6tOM__hlGECiXVft6h9xD4GzEURwAmRmVj7wHbK0PUlw8nn5mA");
        catalog.findVideoByID("5662702213001", new VideoListener() {
            public void onVideo(Video video) {
                brightcoveVideoView.add(video);

                // Auto play: the GoogleIMAComponent will postpone

                // playback until the Ad Rules are loaded.
                brightcoveVideoView.start();
            }

            public void onError(String error) {
                Log.e(TAG, error);
            }
        });
    }

    /**
     * Setup the Brightcove IMA Plugin.
     */
    private void setupGoogleIMA() {
        // Establish the Google IMA SDK factory instance.
        final ImaSdkFactory sdkFactory = ImaSdkFactory.getInstance();

        // Enable logging up ad start.
        eventEmitter.on(EventType.AD_STARTED, new EventListener() {
            @Override
            public void processEvent(Event event) {
                Log.v(TAG, event.getType());
            }
        });

        // Enable logging any failed attempts to play an ad.
        eventEmitter.on(GoogleIMAEventType.DID_FAIL_TO_PLAY_AD, new EventListener() {
            @Override
            public void processEvent(Event event) {
                Log.v(TAG, event.getType());
            }
        });

        // Enable Logging upon ad completion.
        eventEmitter.on(EventType.AD_COMPLETED, new EventListener() {
            @Override
            public void processEvent(Event event) {
                Log.v(TAG, event.getType());
            }
        });

        // Set up a listener for initializing AdsRequests. The Google
        // IMA plugin emits an ad request event as a result of
        // initializeAdsRequests() being called.
        eventEmitter.on(GoogleIMAEventType.ADS_REQUEST_FOR_VIDEO, new EventListener() {
            @Override
            public void processEvent(Event event) {
                // Create a container object for the ads to be presented.
                AdDisplayContainer container = sdkFactory.createAdDisplayContainer();
                container.setPlayer(googleIMAComponent.getVideoAdPlayer());
                container.setAdContainer(brightcoveVideoView);

                // Build an ads request object and point it to the ad
                // display container created above.
                AdsRequest adsRequest = sdkFactory.createAdsRequest();
                adsRequest.setAdTagUrl(QMIAds);
                adsRequest.setAdDisplayContainer(container);

                ArrayList<AdsRequest> adsRequests = new ArrayList<AdsRequest>(1);
                adsRequests.add(adsRequest);

                // Respond to the event with the new ad requests.
                event.properties.put(GoogleIMAComponent.ADS_REQUESTS, adsRequests);
                eventEmitter.respond(event);
            }
        });

        // Create the Brightcove IMA Plugin and pass in the event
        // emitter so that the plugin can integrate with the SDK.
        googleIMAComponent = new GoogleIMAComponent(brightcoveVideoView, eventEmitter, true);
    }

    /*
      This methods show how to the the Google IMA AdsManager, get the cue points and add the markers
      to the Brightcove Seek Bar.
     */
    private void setupAdMarkers(BaseVideoView videoView) {
        final BrightcoveMediaController mediaController = new BrightcoveMediaController(brightcoveVideoView);

        // Add "Ad Markers" where the Ads Manager says ads will appear.
        mediaController.addListener(GoogleIMAEventType.ADS_MANAGER_LOADED, new EventListener() {
            @Override
            public void processEvent(Event event) {
                AdsManager manager = (AdsManager) event.properties.get("adsManager");
                List<Float> cuepoints = manager.getAdCuePoints();
                for (int i = 0; i < cuepoints.size(); i++) {
                    Float cuepoint = cuepoints.get(i);
                    BrightcoveSeekBar brightcoveSeekBar = mediaController.getBrightcoveSeekBar();
                    // If cuepoint is negative it means it is a POST ROLL.
                    int markerTime = cuepoint < 0 ? brightcoveSeekBar.getMax() : (int) (cuepoint * DateUtils.SECOND_IN_MILLIS);
                        mediaController.getBrightcoveSeekBar().addMarker(markerTime);

                }
            }
        });
        videoView.setMediaController(mediaController);

    }
}
