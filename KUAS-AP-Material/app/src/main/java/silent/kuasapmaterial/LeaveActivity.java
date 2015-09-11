package silent.kuasapmaterial;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import silent.kuasapmaterial.base.SilentActivity;
import silent.kuasapmaterial.callback.LeaveCallback;
import silent.kuasapmaterial.callback.SemesterCallback;
import silent.kuasapmaterial.libs.Constant;
import silent.kuasapmaterial.libs.Helper;
import silent.kuasapmaterial.libs.ProgressWheel;
import silent.kuasapmaterial.libs.Utils;
import silent.kuasapmaterial.models.LeaveModel;
import silent.kuasapmaterial.models.SemesterModel;

public class LeaveActivity extends SilentActivity implements SwipeRefreshLayout.OnRefreshListener {

	View mPickYmsView;
	ImageView mPickYmsImageView;
	TextView mNoLeaveTextView, mPickYmsTextView, mLeaveNightTextView;
	LinearLayout mNoLeaveLinearLayout;
	ProgressWheel mProgressWheel;
	SwipeRefreshLayout mSwipeRefreshLayout;
	ScrollView mScrollView;
	TableLayout mLeaveTableLayout;

	String mYms;
	List<LeaveModel> mList;
	List<SemesterModel> mSemesterList;
	SemesterModel mSelectedModel;
	private int mPos = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		setContentView(R.layout.activity_leave);
		init(R.string.leave, R.layout.activity_leave, R.id.nav_leave);

