package com.arise.droid.fragments;


import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.arise.core.tools.Mole;
import com.arise.core.tools.StringUtil;
import com.arise.droid.tools.ContextFragment;


public class AppFragment extends ContextFragment {


    private WebView root;

    private Mole log = Mole.getInstance(AppFragment.class);

//    QuixotInterface quixotInterface;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (root == null){
            root = new WebView(getContext());
            root.getSettings().setJavaScriptEnabled(true);
            root.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            root.getSettings().setDomStorageEnabled(true);
            root.getSettings().setSupportMultipleWindows(false);
            root.setWebChromeClient(new WebChromeClient(){
                @Override
                public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                    AlertDialog dialog = new AlertDialog.Builder(view.getContext()).
                            setTitle(message).
                            setMessage("").
                            setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //do nothing
                                }
                            }).create();
                    dialog.show();
                    result.confirm();
                    return true;
                }

                @Override
                public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder (view.getContext ());

                    builder.setTitle (message)
                            .setMessage ("")
                            .setPositiveButton ("OK", null);

                    //No need to bind key events
                    //Shield keys with keycode equal to 84
                    builder.setOnKeyListener (new DialogInterface.OnKeyListener() {
                        public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event) {
                            log.verbose ("onJsAlert", "keyCode ==" + keyCode + "event =" + event);
                            return true;
                        }
                    });
                    //Forbid to respond to the event of pressing the back key
                    builder.setCancelable (false);
                    AlertDialog dialog = builder.create ();
                    dialog.show ();
                    result.confirm ();//Because there is no binding event, you need to force the confirmation, otherwise the page will become black and the content cannot be displayed.
                    return true;
                }

                @Override
                public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {

                    final AlertDialog.Builder builder = new AlertDialog.Builder (view.getContext ());
                    builder.setTitle(message)
                            .setMessage ("");

                    final EditText et = new EditText (view.getContext ());
                    et.setSingleLine ();
                    et.setText (defaultValue);
                    builder.setView(et)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    result.confirm(et.getText().toString());
                                }
                            })
                            .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    result.cancel();
                                }
                            });

                    AlertDialog dialog = builder.create ();
                    dialog.show ();
                    return true;
                }

                @Override
                public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                    String text = "(" + consoleMessage.sourceId() + " line:" + consoleMessage.lineNumber() + ") " + consoleMessage.message();
                    if (ConsoleMessage.MessageLevel.LOG.equals(consoleMessage.messageLevel())){
                        log.log(text);
                    }
                    else if (ConsoleMessage.MessageLevel.ERROR.equals(consoleMessage.messageLevel())){
                        log.error(text);
                    }
                    else if (ConsoleMessage.MessageLevel.DEBUG.equals(consoleMessage.messageLevel())){
                        log.debug(text);
                    }
                    else if (ConsoleMessage.MessageLevel.WARNING.equals(consoleMessage.messageLevel())){
                        log.warn(text);
                    }
                    else {
                        log.info(text);
                    }

                    return super.onConsoleMessage(consoleMessage);
                }
            });

//            quixotInterface = new QuixotInterface(getContext(), root);
//            root.addJavascriptInterface(quixotInterface, "quixot");
            root.loadUrl("http://localhost:8221/app");
        }

        return root;
    }



    public void showSendUrlOptions(String url) {
        if (root != null){
            root.loadUrl("javascript:show_send_options("+ StringUtil.jsonVal(url) +");");
        }
    }

    public void reloadApp() {
        if (root != null){
            root.reload();
        }
    }

    @Override
    public void onResume() {
        if (root != null){
            root.onResume();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if (root != null){
            root.onPause();
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        if (root != null){
            root.stopLoading();
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (root != null){
            root.stopLoading();
            root.destroy();
            root = null;
        }
        super.onDestroy();
    }
}
