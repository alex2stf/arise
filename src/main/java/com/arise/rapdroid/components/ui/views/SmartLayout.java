package com.arise.rapdroid.components.ui.views;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.annotation.Nullable;
import com.arise.rapdroid.components.ui.Layouts;
import com.arise.rapdroid.components.ui.adapters.ListViewAdapter;

public class SmartLayout extends LinearLayout {
    protected final Context ctx;

    public SmartLayout(Context context) {
        super(context);
        ctx = context;
        setOrientation(VERTICAL);
    }

    public SmartLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        ctx = context;
    }

    public SmartLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        ctx = context;
    }


    public TextView addTextView(String text, int textAlignment){
        TextView textView = new TextView(ctx);
        textView.setText(text);if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            textView.setTextAlignment(textAlignment);
        }
        textView.setGravity(Gravity.CENTER_VERTICAL);
        addView(textView, Layouts.matchParentWrapContent());

        return textView;
    }

    public EditText addEditTextActionDone(String text, EditTextActionDoneListener actionDoneListener) {
        EditText editText = new EditText(ctx);
        editText.setText(text);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        SmartLayout self = this;
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    actionDoneListener.done(editText, self);
                    return true;
                }
                return false;
            }
        });
        addView(editText, Layouts.matchParentWrapContent());
        return editText;
    }



    public void addSpace() {
        Space space = new Space(ctx);
        space.setMinimumHeight(2);
        addView(space, Layouts.matchParentWrapContent());
    }



    public Button addButton(String txt, ButtonClickListener buttonClickListener) {
        Button button = new Button(ctx);
        button.setText(txt);
        SmartLayout self = this;
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                 buttonClickListener.onClick((Button) view, self);
            }
        });
        if (VERTICAL == getOrientation()) {
            addView(button, Layouts.matchParentWrapContent());
        } else {
            addView(button, Layouts.wrapContentMatchParent());
        }
        return button;
    }



    public ListView addListView(ListViewAdapter adapterContainer) {
        ListView listView = new ListView(ctx);
        listView.setAdapter(adapterContainer);
        addView(listView, Layouts.matchParentWrapContent());
        return listView;
    }

    public Spinner addSpinner(String[] items, String text) {
        Spinner spinner = new Spinner(ctx);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_dropdown_item, items);
        spinner.setAdapter(adapter);

        if (text != null){
            TextView textView = new TextView(ctx);
            textView.setText(text);
            textView.setPadding(0, 0, 0, 0);
            spinner.setPadding(0, 0, 0, 0);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout container = new LinearLayout(getContext());
            container.setOrientation(HORIZONTAL);
            container.addView(textView, Layouts.matchParentMatchParent05f());
            container.addView(spinner, Layouts.matchParentMatchParent05f());
            addView(container, Layouts.matchParentWrapContent());
        } else {
            addView(spinner, Layouts.matchParentWrapContent());
        }
        return spinner;
    }

    public LinearLayout addLinearLayout() {
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        addView(linearLayout, Layouts.matchParentWrapContent());
        return linearLayout;
    }

    public CheckBox addCheckBox(String text) {
        CheckBox checkBox = new CheckBox(getContext());
        checkBox.setText(text);

        addView(checkBox, Layouts.matchParentWrapContent());
        return checkBox;
    }

    public interface EditTextActionDoneListener {
        void done(EditText editText, SmartLayout layout);
    }

    public interface ButtonClickListener {
        void onClick(Button view, SmartLayout self);
    }

    public void runOnUiThread(Runnable r){
        if (getActivity() != null){
            getActivity().runOnUiThread(r);
        }
    }


    public Activity getActivity(){
        if (ctx instanceof  Activity){
            return (Activity) ctx;
        }
        return null;
    }
}
