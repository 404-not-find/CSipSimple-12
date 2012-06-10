/**
 * Copyright (C) 2010-2012 Regis Montoya (aka r3gis - www.r3gis.fr)
 * This file is part of CSipSimple.
 *
 *  CSipSimple is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  If you own a pjsip commercial license you can also redistribute it
 *  and/or modify it under the terms of the GNU Lesser General Public License
 *  as an android library.
 *
 *  CSipSimple is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CSipSimple.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * This file contains relicensed code from Apache copyright of 
 * Copyright (C) 2008 The Android Open Source Project
 */

package com.csipsimple.ui.incall;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import com.csipsimple.R;
import com.csipsimple.api.MediaState;
import com.csipsimple.api.SipCallSession;
import com.csipsimple.api.SipConfigManager;
import com.csipsimple.utils.Log;
import com.csipsimple.utils.Theme;
import com.csipsimple.utils.accessibility.AccessibilityWrapper;
import com.csipsimple.widgets.AlternateUnlocker;
import com.csipsimple.widgets.IOnLeftRightChoice;
import com.csipsimple.widgets.SlidingTab;

public class InCallControls extends FrameLayout implements IOnLeftRightChoice, OnClickListener {

	private static final String THIS_FILE = "InCallControls";
	OnTriggerListener onTriggerListener;
	private SlidingTab slidingTabWidget;
	private Button clearCallButton, dialButton, addCallButton;
	private ToggleButton bluetoothButton, speakerButton, muteButton;
	private View inCallButtons;
	private boolean isDialpadOn = false;
	private AlternateUnlocker alternateLockerWidget;
	
	
	private static final int MODE_LOCKER = 0;
	private static final int MODE_CONTROL = 1;
	private static final int MODE_NO_ACTION = 2;
	private int controlMode;
	private MediaState lastMediaState;
	private SipCallSession currentCall;
	private boolean supportMultipleCalls = false;

	/**
	 * Interface definition for a callback to be invoked when a tab is triggered
	 * by moving it beyond a target zone.
	 */
	public interface OnTriggerListener {
		/**
		 * When user clics on clear call
		 */
		int CLEAR_CALL = 1;
		/**
		 * When user clics on take call
		 */
		int TAKE_CALL = CLEAR_CALL + 1;
		/**
		 * When user clics on take call
		 */
		int DECLINE_CALL = TAKE_CALL + 1;
		/**
		 * When user clics on dialpad
		 */
		int DIALPAD_ON = DECLINE_CALL + 1;
		/**
		 * When user clics on dialpad
		 */
		int DIALPAD_OFF = DIALPAD_ON + 1;
		/**
		 * When mute is set on
		 */
		int MUTE_ON = DIALPAD_OFF + 1;
		/**
		 * When mute is set off
		 */
		int MUTE_OFF = MUTE_ON + 1;
		/**
		 * When bluetooth is set on
		 */
		int BLUETOOTH_ON = MUTE_OFF + 1;
		/**
		 * When bluetooth is set off
		 */
		int BLUETOOTH_OFF = BLUETOOTH_ON + 1;
		/**
		 * When speaker is set on
		 */
		int SPEAKER_ON = BLUETOOTH_OFF + 1;
		/**
		 * When speaker is set off
		 */
		int SPEAKER_OFF = SPEAKER_ON + 1;
		/**
		 * When detailed display is asked
		 */
		int DETAILED_DISPLAY = SPEAKER_OFF + 1;
		/**
		 * When hold / reinvite is asked
		 */
		int TOGGLE_HOLD = DETAILED_DISPLAY + 1;
		/**
		 * When media settings is asked
		 */
		int MEDIA_SETTINGS = TOGGLE_HOLD + 1;
		/**
		 * When add call is asked
		 */
		int ADD_CALL = MEDIA_SETTINGS + 1;
		/**
		 * When xfer to a number is asked
		 */
		int XFER_CALL = ADD_CALL + 1;
		
		/**
		 * When start recording is asked
		 */
		int START_RECORDING = XFER_CALL + 1;
		/**
		 * When stop recording is asked
		 */
		int STOP_RECORDING = START_RECORDING + 1;

		/**
		 * Called when the user make an action
		 * 
		 * @param whichAction
		 *            what action has been done
		 */
		void onTrigger(int whichAction, SipCallSession call);
	}

	public InCallControls(Context context) {
        this(context, null, 0);
    }
	