		restoreArgs(savedInstanceState);
		findViews();
		setUpViews();
	}

	@Override
	public void finish() {
		super.finish();

		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	private void restoreArgs(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mYms = savedInstanceState.getString("mYms");
			mPos = savedInstanceState.getInt("mPos");

			if (savedInstanceState.containsKey("mList")) {
				mList = new Gson().fromJson(savedInstanceState.getString("mList"),
						new TypeToken<List<LeaveModel>>() {
						}.getType());
			}
			if (savedInstanceState.containsKey("mSelectedModel")) {
				mSelectedModel = new Gson().fromJson(savedInstanceState.getString("mSelectedModel"),
						new TypeToken<SemesterModel>() {
						}.getType());
			}
			if (savedInstanceState.containsKey("mSemesterList")) {
				mSemesterList = new Gson().fromJson(savedInstanceState.getString("mSemesterList"),
						new TypeToken<List<SemesterModel>>() {
						}.getType());
			}
		}

		if (mList == null) {
			mList = new ArrayList<>();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString("mYms", mYms);
		if (mScrollView != null) {
			outState.putInt("mPos", mScrollView.getVerticalScrollbarPosition());
		}
		if (mList != null) {
			outState.putString("mList", new Gson().toJson(mList));
		}
		if (mSelectedModel != null) {
			outState.putString("mSelectedModel", new Gson().toJson(mSelectedModel));
		}
		if (mSemesterList != null) {
			outState.putString("mSemesterList", new Gson().toJson(mSemesterList));
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case Constant.REQUEST_PICK_SEMESTER:
				if (resultCode == RESULT_OK && data != null) {
					if (data.hasExtra("mSelectedModel")) {
						mSelectedModel = new Gson().fromJson(data.getStringExtra("mSelectedModel"),
								new TypeToken<SemesterModel>() {
								}.getType());
						mYms = mSelectedModel.value;
						mPickYmsTextView.setText(mSelectedModel.text);
						getData();
					}
				}
				break;
		}
	}

	private void getSemester() {
		Helper.getSemester(this, new SemesterCallback() {

			@Override
			public void onSuccess(List<SemesterModel> modelList, SemesterModel selectedModel) {
				super.onSuccess(modelList, selectedModel);
				mSemesterList = modelList;
				mSelectedModel = selectedModel;
				mYms = mSelectedModel.value;
				mPickYmsTextView.setText(mSelectedModel.text);
				getData();
			}

			@Override
			public void onFail(String errorMessage) {
				super.onFail(errorMessage);
			}
		});
	}

	private void findViews() {
		mScrollView = (ScrollView) findViewById(R.id.scrollView);
		mPickYmsTextView = (TextView) findViewById(R.id.textView_pickYms);
		mPickYmsView = findViewById(R.id.view_pickYms);
		mPickYmsImageView = (ImageView) findViewById(R.id.imageView_pickYms);
		mProgressWheel = (ProgressWheel) findViewById(R.id.progress_wheel);
		mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
		mNoLeaveLinearLayout = (LinearLayout) findViewById(R.id.linearLayout_no_leave);
		mNoLeaveTextView = (TextView) findViewById(R.id.textView_no_leave);
		mLeaveTableLayout = (TableLayout) findViewById(R.id.tableLayout_leave);
		mLeaveNightTextView = (TextView) findViewById(R.id.textView_night);
	}

	private void setUpViews() {
		setUpPullRefresh();
		mNoLeaveTextView.setText(getString(R.string.leave_no_leave, "\uD83D\uDE0B"));
		mLeaveNightTextView.setText(getString(R.string.leave_night, "\uD83D\uDE06"));

		Bitmap sourceBitmap = Utils.convertDrawableToBitmap(
				getResources().getDrawable(R.drawable.ic_keyboard_arrow_down_white_24dp));
		int color = getResources().getColor(R.color.accent);
		mPickYmsImageView.setImageBitmap(Utils.changeImageColor(sourceBitmap, color));

		mScrollView.scrollTo(0, mPos);
		mPickYmsView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LeaveActivity.this, PickSemesterActivity.class);
				intent.putExtra("mSemesterList", new Gson().toJson(mSemesterList));
				intent.putExtra("mSelectedModel", new Gson().toJson(mSelectedModel));
				startActivityForResult(intent, Constant.REQUEST_PICK_SEMESTER);
			}
		});
		mNoLeaveLinearLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LeaveActivity.this, PickSemesterActivity.class);
				intent.putExtra("mSemesterList", new Gson().toJson(mSemesterList));
				intent.putExtra("mSelectedModel", new Gson().toJson(mSelectedModel));
				startActivityForResult(intent, Constant.REQUEST_PICK_SEMESTER);
			}
		});

		if (mSelectedModel != null && mSemesterList != null) {
			mPickYmsTextView.setText(mSelectedModel.text);
			setUpLeaveTable();
		} else {
			getSemester();
		}
	}

	@Override
	public void onRefresh() {
		mSwipeRefreshLayout.setRefreshing(true);
		if (mYms != null) {
			getData();
		}
	}

	private void setUpPullRefresh() {
		mSwipeRefreshLayout.setOnRefreshListener(this);
		mSwipeRefreshLayout.setColorSchemeColors(Utils.getSwipeRefreshColors(this));
	}

	private void getData() {
		mProgressWheel.setVisibility(View.VISIBLE);
		mPickYmsView.setEnabled(false);
		mScrollView.setVisibility(View.GONE);
		mNoLeaveLinearLayout.setVisibility(View.GONE);
		mSwipeRefreshLayout.setEnabled(false);

		Helper.getLeaveTable(this, mYms.split(",")[0], mYms.split(",")[1], new LeaveCallback() {

			@Override
			public void onSuccess(List<LeaveModel> modelList) {
				super.onSuccess(modelList);
				mList = modelList;
				setUpLeaveTable();
			}

			@Override
			public void onFail(String errorMessage) {
				super.onFail(errorMessage);

				mList.clear();
				mProgressWheel.setVisibility(View.GONE);
				mSwipeRefreshLayout.setEnabled(true);
				mSwipeRefreshLayout.setRefreshing(false);
				mNoLeaveLinearLayout.setVisibility(View.VISIBLE);
				mPickYmsView.setEnabled(true);
			}
		});
	}

	private void setUpLeaveTable() {
		mLeaveTableLayout.setStretchAllColumns(true);
		mLeaveTableLayout.removeAllViews();

		if (mList.size() == 0) {
			mProgressWheel.setVisibility(View.GONE);
			mSwipeRefreshLayout.setEnabled(true);
			mSwipeRefreshLayout.setRefreshing(false);
			mScrollView.setVisibility(View.VISIBLE);
			mNoLeaveLinearLayout.setVisibility(View.VISIBLE);
			mPickYmsView.setEnabled(true);
			return;
		}

		boolean isNight = checkLeaveTableNightType();
		if (!(Utils.isLand(this) || Utils.isWide(this)) && isNight) {
			mLeaveNightTextView.setVisibility(View.VISIBLE);
		} else {
			mLeaveNightTextView.setVisibility(View.GONE);
		}

		TableRow sectionTableRow = new TableRow(this);
		String[] sections = ((Utils.isLand(this) || Utils.isWide(this)) && isNight) ?
				getResources().getStringArray(R.array.leave_night_sections_fixed) :
				getResources().getStringArray(R.array.leave_sections_fixed);
		for (int i = 0; i < sections.length; i++) {
			TextView sectionTextView = new TextView(this);
			sectionTextView.setText(sections[i]);
			sectionTextView.setTextColor(getResources().getColor(R.color.accent));
			sectionTextView.setTextSize(14);
			sectionTextView.setGravity(Gravity.CENTER);

			int drawable = getResources().getIdentifier("table_top_" +
							(i == 0 ? "left" : (i == sections.length - 1 ? "right" : "center")),
					"drawable", getPackageName());
			sectionTextView.setBackgroundResource(drawable);

			sectionTableRow.addView(sectionTextView);
		}
		mLeaveTableLayout.addView(sectionTableRow);

		for (int i = 0; i < mList.size(); i++) {
			TableRow scoreTableRow = new TableRow(this);
			List<String> allSections = new ArrayList<>(
					Arrays.asList(getResources().getStringArray(R.array.leave_night_sections)));
			for (int j = 0; j < sections.length; j++) {
				TextView scoreTextView = new TextView(this);
				int index = -1;
				for (int k = 0; k < mList.get(i).leave_sections.size() && j != 0; k++) {
					if (allSections.get(j - 1).equals(mList.get(i).leave_sections.get(k).section)) {
						index = k;
						break;
					}
				}
				scoreTextView.setTextSize(14);
				scoreTextView.setTextColor(getResources().getColor(R.color.black_text));

				if (j == 0) {
					scoreTextView.setText(mList.get(i).date.split("/", 2)[1]);
				} else if (index > -1) {
					scoreTextView.setText(mList.get(i).leave_sections.get(index).reason);
				}
				scoreTextView.setGravity(Gravity.CENTER);

				int drawable = getResources()
						.getIdentifier("table_" + (i == mList.size() - 1 ? "bottom_" : "normal_") +
										(j == sections.length - 1 ? "right" :
												(j == 0 ? "left" : "center")), "drawable",
								getPackageName());
				scoreTextView.setBackgroundResource(drawable);

				scoreTableRow.addView(scoreTextView,
						new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
								TableRow.LayoutParams.MATCH_PARENT));
			}
			mLeaveTableLayout.addView(scoreTableRow);
		}

		mProgressWheel.setVisibility(View.GONE);
		mSwipeRefreshLayout.setEnabled(true);
		mSwipeRefreshLayout.setRefreshing(false);
		mScrollView.setVisibility(View.VISIBLE);
		mNoLeaveLinearLayout.setVisibility(View.GONE);
		mPickYmsView.setEnabled(true);
	}

	private boolean checkLeaveTableNightType() {
		List<String> sections = new ArrayList<>(
				Arrays.asList(getResources().getStringArray(R.array.leave_night_sections)));
		for (int i = 0; i < mList.size(); i++) {
			for (int j = 0; j < mList.get(i).leave_sections.size(); j++) {
				if (sections.indexOf(mList.get(i).leave_sections.get(j).section) > 9) {
					return true;
				}
			}
		}
		return false;
	}
}