package com.example.voteboat.models;


import android.util.Log;

import androidx.annotation.Nullable;

import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.List;

@ParseClassName("Election")
public class Election extends ParseObject {

    public static final String TAG = "Election";

    String title;
    String googleId;
    String electionDate;
    List<Race> races;
    List<Poll> earlyPolls;
    List<Poll> electionDayPolls;
    List<Poll> absenteeBallotLocations;
    String registrationUrl;
    String electionInfoUrl;
    String absenteeBallotUrl;
    String electionRulesUrl;
    String ocd_id;
    boolean isStarred;


    // Parse Keys
    public static final String KEY_NAME="name";
    public static final String KEY_GOOGLE_ID="google_id";

    public Election() {
    }

    public List<Race> getRaces() {
        return races;
    }

    public static Election basicInformationFromJson(JSONObject json) throws JSONException {
        Election election = new Election();
        election.title = json.getString("name");
        election.electionDate = json.getString("electionDay");
        election.googleId = json.getString("id");
        election.ocd_id = json.getString("ocdDivisionId");

        election.isStarred = isElectionInStarredList(election.googleId);
        Log.i("Election", election.googleId + " is starred:" + election.isStarred);

        return election;
    }

    // Checks if user's starred elections contain election in question
    private static boolean isElectionInStarredList(String googleId) {
        List<String> starredElections = User.getStarredElections();
        Log.i("Election", "Starred elections are: " + starredElections);
        if (starredElections == null)
            return false;
        return starredElections.contains(googleId);
    }

    public static Election fromJsonObject(JSONObject jsonObject) throws JSONException {
        JSONObject electionBasicInfo = jsonObject.getJSONObject("election");
        Election election = Election.basicInformationFromJson(electionBasicInfo);
        JSONObject state = jsonObject
                .getJSONArray("state")
                .getJSONObject(0)
                .getJSONObject("electionAdministrationBody");
        election.registrationUrl = checkifExistsAndAdd("electionRegistrationUrl", state);
        election.electionInfoUrl = checkifExistsAndAdd("electionInfoUrl", state);
        election.absenteeBallotUrl = checkifExistsAndAdd("absenteeVotingInfoUrl", state);
        election.electionRulesUrl = checkifExistsAndAdd("electionRulesUrl", state);

        if (jsonObject.has("pollingLocations"))
            election.electionDayPolls = Poll.fromJsonArray(jsonObject.getJSONArray("pollingLocations"));

        if (jsonObject.has("earlyVoteSites"))
            election.earlyPolls = Poll.fromJsonArray(jsonObject.getJSONArray("earlyVoteSites"));
        if (jsonObject.has("dropOffLocations"))
            election.absenteeBallotLocations = Poll.fromJsonArray(jsonObject.getJSONArray("dropOffLocations"));

        election.races = Race.fromJsonArray(jsonObject.getJSONArray("contests"));

        return election;

    }

    private static String checkifExistsAndAdd(String field, JSONObject jsonObject) throws JSONException {
        if (jsonObject.has(field))
            return jsonObject.getString(field);
        else
            return "";
    }

    public boolean isStarred() {
        return isStarred;
    }

    public String getOcd_id() {
        return ocd_id;
    }

    public String getTitle() {
        return title;
    }

    public String getGoogleId() {
        if(googleId==null)
            return (String) get(KEY_GOOGLE_ID);
        else
            return googleId;
    }

    public String getElectionDate() {
        return electionDate;
    }

    public List<Poll> getEarlyPolls() {
        return earlyPolls;
    }

    public List<Poll> getElectionDayPolls() {
        return electionDayPolls;
    }

    public List<Poll> getAbsenteeBallotLocations() {
        return absenteeBallotLocations;
    }

    public String getRegistrationUrl() {
        return registrationUrl;
    }

    public String getElectionInfoUrl() {
        return electionInfoUrl;
    }

    public String getAbsenteeBallotUrl() {
        return absenteeBallotUrl;
    }

    public String getElectionRulesUrl() {
        return electionRulesUrl;
    }


    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj.getClass() != Election.class)
            return false;
        Election otherElection = (Election) obj;
        return otherElection.getGoogleId().equals(this.getGoogleId());
    }

    public void putInParse() {
        put(KEY_NAME, this.getTitle());
        put(KEY_GOOGLE_ID, this.getGoogleId());

        final String id = this.getGoogleId();
        this.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null)
                    Log.e(TAG, "Could not save "+id);
                else
                    Log.i(TAG, "Saved "+id);
            }
        });
    }
}