	public InCallControls(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
    public InCallControls(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.in_call_controls, this, true);
        
        boolean useSlider = false;
        
        if(!isInEditMode()) {
            supportMultipleCalls = SipConfigManager.getPreferenceBooleanValue(context, SipConfigManager.SUPPORT_MULTIPLE_CALLS);
            AccessibilityWrapper accessibilityManager = AccessibilityWrapper.getInstance();
            accessibilityManager.init(getContext());
            if(!accessibilityManager.isEnabled()) {
                useSlider = !SipConfigManager.getPreferenceBooleanValue(context, SipConfigManager.USE_ALTERNATE_UNLOCKER, useSlider);
            }
        }
        
        RelativeLayout container = (RelativeLayout) findViewById(R.id.in_call_controls_container);
        IOnLeftRightProvider leftRightChooser;
        if(useSlider) {
            slidingTabWidget = new SlidingTab(context);
            container.addView(slidingTabWidget, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            leftRightChooser = slidingTabWidget;
            slidingTabWidget.setVisibility(GONE);
        }else {
            alternateLockerWidget = new AlternateUnlocker(context);
            container.addView(alternateLockerWidget, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            leftRightChooser = alternateLockerWidget;
            alternateLockerWidget.setVisibility(GONE);
        }
        leftRightChooser.setOnLeftRightListener(this);
        
        inCallButtons = findViewById(R.id.bottomButtonsContainer);
        
        // Hide all components by default
        inCallButtons.setVisibility(GONE);
    
    }
    
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();


		addCallButton = (Button) findViewById(R.id.addCallButton);
		clearCallButton = (Button) findViewById(R.id.clearCallButton);
		dialButton = (Button) findViewById(R.id.dialpadButton);
		bluetoothButton = (ToggleButton) findViewById(R.id.bluetoothButton);
		speakerButton = (ToggleButton) findViewById(R.id.speakerButton);
		muteButton = (ToggleButton) findViewById(R.id.muteButton);
		
		
	//	settingsButton = (ImageButton) findViewById(R.id.settingsButton);
		
		// Finalize object style
		if(slidingTabWidget != null) {
    		slidingTabWidget.setLeftHintText(R.string.take_call);
    		slidingTabWidget.setRightHintText(R.string.decline_call);
		}
		setEnabledMediaButtons(false);
		controlMode = MODE_LOCKER;
		setCallLockerVisibility(VISIBLE);
		inCallButtons.setVisibility(GONE);
		

		// Attach objects
		clearCallButton.setOnClickListener(this);
		dialButton.setOnClickListener(this);
		bluetoothButton.setOnClickListener(this);
		speakerButton.setOnClickListener(this);
		muteButton.setOnClickListener(this);
		addCallButton.setOnClickListener(this);
		addCallButton.setEnabled( supportMultipleCalls );
	}
	
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		
		final int parentWidth = r - l;
		final int parentHeight = b - t;
		if(slidingTabWidget != null) {
    		final int top = parentHeight * 3/4 - slidingTabWidget.getHeight()/2;
    		final int bottom = parentHeight * 3/4 + slidingTabWidget.getHeight() / 2;
    		slidingTabWidget.layout(0, top, parentWidth, bottom);
		}
	}
	

	public void setEnabledMediaButtons(boolean isInCall) {
		if (lastMediaState == null) {
			speakerButton.setEnabled(isInCall);
			muteButton.setEnabled(isInCall);
			bluetoothButton.setEnabled(isInCall);

		} else {
			speakerButton.setEnabled(lastMediaState.canSpeakerphoneOn && isInCall);
			muteButton.setEnabled(lastMediaState.canMicrophoneMute && isInCall);
			bluetoothButton.setEnabled(lastMediaState.canBluetoothSco && isInCall);
		}

		dialButton.setEnabled(isInCall);
	}
	
	
	private void setCallLockerVisibility(int visibility) {
		if (slidingTabWidget != null) {
			slidingTabWidget.setVisibility(visibility);
		} else if(alternateLockerWidget != null){
			alternateLockerWidget.setVisibility(visibility);
		}
	}
	
	
	/**
	 * Toggle the mute button as if pressed by the user.
	 */
	public void toggleMuteButton() {
		muteButton.setChecked(!muteButton.isChecked());
		muteButton.performClick();
	}


