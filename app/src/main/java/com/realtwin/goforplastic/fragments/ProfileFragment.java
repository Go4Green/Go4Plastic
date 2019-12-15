package com.realtwin.goforplastic.fragments;

import android.animation.*;
import android.os.*;
import android.preference.PreferenceManager;
import android.transition.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.realtwin.goforplastic.*;
import com.realtwin.goforplastic.R;

import me.grantland.widget.*;

import static com.realtwin.goforplastic.Constants.TOKEN_PREF;

public class ProfileFragment extends Fragment {

    Handler animHandler;

    MapboxActivity parent;
    //AppSettings app; // HARDCODED
	ProgressBar profileProgress;

	public static final String TAG = ProfileFragment.class.getSimpleName();

	public ProfileFragment(){
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 2. Shared Elements Transition
        parent = (MapboxActivity)getActivity();//.getActivity();

        // TODO: get stats
		final float prog = 0.6f;//app.getUserXPPercentage();
		// Get enter transition
		((Transition)getEnterTransition()).addListener(new Transition.TransitionListener() {
			@Override
			public void onTransitionStart(Transition transition) {
				profileProgress.setProgress(0);
				profileProgress.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onTransitionEnd(Transition transition) {
				int progressBarStart = 0;
				int progressBarEnd = (int) (100.0f * prog);
				ObjectAnimator prof_animation = ObjectAnimator.ofInt(profileProgress, "progress", progressBarStart, progressBarEnd); // see this max value coming back here, we animate towards that value
				prof_animation.setDuration(1000); // in milliseconds
				prof_animation.setInterpolator(new DecelerateInterpolator());

				prof_animation.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationStart(Animator animation) {
						super.onAnimationStart(animation);
						profileProgress.setVisibility(View.VISIBLE);
					}
				});

				prof_animation.start();
			}

			@Override
			public void onTransitionCancel(Transition transition) {

			}

			@Override
			public void onTransitionPause(Transition transition) {

			}

			@Override
			public void onTransitionResume(Transition transition) {

			}
		});

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

		// Fix fonts
		TextView title = rootView.findViewById(R.id.text_header);
		//title.setTypeface(AppSettings.getSchalkeFontBold());

		TextView fName = rootView.findViewById(R.id.text_fname);
		//fName.setTypeface(AppSettings.getSchalkeFont());
		fName.setText("Guest");

		//TextView lName = rootView.findViewById(R.id.text_lname);
		//lName.setTypeface(AppSettings.getSchalkeFont());
		//lName.setText("Kyriazakos");

		TextView xp = rootView.findViewById(R.id.text_xp);
		//xp.setTypeface(AppSettings.getSchalkeFont());
		xp.setText("XP: 452");//+ String.valueOf(app.getUserXP()));

		//TextView events = rootView.findViewById(R.id.text_events);
		//events.setTypeface(AppSettings.getSchalkeFont());
		//events.setText("Events: 5");//+app.getUserEvents());

		//TextView duels = rootView.findViewById(R.id.text_duels);
		//duels.setTypeface(AppSettings.getSchalkeFont());
		//duels.setText("Duels: 13");//+app.getUserDuelsTotal());

		TextView level = rootView.findViewById(R.id.text_events);
		//level.setTypeface(AppSettings.getSchalkeFont());
		level.setText("Events: 4");//String.valueOf(app.getUserXP())+"/"+ String.valueOf(AppSettings.getInstance().getUserRank().xp));
		//TextView nl = rootView.findViewById(R.id.txt_next_level);
		//nl.setTypeface(AppSettings.getSchalkeFont());

		//TextView lineups = rootView.findViewById(R.id.text_lineups);
		//lineups.setTypeface(AppSettings.getSchalkeFont());
		//lineups.setText("LineUps: 3");//+app.getUserLineups());

		//TextView distance = rootView.findViewById(R.id.text_distance);
		//distance.setTypeface(AppSettings.getSchalkeFont());
		//distance.setText("Distance: 1512m");//+app.getUserDistance()+"m");

		profileProgress = rootView.findViewById(R.id.profile_progress_bar);
		profileProgress.setProgress(0);

		TextView tokensView = rootView.findViewById(R.id.stats_plastic_count);
		int tokens = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(TOKEN_PREF, 5);
		tokensView.setText(String.valueOf(tokens));

		/*
		Button rank = rootView.findViewById(R.id.img_level);
		int[] ranks = new int[]{R.drawable.rank_1, R.drawable.rank_2,R.drawable.rank_3,R.drawable.rank_4,R.drawable.rank_5,R.drawable.rank_6,R.drawable.rank_7};
		int rankNumber = 4;//AppSettings.getInstance().getUserRankNumber();
		rank.setBackground(getResources().getDrawable(ranks[rankNumber]));
		//rank.setTypeface(AppSettings.getSchalkeFont());
		rank.setText(String.valueOf(rankNumber+1));
		rank.setSingleLine();
		AutofitHelper.create(rank);
		//*/

		//rank.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				parent.showRanksDialog();
//			}
//		});

		// Profile pic
		ImageView pic = rootView.findViewById(R.id.btn_profile_image);

		// Assigns the user profile pic - hardcoded for go 4 plastic
		//app.getMyUser().assignProfilePicture(pic);

		// Back button
		Button back = rootView.findViewById(R.id.btn_exit);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Show alert
				parent.exitProfile();
			}
		});

		View bg = rootView.findViewById(R.id.layout_profile_screen_back);
		// Intercept clicks from going back
		bg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});

		return rootView;
	}


	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

		startPostponedEnterTransition();
		super.onViewCreated(view, savedInstanceState);

		final Bundle args = getArguments();


		//startPostponedEnterTransition();
		//box.setVisibility(View.VISIBLE);

	}

}
