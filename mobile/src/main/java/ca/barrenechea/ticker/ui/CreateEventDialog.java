/*
 *
 *  * Copyright (C) 2014 Eduardo Barrenechea
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package ca.barrenechea.ticker.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ca.barrenechea.ticker.R;
import ca.barrenechea.ticker.data.EventLoader;

public class CreateEventDialog extends BaseDialog {
    @InjectView(R.id.edit_name)
    EditText mEditName;
    @InjectView(R.id.button_positive)
    Button mButtonPositive;

    @Inject
    EventLoader mEventLoader;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_create_event, container, false);

        ButterKnife.inject(this, view);

        mEditName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                // do nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                // do nothing
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mButtonPositive.setEnabled(editable.length() > 0);
            }
        });

        mButtonPositive.setEnabled(false);
        mButtonPositive.setOnClickListener(v -> {
            final String name = mEditName.getText().toString();

            mEventLoader.create(name, null);

            CreateEventDialog.this.dismiss();
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ButterKnife.reset(this);
    }
}