	public void setCallState(SipCallSession callInfo) {
		currentCall = callInfo;
		
		if(currentCall == null) {
			controlMode = MODE_NO_ACTION;
			inCallButtons.setVisibility(GONE);
			setCallLockerVisibility(GONE);
			return;
		}
		
		int state = currentCall.getCallState();
		Log.d(THIS_FILE, "Mode is : "+state);
		switch (state) {
		case SipCallSession.InvState.INCOMING:
			controlMode = MODE_LOCKER;
			inCallButtons.setVisibility(GONE);
			setCallLockerVisibility(VISIBLE);
			inCallButtons.setVisibility(GONE);
			break;
		case SipCallSession.InvState.CALLING:
		case SipCallSession.InvState.CONNECTING:
			controlMode = MODE_CONTROL;
			setCallLockerVisibility(GONE);
			inCallButtons.setVisibility(VISIBLE);
			clearCallButton.setEnabled(true);
			setEnabledMediaButtons(true);
			break;
		case SipCallSession.InvState.CONFIRMED:
			controlMode = MODE_CONTROL;
			setCallLockerVisibility(GONE);
			inCallButtons.setVisibility(VISIBLE);

			clearCallButton.setEnabled(true);
			setEnabledMediaButtons(true);
			break;
		case SipCallSession.InvState.NULL:
		case SipCallSession.InvState.DISCONNECTED:
			controlMode = MODE_NO_ACTION;
			inCallButtons.setVisibility(GONE);
			setCallLockerVisibility(GONE);
			break;
		case SipCallSession.InvState.EARLY:
		default:
			if (currentCall.isIncoming()) {
				controlMode = MODE_LOCKER;
				inCallButtons.setVisibility(GONE);
				setCallLockerVisibility(VISIBLE);
				inCallButtons.setVisibility(GONE);
			} else {
				controlMode = MODE_CONTROL;
				setCallLockerVisibility(GONE);
				inCallButtons.setVisibility(VISIBLE);
				clearCallButton.setEnabled(true);
				setEnabledMediaButtons(true);
			}
			break;
		}
		
	}
	
	/**
	 * Registers a callback to be invoked when the user triggers an event.
	 * 
	 * @param listener
	 *            the OnTriggerListener to attach to this view
	 */
	public void setOnTriggerListener(OnTriggerListener listener) {
		onTriggerListener = listener;
	}

	private void dispatchTriggerEvent(int whichHandle) {
		if (onTriggerListener != null) {
			onTriggerListener.onTrigger(whichHandle, currentCall);
		}
	}
	

	@Override
	public void onLeftRightChoice(int whichHandle) {
		Log.d(THIS_FILE, "Call controls receive info from slider " + whichHandle);
		if (controlMode != MODE_LOCKER) {
			// Oups we are not in locker mode and we get a trigger from
			// locker...
			// Should not happen... but... to be sure
			return;
		}
		switch (whichHandle) {
		case LEFT_HANDLE:
			Log.d(THIS_FILE, "We take the call");
			
			dispatchTriggerEvent(OnTriggerListener.TAKE_CALL);
			break;
		case RIGHT_HANDLE:
			Log.d(THIS_FILE, "We clear the call");
			dispatchTriggerEvent(OnTriggerListener.DECLINE_CALL);
		default:
			break;
		}
		if(slidingTabWidget != null) {
		    slidingTabWidget.resetView();
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.clearCallButton) {
			dispatchTriggerEvent(OnTriggerListener.CLEAR_CALL);
		} else if (id == R.id.dialpadButton) {
			dispatchTriggerEvent(isDialpadOn ? OnTriggerListener.DIALPAD_OFF : OnTriggerListener.DIALPAD_ON);
			isDialpadOn = !isDialpadOn;
		} else if (id == R.id.bluetoothButton) {
			if (((ToggleButton) v).isChecked()) {
				dispatchTriggerEvent(OnTriggerListener.BLUETOOTH_ON);
			} else {
				dispatchTriggerEvent(OnTriggerListener.BLUETOOTH_OFF);
			}
		} else if (id == R.id.speakerButton) {
			if (((ToggleButton) v).isChecked()) {
				dispatchTriggerEvent(OnTriggerListener.SPEAKER_ON);
			} else {
				dispatchTriggerEvent(OnTriggerListener.SPEAKER_OFF);
			}
		} else if (id == R.id.muteButton) {
			if (((ToggleButton) v).isChecked()) {
				dispatchTriggerEvent(OnTriggerListener.MUTE_ON);
			} else {
				dispatchTriggerEvent(OnTriggerListener.MUTE_OFF);
			}
		} else if (id == R.id.addCallButton) {
			dispatchTriggerEvent(OnTriggerListener.ADD_CALL);
		}
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d(THIS_FILE, "Hey you hit the key : " + keyCode);
		switch (keyCode) {
		case KeyEvent.KEYCODE_CALL:
			if (controlMode == MODE_LOCKER) {
				dispatchTriggerEvent(OnTriggerListener.TAKE_CALL);
				return true;
			}
			break;
		case KeyEvent.KEYCODE_ENDCALL:
		//case KeyEvent.KEYCODE_POWER:
			if (controlMode == MODE_LOCKER) {
				dispatchTriggerEvent(OnTriggerListener.DECLINE_CALL);
				return true;
			} else if (controlMode == MODE_CONTROL) {
				dispatchTriggerEvent(OnTriggerListener.CLEAR_CALL);
				return true;
			}
		default:
			break;
		}
		
		return super.onKeyDown(keyCode, event);
	}

