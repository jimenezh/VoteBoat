package com.example.voteboat.adapters;

import android.content.Context;
import android.location.Address;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.voteboat.activities.MainActivity;
import com.example.voteboat.clients.GoogleCivicClient;
import com.example.voteboat.databinding.ItemElectionBinding;
import com.example.voteboat.fragments.ElectionDetailFragment;
import com.example.voteboat.models.Election;
import com.example.voteboat.models.ToDoItem;
import com.example.voteboat.models.User;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.parse.ParseException;
import com.parse.SaveCallback;

import org.json.JSONException;

import java.util.List;

import okhttp3.Headers;

public class ElectionAdapter extends RecyclerView.Adapter<ElectionAdapter.ViewHolder> {

    public static final String TAG = "ElectionFeedAdapter";

    private Context context;
    private List<Election> elections;
    public Address address;

    public ElectionAdapter(Context context, List<Election> elections, Address address) {
        this.context = context;
        this.elections = elections;
        this.address = address;
    }

    // Interface to access listener on
    public interface ElectionAdapterListener {
        void setElectionListener(Object object, Fragment fragment, String type);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemElectionBinding binding = ItemElectionBinding.inflate(LayoutInflater.from(context));
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(elections.get(position));
    }

    @Override
    public int getItemCount() {
        return elections.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ItemElectionBinding binding;

        public ViewHolder(@NonNull ItemElectionBinding itemElectionBinding) {
            super(itemElectionBinding.getRoot());
            this.binding = itemElectionBinding;
            itemElectionBinding.getRoot().setOnClickListener(this);
        }

        public void bind(final Election election) {
            binding.tvTitle.setText(election.getTitle());
            binding.tvDate.setText(election.getElectionDate().toString());
            // TODO: bind proper selector for star
            binding.btnStar.starButton.setLiked(election.isStarred());
            setOnStarListener(election);
        }

        private void setOnStarListener(final Election election) {
            binding.btnStar.starButton.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    // only update if election is originally unstarred
                    if (!election.isStarred()) {
                        User.user.add(User.KEY_STARRED_ELECTIONS, election.getGoogleId());
                        ToDoItem toDoItem = new ToDoItem();
                        toDoItem.put("name", election.getTitle());
                        toDoItem.put("googleId", election.getGoogleId());
                        toDoItem.put("user", User.user);

                        User.user.add("toDo",toDoItem );

                        User.user.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e != null)
                                    Log.e(TAG, "Could not save toDoItem", e);
                                else
                                    Log.i(TAG, "Saved to do item");
                            }
                        });
                        // Make new to do
                        // Add to starred
//                        User.addToStarredElections(election);

                    }
                }
                @Override
                public void unLiked(LikeButton likeButton) {
                    // only update if election is originally starred
                    if (election.isStarred()) {
                        User.removeFromStarredElections(election.getGoogleId());
                    }
                    // then we want to add to the list to remove
                }
            });
        }

        @Override
        public void onClick(View view) {
            // Get correct election, then make query for election details
            Election election = elections.get(getAdapterPosition());
            try {
                launchRaceFragment(election.getOcd_id(), address);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    // API request for more information on the election
    private void launchRaceFragment(String ocd_id, Address address) throws JSONException {
        GoogleCivicClient googleCivicClient = new GoogleCivicClient();
        googleCivicClient
                .voterInformationElections(ocd_id, address.getAddressLine(0), new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "Got races " + json.toString());
                        // now that we have the election information, we can pass it into the new fragment
                        // we do so by calling the listener on mainactivity
                        try {
                            Election e = Election.fromJsonObject(json.jsonObject);
                            displayElectionDetail(e);
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                    }
                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "Could not get races");
                    }
                });
    }

    private void displayElectionDetail(Election e) {
        MainActivity mainActivity = (MainActivity) context;
        mainActivity.setElectionListener(e, new ElectionDetailFragment(), Election.class.getSimpleName());
    }
}
