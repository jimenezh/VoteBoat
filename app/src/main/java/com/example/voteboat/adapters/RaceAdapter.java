package com.example.voteboat.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voteboat.databinding.ItemRaceBinding;
import com.example.voteboat.models.Candidate;
import com.example.voteboat.models.Race;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseRelation;

import java.util.List;

public class RaceAdapter extends RecyclerView.Adapter<RaceAdapter.ViewHolder> {

    public static final String TAG = "ElectionFeedAdapter";

    private Context context;
    private List<Race> races;

    public RaceAdapter(Context context, List<Race> races) {
        this.context = context;
        this.races = races;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRaceBinding binding = ItemRaceBinding.inflate(LayoutInflater.from(context));
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(races.get(position));
    }

    @Override
    public int getItemCount() {
        return races.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ItemRaceBinding binding;

        public ViewHolder(@NonNull ItemRaceBinding itemRaceBinding) {
            super(itemRaceBinding.getRoot());
            this.binding = itemRaceBinding;
        }

        public void bind(Race race) {
            binding.tvTitle.setText(race.getOffice());
            ParseRelation<Candidate> relation = race.getCandidates();
            relation.getQuery().findInBackground(new FindCallback<Candidate>() {
                @Override
                public void done(List<Candidate> objects, ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Could not get race candidates", e);
                        return;
                    }
                    if (!objects.isEmpty())
                        setCandidateInfo(objects);
                    else {
                        binding.rlCandidate1.setVisibility(View.GONE);
                        binding.rlCandidate2.setVisibility(View.GONE);
                    }
                }
            });

        }

        void setCandidateInfo(List<Candidate> candidates) {
            setCandidate1Info(candidates.get(0));
            if (candidates.size() > 1) {
                setCandidate2Info(candidates.get(1));
            } else
                binding.rlCandidate2.setVisibility(View.GONE);

        }

        private void setCandidate1Info(Candidate candidate) {
            binding.rlCandidate1.setVisibility(View.VISIBLE);
            binding.tvCandidate1Name.setText(candidate.getName());
            binding.tvCandidate1Party.setText("(" + candidate.getParty() + ")");
            binding.tvUrl1.setText(candidate.getWebsiteUrl());
            if (candidate.getWebsiteUrl().isEmpty())
                binding.tvUrl1.setVisibility(View.GONE);
            else
                binding.tvUrl1.setVisibility(View.VISIBLE);

        }

        private void setCandidate2Info(Candidate candidate) {
            binding.rlCandidate2.setVisibility(View.VISIBLE);
            binding.tvCandidate2Name.setText(candidate.getName());
            binding.tvCandidate2Party.setText("(" + candidate.getParty() + ")");
            binding.tvUrl2.setText(candidate.getWebsiteUrl());
            if (candidate.getWebsiteUrl().isEmpty())
                binding.tvUrl2.setVisibility(View.GONE);
            else
                binding.tvUrl2.setVisibility(View.VISIBLE);
        }
    }
}