	public void setMediaState(MediaState mediaState) {
		lastMediaState = mediaState;
		muteButton.setEnabled(mediaState.canMicrophoneMute);
		muteButton.setChecked(mediaState.isMicrophoneMute);
		
	//	Log.d(THIS_FILE, ">>> Can bluetooth : "+mediaState.canBluetoothSco);
		bluetoothButton.setEnabled(mediaState.canBluetoothSco);
		bluetoothButton.setChecked(mediaState.isBluetoothScoOn);
		
		speakerButton.setEnabled(mediaState.canSpeakerphoneOn);
		speakerButton.setChecked(mediaState.isSpeakerphoneOn);
		
		
	}

	public void applyTheme(Theme t) {
		//Apply backgrounds
		
		// To toggle buttons
		StateListDrawable tStd = getToggleButtonDrawable(t);
		if(tStd != null) {
			speakerButton.setBackgroundDrawable(tStd);
			// yeah we can't recycle the std drawable
			muteButton.setBackgroundDrawable(getToggleButtonDrawable(t));
			bluetoothButton.setBackgroundDrawable(getToggleButtonDrawable(t));
		}
		
		// To buttons
		StateListDrawable bStd = getButtonDrawable(t);
		if(bStd != null) {
			addCallButton.setBackgroundDrawable(bStd);
			clearCallButton.setBackgroundDrawable(getButtonDrawable(t));
			dialButton.setBackgroundDrawable(getButtonDrawable(t));
		}
		
		// To buttons icons
		Drawable addCallDrawable = t.getDrawableResource("ic_in_call_touch_add_call");
		if(addCallDrawable != null) {
			addCallButton.setCompoundDrawablesWithIntrinsicBounds(null, addCallDrawable, null, null);
		}
		Drawable clearCallDrawable = t.getDrawableResource("ic_in_call_touch_end");
		if(clearCallDrawable != null) {
			clearCallButton.setCompoundDrawablesWithIntrinsicBounds(null, clearCallDrawable, null, null);
		}
		Drawable dialDrawable = t.getDrawableResource("ic_in_call_touch_dialpad");
		if(dialDrawable != null) {
			dialButton.setCompoundDrawablesWithIntrinsicBounds(null, dialDrawable, null, null);
		}
		
		if(slidingTabWidget != null) {
    		// To sliding tab
    		slidingTabWidget.setLeftTabDrawables(t.getDrawableResource("ic_jog_dial_answer"), 
    				t.getDrawableResource("jog_tab_target_green"), 
    				t.getDrawableResource("jog_tab_bar_left_answer"), 
    				t.getDrawableResource("jog_tab_left_answer"));
    		
    		slidingTabWidget.setRightTabDrawables(t.getDrawableResource("ic_jog_dial_decline"), 
    				t.getDrawableResource("jog_tab_target_red"), 
    				t.getDrawableResource("jog_tab_bar_right_decline"), 
    				t.getDrawableResource("jog_tab_right_decline"));
    		
		}
	}
	
