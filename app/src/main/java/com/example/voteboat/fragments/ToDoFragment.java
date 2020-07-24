package com.example.voteboat.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.voteboat.adapters.RepresentativesAdapter;
import com.example.voteboat.adapters.ToDoAdapter;
import com.example.voteboat.clients.GoogleCivicClient;
import com.example.voteboat.databinding.FragmentToDoBinding;
import com.example.voteboat.models.Election;
import com.example.voteboat.models.Representative;
import com.example.voteboat.models.ToDoItem;
import com.example.voteboat.models.User;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.json.JSONException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Headers;

import static android.app.Activity.RESULT_OK;

public class ToDoFragment extends Fragment {
    public static final String TAG = "ToDoFragment";
    FragmentToDoBinding binding;

    List<ToDoItem> toDoItems;
    ToDoAdapter toDoAdapter;

    List<Representative> representatives;
    RepresentativesAdapter representativesAdapter;

    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
    public String photoFileName = "photo.jpg";


    public ToDoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentToDoBinding.inflate(inflater);

        // Setting up To Do tab
        setToDoAdapter();
        getToDoItems();
        // Setting up representatives tab
        setRepresentativesAdapter();
        getRepresentatives();
        return binding.getRoot();
    }

    private void setRepresentativesAdapter() {
        representatives = new ArrayList<>();
        representativesAdapter = new RepresentativesAdapter(getContext(), representatives);
        binding.rvRepresentatives.setAdapter(representativesAdapter);
        LinearLayoutManager representativeLinearLayout = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, true);
        representativeLinearLayout.setStackFromEnd(true);
        binding.rvRepresentatives.setLayoutManager(representativeLinearLayout);
        binding.tvRepresentativesTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleVisibility(binding.rvRepresentatives);
            }
        });
    }

    private void setToDoAdapter() {
        toDoItems = new ArrayList<>();
        toDoAdapter = new ToDoAdapter(getContext(), toDoItems, this);
        binding.rvToDoList.setAdapter(toDoAdapter);
        binding.rvToDoList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.tvToDoTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleVisibility(binding.rvToDoList);
            }
        });
    }

    private void toggleVisibility(RecyclerView rv) {
        if (rv.getVisibility() == View.GONE)
            rv.setVisibility(View.VISIBLE);
        else
            rv.setVisibility(View.GONE);
    }

    // Call to API using GoogleCivicAPI
    private void getRepresentatives() {
        GoogleCivicClient googleCivicClient = new GoogleCivicClient();
        googleCivicClient.getRepresentatives(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess: retreived reps ");
                try {
                    // Transform json into list of Representative objects
                    representatives.addAll(Representative.fromJSONArray(json.jsonObject));
                    // Notify adaptrs
                    representativesAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure: failed to retreive reps: " + response, throwable);
            }
        });
    }

    // Parse query for user's toDOItems
    private void getToDoItems() {
        User.getToDo(new FindCallback<ToDoItem>() {
            @Override
            public void done(List<ToDoItem> objects, ParseException e) {
                if(e != null){
                    Log.e(TAG, "Could not get ToDo's");
                    return;
                }
                for (int i = 0; i < objects.size(); i++) {
                    // Here, we check to see if the election has passed to see
                    // if the todoitem is still valid
                    addItemIfElectionHasNotPassed(i, objects.get(i));
                }
                // We save the user
                User.saveUser("Could not move item to past Elections", "Moved Item to past elections");
                // Notify the adapter that we now have all the valid elections
                toDoAdapter.notifyDataSetChanged();
            }
        });
    }

    private void addItemIfElectionHasNotPassed(int i, final ToDoItem item) {
        final Election election = (Election) item.get("election");
        ParseQuery<Election> query = new ParseQuery<Election>("Election");
        query.whereEqualTo("objectId", election.getObjectId());
        query.include(Election.KEY_ELECTION_DATE);
        query.findInBackground(new FindCallback<Election>() {
            @Override
            public void done(List<Election> objects, ParseException e) {
                if(e != null)
                    Log.e(TAG, "Could not get election", e);
                else{
                    Election result = objects.get(0);
                    if (hasElectionPassed(result))
                        // Delete the item, add it to past election, update election
                        updateElectionAndToDoItem(item, result);
                    else
                        // Otherwise, still valid todoItem
                        toDoItems.add(item);
                }
            }
        });
    }

    private void updateElectionAndToDoItem(ToDoItem item, Election election) {
        // Update election item
        if (!election.getHasPassed())
            election.setElectionHasPassed();
        // Add to user's past elections if user voted
        if (item.hasVoted())
            User.addToPastElections(election);
        // Remove item database
        item.deleteInBackground();
    }

    private boolean hasElectionPassed(Election election) {
        String d = election.getElectionDate();
        try {
            Date electionDate = new SimpleDateFormat("yyyy-MM-dd").parse(d);
            Date today = new Date();
            return electionDate.before(today);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Photo is on disk, we now publish it
                publishToFacebook();
            } else { // Result was a failure
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void publishToFacebook() {
        File file = getPhotoFileUri(photoFileName);
        Bitmap takenImage = BitmapFactory.decodeFile(file.getAbsolutePath());
        SharePhotoContent content = convertBitmapIntoFacebookContent(takenImage);
        openShareDialog(content);
    }

    private void openShareDialog(SharePhotoContent content) {
        ShareDialog shareDialog = new ShareDialog(this);
        if(!shareDialog.canShow(content))
            Log.e(TAG, "Cannot share to Facebook");
        else
            shareDialog.show(this, content);
    }

    private SharePhotoContent convertBitmapIntoFacebookContent(Bitmap takenImage) {
        // SharePhoto is Facebook's data model for photos
        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(takenImage)
                .setCaption("I Voted!")
                .build();
        // Add the photo to the post's content
        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();
        // Triggers intent to the Facebook app
        return content;
    }

    // Get safe storage directory for photos
    // Use `getExternalFilesDir` on Context to access package-specific directories.
    // This way, we don't need to request external read/write runtime permissions.
    public File getPhotoFileUri(String fileName) {
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "failed to create directory");
        }
        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);
        return file;
    }

}