	private StateListDrawable getToggleButtonDrawable(Theme t) {

		Drawable toggleOnNormal = t.getDrawableResource("btn_in_call_switch_on_normal");
		Drawable toggleOnDisabled = t.getDrawableResource("btn_in_call_switch_on_disable");
		Drawable toggleOnPressed = t.getDrawableResource("btn_in_call_switch_on_pressed");
		Drawable toggleOnSelected = t.getDrawableResource("btn_in_call_switch_on_selected");
		Drawable toggleOnDisabledFocus = t.getDrawableResource("btn_in_call_switch_on_disable_focused");
		Drawable toggleOffNormal = t.getDrawableResource("btn_in_call_switch_off_normal");
		Drawable toggleOffDisabled = t.getDrawableResource("btn_in_call_switch_off_disable");
		Drawable toggleOffPressed = t.getDrawableResource("btn_in_call_switch_off_pressed");
		Drawable toggleOffSelected = t.getDrawableResource("btn_in_call_switch_off_selected");
		Drawable toggleOffDisabledFocus = t.getDrawableResource("btn_in_call_switch_off_disable_focused");
		
		if(toggleOnSelected == null) {
			toggleOnSelected = toggleOnPressed;
		}
		if(toggleOffSelected == null) {
			toggleOffSelected = toggleOffPressed;
		}
		
		if(toggleOnNormal != null && toggleOnDisabled != null && 
				toggleOnPressed != null && toggleOnSelected != null &&
				toggleOnDisabledFocus != null && toggleOffNormal != null &&
				toggleOffDisabled != null && toggleOffPressed != null &&
				toggleOffSelected != null && toggleOffDisabledFocus != null ){
			
			StateListDrawable toggleStd = new StateListDrawable();
		//	toggleStd.addState(new int[] { - android.R.attr.state_focused, android.R.attr.state_enabled, android.R.attr.state_checked}, toggleOnNormal);
			toggleStd.addState(new int[] { - android.R.attr.state_focused, - android.R.attr.state_enabled, android.R.attr.state_checked}, toggleOnDisabled);
			toggleStd.addState(new int[] { android.R.attr.state_pressed, android.R.attr.state_checked}, toggleOnPressed);
			toggleStd.addState(new int[] { android.R.attr.state_focused, android.R.attr.state_enabled, android.R.attr.state_checked}, toggleOnSelected);
			toggleStd.addState(new int[] { android.R.attr.state_enabled, android.R.attr.state_checked}, toggleOnNormal);
			toggleStd.addState(new int[] { android.R.attr.state_focused, android.R.attr.state_checked}, toggleOnDisabledFocus);
			toggleStd.addState(new int[] { android.R.attr.state_checked}, toggleOnDisabled);
			
			// UnChecked
		//	toggleStd.addState(new int[] { - android.R.attr.state_focused, android.R.attr.state_enabled, -android.R.attr.state_checked}, toggleOffNormal);
			toggleStd.addState(new int[] { - android.R.attr.state_focused, - android.R.attr.state_enabled, -android.R.attr.state_checked}, toggleOffDisabled);
			toggleStd.addState(new int[] { android.R.attr.state_pressed, -android.R.attr.state_checked}, toggleOffPressed);
			toggleStd.addState(new int[] { android.R.attr.state_focused, android.R.attr.state_enabled, -android.R.attr.state_checked}, toggleOffSelected);
			toggleStd.addState(new int[] { android.R.attr.state_enabled, -android.R.attr.state_checked}, toggleOffNormal);
			toggleStd.addState(new int[] { android.R.attr.state_focused, -android.R.attr.state_checked}, toggleOffDisabledFocus);
			toggleStd.addState(new int[] { -android.R.attr.state_checked}, toggleOffDisabled);
			return toggleStd;
		}
		return null;
	}
	
	
	
	private StateListDrawable getButtonDrawable(Theme t) {

		Drawable btNormal = t.getDrawableResource("btn_in_call_main_normal");
		Drawable btDisabled = t.getDrawableResource("btn_in_call_main_disable");
		Drawable btPressed = t.getDrawableResource("btn_in_call_main_pressed");
		Drawable btSelected = t.getDrawableResource("btn_in_call_main_selected");
		Drawable btDisabledFocus = t.getDrawableResource("btn_in_call_main_disable_focused");
		
		if(btSelected == null) {
			btSelected = btPressed;
		}
		
		if(btNormal != null && btDisabled != null && 
				btPressed != null && btSelected != null &&
				btDisabledFocus != null  ){
			
			StateListDrawable btStd = new StateListDrawable();
	//		btStd.addState(new int[] { -android.R.attr.state_focused, android.R.attr.state_enabled }, btNormal);
			btStd.addState(new int[] { -android.R.attr.state_focused, -android.R.attr.state_enabled }, btDisabled);
			btStd.addState(new int[] { android.R.attr.state_pressed }, btPressed);
			btStd.addState(new int[] { android.R.attr.state_focused, android.R.attr.state_enabled }, btSelected);
			btStd.addState(new int[] { android.R.attr.state_enabled }, btNormal);
			btStd.addState(new int[] { android.R.attr.state_focused }, btDisabledFocus);
			btStd.addState(new int[] {}, btDisabled);
			
			return btStd;
		}
		return null;
	}

    public int getLockerVisibility() {
        if(slidingTabWidget != null) {
            return slidingTabWidget.getVisibility();
        }else if(alternateLockerWidget != null) {
            return alternateLockerWidget.getVisibility();
        }
        return View.GONE;
    }
}
